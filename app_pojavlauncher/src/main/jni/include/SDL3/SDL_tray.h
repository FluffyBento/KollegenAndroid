



#ifndef SDL_tray_h_
#define SDL_tray_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_surface.h>
#include <SDL3/SDL_video.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_Tray SDL_Tray;


typedef struct SDL_TrayMenu SDL_TrayMenu;


typedef struct SDL_TrayEntry SDL_TrayEntry;


typedef Uint32 SDL_TrayEntryFlags;

#define SDL_TRAYENTRY_BUTTON      0x00000001u 
#define SDL_TRAYENTRY_CHECKBOX    0x00000002u 
#define SDL_TRAYENTRY_SUBMENU     0x00000004u 
#define SDL_TRAYENTRY_DISABLED    0x80000000u 
#define SDL_TRAYENTRY_CHECKED     0x40000000u 


typedef void (SDLCALL *SDL_TrayCallback)(void *userdata, SDL_TrayEntry *entry);


extern SDL_DECLSPEC SDL_Tray * SDLCALL SDL_CreateTray(SDL_Surface *icon, const char *tooltip);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayIcon(SDL_Tray *tray, SDL_Surface *icon);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayTooltip(SDL_Tray *tray, const char *tooltip);


extern SDL_DECLSPEC SDL_TrayMenu * SDLCALL SDL_CreateTrayMenu(SDL_Tray *tray);


extern SDL_DECLSPEC SDL_TrayMenu * SDLCALL SDL_CreateTraySubmenu(SDL_TrayEntry *entry);


extern SDL_DECLSPEC SDL_TrayMenu * SDLCALL SDL_GetTrayMenu(SDL_Tray *tray);


extern SDL_DECLSPEC SDL_TrayMenu * SDLCALL SDL_GetTraySubmenu(SDL_TrayEntry *entry);


extern SDL_DECLSPEC const SDL_TrayEntry ** SDLCALL SDL_GetTrayEntries(SDL_TrayMenu *menu, int *count);


extern SDL_DECLSPEC void SDLCALL SDL_RemoveTrayEntry(SDL_TrayEntry *entry);


extern SDL_DECLSPEC SDL_TrayEntry * SDLCALL SDL_InsertTrayEntryAt(SDL_TrayMenu *menu, int pos, const char *label, SDL_TrayEntryFlags flags);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayEntryLabel(SDL_TrayEntry *entry, const char *label);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetTrayEntryLabel(SDL_TrayEntry *entry);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayEntryChecked(SDL_TrayEntry *entry, bool checked);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTrayEntryChecked(SDL_TrayEntry *entry);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayEntryEnabled(SDL_TrayEntry *entry, bool enabled);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTrayEntryEnabled(SDL_TrayEntry *entry);


extern SDL_DECLSPEC void SDLCALL SDL_SetTrayEntryCallback(SDL_TrayEntry *entry, SDL_TrayCallback callback, void *userdata);


extern SDL_DECLSPEC void SDLCALL SDL_ClickTrayEntry(SDL_TrayEntry *entry);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyTray(SDL_Tray *tray);


extern SDL_DECLSPEC SDL_TrayMenu * SDLCALL SDL_GetTrayEntryParent(SDL_TrayEntry *entry);


extern SDL_DECLSPEC SDL_TrayEntry * SDLCALL SDL_GetTrayMenuParentEntry(SDL_TrayMenu *menu);


extern SDL_DECLSPEC SDL_Tray * SDLCALL SDL_GetTrayMenuParentTray(SDL_TrayMenu *menu);


extern SDL_DECLSPEC void SDLCALL SDL_UpdateTrays(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
