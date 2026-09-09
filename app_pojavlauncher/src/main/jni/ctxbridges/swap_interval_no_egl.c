



#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <android/native_window.h>

#define TAG __FILE_NAME__
#include <log.h>




typedef struct android_native_base_t
{
    
    int magic;
    
    int version;
    void* reserved[4];
    
    void (*incRef)(struct android_native_base_t* base);
    void (*decRef)(struct android_native_base_t* base);
} android_native_base_t;


#define ANDROID_NATIVE_MAKE_CONSTANT(a,b,c,d) \
    (((unsigned)(a)<<24)|((unsigned)(b)<<16)|((unsigned)(c)<<8)|(unsigned)(d))
#define ANDROID_NATIVE_WINDOW_MAGIC \
    ANDROID_NATIVE_MAKE_CONSTANT('_','w','n','d')

struct ANativeWindowBuffer; 



struct ANativeWindow_real
{
    struct android_native_base_t common;
    
    const uint32_t flags;
    
    const int   minSwapInterval;
    
    const int   maxSwapInterval;
    
    const float xdpi;
    const float ydpi;
    
    intptr_t    oem[4];
    
    int     (*setSwapInterval)(struct ANativeWindow* window,
                               int interval);
    
    int     (*dequeueBuffer_DEPRECATED)(struct ANativeWindow* window,
                                        struct ANativeWindowBuffer** buffer);
    
    int     (*lockBuffer_DEPRECATED)(struct ANativeWindow* window,
                                     struct ANativeWindowBuffer* buffer);
    
    int     (*queueBuffer_DEPRECATED)(struct ANativeWindow* window,
                                      struct ANativeWindowBuffer* buffer);
    
    int     (*query)(const struct ANativeWindow* window,
                     int what, int* value);
    
    int     (*perform)(struct ANativeWindow* window,
                       int operation, ... );
    
    int     (*cancelBuffer_DEPRECATED)(struct ANativeWindow* window,
                                       struct ANativeWindowBuffer* buffer);
    
    int     (*dequeueBuffer)(struct ANativeWindow* window,
                             struct ANativeWindowBuffer** buffer, int* fenceFd);
    
    int     (*queueBuffer)(struct ANativeWindow* window,
                           struct ANativeWindowBuffer* buffer, int fenceFd);
    
    int     (*cancelBuffer)(struct ANativeWindow* window,
                            struct ANativeWindowBuffer* buffer, int fenceFd);
};


void setNativeWindowSwapInterval(struct ANativeWindow* nativeWindow, int swapInterval) {
    if(!getenv("POJAV_VSYNC_IN_ZINK")) {
        return;
    }
    struct ANativeWindow_real* nativeWindowReal = (struct ANativeWindow_real*) nativeWindow;
    if(nativeWindowReal->common.magic != ANDROID_NATIVE_WINDOW_MAGIC) {
        LOGW("ANativeWindow magic does not match. Expected %i, got %i",
                            ANDROID_NATIVE_WINDOW_MAGIC, nativeWindowReal->common.magic);
        return;
    }
    if(nativeWindowReal->common.version != sizeof(struct ANativeWindow_real)) {
        LOGW("ANativeWindow version does not match. Expected %i, got %i",
                            sizeof(struct ANativeWindow_real), nativeWindowReal->common.version);
        return;
    }
    int error;
    if((error = nativeWindowReal->setSwapInterval(nativeWindow, swapInterval)) != 0) {
        LOGW("Failed to set swap interval: %s", strerror(-error));
    }
}