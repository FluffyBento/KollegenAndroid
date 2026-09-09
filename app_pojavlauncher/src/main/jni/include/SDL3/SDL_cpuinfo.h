





#ifndef SDL_cpuinfo_h_
#define SDL_cpuinfo_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


#define SDL_CACHELINE_SIZE  128


extern SDL_DECLSPEC int SDLCALL SDL_GetNumLogicalCPUCores(void);


extern SDL_DECLSPEC int SDLCALL SDL_GetCPUCacheLineSize(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasAltiVec(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasMMX(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasSSE(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasSSE2(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasSSE3(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasSSE41(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasSSE42(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasAVX(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasAVX2(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasAVX512F(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasARMSIMD(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasNEON(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasLSX(void);


extern SDL_DECLSPEC bool SDLCALL SDL_HasLASX(void);


extern SDL_DECLSPEC int SDLCALL SDL_GetSystemRAM(void);


extern SDL_DECLSPEC size_t SDLCALL SDL_GetSIMDAlignment(void);


extern SDL_DECLSPEC int SDLCALL SDL_GetSystemPageSize(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
