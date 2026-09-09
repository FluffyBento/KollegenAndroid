





#ifndef SDL_test_assert_h_
#define SDL_test_assert_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


#define ASSERT_FAIL     0


#define ASSERT_PASS     1


void SDLCALL SDLTest_Assert(int assertCondition, SDL_PRINTF_FORMAT_STRING const char *assertDescription, ...) SDL_PRINTF_VARARG_FUNC(2);


int SDLCALL SDLTest_AssertCheck(int assertCondition, SDL_PRINTF_FORMAT_STRING const char *assertDescription, ...) SDL_PRINTF_VARARG_FUNC(2);


void SDLCALL SDLTest_AssertPass(SDL_PRINTF_FORMAT_STRING const char *assertDescription, ...) SDL_PRINTF_VARARG_FUNC(1);


void SDLCALL SDLTest_ResetAssertSummary(void);


void SDLCALL SDLTest_LogAssertSummary(void);


int SDLCALL SDLTest_AssertSummaryToTestResult(void);

#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
