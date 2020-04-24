//
// Created by zzg on 2018/10/6.
//
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdio.h>

#include "com_chtj_base_iotutils_serialport_SerialPort.h"

#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

int fd;



/**
*@brief   设置串口数据位，停止位和效验位
*@param  fd     类型  int  打开的串口文件句柄*
*@param  databits 类型  int 数据位   取值 为 7 或者8*
*@param  stopbits 类型  int 停止位   取值为 1 或者2*
*@param  parity  类型  int  效验类型 取值为N,E,O,,S
*/
int set_Parity(int fd,int databits,int stopbits,int parity,int rctsbits)
{
    struct termios options;
    if  ( tcgetattr( fd,&options)  !=  0)
    {
        perror("SetupSerial 1");
        return 1;
    }

    options.c_oflag &= ~OPOST;
    options.c_cflag &= ~CSIZE;
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    options.c_cflag &= ~CRTSCTS;

    //printf("%s :---------------------00\r\n",__func__);

    switch (databits) /*设置数据位数*/
    {
        case 7:
            options.c_cflag |= CS7;
            break;
        case 8:
            options.c_cflag |= CS8;
            break;
        default:
            fprintf(stderr,"Unsupported data size\n");
            return 1;
    }
    //printf("%s :---------------------11\r\n",__func__);
    switch (parity)
    {
        case 'n':
        case 'N':
            options.c_cflag &= ~PARENB;   /* Clear parity enable */
            //options.c_iflag &= ~INPCK;     /* Enable parity checking */
            break;
        case 'o':
        case 'O':
            options.c_cflag |= (PARODD | PARENB);  /* 设置为奇效验*/
            options.c_iflag |= INPCK;             /* Disnable parity checking */
            break;
        case 'e':
        case 'E':
            options.c_cflag |= PARENB;     /* Enable parity */
            options.c_cflag &= ~PARODD;   /* 转换为偶效验*/
            options.c_iflag |= INPCK;       /* Disnable parity checking */
            break;
        case 'S':
        case 's':  /*as no parity*/
            options.c_cflag &= ~PARENB;
            options.c_cflag &= ~CSTOPB;
            break;
        default:
            fprintf(stderr,"Unsupported parity\n");
            return 1;
    }
    //printf("%s :---------------------22\r\n",__func__);

    /* 设置停止位*/
    switch (stopbits)
    {
        case 1:
            options.c_cflag &= ~CSTOPB;
            break;
        case 2:
            options.c_cflag |= CSTOPB;
            break;
        default:
            fprintf(stderr,"Unsupported stop bits\n");
            return 1;
    }

    /* Set input parity option */
    // if (parity != 'n')
    // 		options.c_iflag |= INPCK;
    //printf("%s :---------------------33\r\n",__func__);
    //硬件流控制设置
    switch (rctsbits)
    {
        case 'h':
        case 'H':
            options.c_cflag |= CRTSCTS;
            break;
        case 's':
        case 'S':
            options.c_iflag |= (IXON | IXOFF | IXANY);
            break;
        case 'n':
        case 'N':
            options.c_cflag &= ~CRTSCTS;
            //options.c_iflag &= ~(IXON | IXOFF | IXANY | CRTSCTS);
            break;
    }

    //printf("%s :---------------------options.c_cflag == 0x%x \r\n",__func__,options.c_cflag);
    options.c_cc[VTIME] = 10;//150 - 15s
    options.c_cc[VMIN] = 0;

    options.c_iflag &= ~(ICRNL | IXON);
    //options.c_iflag &= ~(ICRNL | INPCK | ISTRIP | IXON | BRKINT );

    //==============================================================
    /*
    * 激活选项有CLOCAL和CREAD
    * CLOCAL和CREAD分别用于本地连接和接受使能，通过位掩码的方式激活这两个选项。
    */
    options.c_cflag |= CLOCAL | CREAD;
    //==============================================================
    //printf("%s :---------------------55\r\n",__func__);

    tcflush(fd,TCIFLUSH); /* Update the options and do it NOW */
    if (tcsetattr(fd,TCSANOW,&options) != 0)
    {
        perror("SetupSerial 3");
        return 1;
    }
    return 0;
}

