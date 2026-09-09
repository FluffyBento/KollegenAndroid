



#include <SDL3/SDL_platform_defines.h>

#ifdef SDL_PLATFORM_IOS
#include <OpenGLES/ES1/gl.h>
#include <OpenGLES/ES1/glext.h>
#else
#include <GLES/gl.h>
#include <GLES/glext.h>
#endif

#ifndef APIENTRY
#define APIENTRY
#endif
