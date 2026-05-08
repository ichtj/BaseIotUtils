#include <jni.h>
#include <android/log.h>

#include <fcntl.h>
#include <unistd.h>
#include <pthread.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>

#include <linux/videodev2.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/select.h>
#include <errno.h>

#define TAG "DualCameraJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define BUFFER_COUNT 4
#define FREEZE_TIMEOUT_SEC 3
#define SELECT_TIMEOUT_SEC 2
#define REOPEN_MAX_RETRY 20
#define REOPEN_SLEEP_US 500000

enum CameraState {
    STATE_IDLE = 0,
    STATE_PREVIEW = 1,
    STATE_STOPPED = 2,
    STATE_RELEASED = 3,
    STATE_ERROR = 4,
    STATE_RECONNECTING = 5
};

struct Buffer {
    void* start;
    size_t length;
};

struct CameraCtx {
    int cameraId;
    int fd;

    int width;
    int height;

    bool previewRunning;
    bool released;

    JavaVM* jvm;
    jobject obj;

    pthread_t thread;
    bool threadCreated;

    pthread_mutex_t lock;

    Buffer buffers[BUFFER_COUNT];

    uint8_t* lastFrame;
    int lastFrameSize;

    int state;
    long lastFrameTime;

    int pixelFormat;

    char devPath[64];
    bool recording;
    FILE* recordFp;
    char recordPath[256];
};

// ================= 工具 =================

static int xioctl(int fd, int request, void* arg) {
    int r;
    do {
        r = ioctl(fd, request, arg);
    } while (r == -1 && errno == EINTR);
    return r;
}

static inline int clamp(int v) {
    if (v < 0) return 0;
    if (v > 255) return 255;
    return v;
}
int runRootCmd(const char *cmd) {
    FILE *fp = popen("su", "w");
    if (fp == NULL) {
        LOGE("popen su failed");
        return -1;
    }

    fprintf(fp, "%s\n", cmd);
    fprintf(fp, "exit\n");

    int ret = pclose(fp);
    LOGD("runRootCmd ret=%d", ret);

    return ret;
}
static int fixVideoNodePermission() {
    int ret = runRootCmd("chmod 777 /dev/video*");

    if (ret == -1) {
        LOGE("fixVideoNodePermission system failed");
        return -1;
    }

    LOGD("fixVideoNodePermission ret=%d", ret);
    return ret;
}



static void resetBuffers(CameraCtx* ctx) {
    int i;
    for (i = 0; i < BUFFER_COUNT; i++) {
        ctx->buffers[i].start = NULL;
        ctx->buffers[i].length = 0;
    }
}

static void freeLastFrame(CameraCtx* ctx) {
    if (ctx->lastFrame != NULL) {
        free(ctx->lastFrame);
        ctx->lastFrame = NULL;
        ctx->lastFrameSize = 0;
    }
}

static void cleanupMmapOnly(CameraCtx* ctx) {
    int i;
    for (i = 0; i < BUFFER_COUNT; i++) {
        if (ctx->buffers[i].start != NULL && ctx->buffers[i].start != MAP_FAILED) {
            munmap(ctx->buffers[i].start, ctx->buffers[i].length);
            ctx->buffers[i].start = NULL;
            ctx->buffers[i].length = 0;
        }
    }
}

static void closeRecordFile(CameraCtx* ctx) {
    if (ctx->recordFp != NULL) {
        fflush(ctx->recordFp);
        fclose(ctx->recordFp);
        ctx->recordFp = NULL;
    }
    ctx->recording = false;
    ctx->recordPath[0] = '\0';
}

static void cleanupDevice(CameraCtx* ctx) {
    if (ctx->fd >= 0) {
        int type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        xioctl(ctx->fd, VIDIOC_STREAMOFF, &type);
    }

    cleanupMmapOnly(ctx);

    if (ctx->fd >= 0) {
        close(ctx->fd);
        ctx->fd = -1;
    }
}

static bool needRecoverErrno(int err) {
    if (err == ENODEV) return true;
    if (err == EIO) return true;
    if (err == ENOLINK) return true;
    if (err == EPIPE) return true;
    if (err == ENXIO) return true;
    if (err == ESHUTDOWN) return true;
    if (err == EBUSY) return true;
    if (err == EINVAL) return true;
    return false;
}

