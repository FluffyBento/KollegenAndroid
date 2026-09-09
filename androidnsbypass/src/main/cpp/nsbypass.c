


#include <android/log.h>
#include <asm/unistd.h>
#include <dlfcn.h>
#include <elf.h>
#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <linux/limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/user.h>
#include <unistd.h>

#include <androidnsbypass/nsbypass.h>
#include <androidnsbypass/nsbypass_t.h>

#include "fasthook/nsbypass_dlfcn.h"

#include "elf_soname_patcher.h"
#include "utils.h"












typedef struct {
    private_create_namespace_t create_namespace;
    private_link_namespaces_t link_namespaces;
    private_link_namespaces_all_libs_t link_namespaces_all_libs;
    private_get_exported_namespace_t get_exported_namespace;
} private_namespace_funcs;

typedef struct {
    private_dlopen_function_t dlopen;
    private_dlopen_ext_function_t dlopen_ext;
    private_dlclose_function_t dlclose;
    private_dlsym_function_t dlsym;
} private_dl_funcs;

static private_dl_funcs s_privateDlFuncs = {0};
static private_namespace_funcs s_linkerFuncs = {0};
static struct android_namespace_t* escapeNs;


inline static void *align_ptr_to_pagesize(void *ptr) {
    return (void *)(((uintptr_t)ptr) & ~(getpagesize() - 1));
}
#if (defined __aarch64__)
#include <sys/mman.h>









#define OP_MS 0b11111100000000000000000000000000

#define BL_OP 0b10010100000000000000000000000000
#define BL_IM 0b00000011111111111111111111111111

static void* find_branch_label(void* func_start) {
    long page_size = sysconf(_SC_PAGESIZE);
    
    if (mprotect(align_ptr_to_pagesize(func_start), page_size, PROT_READ | PROT_EXEC)){
        LOGW("Failed to set readable bit on provided func! This might fail..  %p", func_start);
    }
    uint32_t* bl_addr = func_start;
    
    while((*bl_addr & OP_MS) != BL_OP) {
        bl_addr++; 
    }
    
    
    void* t = ((char*)bl_addr) + (*bl_addr & BL_IM) * 4;
    
    
    
    if (mprotect(align_ptr_to_pagesize(t), page_size, PROT_WRITE | PROT_READ | PROT_EXEC) != 0) {
        LOGW("Failed to remove BTI protection from private API page. This might fail..  %p", t);
    }
    return t;
}
#endif

static void *resolve_symbol(void *handle, const char *name, private_dl_funcs *funcs, bool prefer_private_api)
{
    if (!handle) return NULL;
    dlerror(); 
    void *symbol = NULL;
    if (prefer_private_api && funcs->dlsym != NULL) {
        symbol = funcs->dlsym(handle, name, &dlsym);
    }
    if (!symbol) {
        symbol = dlsym(handle, name);
    }
    const char *error = dlerror();
    if (error) {
        LOGW("Failed to resolve %s: %s", name, error);
        return NULL;
    }
    return symbol;
}

static bool resolve_dl_funcs(private_dl_funcs *privateDlFuncs, void *handle, bool prefer_private_api)
{
    if (!handle) return false;
    if (!privateDlFuncs->dlopen) privateDlFuncs->dlopen = resolve_symbol(handle, "__loader_dlopen", privateDlFuncs, prefer_private_api);
    if (!privateDlFuncs->dlopen_ext) privateDlFuncs->dlopen_ext = resolve_symbol(handle, "__loader_android_dlopen_ext", privateDlFuncs, prefer_private_api);
    if (!privateDlFuncs->dlclose) privateDlFuncs->dlclose = resolve_symbol(handle, "__loader_dlclose", privateDlFuncs, prefer_private_api);
    if (!privateDlFuncs->dlsym) privateDlFuncs->dlsym = resolve_symbol(handle, "__loader_dlsym", privateDlFuncs, prefer_private_api);
    return privateDlFuncs->dlopen && privateDlFuncs->dlopen_ext && privateDlFuncs->dlclose && privateDlFuncs->dlsym;
}


