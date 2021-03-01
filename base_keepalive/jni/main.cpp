#include <jni.h>
#include "art.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_chtj_keepalive_Reflection_unsealNative(JNIEnv *env, jclass type, jint targetSdkVersion) {
    return unseal(env, targetSdkVersion);
}
