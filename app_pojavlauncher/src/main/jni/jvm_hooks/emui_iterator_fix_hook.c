



#include "jvm_hooks.h"
#include <stdlib.h>

#define TAG __FILE_NAME__
#include <log.h>


jint getLibraryPath_fix(__attribute__((unused)) JNIEnv *env,
                        __attribute__((unused)) jclass class,
                        __attribute__((unused)) jlong pLibAddress,
                        __attribute__((unused)) jlong sOutAddress,
                        __attribute__((unused)) jint bufSize){
    return 0;
}


void installEMUIIteratorMititgation(JNIEnv *env) {
    if(getenv("POJAV_EMUI_ITERATOR_MITIGATE") == NULL) return;
    LOGI("Installing...");
    jclass sharedLibraryUtil = (*env)->FindClass(env, "org/lwjgl/system/SharedLibraryUtil");
    if(sharedLibraryUtil == NULL) {
        LOGE("Failed to find target class");
        (*env)->ExceptionClear(env);
        return;
    }
    JNINativeMethod getLibraryPathMethod[] = {
            {"getLibraryPath", "(JJI)I", &getLibraryPath_fix}
    };
    if((*env)->RegisterNatives(env, sharedLibraryUtil, getLibraryPathMethod, 1) != 0) {
        LOGE("Failed to register the mitigation method");
        (*env)->ExceptionClear(env);
    }
}