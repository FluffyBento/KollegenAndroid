





#ifndef SDL_loadso_h_
#define SDL_loadso_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_SharedObject SDL_SharedObject;


extern SDL_DECLSPEC SDL_SharedObject * SDLCALL SDL_LoadObject(const char *sofile);


extern SDL_DECLSPEC SDL_FunctionPointer SDLCALL SDL_LoadFunction(SDL_SharedObject *handle, const char *name);


extern SDL_DECLSPEC void SDLCALL SDL_UnloadObject(SDL_SharedObject *handle);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
