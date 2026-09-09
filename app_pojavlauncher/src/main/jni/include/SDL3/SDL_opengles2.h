



#include <SDL3/SDL_platform_defines.h>

#if !defined(_MSC_VER) && !defined(SDL_USE_BUILTIN_OPENGL_DEFINITIONS)

#ifdef SDL_PLATFORM_IOS
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#else
#include <GLES2/gl2platform.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#else 


#include <SDL3/SDL_opengles2_khrplatform.h>
#include <SDL3/SDL_opengles2_gl2platform.h>
#include <SDL3/SDL_opengles2_gl2.h>
#include <SDL3/SDL_opengles2_gl2ext.h>

#endif 

#ifndef APIENTRY
#define APIENTRY GL_APIENTRY
#endif
