


#include <dlfcn.h>
#include <linux/limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "androidnsbypass/nsbypass.h"
#include "androidnsbypass/nsbypass_t.h"
#include "global_state.h"

void* loader_dlopen(char* primaryName, char* secondaryName, int flags) {
    void* dl_handle;
    if(primaryName == NULL) goto secondary;

    dl_handle = dlopen(primaryName, flags);
    if(dl_handle != NULL) return dl_handle;
    
    if (!app_escapeNs) {
        app_escapeNs = private_create_namespace(
                "app-escapeNs",
                NULL,
                getenv("POJAV_NATIVEDIR"), 
                ANDROID_NAMESPACE_TYPE_SHARED, 
                getenv("POJAV_NATIVEDIR"), 
                get_escape_namespace(), 
                __builtin_return_address(0));
    }
    dl_handle = linker_ns_dlopen(primaryName, RTLD_LOCAL | RTLD_LAZY, app_escapeNs);
    if(dl_handle != NULL) return dl_handle;

    if(secondaryName == NULL) goto dl_error;

    secondary:
    dl_handle = dlopen(secondaryName, flags);
    if(dl_handle == NULL) goto dl_error;
    
    if (!app_escapeNs) {
        app_escapeNs = private_create_namespace(
                "app-escapeNs",
                NULL,
                getenv("POJAV_NATIVEDIR"), 
                ANDROID_NAMESPACE_TYPE_SHARED, 
                getenv("POJAV_NATIVEDIR"), 
                get_escape_namespace(), 
                __builtin_return_address(0));
    }
    dl_handle = linker_ns_dlopen(secondaryName, RTLD_LOCAL | RTLD_LAZY, app_escapeNs);
    return dl_handle;

    dl_error:
    printf("%s", dlerror());
    return NULL;
}