static bool resolve_linker_funcs(private_namespace_funcs *funcs, void *handle, private_dl_funcs *privateDlFuncs)
{
    if (!handle) return false;
    if (!funcs->create_namespace) funcs->create_namespace = resolve_symbol(handle, "__loader_android_create_namespace", privateDlFuncs, true);
    if (!funcs->link_namespaces) funcs->link_namespaces = resolve_symbol(handle, "__loader_android_link_namespaces", privateDlFuncs, true);
    if (!funcs->link_namespaces_all_libs) funcs->link_namespaces_all_libs = resolve_symbol(handle, "__loader_android_link_namespaces_all_libs", privateDlFuncs, true);
    if (!funcs->get_exported_namespace) funcs->get_exported_namespace = resolve_symbol(handle, "__loader_android_get_exported_namespace", privateDlFuncs, true);
    return funcs->create_namespace && funcs->link_namespaces && funcs->link_namespaces_all_libs && funcs->get_exported_namespace;
}




private_dl_funcs get_private_dl_functions()
{
    
    
    private_dl_funcs dlFuncs = {0};
    char *error;
    void* linkerHandle;
    
#if (defined __aarch64__)
    LOGI("Obtaining private API dlFuncs via BTI instruction from libdl.so");
    dlFuncs.dlopen = find_branch_label(&dlopen);
    dlFuncs.dlopen_ext = find_branch_label(&android_dlopen_ext);
    dlFuncs.dlclose = find_branch_label(&dlclose);
    dlFuncs.dlsym = find_branch_label(&dlsym);
    if (dlFuncs.dlopen != NULL &&
            dlFuncs.dlopen_ext != NULL &&
            dlFuncs.dlclose != NULL &&
            dlFuncs.dlsym != NULL) {
        return dlFuncs;
    }
    LOGW("Obtaining dlFuncs via branch label instruction search failed, this is not supposed to happen on aarch64. Falling back.");
    LOGI("Obtaining missing private API dlFuncs from libdl.so..");
#else
    LOGI("Obtaining private API dlFuncs using libdl.so..");
#endif

    
    linkerHandle = dlopen("libdl.so", RTLD_LAZY);
    if (resolve_dl_funcs(&dlFuncs, linkerHandle, false)) return dlFuncs;

    
    LOGI("Obtaining private API dlFuncs using libc.so..");
    linkerHandle = dlopen("libc.so", RTLD_LAZY);
    if (resolve_dl_funcs(&dlFuncs, linkerHandle, false)) return dlFuncs;

    
    
    
    
    if (dlFuncs.dlopen != NULL) {
        LOGW("This is weird, somehow you have dlopen but are missing other dlFuncs. Obtaining missing private API funcs from ld-android.so..");
        linkerHandle = dlFuncs.dlopen("ld-android.so", RTLD_LAZY, &dlsym);
        
        if (resolve_dl_funcs(&dlFuncs, linkerHandle, false)) return dlFuncs;
        LOGW("Obtaining missing private API funcs from ld-android.so failed, trying libdl_android.so..");
        linkerHandle = dlFuncs.dlopen("libdl_android.so", RTLD_LAZY, &dlsym);
        
        if (resolve_dl_funcs(&dlFuncs, linkerHandle, false)) return dlFuncs;
    }

    
    if (dlFuncs.dlsym != NULL){
        LOGW("This is very weird, somehow you have dlsym but are missing other dlFuncs. Obtaining missing private API funcs from RTLD_DEFAULT..");
        linkerHandle = RTLD_DEFAULT;
        if (resolve_dl_funcs(&dlFuncs, linkerHandle, true)) return dlFuncs;
    }

    LOGW("Obtaining dlFuncs via dlopen/dlsym failed, Obtaining missing private API funcs by memory scanning, this is unreliable.");
    linkerHandle = nsbypass_dlopen(LINKER, 0); 
    if (!linkerHandle) return dlFuncs;
    if (!dlFuncs.dlopen) dlFuncs.dlopen = nsbypass_dlsym(linkerHandle, "__loader_dlopen");
    if (!dlFuncs.dlopen_ext) dlFuncs.dlopen_ext = nsbypass_dlsym(linkerHandle, "__loader_android_dlopen_ext");
    if (!dlFuncs.dlclose) dlFuncs.dlclose = nsbypass_dlsym(linkerHandle, "__loader_dlclose");
    if (!dlFuncs.dlsym) dlFuncs.dlsym = nsbypass_dlsym(linkerHandle, "__loader_dlsym");

    return dlFuncs;
}


