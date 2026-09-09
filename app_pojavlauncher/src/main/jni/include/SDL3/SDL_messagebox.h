



#ifndef SDL_messagebox_h_
#define SDL_messagebox_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_video.h>      

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef Uint32 SDL_MessageBoxFlags;

#define SDL_MESSAGEBOX_ERROR                    0x00000010u 
#define SDL_MESSAGEBOX_WARNING                  0x00000020u 
#define SDL_MESSAGEBOX_INFORMATION              0x00000040u 
#define SDL_MESSAGEBOX_BUTTONS_LEFT_TO_RIGHT    0x00000080u 
#define SDL_MESSAGEBOX_BUTTONS_RIGHT_TO_LEFT    0x00000100u 


typedef Uint32 SDL_MessageBoxButtonFlags;

#define SDL_MESSAGEBOX_BUTTON_RETURNKEY_DEFAULT 0x00000001u 
#define SDL_MESSAGEBOX_BUTTON_ESCAPEKEY_DEFAULT 0x00000002u 


typedef struct SDL_MessageBoxButtonData
{
    SDL_MessageBoxButtonFlags flags;
    int buttonID;       
    const char *text;   
} SDL_MessageBoxButtonData;


typedef struct SDL_MessageBoxColor
{
    Uint8 r, g, b;
} SDL_MessageBoxColor;


typedef enum SDL_MessageBoxColorType
{
    SDL_MESSAGEBOX_COLOR_BACKGROUND,
    SDL_MESSAGEBOX_COLOR_TEXT,
    SDL_MESSAGEBOX_COLOR_BUTTON_BORDER,
    SDL_MESSAGEBOX_COLOR_BUTTON_BACKGROUND,
    SDL_MESSAGEBOX_COLOR_BUTTON_SELECTED,
    SDL_MESSAGEBOX_COLOR_COUNT                    
} SDL_MessageBoxColorType;


typedef struct SDL_MessageBoxColorScheme
{
    SDL_MessageBoxColor colors[SDL_MESSAGEBOX_COLOR_COUNT];
} SDL_MessageBoxColorScheme;


typedef struct SDL_MessageBoxData
{
    SDL_MessageBoxFlags flags;
    SDL_Window *window;                 
    const char *title;                  
    const char *message;                

    int numbuttons;
    const SDL_MessageBoxButtonData *buttons;

    const SDL_MessageBoxColorScheme *colorScheme;   
} SDL_MessageBoxData;


extern SDL_DECLSPEC bool SDLCALL SDL_ShowMessageBox(const SDL_MessageBoxData *messageboxdata, int *buttonid);


extern SDL_DECLSPEC bool SDLCALL SDL_ShowSimpleMessageBox(SDL_MessageBoxFlags flags, const char *title, const char *message, SDL_Window *window);



#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
