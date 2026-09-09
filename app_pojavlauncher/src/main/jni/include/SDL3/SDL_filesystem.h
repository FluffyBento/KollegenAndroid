



#ifndef SDL_filesystem_h_
#define SDL_filesystem_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>

#include <SDL3/SDL_begin_code.h>


#ifdef __cplusplus
extern "C" {
#endif


extern SDL_DECLSPEC const char * SDLCALL SDL_GetBasePath(void);


extern SDL_DECLSPEC char * SDLCALL SDL_GetPrefPath(const char *org, const char *app);


typedef enum SDL_Folder
{
    SDL_FOLDER_HOME,        
    SDL_FOLDER_DESKTOP,     
    SDL_FOLDER_DOCUMENTS,   
    SDL_FOLDER_DOWNLOADS,   
    SDL_FOLDER_MUSIC,       
    SDL_FOLDER_PICTURES,    
    SDL_FOLDER_PUBLICSHARE, 
    SDL_FOLDER_SAVEDGAMES,  
    SDL_FOLDER_SCREENSHOTS, 
    SDL_FOLDER_TEMPLATES,   
    SDL_FOLDER_VIDEOS,      
    SDL_FOLDER_COUNT        
} SDL_Folder;


extern SDL_DECLSPEC const char * SDLCALL SDL_GetUserFolder(SDL_Folder folder);





typedef enum SDL_PathType
{
    SDL_PATHTYPE_NONE,      
    SDL_PATHTYPE_FILE,      
    SDL_PATHTYPE_DIRECTORY, 
    SDL_PATHTYPE_OTHER      
} SDL_PathType;


typedef struct SDL_PathInfo
{
    SDL_PathType type;      
    Uint64 size;            
    SDL_Time create_time;   
    SDL_Time modify_time;   
    SDL_Time access_time;   
} SDL_PathInfo;


typedef Uint32 SDL_GlobFlags;

#define SDL_GLOB_CASEINSENSITIVE (1u << 0)


extern SDL_DECLSPEC bool SDLCALL SDL_CreateDirectory(const char *path);


typedef enum SDL_EnumerationResult
{
    SDL_ENUM_CONTINUE,   
    SDL_ENUM_SUCCESS,    
    SDL_ENUM_FAILURE     
} SDL_EnumerationResult;


typedef SDL_EnumerationResult (SDLCALL *SDL_EnumerateDirectoryCallback)(void *userdata, const char *dirname, const char *fname);


extern SDL_DECLSPEC bool SDLCALL SDL_EnumerateDirectory(const char *path, SDL_EnumerateDirectoryCallback callback, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_RemovePath(const char *path);


extern SDL_DECLSPEC bool SDLCALL SDL_RenamePath(const char *oldpath, const char *newpath);


extern SDL_DECLSPEC bool SDLCALL SDL_CopyFile(const char *oldpath, const char *newpath);


extern SDL_DECLSPEC bool SDLCALL SDL_GetPathInfo(const char *path, SDL_PathInfo *info);


extern SDL_DECLSPEC char ** SDLCALL SDL_GlobDirectory(const char *path, const char *pattern, SDL_GlobFlags flags, int *count);


extern SDL_DECLSPEC char * SDLCALL SDL_GetCurrentDirectory(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
