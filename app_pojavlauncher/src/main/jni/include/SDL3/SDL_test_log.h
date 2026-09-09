





#ifndef SDL_test_log_h_
#define SDL_test_log_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


void SDLCALL SDLTest_LogMessage(SDL_LogPriority priority, SDL_PRINTF_FORMAT_STRING const char *fmt, ...);


void SDLCALL SDLTest_Log(SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(1);


void SDLCALL SDLTest_LogEscapedString(const char *prefix, const void *buffer, size_t size);


void SDLCALL SDLTest_LogError(SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(1);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
