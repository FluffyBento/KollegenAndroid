



#ifndef SDL_main_h_
#define SDL_main_h_

#include <SDL3/SDL_platform_defines.h>
#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_events.h>

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_MAIN_HANDLED 1


#define SDL_MAIN_USE_CALLBACKS 1


#define SDL_MAIN_AVAILABLE


#define SDL_MAIN_NEEDED

#endif

#if defined(__has_include)
    #if __has_include("SDL_main_private.h") && __has_include("SDL_main_impl_private.h")
        #define SDL_PLATFORM_PRIVATE_MAIN
    #endif
#endif

#ifndef SDL_MAIN_HANDLED
    #if defined(SDL_PLATFORM_PRIVATE_MAIN)
        
        #include "SDL_main_private.h"

    #elif defined(SDL_PLATFORM_WIN32)
        
        #define SDL_MAIN_AVAILABLE

    #elif defined(SDL_PLATFORM_GDK)
        
        #define SDL_MAIN_NEEDED

    #elif defined(SDL_PLATFORM_IOS) || defined(SDL_PLATFORM_TVOS)
        
        #define SDL_MAIN_NEEDED

    #elif defined(SDL_PLATFORM_ANDROID)
        
        #define SDL_MAIN_NEEDED

        
        #define SDL_MAIN_EXPORTED

    #elif defined(SDL_PLATFORM_EMSCRIPTEN)
        
        #define SDL_MAIN_AVAILABLE

    #elif defined(SDL_PLATFORM_PSP)
        
        #define SDL_MAIN_AVAILABLE

    #elif defined(SDL_PLATFORM_PS2)
        #define SDL_MAIN_AVAILABLE

        #define SDL_PS2_SKIP_IOP_RESET() \
           void reset_IOP(); \
           void reset_IOP() {}

    #elif defined(SDL_PLATFORM_3DS)
        
        #define SDL_MAIN_AVAILABLE

    #endif
#endif 


#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDLMAIN_DECLSPEC

#elif defined(SDL_MAIN_EXPORTED)

#define SDLMAIN_DECLSPEC    SDL_DECLSPEC
#else

#define SDLMAIN_DECLSPEC
#endif 

#if defined(SDL_MAIN_NEEDED) || defined(SDL_MAIN_AVAILABLE) || defined(SDL_MAIN_USE_CALLBACKS)
#define main SDL_main
#endif

#include <SDL3/SDL_init.h>
#include <SDL3/SDL_begin_code.h>
#ifdef __cplusplus
extern "C" {
#endif


#ifdef SDL_MAIN_USE_CALLBACKS


extern SDLMAIN_DECLSPEC SDL_AppResult SDLCALL SDL_AppInit(void **appstate, int argc, char *argv[]);


extern SDLMAIN_DECLSPEC SDL_AppResult SDLCALL SDL_AppIterate(void *appstate);


extern SDLMAIN_DECLSPEC SDL_AppResult SDLCALL SDL_AppEvent(void *appstate, SDL_Event *event);


extern SDLMAIN_DECLSPEC void SDLCALL SDL_AppQuit(void *appstate, SDL_AppResult result);

#endif  



typedef int (SDLCALL *SDL_main_func)(int argc, char *argv[]);


extern SDLMAIN_DECLSPEC int SDLCALL SDL_main(int argc, char *argv[]);


extern SDL_DECLSPEC void SDLCALL SDL_SetMainReady(void);


extern SDL_DECLSPEC int SDLCALL SDL_RunApp(int argc, char *argv[], SDL_main_func mainFunction, void *reserved);


extern SDL_DECLSPEC int SDLCALL SDL_EnterAppMainCallbacks(int argc, char *argv[], SDL_AppInit_func appinit, SDL_AppIterate_func appiter, SDL_AppEvent_func appevent, SDL_AppQuit_func appquit);


#if defined(SDL_PLATFORM_WINDOWS)


extern SDL_DECLSPEC bool SDLCALL SDL_RegisterApp(const char *name, Uint32 style, void *hInst);


extern SDL_DECLSPEC void SDLCALL SDL_UnregisterApp(void);

#endif 


extern SDL_DECLSPEC void SDLCALL SDL_GDKSuspendComplete(void);

#ifdef __cplusplus
}
#endif

#include <SDL3/SDL_close_code.h>

#if !defined(SDL_MAIN_HANDLED) && !defined(SDL_MAIN_NOIMPL)
    
    #if defined(SDL_MAIN_USE_CALLBACKS) || defined(SDL_MAIN_NEEDED) || defined(SDL_MAIN_AVAILABLE)
        
        #include <SDL3/SDL_main_impl.h>
    #endif
#endif

#endif 