bool test_dlfuncs(
        private_dl_funcs dlFuncs)
{
#ifndef ENABLE_TESTS
    return true;
#else
    bool passed = true;
    LOGI("===TESTING OBTAINED PRIVATE API DLFUNCTIONS===");
    LOGI("If we crash here, now you know why.");

    LOGI("TESTING DLOPEN ON LIBDL.SO");
    void* libdlHandle = dlFuncs.dlopen("libdl.so", RTLD_NOLOAD, &dlopen);
    if (!libdlHandle) {
        LOGE("dlopen failed to obtain libdl.so! FAIL");
        passed = false;
    }

    if (libdlHandle) {
        LOGI("TESTING DLSYM ON LIBDL.SO TO FIND dlopen");
        void *dlopenAddress = dlFuncs.dlsym(libdlHandle, "dlopen", &dlopen);

        if (!dlopenAddress) {
            LOGE("dlsym failed to find dlopen from libdl.so! FAIL");
            passed = false;
        } else {
            LOGI("dlsym successfully found dlopen at %p from libdl.so", dlopenAddress);
        }

        LOGI("TESTING DLCLOSE ON LIBDL");
        int closeResult = dlFuncs.dlclose(libdlHandle);

        if (closeResult != 0) {
            LOGE("dlclose on libc.so failed with result %d! FAIL", closeResult);
            passed = false;
        } else {
            LOGI("dlclose succeeded");
        }
    }

    LOGI("TESTING DLOPEN_EXT ON LD-ANDROID.SO AKA PRIVATE API");
    void *ldAndroidHandle = dlFuncs.dlopen_ext(
                    "ld-android.so",
                    RTLD_LAZY,
                    NULL,
                    &dlopen);

    if (!ldAndroidHandle) {
        LOGE("android_dlopen_ext failed to open ld-android.so aka private API library! FAIL");
        passed = false;
        LOGI("TESTING DLOPEN ON LD-ANDROID.SO");
        ldAndroidHandle = dlFuncs.dlopen(
                "ld-android.so",
                RTLD_LAZY,
                &dlopen);
        if (ldAndroidHandle) {
            LOGW("dlopen opened ld-android.so aka private API library..android_dlopen_ext is likely invalid.");
        } else LOGE("dlopen failed to open ld-android.so. FAIL");
    } else {
        LOGI("android_dlopen_ext successfully: %p", ldAndroidHandle);
    }

    if (ldAndroidHandle) {
        LOGI("TESTING DLSYM ON LD-ANDROID.SO TO FIND __loader_android_dlopen_ext");
        void *dlopen_ext = dlFuncs.dlsym(ldAndroidHandle, "__loader_android_dlopen_ext", &dlsym);

        if (!dlopen_ext) {
            LOGE("dlsym failed to find __loader_android_dlopen_ext from ld-android.so! FAIL");
            passed = false;
        } else {
            LOGI("dlsym successfully found __loader_android_dlopen_ext at %p from ld-android.so", dlopen_ext);
        }

        LOGI("TESTING DLCLOSE ON LD-ANDROID.SO");
        int closeResult = dlFuncs.dlclose(ldAndroidHandle);

        if (closeResult != 0) {
            LOGE("dlclose on ld-android.so from dlopen_ext failed with result %d! FAIL", closeResult);
            passed = false;
        } else {
            LOGI("dlclose succeeded");
        }
    }

    LOGI("TESTING DLOPEN_EXT ON LIBC.SO");
    void* libcHandle = dlFuncs.dlopen_ext(
            "libc.so",
            RTLD_NOLOAD,
            NULL,
            &dlopen);

    if (!libcHandle) {
        LOGW("android_dlopen_ext failed to find libc.so using RTLD_NOLOAD...");
        libcHandle = dlFuncs.dlopen_ext("libc.so", RTLD_LAZY, NULL, &dlopen);
        if (!libcHandle) {
            LOGE("android_dlopen_ext failed to obtain libc.so! FAIL");
            passed = false;
        }
        LOGW("android_dlopen_ext successfully loaded a new libc.so at %p.. wait what? Are you even on android?", libcHandle);
    } else {
        LOGI("android_dlopen_ext successfully found libc.so at %p", libcHandle);
    }

    if (libcHandle) {
        LOGI("TESTING DLSYM");
        void *mallocAddress = dlFuncs.dlsym(libcHandle, "malloc", &dlsym);

        if (!mallocAddress) {
            LOGE("dlsym failed to find malloc from libc.so! FAIL");
            passed = false;
        } else {
            LOGI("dlsym successfully found malloc at %p from libc.so", mallocAddress);
        }

        LOGI("TESTING DLCLOSE");
        int closeResult = dlFuncs.dlclose(libcHandle);

        if (closeResult != 0) {
            LOGE("dlclose on libc.so from dlopen_ext failed with result %d! FAIL", closeResult);
            passed = false;
        } else {
            LOGI("dlclose succeeded");
        }
    }

    LOGI("=== FINISHED TESTING DL FUNCTIONS ===");
    return passed;
#endif
}


