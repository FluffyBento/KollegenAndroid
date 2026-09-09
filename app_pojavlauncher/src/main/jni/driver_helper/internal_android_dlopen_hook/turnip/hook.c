#include <string.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <inttypes.h>

#include <androidnsbypass/nsbypass_t.h>
#include <androidnsbypass/nsbypass.h>

#include "log.h"

static void* turnipHandle;

static struct android_namespace_t* turnipNs;

static uint64_t (*atrace_get_enabled_tags_p)();

__attribute__((visibility("default"), used)) void *android_dlopen_ext(const char *filename, int flags, const android_dlextinfo *extinfo) {
    if(!strstr(filename, "vulkan."))
        return private_dlopen_ext(filename, flags, extinfo, &android_dlopen_ext);
    if (!turnipHandle){
        
        
        turnipNs = private_create_namespace(
                "turnip-driver-NS",
                NULL,
                getenv("POJAV_NATIVEDIR"),
                
                
                ANDROID_NAMESPACE_TYPE_SHARED_ISOLATED,
                NULL,
                
                
                extinfo->library_namespace,
                __builtin_return_address(0)
                );
        turnipHandle = linker_ns_dlopen("libvulkan_freedreno.so", RTLD_LOCAL | RTLD_NOW, turnipNs);
        if(turnipHandle == NULL) {
            printf("AdrenoSupp: Failed to load Turnip!\n%s\n", dlerror());
            return NULL;
        }
    }
    return turnipHandle;
}

__attribute__((visibility("default"), used)) void *android_load_sphal_library(const char *filename, int flags) {
    
    const char *sphal_namespaces[3] = {
            "sphal", "vendor", "default"
    };

    struct android_namespace_t* androidNamespace;
    for(int i = 0; i < 3; i++) {
        androidNamespace = private_get_exported_namespace(sphal_namespaces[i]);
        if(androidNamespace != NULL) break;
    }
    android_dlextinfo info = {0};
    info.flags = ANDROID_DLEXT_USE_NAMESPACE;
    info.library_namespace = androidNamespace;
    return android_dlopen_ext(filename, flags, &info);
}


__attribute__((visibility("default"), used)) uint64_t atrace_get_enabled_tags() {
    if (!atrace_get_enabled_tags_p) {
        dlerror(); 
        void* cutilsHandle = private_dlopen("libcutils.so", RTLD_LOCAL | RTLD_LAZY, &dlopen);
        if (!cutilsHandle) {
            LOGW("Unable to load libcutils in liblinkerhook, are we in an escape namespace? Assuming atrace tags to be ATRACE_TAG_NEVER.\n dlopen error: %s", dlerror());
            return 0; 
        }
        atrace_get_enabled_tags_p = private_dlsym(
                cutilsHandle,
                "atrace_get_enabled_tags",
                &dlopen
        );
        if (!atrace_get_enabled_tags_p) {
            LOGW("Unable to get atrace_get_enabled_tags from libcutils, dlsym error: %s", dlerror());
            return 0; 
        }
    }
    uint64_t result = atrace_get_enabled_tags_p();
    return result;
}