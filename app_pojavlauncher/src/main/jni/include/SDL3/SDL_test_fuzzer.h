





#ifndef SDL_test_fuzzer_h_
#define SDL_test_fuzzer_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif






void SDLCALL SDLTest_FuzzerInit(Uint64 execKey);


Uint8 SDLCALL SDLTest_RandomUint8(void);


Sint8 SDLCALL SDLTest_RandomSint8(void);


Uint16 SDLCALL SDLTest_RandomUint16(void);


Sint16 SDLCALL SDLTest_RandomSint16(void);


Sint32 SDLCALL SDLTest_RandomSint32(void);


Uint32 SDLCALL SDLTest_RandomUint32(void);


Uint64 SDLTest_RandomUint64(void);


Sint64 SDLCALL SDLTest_RandomSint64(void);


float SDLCALL SDLTest_RandomUnitFloat(void);


double SDLCALL SDLTest_RandomUnitDouble(void);


float SDLCALL SDLTest_RandomFloat(void);


double SDLCALL SDLTest_RandomDouble(void);


Uint8 SDLCALL SDLTest_RandomUint8BoundaryValue(Uint8 boundary1, Uint8 boundary2, bool validDomain);


Uint16 SDLCALL SDLTest_RandomUint16BoundaryValue(Uint16 boundary1, Uint16 boundary2, bool validDomain);


Uint32 SDLCALL SDLTest_RandomUint32BoundaryValue(Uint32 boundary1, Uint32 boundary2, bool validDomain);


Uint64 SDLCALL SDLTest_RandomUint64BoundaryValue(Uint64 boundary1, Uint64 boundary2, bool validDomain);


Sint8 SDLCALL SDLTest_RandomSint8BoundaryValue(Sint8 boundary1, Sint8 boundary2, bool validDomain);


Sint16 SDLCALL SDLTest_RandomSint16BoundaryValue(Sint16 boundary1, Sint16 boundary2, bool validDomain);


Sint32 SDLCALL SDLTest_RandomSint32BoundaryValue(Sint32 boundary1, Sint32 boundary2, bool validDomain);


Sint64 SDLCALL SDLTest_RandomSint64BoundaryValue(Sint64 boundary1, Sint64 boundary2, bool validDomain);


Sint32 SDLCALL SDLTest_RandomIntegerInRange(Sint32 min, Sint32 max);


char * SDLCALL SDLTest_RandomAsciiString(void);


char * SDLCALL SDLTest_RandomAsciiStringWithMaximumLength(int maxLength);


char * SDLCALL SDLTest_RandomAsciiStringOfSize(int size);


int SDLCALL SDLTest_GetFuzzerInvocationCount(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