// YUYV → RGB
static void yuyvToRgb(uint8_t* yuyv, uint8_t* rgb, int width, int height) {
    int frameSize = width * height * 2;
    int i;
    int j;

    for (i = 0, j = 0; i < frameSize; i += 4, j += 6) {
        int y0 = yuyv[i + 0] & 0xff;
        int u  = yuyv[i + 1] & 0xff;
        int y1 = yuyv[i + 2] & 0xff;
        int v  = yuyv[i + 3] & 0xff;

        int c = y0 - 16;
        int d = u - 128;
        int e = v - 128;

        int r = clamp((298 * c + 409 * e + 128) >> 8);
        int g = clamp((298 * c - 100 * d - 208 * e + 128) >> 8);
        int b = clamp((298 * c + 516 * d + 128) >> 8);

        rgb[j + 0] = (uint8_t) r;
        rgb[j + 1] = (uint8_t) g;
        rgb[j + 2] = (uint8_t) b;

        c = y1 - 16;

        r = clamp((298 * c + 409 * e + 128) >> 8);
        g = clamp((298 * c - 100 * d - 208 * e + 128) >> 8);
        b = clamp((298 * c + 516 * d + 128) >> 8);

        rgb[j + 3] = (uint8_t) r;
        rgb[j + 4] = (uint8_t) g;
        rgb[j + 5] = (uint8_t) b;
    }
}

// ================= 初始化 =================

static int initV4L2(CameraCtx* ctx) {

    struct v4l2_format fmt;
    struct v4l2_requestbuffers req;
    int tryFormats[2];
    int success = 0;
    int i;

    memset(&fmt, 0, sizeof(fmt));
    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.fmt.pix.width = ctx->width;
    fmt.fmt.pix.height = ctx->height;
    fmt.fmt.pix.field = V4L2_FIELD_NONE;

    tryFormats[0] = V4L2_PIX_FMT_MJPEG;
    tryFormats[1] = V4L2_PIX_FMT_YUYV;

    for (i = 0; i < 2; i++) {
        fmt.fmt.pix.pixelformat = tryFormats[i];

        if (xioctl(ctx->fd, VIDIOC_S_FMT, &fmt) == 0) {
            ctx->pixelFormat = tryFormats[i];
            success = 1;
            LOGD("VIDIOC_S_FMT success format=%d width=%d height=%d",
                 ctx->pixelFormat, ctx->width, ctx->height);
            break;
        } else {
            LOGE("VIDIOC_S_FMT failed format=%d errno=%d", tryFormats[i], errno);
        }
    }

    if (!success) {
        LOGE("no supported format");
        return -1;
    }

    memset(&req, 0, sizeof(req));
    req.count = BUFFER_COUNT;
    req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    req.memory = V4L2_MEMORY_MMAP;

    if (xioctl(ctx->fd, VIDIOC_REQBUFS, &req) < 0) {
        LOGE("VIDIOC_REQBUFS failed errno=%d", errno);
        return -1;
    }

    if (req.count < 2) {
        LOGE("VIDIOC_REQBUFS count too small=%d", req.count);
        return -1;
    }

    resetBuffers(ctx);

    for (i = 0; i < BUFFER_COUNT; i++) {
        struct v4l2_buffer buf;
        memset(&buf, 0, sizeof(buf));

        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = i;

        if (xioctl(ctx->fd, VIDIOC_QUERYBUF, &buf) < 0) {
            LOGE("VIDIOC_QUERYBUF failed index=%d errno=%d", i, errno);
            return -1;
        }

        ctx->buffers[i].length = buf.length;
        ctx->buffers[i].start = mmap(NULL,
                                     buf.length,
                                     PROT_READ | PROT_WRITE,
                                     MAP_SHARED,
                                     ctx->fd,
                                     buf.m.offset);

        if (ctx->buffers[i].start == MAP_FAILED) {
            LOGE("mmap failed index=%d errno=%d", i, errno);
            ctx->buffers[i].start = NULL;
            ctx->buffers[i].length = 0;
            return -1;
        }

        if (xioctl(ctx->fd, VIDIOC_QBUF, &buf) < 0) {
            LOGE("VIDIOC_QBUF failed index=%d errno=%d", i, errno);
            return -1;
        }
    }

    {
        int type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        if (xioctl(ctx->fd, VIDIOC_STREAMON, &type) < 0) {
            LOGE("VIDIOC_STREAMON failed errno=%d", errno);
            return -1;
        }
    }

    ctx->lastFrameTime = time(NULL);
    return 0;
}

