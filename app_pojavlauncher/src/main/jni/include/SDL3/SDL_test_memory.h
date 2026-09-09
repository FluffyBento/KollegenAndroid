



#ifndef SDL_test_memory_h_
#define SDL_test_memory_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


void SDLCALL SDLTest_TrackAllocations(void);


void SDLCALL SDLTest_RandFillAllocations(void);


void SDLCALL SDLTest_LogAllocations(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
