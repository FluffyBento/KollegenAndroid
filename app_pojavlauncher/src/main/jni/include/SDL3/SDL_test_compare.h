





#ifndef SDL_test_compare_h_
#define SDL_test_compare_h_

#include <SDL3/SDL.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


int SDLCALL SDLTest_CompareSurfaces(SDL_Surface *surface, SDL_Surface *referenceSurface, int allowable_error);
int SDLCALL SDLTest_CompareSurfacesIgnoreTransparentPixels(SDL_Surface *surface, SDL_Surface *referenceSurface, int allowable_error);


int SDLCALL SDLTest_CompareMemory(const void *actual, size_t size_actual, const void *reference, size_t size_reference);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
