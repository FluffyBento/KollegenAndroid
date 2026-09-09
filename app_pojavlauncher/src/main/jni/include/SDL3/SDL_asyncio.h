





#ifndef SDL_asyncio_h_
#define SDL_asyncio_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_AsyncIO SDL_AsyncIO;


typedef enum SDL_AsyncIOTaskType
{
    SDL_ASYNCIO_TASK_READ,   
    SDL_ASYNCIO_TASK_WRITE,  
    SDL_ASYNCIO_TASK_CLOSE   
} SDL_AsyncIOTaskType;


typedef enum SDL_AsyncIOResult
{
    SDL_ASYNCIO_COMPLETE,  
    SDL_ASYNCIO_FAILURE,   
    SDL_ASYNCIO_CANCELED   
} SDL_AsyncIOResult;


typedef struct SDL_AsyncIOOutcome
{
    SDL_AsyncIO *asyncio;   
    SDL_AsyncIOTaskType type;  
    SDL_AsyncIOResult result;  
    void *buffer;  
    Uint64 offset;  
    Uint64 bytes_requested;  
    Uint64 bytes_transferred;  
    void *userdata;    
} SDL_AsyncIOOutcome;


typedef struct SDL_AsyncIOQueue SDL_AsyncIOQueue;


extern SDL_DECLSPEC SDL_AsyncIO * SDLCALL SDL_AsyncIOFromFile(const char *file, const char *mode);


extern SDL_DECLSPEC Sint64 SDLCALL SDL_GetAsyncIOSize(SDL_AsyncIO *asyncio);


extern SDL_DECLSPEC bool SDLCALL SDL_ReadAsyncIO(SDL_AsyncIO *asyncio, void *ptr, Uint64 offset, Uint64 size, SDL_AsyncIOQueue *queue, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_WriteAsyncIO(SDL_AsyncIO *asyncio, void *ptr, Uint64 offset, Uint64 size, SDL_AsyncIOQueue *queue, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_CloseAsyncIO(SDL_AsyncIO *asyncio, bool flush, SDL_AsyncIOQueue *queue, void *userdata);


extern SDL_DECLSPEC SDL_AsyncIOQueue * SDLCALL SDL_CreateAsyncIOQueue(void);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyAsyncIOQueue(SDL_AsyncIOQueue *queue);


extern SDL_DECLSPEC bool SDLCALL SDL_GetAsyncIOResult(SDL_AsyncIOQueue *queue, SDL_AsyncIOOutcome *outcome);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitAsyncIOResult(SDL_AsyncIOQueue *queue, SDL_AsyncIOOutcome *outcome, Sint32 timeoutMS);


extern SDL_DECLSPEC void SDLCALL SDL_SignalAsyncIOQueue(SDL_AsyncIOQueue *queue);


extern SDL_DECLSPEC bool SDLCALL SDL_LoadFileAsync(const char *file, SDL_AsyncIOQueue *queue, void *userdata);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
