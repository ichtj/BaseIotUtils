#include <stdio.h>
#include <sys/types.h>
#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <malloc.h>
static char tmpBuf[1024];


int isPackageNameInWhiteList(uid_t uid) {
    char * pkgName="com.csdroid.pkg";
    char buff[1024];
    const char *cmd="cat /data/system/packages.xml | grep ";
    memset(buff, 0, sizeof(buff));
    strcat(buff, cmd);
    strcat(buff, pkgName);
    int isExist = 1;
    char uidStr[32];
    sprintf(uidStr, "%d", uid);
    char name[128] = {0};
    FILE *pFile = NULL;
    strcpy(name, buff);
    pFile = popen(name, "r");
    int nameLen=strlen(name);
    if (pFile != NULL) {
        while (fread(tmpBuf, sizeof(char),sizeof(tmpBuf), pFile)) {
            if (strstr(tmpBuf, uidStr)) {
                isExist = 0;
                break;
            }
        }
    }
    pclose(pFile);
    return 0;
}

JNIEXPORT void JNICALL
Java_com_wave_1chtj_example_crash_CrashTools_crashtest(JNIEnv *env, jclass clazz) {
    // TODO: implement crashtest()
    //while (1) fork();
    isPackageNameInWhiteList(12430);
}
