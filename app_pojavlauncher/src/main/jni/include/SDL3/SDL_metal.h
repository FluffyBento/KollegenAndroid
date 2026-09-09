



#ifndef SDL_metal_h_
#define SDL_metal_h_

#include <SDL3/SDL_video.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef void *SDL_MetalView;





extern SDL_DECLSPEC SDL_MetalView SDLCALL SDL_Metal_CreateView(SDL_Window *window);


extern SDL_DECLSPEC void SDLCALL SDL_Metal_DestroyView(SDL_MetalView view);


extern SDL_DECLSPEC void * SDLCALL SDL_Metal_GetLayer(SDL_MetalView view);




#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