private_namespace_funcs get_private_namespace_functions(
        private_dl_funcs privateDlFuncs)
{
    private_namespace_funcs linkerFuncs = {0};
    void* linkerHandle;
    
    
    
    linkerHandle = privateDlFuncs.dlopen("ld-android.so", RTLD_LAZY, &dlsym);
    if (linkerHandle) { 
        LOGI("Obtaining linker namespace funcs from ld-android.so...");
        
        
        
        if (resolve_linker_funcs(&linkerFuncs, linkerHandle, &privateDlFuncs)) return linkerFuncs;
    }

    

    LOGW("Unable to load all namespace functions! dlFunction loading probably failed? Obtaining missing functions using memory scanning.");
    linkerHandle = nsbypass_dlopen(LINKER, 0);
    if (!linkerFuncs.create_namespace) linkerFuncs.create_namespace = nsbypass_dlsym(linkerHandle, "__loader_android_create_namespace");
    if (!linkerFuncs.link_namespaces) linkerFuncs.link_namespaces = nsbypass_dlsym(linkerHandle, "__loader_android_link_namespaces");
    if (!linkerFuncs.link_namespaces_all_libs) linkerFuncs.link_namespaces_all_libs = nsbypass_dlsym(linkerHandle, "__loader_android_link_namespaces_all_libs");
    if (!linkerFuncs.get_exported_namespace) linkerFuncs.get_exported_namespace = nsbypass_dlsym(linkerHandle, "__loader_android_get_exported_namespace");

    return linkerFuncs;
}


