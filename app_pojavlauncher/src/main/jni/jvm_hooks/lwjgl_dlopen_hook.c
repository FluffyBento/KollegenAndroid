



#include "jvm_hooks.h"

#include <android/api-level.h>

#include "environ/environ.h"

#include <dlfcn.h>
#include <string.h>
#include <stdlib.h>

#define TAG __FILE_NAME__
#include <log.h>

extern void* maybe_load_vulkan();


static jlong ndlopen_bugfix(__attribute__((unused)) JNIEnv *env,
                     __attribute__((unused)) jclass class,
                     jlong filename_ptr,
                     jint jmode) {
    const char* filename = (const char*) filename_ptr;

    
    if(strstr(filename, "libvulkan.so") == filename) {
        printf("LWJGL linkerhook: replacing load for libvulkan.so with custom driver\n");
        return (jlong) maybe_load_vulkan();
    }

    
    
    
    
    
    

    int mode = (int)jmode;
    return (jlong) dlopen(filename, mode);
}


void installLwjglDlopenHook(JNIEnv *env) {
    LOGI("Installing LWJGL dlopen() hook");
    jclass dynamicLinkLoader = (*env)->FindClass(env, "org/lwjgl/system/linux/DynamicLinkLoader");
    if(dynamicLinkLoader == NULL) {
        LOGE("Failed to find the target class");
        (*env)->ExceptionClear(env);
        return;
    }
    JNINativeMethod ndlopenMethod[] = {
            {"ndlopen", "(JI)J", &ndlopen_bugfix}
    };
    if((*env)->RegisterNatives(env, dynamicLinkLoader, ndlopenMethod, 1) != 0) {
        LOGE("Failed to register the hooked method");
        (*env)->ExceptionClear(env);
    }
}