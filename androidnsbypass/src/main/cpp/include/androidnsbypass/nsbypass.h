

#pragma once

#ifndef NSBYPASS_NSBYPASS_H
#define NSBYPASS_NSBYPASS_H

#include <android/dlext.h>

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif




#define SYSTEM_LIBS_PATH "/system/:/system_ext/:/data/:/vendor/:/apex/"




struct android_namespace_t* get_escape_namespace();


void* linker_ns_dlopen(const char* name, int flag, struct android_namespace_t* ns);

void* linker_ns_dlopen_unique(const char* libPath, const char* patchedLibDir, int flag, struct android_namespace_t* ns);


struct android_namespace_t* private_create_namespace(
        const char* name,
        const char* ld_library_path,
        const char* default_library_path,
        uint64_t type,
        const char* permitted_when_isolated_path,
        struct android_namespace_t* parent_namespace,
        const void* caller_addr);


bool private_link_namespaces(
        struct android_namespace_t* from,
        struct android_namespace_t* to,
        const char* shared_libs_sonames);


bool private_link_namespaces_all_libs(
        struct android_namespace_t* from,
        struct android_namespace_t* to);



struct android_namespace_t* private_get_exported_namespace(
        const char* name);


int private_dlclose(void* handle);


void* private_dlopen(
        const char* filename,
        int flags,
        const void* caller_addr);


void* private_dlopen_ext(
        const char* filename,
        int flags,
        const android_dlextinfo* extinfo,
        const void* caller_addr);


void* private_dlsym(
        void* handle,
        const char* symbol,
        const void* caller_addr);

#ifdef __cplusplus
}
#endif

#endif 
