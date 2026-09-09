



#ifndef SDL_main_impl_h_
#define SDL_main_impl_h_

#ifndef SDL_main_h_
#error "This header should not be included directly, but only via SDL_main.h!"
#endif


#if !defined(SDL_MAIN_HANDLED) && !defined(SDL_MAIN_NOIMPL)

    
    #ifdef main
        #undef main
    #endif

    #ifdef SDL_MAIN_USE_CALLBACKS

        #if 0
            

        #else 

            
            #define SDL_MAIN_CALLBACK_STANDARD 1

            int SDL_main(int argc, char **argv)
            {
                return SDL_EnterAppMainCallbacks(argc, argv, SDL_AppInit, SDL_AppIterate, SDL_AppEvent, SDL_AppQuit);
            }

        #endif  

    #endif  


    
    #if (!defined(SDL_MAIN_USE_CALLBACKS) || defined(SDL_MAIN_CALLBACK_STANDARD)) && !defined(SDL_MAIN_EXPORTED)

        #if defined(SDL_PLATFORM_PRIVATE_MAIN)
            
            #include "SDL_main_impl_private.h"

        #elif defined(SDL_PLATFORM_WINDOWS)

            
            #ifndef WINAPI
                #define WINAPI __stdcall
            #endif

            typedef struct HINSTANCE__ * HINSTANCE;
            typedef char *LPSTR;
            typedef wchar_t *PWSTR;

            
            #if defined(_MSC_VER) && !defined(SDL_PLATFORM_GDK)

                
                #if defined(UNICODE) && UNICODE
                    int wmain(int argc, wchar_t *wargv[], wchar_t *wenvp)
                    {
                        (void)argc;
                        (void)wargv;
                        (void)wenvp;
                        return SDL_RunApp(0, NULL, SDL_main, NULL);
                    }
                #else 
                    int main(int argc, char *argv[])
                    {
                        (void)argc;
                        (void)argv;
                        return SDL_RunApp(0, NULL, SDL_main, NULL);
                    }
                #endif 

            #endif 

            

            #ifdef __cplusplus
            extern "C" {
            #endif

            #if defined(UNICODE) && UNICODE
            int WINAPI wWinMain(HINSTANCE hInst, HINSTANCE hPrev, PWSTR szCmdLine, int sw)
            #else 
            int WINAPI WinMain(HINSTANCE hInst, HINSTANCE hPrev, LPSTR szCmdLine, int sw)
            #endif
            {
                (void)hInst;
                (void)hPrev;
                (void)szCmdLine;
                (void)sw;
                return SDL_RunApp(0, NULL, SDL_main, NULL);
            }

            #ifdef __cplusplus
            } 
            #endif

            

        #else 
            int main(int argc, char *argv[])
            {
                return SDL_RunApp(argc, argv, SDL_main, NULL);
            }

            

        #endif 

    #endif 

    
    #define main    SDL_main

#endif 

#endif 
