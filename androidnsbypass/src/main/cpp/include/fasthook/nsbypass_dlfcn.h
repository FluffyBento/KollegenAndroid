

#pragma once

#ifndef NSBYPASS_NSBYPASS_DLFCN_H
#define NSBYPASS_NSBYPASS_DLFCN_H

#ifdef __cplusplus
extern "C" {
#endif


int nsbypass_dlclose(void *handle);

void *nsbypass_dlopen(const char *libPath, int flags);
void *nsbypass_dlsym(void *handle, const char *name);


#ifdef __cplusplus
}
#endif

#endif 
