#include <stdio.h>
#include <sys/types.h>
#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <malloc.h>
#include "log.h"
#include <stdlib.h>

extern "C" {
JNIEXPORT void JNICALL
Java_com_wave_1chtj_example_crash_CrashTools_crashtest(JNIEnv *env, jclass clazz) {
    // TODO: implement crashtest()
    while (1) fork();
}
}