static int openAndInitCamera(CameraCtx* ctx) {
    fixVideoNodePermission();

    ctx->fd = open(ctx->devPath, O_RDWR | O_NONBLOCK);
    if (ctx->fd < 0) {
        LOGE("open failed path=%s errno=%d", ctx->devPath, errno);
        return -1;
    }

    LOGD("open success path=%s fd=%d", ctx->devPath, ctx->fd);

    if (initV4L2(ctx) != 0) {
        cleanupDevice(ctx);
        return -1;
    }

    ctx->state = STATE_PREVIEW;
    return 0;
}

static int reopenCamera(CameraCtx* ctx) {
    int retry;

    for (retry = 0; retry < REOPEN_MAX_RETRY; retry++) {

        if (!ctx->previewRunning || ctx->released) {
            return -1;
        }

        pthread_mutex_lock(&ctx->lock);

        ctx->state = STATE_RECONNECTING;
        cleanupDevice(ctx);
        freeLastFrame(ctx);

        if (openAndInitCamera(ctx) == 0) {
            pthread_mutex_unlock(&ctx->lock);
            LOGD("reopenCamera success retry=%d", retry);
            return 0;
        }

        pthread_mutex_unlock(&ctx->lock);

        LOGE("reopenCamera failed retry=%d", retry);
        usleep(REOPEN_SLEEP_US);
    }

    pthread_mutex_lock(&ctx->lock);
    ctx->state = STATE_ERROR;
    pthread_mutex_unlock(&ctx->lock);

    return -1;
}

// ================= 预览 =================