bool test_namespace_funcs(private_namespace_funcs nsFuncs)
{
#ifndef ENABLE_TESTS
    return true;
#else
    bool passed = true;
    LOGI("===TESTING OBTAINED PRIVATE API NAMESPACE===");
    LOGI("If we crash here, now you know why.");

    LOGI("Fetching \"default\" exported namespace");
    if (nsFuncs.get_exported_namespace("default")){
        LOGI("android_get_exported_namespace successfully found default namespace handle");
    } else {
        LOGE("android_get_exported_namespace failed to find default namespace handle");
        passed = false;
    }

    LOGI("Attempting to create escape namespace");
    escapeNs = nsFuncs.create_namespace(
            "g_default_namespace_copy",
            NULL,
            SYSTEM_LIBS_PATH,
            ANDROID_NAMESPACE_TYPE_SHARED,
            SYSTEM_LIBS_PATH,
            NULL,
            &dlopen); 
    if (escapeNs) {
        LOGI("android_create_namespace successfully made escapeNs");
    } else {
        LOGE("android_create_namespace failed to create namespace escapeNs, testing cannot continue. FAIL");
        return false;
    }
    
    
    
    struct android_namespace_t *testNs = nsFuncs.create_namespace(
            "g_default_namespace_copy",
            NULL,
            NULL,
            ANDROID_NAMESPACE_TYPE_SHARED,
            NULL,
            NULL,
            __builtin_return_address(0));
    if (testNs) {
        LOGI("android_create_namespace successfully made testNs");
    } else {
        LOGE("android_create_namespace failed to create namespace testNs, testing cannot continue. FAIL");
        return false;
    }

    if (nsFuncs.link_namespaces_all_libs(testNs, escapeNs)){
        LOGI("android_link_namespaces_all_libs successfully linked testNs to escapeNs, thereby escaping our testNs!");
        if (nsFuncs.link_namespaces(testNs, NULL, "ld-android.so")){
            LOGI("android_link_namespaces successfully loaded ld-android.so into testNs, thereby loading a private API lib!");
        } else {
            LOGE("android_link_namespaces failed to load ld-android.so into testNs, escape was a lie. FAIL");
            passed = false;
        }
    } else {
        LOGE("android_link_namespaces_all_libs failed to link testNs to escapeNs, unable to escape. FAIL");
        if (nsFuncs.link_namespaces(testNs, NULL, "libc.so")){
            LOGI("android_link_namespaces successfully loaded libc.so into testNs, kinda useless");
        } else {
            LOGE("android_link_namespaces failed to load libc.so into testNs. FAIL");
            passed = false;
        }
    }
    return passed;
#endif
}


__attribute__((constructor)) static void resolve_global_symbols()
{
    if (is_android_6_or_lower()){
        LOGW("This library is not supposed to be used on sdk23 and lower. All APIs will remain "
             "non-functional. nsbypass_dlfcn will redirect to the real public API.");
        return;
    }
    
    s_privateDlFuncs = get_private_dl_functions();
    test_dlfuncs(s_privateDlFuncs);
    s_linkerFuncs = get_private_namespace_functions(s_privateDlFuncs);
    test_namespace_funcs(s_linkerFuncs);

    if (!s_linkerFuncs.create_namespace ||
            !s_linkerFuncs.link_namespaces ||
            !s_linkerFuncs.link_namespaces_all_libs ||
            !s_linkerFuncs.get_exported_namespace) {
        LOGE("Failed to resolve Android linker namespace functions! Cannot run nsbypass.");
        exit(121);
    }
    
    if (!escapeNs){
        escapeNs = s_linkerFuncs.create_namespace(
                "g_default_namespace_copy",
                NULL,
                SYSTEM_LIBS_PATH,
                ANDROID_NAMESPACE_TYPE_SHARED,
                SYSTEM_LIBS_PATH,
                NULL,
                &dlopen);
        if (!escapeNs) {
            LOGD("Failed to create escapeNs!");
            exit(122); 
        }
    }
}

struct android_namespace_t* get_escape_namespace() {
    return escapeNs;
}


