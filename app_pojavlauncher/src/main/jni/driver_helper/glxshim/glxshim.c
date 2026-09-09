


#include "../../ctxbridges/egl_loader.h"



__attribute__((used)) void *glXGetProcAddress(const char *name) {
    return getProcAddress(name);
}