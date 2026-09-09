







#ifndef SDL_test_md5_h_
#define SDL_test_md5_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif




typedef Uint32 MD5UINT4;


typedef struct SDLTest_Md5Context {
    MD5UINT4 i[2];              
    MD5UINT4 buf[4];            
    unsigned char in[64];       
    unsigned char digest[16];   
} SDLTest_Md5Context;




void SDLCALL SDLTest_Md5Init(SDLTest_Md5Context *mdContext);


void SDLCALL SDLTest_Md5Update(SDLTest_Md5Context *mdContext, unsigned char *inBuf,
                 unsigned int inLen);


void SDLCALL SDLTest_Md5Final(SDLTest_Md5Context *mdContext);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
