



#include <dlfcn.h>
#include "utils.h"

typedef int (*get_device_api_level_fn)(void);

int is_android_6_or_lower(void)
{
    void *symbol;
    int api_level = 0;
    api_level = android_get_device_api_level();
    if (!api_level) {
        LOGE("android_get_device_api_level() failed, trying android_get_device_api_level()..");
        void *libcHandle = dlopen("libc.so", RTLD_LAZY);
        if (libcHandle == NULL) {
            LOGE("Can't check device API level because libc dlopen failed: %s. "
                 "Assuming modern Android.", dlerror());
            return 0; 
        }
        symbol = dlsym(libcHandle, "android_get_device_api_level");
        
        if (!symbol) return 1; 
        api_level = ((get_device_api_level_fn) symbol)();
        
        if (!api_level) return 0; 
        dlclose(libcHandle);
    }
    return api_level < 24 ? 1 : 0;
}