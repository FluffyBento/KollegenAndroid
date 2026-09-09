





#ifndef SDL_test_crc32_h_
#define SDL_test_crc32_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif





#ifndef CrcUint32
 #define CrcUint32  unsigned int
#endif
#ifndef CrcUint8
 #define CrcUint8   unsigned char
#endif

#ifdef ORIGINAL_METHOD
 #define CRC32_POLY 0x04c11db7   
#else
 #define CRC32_POLY 0xEDB88320   
#endif


  typedef struct SDLTest_Crc32Context {
    CrcUint32    crc32_table[256]; 
  } SDLTest_Crc32Context;




bool SDLCALL SDLTest_Crc32Init(SDLTest_Crc32Context *crcContext);


bool SDLCALL SDLTest_Crc32Calc(SDLTest_Crc32Context *crcContext, CrcUint8 *inBuf, CrcUint32 inLen, CrcUint32 *crc32);


bool SDLCALL SDLTest_Crc32CalcStart(SDLTest_Crc32Context *crcContext, CrcUint32 *crc32);
bool SDLCALL SDLTest_Crc32CalcEnd(SDLTest_Crc32Context *crcContext, CrcUint32 *crc32);
bool SDLCALL SDLTest_Crc32CalcBuffer(SDLTest_Crc32Context *crcContext, CrcUint8 *inBuf, CrcUint32 inLen, CrcUint32 *crc32);



bool SDLCALL SDLTest_Crc32Done(SDLTest_Crc32Context *crcContext);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
