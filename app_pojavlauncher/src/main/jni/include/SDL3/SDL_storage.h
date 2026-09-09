



#ifndef SDL_storage_h_
#define SDL_storage_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_filesystem.h>
#include <SDL3/SDL_properties.h>

#include <SDL3/SDL_begin_code.h>


#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_StorageInterface
{
    
    Uint32 version;

    
    bool (SDLCALL *close)(void *userdata);

    
    bool (SDLCALL *ready)(void *userdata);

    
    bool (SDLCALL *enumerate)(void *userdata, const char *path, SDL_EnumerateDirectoryCallback callback, void *callback_userdata);

    
    bool (SDLCALL *info)(void *userdata, const char *path, SDL_PathInfo *info);

    
    bool (SDLCALL *read_file)(void *userdata, const char *path, void *destination, Uint64 length);

    
    bool (SDLCALL *write_file)(void *userdata, const char *path, const void *source, Uint64 length);

    
    bool (SDLCALL *mkdir)(void *userdata, const char *path);

    
    bool (SDLCALL *remove)(void *userdata, const char *path);

    
    bool (SDLCALL *rename)(void *userdata, const char *oldpath, const char *newpath);

    
    bool (SDLCALL *copy)(void *userdata, const char *oldpath, const char *newpath);

    
    Uint64 (SDLCALL *space_remaining)(void *userdata);
} SDL_StorageInterface;


SDL_COMPILE_TIME_ASSERT(SDL_StorageInterface_SIZE,
    (sizeof(void *) == 4 && sizeof(SDL_StorageInterface) == 48) ||
    (sizeof(void *) == 8 && sizeof(SDL_StorageInterface) == 96));


typedef struct SDL_Storage SDL_Storage;


extern SDL_DECLSPEC SDL_Storage * SDLCALL SDL_OpenTitleStorage(const char *override, SDL_PropertiesID props);


extern SDL_DECLSPEC SDL_Storage * SDLCALL SDL_OpenUserStorage(const char *org, const char *app, SDL_PropertiesID props);


extern SDL_DECLSPEC SDL_Storage * SDLCALL SDL_OpenFileStorage(const char *path);


extern SDL_DECLSPEC SDL_Storage * SDLCALL SDL_OpenStorage(const SDL_StorageInterface *iface, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_CloseStorage(SDL_Storage *storage);


extern SDL_DECLSPEC bool SDLCALL SDL_StorageReady(SDL_Storage *storage);


extern SDL_DECLSPEC bool SDLCALL SDL_GetStorageFileSize(SDL_Storage *storage, const char *path, Uint64 *length);


extern SDL_DECLSPEC bool SDLCALL SDL_ReadStorageFile(SDL_Storage *storage, const char *path, void *destination, Uint64 length);


extern SDL_DECLSPEC bool SDLCALL SDL_WriteStorageFile(SDL_Storage *storage, const char *path, const void *source, Uint64 length);


extern SDL_DECLSPEC bool SDLCALL SDL_CreateStorageDirectory(SDL_Storage *storage, const char *path);


extern SDL_DECLSPEC bool SDLCALL SDL_EnumerateStorageDirectory(SDL_Storage *storage, const char *path, SDL_EnumerateDirectoryCallback callback, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_RemoveStoragePath(SDL_Storage *storage, const char *path);


extern SDL_DECLSPEC bool SDLCALL SDL_RenameStoragePath(SDL_Storage *storage, const char *oldpath, const char *newpath);


extern SDL_DECLSPEC bool SDLCALL SDL_CopyStorageFile(SDL_Storage *storage, const char *oldpath, const char *newpath);


extern SDL_DECLSPEC bool SDLCALL SDL_GetStoragePathInfo(SDL_Storage *storage, const char *path, SDL_PathInfo *info);


extern SDL_DECLSPEC Uint64 SDLCALL SDL_GetStorageSpaceRemaining(SDL_Storage *storage);


extern SDL_DECLSPEC char ** SDLCALL SDL_GlobStorageDirectory(SDL_Storage *storage, const char *path, const char *pattern, SDL_GlobFlags flags, int *count);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