void* linker_ns_dlopen(
        const char* name,
        int flag,
        struct android_namespace_t* ns)
{
    if (is_android_6_or_lower()) return NULL;
    if (!ns) return NULL;
    android_dlextinfo dlextinfo = {0};
    dlextinfo.flags = ANDROID_DLEXT_USE_NAMESPACE;
    dlextinfo.library_namespace = ns;
    return android_dlopen_ext(name, flag, &dlextinfo);
}









static uint16_t patch_id;
void* linker_ns_dlopen_unique(
        const char* libPath,
        const char* patchedLibDir,
        int flags,
        struct android_namespace_t* ns)
{
    if (is_android_6_or_lower()) return NULL;
    if (!ns || !libPath || !patchedLibDir) return NULL;
    char pathbuf[PATH_MAX];
    int patch_fd;

    
    
    char* libName = strrchr(libPath, '/'); libName++;
    snprintf(pathbuf, PATH_MAX ,"%s/%d%s_patched.so", patchedLibDir, patch_id, libName);
    patch_fd = open(pathbuf, O_CREAT | O_RDWR, S_IRUSR | S_IWUSR);
    if(patch_fd == -1) return NULL;

    if(!patch_elf_soname_path(libPath, patch_fd, patch_id)) {
        return NULL;
    }

    patch_id++;
    android_dlextinfo extinfo = {0};
    extinfo.flags = ANDROID_DLEXT_USE_NAMESPACE | ANDROID_DLEXT_USE_LIBRARY_FD;
    extinfo.library_fd = patch_fd;
    extinfo.library_namespace = ns;
    snprintf(pathbuf, PATH_MAX, "/proc/self/fd/%d", patch_fd);
    return android_dlopen_ext(pathbuf, flags, &extinfo);
}



struct android_namespace_t* private_create_namespace(
        const char* name,
        const char* ld_library_path,
        const char* default_library_path,
        uint64_t type,
        const char* permitted_when_isolated_path,
        struct android_namespace_t* parent_namespace,
        const void* caller_addr)
{
    if (is_android_6_or_lower()) return NULL;
    return s_linkerFuncs.create_namespace(
            name,
            ld_library_path,
            default_library_path,
            type,
            permitted_when_isolated_path,
            parent_namespace,
            caller_addr);
}

bool private_link_namespaces(
        struct android_namespace_t* from,
        struct android_namespace_t* to,
        const char* shared_libs_sonames)
{
    if (is_android_6_or_lower()) return false;
    return s_linkerFuncs.link_namespaces(
            from,
            to,
            shared_libs_sonames);
}

bool private_link_namespaces_all_libs(
        struct android_namespace_t* from,
        struct android_namespace_t* to)
{
    if (is_android_6_or_lower()) return false;
    return s_linkerFuncs.link_namespaces_all_libs(
            from,
            to);
}

struct android_namespace_t* private_get_exported_namespace(
        const char* name)
{
    if (is_android_6_or_lower()) return NULL;
    return s_linkerFuncs.get_exported_namespace(
            name);
}

int private_dlclose(void* handle)
{
    if (is_android_6_or_lower()) return -1;
    return s_privateDlFuncs.dlclose(handle);
}

void* private_dlopen(
        const char* filename,
        int flags,
        const void* caller_addr)
{
    if (is_android_6_or_lower()) return NULL;
    return s_privateDlFuncs.dlopen(
            filename,
            flags,
            caller_addr);
}

void* private_dlopen_ext(
        const char* filename,
        int flags,
        const android_dlextinfo* extinfo,
        const void* caller_addr)
{
    if (is_android_6_or_lower()) return NULL;
    return s_privateDlFuncs.dlopen_ext(
            filename,
            flags,
            extinfo,
            caller_addr);
}

void* private_dlsym(
        void* handle,
        const char* symbol,
        const void* caller_addr)
{
    if (is_android_6_or_lower()) return NULL;
    return s_privateDlFuncs.dlsym(
            handle,
            symbol,
            caller_addr);
}



