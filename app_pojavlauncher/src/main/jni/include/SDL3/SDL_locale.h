



#ifndef SDL_locale_h
#define SDL_locale_h

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus

extern "C" {

#endif


typedef struct SDL_Locale
{
    const char *language;  
    const char *country;  
} SDL_Locale;


extern SDL_DECLSPEC SDL_Locale ** SDLCALL SDL_GetPreferredLocales(int *count);


#ifdef __cplusplus

}

#endif
#include <SDL3/SDL_close_code.h>

#endif 