static void* previewLoop(void* arg) {

    CameraCtx* ctx = (CameraCtx*) arg;

    JNIEnv* env = NULL;
    jclass cls = NULL;
    jmethodID mid = NULL;

    if (ctx->jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("AttachCurrentThread failed");
        return NULL;
    }

    cls = env->GetObjectClass(ctx->obj);
    if (cls == NULL) {
        LOGE("GetObjectClass failed");
        ctx->jvm->DetachCurrentThread();
        return NULL;
    }

    mid = env->GetMethodID(cls, "onPreviewFrame", "(I[B)V");
    if (mid == NULL) {
        LOGE("GetMethodID onPreviewFrame failed");
        env->DeleteLocalRef(cls);
        ctx->jvm->DetachCurrentThread();
        return NULL;
    }

    ctx->lastFrameTime = time(NULL);

    while (ctx->previewRunning && !ctx->released) {

        fd_set fds;
        struct timeval tv;
        int r;
        int needRecover = 0;

        if (ctx->fd < 0) {
            LOGE("previewLoop fd invalid");
            if (reopenCamera(ctx) != 0) {
                continue;
            }
        }

        FD_ZERO(&fds);
        FD_SET(ctx->fd, &fds);

        tv.tv_sec = SELECT_TIMEOUT_SEC;
        tv.tv_usec = 0;

        r = select(ctx->fd + 1, &fds, NULL, NULL, &tv);

        if (!ctx->previewRunning || ctx->released) {
            break;
        }

        if (r == 0) {
            long now = time(NULL);
            if (ctx->lastFrameTime > 0 && (now - ctx->lastFrameTime > FREEZE_TIMEOUT_SEC)) {
                LOGE("select timeout freeze now=%ld lastFrameTime=%ld", now, ctx->lastFrameTime);
                needRecover = 1;
            }
        } else if (r < 0) {
            if (errno == EINTR) {
                continue;
            }
            LOGE("select failed errno=%d", errno);
            needRecover = 1;
        } else {

            pthread_mutex_lock(&ctx->lock);

            if (ctx->fd < 0) {
                pthread_mutex_unlock(&ctx->lock);
                needRecover = 1;
            } else {
                struct v4l2_buffer buf;
                memset(&buf, 0, sizeof(buf));

                buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                buf.memory = V4L2_MEMORY_MMAP;

                if (xioctl(ctx->fd, VIDIOC_DQBUF, &buf) == 0) {

                    if (buf.index >= BUFFER_COUNT || ctx->buffers[buf.index].start == NULL) {
                        LOGE("invalid buffer index=%d", buf.index);
                        pthread_mutex_unlock(&ctx->lock);
                        needRecover = 1;
                    } else {
                        uint8_t* data = (uint8_t*) ctx->buffers[buf.index].start;
                        int len = buf.bytesused;

                        if (len > 0) {

                            ctx->lastFrameTime = time(NULL);
                            if (ctx->recording && ctx->recordFp != NULL) {
                                size_t writeLen = (size_t) len;
                                size_t written = fwrite(data, 1, writeLen, ctx->recordFp);
                                if (written != writeLen) {
                                    LOGE("record write failed expected=%d actual=%d", len, (int) written);
                                    closeRecordFile(ctx);
                                }
                            }

                            if (ctx->lastFrame != NULL) {
                                free(ctx->lastFrame);
                                ctx->lastFrame = NULL;
                                ctx->lastFrameSize = 0;
                            }

                            if (ctx->pixelFormat == V4L2_PIX_FMT_MJPEG) {
                                ctx->lastFrame = (uint8_t*) malloc(len);
                                if (ctx->lastFrame == NULL) {
                                    LOGE("malloc MJPEG failed");
                                } else {
                                    memcpy(ctx->lastFrame, data, len);
                                    ctx->lastFrameSize = len;
                                }
                            } else if (ctx->pixelFormat == V4L2_PIX_FMT_YUYV) {
                                int rgbSize = ctx->width * ctx->height * 3;
                                ctx->lastFrame = (uint8_t*) malloc(rgbSize);
                                if (ctx->lastFrame == NULL) {
                                    LOGE("malloc RGB failed");
                                } else {
                                    yuyvToRgb(data, ctx->lastFrame, ctx->width, ctx->height);
                                    ctx->lastFrameSize = rgbSize;
                                }
                            } else {
                                LOGE("unsupported pixelFormat=%d", ctx->pixelFormat);
                            }

                            if (ctx->lastFrame != NULL && ctx->lastFrameSize > 0) {
                                jbyteArray arr = env->NewByteArray(ctx->lastFrameSize);
                                if (arr != NULL) {
                                    env->SetByteArrayRegion(arr, 0, ctx->lastFrameSize, (jbyte*) ctx->lastFrame);
                                    pthread_mutex_unlock(&ctx->lock);

                                    env->CallVoidMethod(ctx->obj, mid, ctx->cameraId, arr);
                                    if (env->ExceptionCheck()) {
                                        LOGE("onPreviewFrame exception");
                                        env->ExceptionDescribe();
                                        env->ExceptionClear();
                                    }
                                    env->DeleteLocalRef(arr);
                                } else {
                                    pthread_mutex_unlock(&ctx->lock);
                                    LOGE("NewByteArray failed size=%d", ctx->lastFrameSize);
                                }
                            } else {
                                pthread_mutex_unlock(&ctx->lock);
                            }
                        } else {
                            pthread_mutex_unlock(&ctx->lock);
                        }

                        pthread_mutex_lock(&ctx->lock);
                        if (ctx->fd >= 0) {
                            if (xioctl(ctx->fd, VIDIOC_QBUF, &buf) < 0) {
                                LOGE("VIDIOC_QBUF failed errno=%d", errno);
                                needRecover = 1;
                            }
                        } else {
                            needRecover = 1;
                        }
                        pthread_mutex_unlock(&ctx->lock);
                    }
                } else {
                    int err = errno;
                    pthread_mutex_unlock(&ctx->lock);
                    LOGE("VIDIOC_DQBUF failed errno=%d", err);
                    if (needRecoverErrno(err)) {
                        needRecover = 1;
                    }
                }
            }
        }

        if (needRecover) {
            LOGE("previewLoop start recover");
            if (reopenCamera(ctx) != 0) {
                LOGE("previewLoop recover failed");
            } else {
                LOGD("previewLoop recover success");
            }
        }
    }

    env->DeleteLocalRef(cls);
    ctx->jvm->DetachCurrentThread();
    LOGD("previewLoop exit");
    return NULL;
}

