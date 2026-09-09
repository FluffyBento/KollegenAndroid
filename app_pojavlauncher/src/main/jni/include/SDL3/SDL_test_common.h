





#ifndef SDL_test_common_h_
#define SDL_test_common_h_

#include <SDL3/SDL.h>

#ifdef SDL_PLATFORM_PSP
#define DEFAULT_WINDOW_WIDTH  480
#define DEFAULT_WINDOW_HEIGHT 272
#elif defined(SDL_PLATFORM_VITA)
#define DEFAULT_WINDOW_WIDTH  960
#define DEFAULT_WINDOW_HEIGHT 544
#else
#define DEFAULT_WINDOW_WIDTH  640
#define DEFAULT_WINDOW_HEIGHT 480
#endif

typedef Uint32 SDLTest_VerboseFlags;
#define VERBOSE_VIDEO   0x00000001
#define VERBOSE_MODES   0x00000002
#define VERBOSE_RENDER  0x00000004
#define VERBOSE_EVENT   0x00000008
#define VERBOSE_AUDIO   0x00000010
#define VERBOSE_MOTION  0x00000020


typedef int (SDLCALL *SDLTest_ParseArgumentsFp)(void *data, char **argv, int index);


typedef void (SDLCALL *SDLTest_FinalizeArgumentParserFp)(void *arg);

typedef struct SDLTest_ArgumentParser
{
    
    SDLTest_ParseArgumentsFp parse_arguments;
    
    SDLTest_FinalizeArgumentParserFp finalize;
    
    const char **usage;
    
    void *data;
    
    struct SDLTest_ArgumentParser *next;
} SDLTest_ArgumentParser;

typedef struct
{
    
    char **argv;
    SDL_InitFlags flags;
    SDLTest_VerboseFlags verbose;

    
    const char *videodriver;
    int display_index;
    SDL_DisplayID displayID;
    const char *window_title;
    const char *window_icon;
    SDL_WindowFlags window_flags;
    bool flash_on_focus_loss;
    int window_x;
    int window_y;
    int window_w;
    int window_h;
    int window_minW;
    int window_minH;
    int window_maxW;
    int window_maxH;
    float window_min_aspect;
    float window_max_aspect;
    int logical_w;
    int logical_h;
    bool auto_scale_content;
    SDL_RendererLogicalPresentation logical_presentation;
    float scale;
    int depth;
    float refresh_rate;
    bool fill_usable_bounds;
    bool fullscreen_exclusive;
    SDL_DisplayMode fullscreen_mode;
    int num_windows;
    SDL_Window **windows;
    const char *gpudriver;

    
    const char *renderdriver;
    int render_vsync;
    bool skip_renderer;
    SDL_Renderer **renderers;
    SDL_Texture **targets;

    
    const char *audiodriver;
    SDL_AudioFormat audio_format;
    int audio_channels;
    int audio_freq;
    SDL_AudioDeviceID audio_id;

    
    int gl_red_size;
    int gl_green_size;
    int gl_blue_size;
    int gl_alpha_size;
    int gl_buffer_size;
    int gl_depth_size;
    int gl_stencil_size;
    int gl_double_buffer;
    int gl_accum_red_size;
    int gl_accum_green_size;
    int gl_accum_blue_size;
    int gl_accum_alpha_size;
    int gl_stereo;
    int gl_release_behavior;
    int gl_multisamplebuffers;
    int gl_multisamplesamples;
    int gl_retained_backing;
    int gl_accelerated;
    int gl_major_version;
    int gl_minor_version;
    int gl_debug;
    int gl_profile_mask;

    
    SDL_Rect confine;
    bool hide_cursor;

    
    int quit_after_ms_interval;
    SDL_TimerID quit_after_ms_timer;

    
    SDLTest_ArgumentParser common_argparser;
    SDLTest_ArgumentParser video_argparser;
    SDLTest_ArgumentParser audio_argparser;

    SDLTest_ArgumentParser *argparser;
} SDLTest_CommonState;

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif




SDLTest_CommonState * SDLCALL SDLTest_CommonCreateState(char **argv, SDL_InitFlags flags);


void SDLCALL SDLTest_CommonDestroyState(SDLTest_CommonState *state);


int SDLCALL SDLTest_CommonArg(SDLTest_CommonState *state, int index);



void SDLCALL SDLTest_CommonLogUsage(SDLTest_CommonState *state, const char *argv0, const char **options);


bool SDLCALL SDLTest_CommonInit(SDLTest_CommonState *state);


bool SDLCALL SDLTest_CommonDefaultArgs(SDLTest_CommonState *state, int argc, char **argv);


void SDLCALL SDLTest_PrintEvent(const SDL_Event *event);


void SDLCALL SDLTest_CommonEvent(SDLTest_CommonState *state, SDL_Event *event, int *done);


SDL_AppResult SDLCALL SDLTest_CommonEventMainCallbacks(SDLTest_CommonState *state, const SDL_Event *event);


void SDLCALL SDLTest_CommonQuit(SDLTest_CommonState *state);


void SDLCALL SDLTest_CommonDrawWindowInfo(SDL_Renderer *renderer, SDL_Window *window, float *usedHeight);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
