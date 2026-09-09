

#pragma once



#ifndef NSBYPASS_NSBYPASS_T_H
#define NSBYPASS_NSBYPASS_T_H

#include <android/dlext.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct android_namespace_t* (*private_create_namespace_t)(
        const char* name,
        const char* ld_library_path,
        const char* default_library_path,
        uint64_t type,
        const char* permitted_when_isolated_path,
        struct android_namespace_t* parent_namespace,
        const void* caller_addr);

typedef bool (*private_link_namespaces_t)(
        struct android_namespace_t* from,
        struct android_namespace_t* to,
        const char* shared_libs_sonames);

typedef bool (*private_link_namespaces_all_libs_t)(
        struct android_namespace_t* from,
        struct android_namespace_t* to);

typedef struct android_namespace_t* (*private_get_exported_namespace_t)(
        const char* name);


typedef int (*private_dlclose_function_t)(
        void *handle);

typedef void *(*private_dlopen_function_t)(
        const char* filename,
        int flags,
        const void* caller_addr);

typedef void *(*private_dlsym_function_t)(
        void* handle,
        const char* symbol,
        const void* caller_addr);




typedef void *(*private_dlopen_ext_function_t)(const char* filename,
        int flags,
        const android_dlextinfo* extinfo,
        const void* caller_addr);


typedef void *(*android_dlopen_ext_t)(
        const char* filename,
        int flag,
        const android_dlextinfo* extinfo);


typedef struct android_namespace_t *(*android_get_exported_namespace_t)(
        const char* name);


enum {
    
    ANDROID_NAMESPACE_TYPE_REGULAR = 0,

    
    
    ANDROID_NAMESPACE_TYPE_ISOLATED = 1,

    
    ANDROID_NAMESPACE_TYPE_SHARED = 2,

    
    ANDROID_NAMESPACE_TYPE_EXEMPT_LIST_ENABLED = 0x08000000,

    
    ANDROID_NAMESPACE_TYPE_ALSO_USED_AS_ANONYMOUS = 0x10000000,

    
    ANDROID_NAMESPACE_TYPE_SHARED_ISOLATED =
    ANDROID_NAMESPACE_TYPE_SHARED | ANDROID_NAMESPACE_TYPE_ISOLATED,
};

#ifdef __cplusplus
}
#endif

#endif 