int set_opt(jint nBits, jchar nEvent, jint nStop)
{

    LOGE("set_opt:nBits=%d,nEvent=%c,nSpeed=%d,nStop=%d", nBits, nEvent, nStop);

    LOGE("set_opt:nStop=%d",nStop);
    struct termios newtio;

    if(tcgetattr(fd, & newtio) != 0)
    {

        LOGE("setup serial failure");

        return -1;

    }

    bzero( & newtio, sizeof(newtio));   // memset(&newtio,0,sizeof(newtio));


    //c_cflag标志可以定义CLOCAL和CREAD，这将确保该程序不被其他端口控制和信号干扰，同时串口驱动将读取进入的数据。CLOCAL和CREAD通常总是被是能的

    newtio.c_cflag |= CLOCAL | CREAD;


    switch(nBits) //设置数据位数
    {

        case 7:

            newtio.c_cflag &= ~CSIZE;

            newtio.c_cflag |= CS7;

            break;

        case 8:

            newtio.c_cflag &= ~CSIZE;

            newtio.c_cflag |= CS8;

            LOGD("8位数据位");

            break;

        default:


            break;

    }

    switch(nEvent) //设置校验位
    {

        case 'O':

            newtio.c_cflag |= PARENB; //enable parity checking

            newtio.c_cflag |= PARODD; //奇校验位

            newtio.c_iflag |= (INPCK | ISTRIP);



            break;

        case 'E':

           newtio.c_cflag |= PARENB; //

            newtio.c_cflag &= ~PARODD; //偶校验位

            newtio.c_iflag |= (INPCK | ISTRIP);


        /*    newtio.c_cflag |= PARENB;
            newtio.c_cflag &= ~PARODD;
            newtio.c_iflag |= INPCK;*/

            LOGD("偶校验位..");

            break;

        case 'N':

            newtio.c_cflag &= ~PARENB; //清除校验位



            break;


        default:


            break;

    }
    switch(nStop) //设置停止位
    {

        case 1:

            newtio.c_cflag &= ~CSTOPB;
            LOGD("1位停止位");

            break;

        case 2:

            newtio.c_cflag |= CSTOPB;

            break;

        default:

            // LOGW("nStop:%d,invalid param", nStop);

            break;

    }

    newtio.c_cc[VTIME] = 0;//设置等待时间

    newtio.c_cc[VMIN] = 0;//设置最小接收字符

    tcflush(fd, TCIFLUSH);

    if(tcsetattr(fd, TCSANOW, & newtio) != 0)
    {

        LOGE("options set error");

        return -1;

    }


    LOGE("options set success");
    return 1;

}



static speed_t getBaudrate(jint baudrate)
{
    switch(baudrate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200:
            LOGD(" case 1200");
            return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return -1;
    }
}


/*
 * Class:     android_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_com_face_1chtj_base_1iotutils_serialport_SerialPort_open
        (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags)
{
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Check arguments */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1) {
            /* TODO: throw an exception */
            LOGE("Invalid baudrate|无效的波特率");
            return NULL;
        }
    }

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1)
        {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Configure device */
    {
        struct termios cfg;
        LOGD("Configuring serial port");
        if (tcgetattr(fd, &cfg))
        {
            LOGE("tcgetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd, TCSANOW, &cfg))
        {
            LOGE("tcsetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
    }

    return mFileDescriptor;
}


JNIEXPORT jobject JNICALL Java_com_face_1chtj_base_1iotutils_serialport_SerialPort_open2
        (JNIEnv *env, jclass thiz, jstring path, jint baudrate,
         jint databits, jint stopbits, jchar parity)
{

    speed_t speed;
    jobject mFileDescriptor;

    /*波特率 */
    {
        speed = getBaudrate(baudrate);
        if (speed == -1)
        {
            /* TODO: throw an exception */
            LOGE("Invalid baudrate");
            return NULL;
        }
    }

    /* Opening device */
    {
        jint flags = 0;
        jboolean iscopy;
        const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | O_NONBLOCK);
        //fd=fd;
        LOGD("open() fd = %d", fd);
        (*env)->ReleaseStringUTFChars(env, path, path_utf);
        if (fd == -1)
        {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }

    /* Configure device */
    {
        struct termios cfg;
        LOGD("Configuring serial port");
        if (tcgetattr(fd, &cfg))
        {
            LOGE("tcgetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }



        cfmakeraw(&cfg);
        //设置波特率
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd, TCSANOW, &cfg))
        {
            LOGE("tcsetattr() failed");
            close(fd);
            /* TODO: throw an exception */
            return NULL;
        }

        //配置校验位 停止位等等
     //   set_opt(databits, parity, stopbits);

        set_Parity(fd,8,1,'E','n');
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
        jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
        mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
        (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
    }

    return mFileDescriptor;

}



/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_face_1chtj_base_1iotutils_serialport_SerialPort_close
        (JNIEnv *env, jobject thiz)
{
    jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
    jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

    jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

    jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
    jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}

