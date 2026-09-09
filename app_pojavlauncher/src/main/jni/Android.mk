LOCAL_PATH := $(call my-dir)
HERE_PATH := $(LOCAL_PATH)


LOCAL_PATH := $(HERE_PATH)

$(call import-module,prefab/bytehook)

LOCAL_PATH := $(HERE_PATH)

$(call import-module,prefab/androidnsbypass)

LOCAL_PATH := $(HERE_PATH)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -ldl -llog -landroid
LOCAL_MODULE := pojavexec
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := androidnsbypass
LOCAL_SRC_FILES := \
    bigcoreaffinity.c \
    egl_bridge.c \
    ctxbridges/loader_dlopen.c \
    ctxbridges/gl_bridge.c \
    ctxbridges/osm_bridge.c \
    ctxbridges/egl_loader.c \
    ctxbridges/osmesa_loader.c \
    ctxbridges/swap_interval_no_egl.c \
    environ/environ.c \
    jvm_hooks/emui_iterator_fix_hook.c \
    jvm_hooks/java_exec_hooks.c \
    jvm_hooks/lwjgl_dlopen_hook.c \
    input_bridge_v3.c \
    jre_launcher.c \
    utils.c \
    stdio_is.c

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -DADRENO_POSSIBLE
endif
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := exithook
LOCAL_LDLIBS := -ldl -llog
LOCAL_SHARED_LIBRARIES := bytehook pojavexec
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SRC_FILES := \
    native_hooks/exit_hook.c \
    native_hooks/chmod_hook.c \
    native_hooks/sdl_hook.c \
    native_hooks/dlopen_hook.c
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := linkerhook
LOCAL_LDLIBS := -llog
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := androidnsbypass
LOCAL_SRC_FILES := \
	driver_helper/internal_android_dlopen_hook/turnip/hook.c
LOCAL_LDFLAGS := -z global # Used so symbol resolving prioritizes this over everything else
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := glxshim
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := pojavexec
LOCAL_SRC_FILES := \
	driver_helper/glxshim/glxshim.c
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := pojavexec_awt
LOCAL_SRC_FILES := \
    awt_bridge.c
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := awt_headless
include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(HERE_PATH)/awt_xawt
include $(CLEAR_VARS)
LOCAL_MODULE := awt_xawt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SHARED_LIBRARIES := awt_headless
LOCAL_SRC_FILES := xawt_fake.c
include $(BUILD_SHARED_LIBRARY)

$(info $(shell (rm $(HERE_PATH)/../jniLibs/*/libawt_headless.so)))

