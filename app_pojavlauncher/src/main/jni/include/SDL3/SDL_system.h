



#ifndef SDL_system_h_
#define SDL_system_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_keyboard.h>
#include <SDL3/SDL_video.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif



#if defined(SDL_PLATFORM_WINDOWS)

typedef struct tagMSG MSG;


typedef bool (SDLCALL *SDL_WindowsMessageHook)(void *userdata, MSG *msg);


extern SDL_DECLSPEC void SDLCALL SDL_SetWindowsMessageHook(SDL_WindowsMessageHook callback, void *userdata);

#endif 

#if defined(SDL_PLATFORM_WIN32) || defined(SDL_PLATFORM_WINGDK)


extern SDL_DECLSPEC int SDLCALL SDL_GetDirect3D9AdapterIndex(SDL_DisplayID displayID);


extern SDL_DECLSPEC bool SDLCALL SDL_GetDXGIOutputInfo(SDL_DisplayID displayID, int *adapterIndex, int *outputIndex);

#endif 





typedef union _XEvent XEvent;


typedef bool (SDLCALL *SDL_X11EventHook)(void *userdata, XEvent *xevent);


extern SDL_DECLSPEC void SDLCALL SDL_SetX11EventHook(SDL_X11EventHook callback, void *userdata);


#ifdef SDL_PLATFORM_LINUX


extern SDL_DECLSPEC bool SDLCALL SDL_SetLinuxThreadPriority(Sint64 threadID, int priority);


extern SDL_DECLSPEC bool SDLCALL SDL_SetLinuxThreadPriorityAndPolicy(Sint64 threadID, int sdlPriority, int schedPolicy);

#endif 


#ifdef SDL_PLATFORM_IOS


typedef void (SDLCALL *SDL_iOSAnimationCallback)(void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_SetiOSAnimationCallback(SDL_Window *window, int interval, SDL_iOSAnimationCallback callback, void *callbackParam);


extern SDL_DECLSPEC void SDLCALL SDL_SetiOSEventPump(bool enabled);

#endif 



#ifdef SDL_PLATFORM_ANDROID


extern SDL_DECLSPEC void * SDLCALL SDL_GetAndroidJNIEnv(void);


extern SDL_DECLSPEC void * SDLCALL SDL_GetAndroidActivity(void);


extern SDL_DECLSPEC int SDLCALL SDL_GetAndroidSDKVersion(void);


extern SDL_DECLSPEC bool SDLCALL SDL_IsChromebook(void);


extern SDL_DECLSPEC bool SDLCALL SDL_IsDeXMode(void);


extern SDL_DECLSPEC void SDLCALL SDL_SendAndroidBackButton(void);


#define SDL_ANDROID_EXTERNAL_STORAGE_READ   0x01


#define SDL_ANDROID_EXTERNAL_STORAGE_WRITE  0x02


extern SDL_DECLSPEC const char * SDLCALL SDL_GetAndroidInternalStoragePath(void);


extern SDL_DECLSPEC Uint32 SDLCALL SDL_GetAndroidExternalStorageState(void);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetAndroidExternalStoragePath(void);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetAndroidCachePath(void);


typedef void (SDLCALL *SDL_RequestAndroidPermissionCallback)(void *userdata, const char *permission, bool granted);


extern SDL_DECLSPEC bool SDLCALL SDL_RequestAndroidPermission(const char *permission, SDL_RequestAndroidPermissionCallback cb, void *userdata);


extern SDL_DECLSPEC bool SDLCALL SDL_ShowAndroidToast(const char *message, int duration, int gravity, int xoffset, int yoffset);


extern SDL_DECLSPEC bool SDLCALL SDL_SendAndroidMessage(Uint32 command, int param);

#endif 


extern SDL_DECLSPEC bool SDLCALL SDL_IsTablet(void);


extern SDL_DECLSPEC bool SDLCALL SDL_IsTV(void);


typedef enum SDL_Sandbox
{
    SDL_SANDBOX_NONE = 0,
    SDL_SANDBOX_UNKNOWN_CONTAINER,
    SDL_SANDBOX_FLATPAK,
    SDL_SANDBOX_SNAP,
    SDL_SANDBOX_MACOS
} SDL_Sandbox;


extern SDL_DECLSPEC SDL_Sandbox SDLCALL SDL_GetSandbox(void);





extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationWillTerminate(void);


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationDidReceiveMemoryWarning(void);


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationWillEnterBackground(void);


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationDidEnterBackground(void);


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationWillEnterForeground(void);


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationDidEnterForeground(void);

#ifdef SDL_PLATFORM_IOS


extern SDL_DECLSPEC void SDLCALL SDL_OnApplicationDidChangeStatusBarOrientation(void);
#endif


#ifdef SDL_PLATFORM_GDK
typedef struct XTaskQueueObject *XTaskQueueHandle;
typedef struct XUser *XUserHandle;


extern SDL_DECLSPEC bool SDLCALL SDL_GetGDKTaskQueue(XTaskQueueHandle *outTaskQueue);


extern SDL_DECLSPEC bool SDLCALL SDL_GetGDKDefaultUser(XUserHandle *outUserHandle);

#endif


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
