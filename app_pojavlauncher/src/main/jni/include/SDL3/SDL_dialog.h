



#ifndef SDL_dialog_h_
#define SDL_dialog_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_properties.h>
#include <SDL3/SDL_video.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_DialogFileFilter
{
    const char *name;
    const char *pattern;
} SDL_DialogFileFilter;


typedef void (SDLCALL *SDL_DialogFileCallback)(void *userdata, const char * const *filelist, int filter);


extern SDL_DECLSPEC void SDLCALL SDL_ShowOpenFileDialog(SDL_DialogFileCallback callback, void *userdata, SDL_Window *window, const SDL_DialogFileFilter *filters, int nfilters, const char *default_location, bool allow_many);


extern SDL_DECLSPEC void SDLCALL SDL_ShowSaveFileDialog(SDL_DialogFileCallback callback, void *userdata, SDL_Window *window, const SDL_DialogFileFilter *filters, int nfilters, const char *default_location);


extern SDL_DECLSPEC void SDLCALL SDL_ShowOpenFolderDialog(SDL_DialogFileCallback callback, void *userdata, SDL_Window *window, const char *default_location, bool allow_many);


typedef enum SDL_FileDialogType
{
    SDL_FILEDIALOG_OPENFILE,
    SDL_FILEDIALOG_SAVEFILE,
    SDL_FILEDIALOG_OPENFOLDER
} SDL_FileDialogType;


extern SDL_DECLSPEC void SDLCALL SDL_ShowFileDialogWithProperties(SDL_FileDialogType type, SDL_DialogFileCallback callback, void *userdata, SDL_PropertiesID props);

#define SDL_PROP_FILE_DIALOG_FILTERS_POINTER     "SDL.filedialog.filters"
#define SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER     "SDL.filedialog.nfilters"
#define SDL_PROP_FILE_DIALOG_WINDOW_POINTER      "SDL.filedialog.window"
#define SDL_PROP_FILE_DIALOG_LOCATION_STRING     "SDL.filedialog.location"
#define SDL_PROP_FILE_DIALOG_MANY_BOOLEAN        "SDL.filedialog.many"
#define SDL_PROP_FILE_DIALOG_TITLE_STRING        "SDL.filedialog.title"
#define SDL_PROP_FILE_DIALOG_ACCEPT_STRING       "SDL.filedialog.accept"
#define SDL_PROP_FILE_DIALOG_CANCEL_STRING       "SDL.filedialog.cancel"


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
