



#ifndef SDL_test_font_h_
#define SDL_test_font_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_rect.h>
#include <SDL3/SDL_render.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif



extern int FONT_CHARACTER_SIZE;

#define FONT_LINE_HEIGHT    (FONT_CHARACTER_SIZE + 2)


bool SDLCALL SDLTest_DrawCharacter(SDL_Renderer *renderer, float x, float y, Uint32 c);


bool SDLCALL SDLTest_DrawString(SDL_Renderer *renderer, float x, float y, const char *s);


typedef struct SDLTest_TextWindow
{
    SDL_FRect rect;
    int current;
    int numlines;
    char **lines;
} SDLTest_TextWindow;


SDLTest_TextWindow * SDLCALL SDLTest_TextWindowCreate(float x, float y, float w, float h);


void SDLCALL SDLTest_TextWindowDisplay(SDLTest_TextWindow *textwin, SDL_Renderer *renderer);


void SDLCALL SDLTest_TextWindowAddText(SDLTest_TextWindow *textwin, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


void SDLCALL SDLTest_TextWindowAddTextWithLength(SDLTest_TextWindow *textwin, const char *text, size_t len);


void SDLCALL SDLTest_TextWindowClear(SDLTest_TextWindow *textwin);


void SDLCALL SDLTest_TextWindowDestroy(SDLTest_TextWindow *textwin);


void SDLCALL SDLTest_CleanupTextDrawing(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