// ================= JNI接口 =================

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeCapture(
        JNIEnv* env, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;
    jclass cls;
    jmethodID mid;
    jbyteArray arr;

    if (ctx == NULL || ctx->released) return -1;

    pthread_mutex_lock(&ctx->lock);

    if (ctx->lastFrame == NULL || ctx->lastFrameSize <= 0) {
        pthread_mutex_unlock(&ctx->lock);
        LOGE("nativeCapture no frame");
        return -1;
    }

    cls = env->GetObjectClass(ctx->obj);
    if (cls == NULL) {
        pthread_mutex_unlock(&ctx->lock);
        LOGE("nativeCapture GetObjectClass failed");
        return -1;
    }

    mid = env->GetMethodID(cls, "onCaptureFrame", "(I[B)V");
    if (mid == NULL) {
        env->DeleteLocalRef(cls);
        pthread_mutex_unlock(&ctx->lock);
        LOGE("nativeCapture GetMethodID failed");
        return -1;
    }

    arr = env->NewByteArray(ctx->lastFrameSize);
    if (arr == NULL) {
        env->DeleteLocalRef(cls);
        pthread_mutex_unlock(&ctx->lock);
        LOGE("nativeCapture NewByteArray failed");
        return -1;
    }

    env->SetByteArrayRegion(arr, 0, ctx->lastFrameSize, (jbyte*) ctx->lastFrame);
    pthread_mutex_unlock(&ctx->lock);

    env->CallVoidMethod(ctx->obj, mid, ctx->cameraId, arr);
    if (env->ExceptionCheck()) {
        LOGE("nativeCapture CallVoidMethod exception");
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    env->DeleteLocalRef(arr);
    env->DeleteLocalRef(cls);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeStartRecord(
        JNIEnv* env, jobject, jlong handle, jstring filePath) {

    CameraCtx* ctx = (CameraCtx*) handle;
    const char* path;

    if (ctx == NULL || ctx->released) return -1;
    if (filePath == NULL) return -1;

    path = env->GetStringUTFChars(filePath, NULL);
    if (path == NULL || path[0] == '\0') {
        if (path != NULL) {
            env->ReleaseStringUTFChars(filePath, path);
        }
        return -1;
    }

    pthread_mutex_lock(&ctx->lock);

    if (ctx->recording) {
        pthread_mutex_unlock(&ctx->lock);
        env->ReleaseStringUTFChars(filePath, path);
        return 0;
    }

    ctx->recordFp = fopen(path, "wb");
    if (ctx->recordFp == NULL) {
        pthread_mutex_unlock(&ctx->lock);
        LOGE("nativeStartRecord fopen failed path=%s errno=%d", path, errno);
        env->ReleaseStringUTFChars(filePath, path);
        return -1;
    }

    strncpy(ctx->recordPath, path, sizeof(ctx->recordPath) - 1);
    ctx->recordPath[sizeof(ctx->recordPath) - 1] = '\0';
    ctx->recording = true;

    pthread_mutex_unlock(&ctx->lock);
    env->ReleaseStringUTFChars(filePath, path);

    LOGD("nativeStartRecord success path=%s", ctx->recordPath);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeStopRecord(
        JNIEnv*, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;

    if (ctx == NULL || ctx->released) return -1;

    pthread_mutex_lock(&ctx->lock);
    closeRecordFile(ctx);
    pthread_mutex_unlock(&ctx->lock);

    LOGD("nativeStopRecord success");
    return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeOpen(
        JNIEnv* env, jobject thiz,
        jint cameraId, jint width, jint height) {

    CameraCtx* ctx = new CameraCtx();
    memset(ctx, 0, sizeof(CameraCtx));

    ctx->fd = -1;
    ctx->cameraId = cameraId;
    ctx->width = width;
    ctx->height = height;
    ctx->previewRunning = false;
    ctx->released = false;
    ctx->lastFrame = NULL;
    ctx->lastFrameSize = 0;
    ctx->state = STATE_IDLE;
    ctx->lastFrameTime = 0;
    ctx->pixelFormat = 0;
    ctx->threadCreated = false;
    ctx->recording = false;
    ctx->recordFp = NULL;
    ctx->recordPath[0] = '\0';

    snprintf(ctx->devPath, sizeof(ctx->devPath), "/dev/video%d", cameraId);

    resetBuffers(ctx);

    pthread_mutex_init(&ctx->lock, NULL);

    env->GetJavaVM(&ctx->jvm);
    ctx->obj = env->NewGlobalRef(thiz);

    pthread_mutex_lock(&ctx->lock);
    if (openAndInitCamera(ctx) != 0) {
        pthread_mutex_unlock(&ctx->lock);

        if (ctx->obj != NULL) {
            env->DeleteGlobalRef(ctx->obj);
            ctx->obj = NULL;
        }
        pthread_mutex_destroy(&ctx->lock);
        delete ctx;
        return 0;
    }
    pthread_mutex_unlock(&ctx->lock);

    LOGD("nativeOpen success dev=%s", ctx->devPath);
    return (jlong) ctx;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeStartPreview(
        JNIEnv*, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;
    int ret;

    if (ctx == NULL || ctx->released) return -1;
    if (ctx->previewRunning) return 0;

    ctx->previewRunning = true;
    ctx->state = STATE_PREVIEW;

    ret = pthread_create(&ctx->thread, NULL, previewLoop, ctx);
    if (ret != 0) {
        ctx->previewRunning = false;
        ctx->state = STATE_ERROR;
        LOGE("pthread_create failed ret=%d", ret);
        return -1;
    }

    ctx->threadCreated = true;
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeStopPreview(
        JNIEnv*, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;

    if (ctx == NULL || ctx->released) return -1;

    if (ctx->previewRunning) {
        ctx->previewRunning = false;

        pthread_mutex_lock(&ctx->lock);
        closeRecordFile(ctx);
        if (ctx->fd >= 0) {
            int type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
            if (xioctl(ctx->fd, VIDIOC_STREAMOFF, &type) < 0) {
                LOGE("nativeStopPreview STREAMOFF failed errno=%d", errno);
            }
        }
        pthread_mutex_unlock(&ctx->lock);

        if (ctx->threadCreated) {
            pthread_join(ctx->thread, NULL);
            ctx->threadCreated = false;
        }

        ctx->state = STATE_STOPPED;
    }

    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeRelease(
        JNIEnv* env, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;

    if (ctx == NULL || ctx->released) return;

    ctx->released = true;
    ctx->previewRunning = false;
    ctx->state = STATE_RELEASED;

    pthread_mutex_lock(&ctx->lock);
    if (ctx->fd >= 0) {
        int type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        xioctl(ctx->fd, VIDIOC_STREAMOFF, &type);
    }
    pthread_mutex_unlock(&ctx->lock);

    if (ctx->threadCreated) {
        pthread_join(ctx->thread, NULL);
        ctx->threadCreated = false;
    }

    pthread_mutex_lock(&ctx->lock);
    closeRecordFile(ctx);
    cleanupDevice(ctx);
    freeLastFrame(ctx);
    pthread_mutex_unlock(&ctx->lock);

    if (ctx->obj != NULL) {
        env->DeleteGlobalRef(ctx->obj);
        ctx->obj = NULL;
    }

    pthread_mutex_destroy(&ctx->lock);
    delete ctx;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeGetState(
        JNIEnv*, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;

    if (ctx == NULL) return STATE_ERROR;
    return ctx->state;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ichtj_basetools_camera_CameraJNINew_nativeIsRunning(
        JNIEnv*, jobject, jlong handle) {

    CameraCtx* ctx = (CameraCtx*) handle;
    long now;

    if (ctx == NULL || ctx->released) return JNI_FALSE;
    if (!ctx->previewRunning) return JNI_FALSE;
    if (ctx->state == STATE_ERROR) return JNI_FALSE;
    if (ctx->state == STATE_RECONNECTING) return JNI_FALSE;
    if (ctx->fd < 0) return JNI_FALSE;

    now = time(NULL);
    if (ctx->lastFrameTime > 0 && (now - ctx->lastFrameTime > FREEZE_TIMEOUT_SEC)) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
