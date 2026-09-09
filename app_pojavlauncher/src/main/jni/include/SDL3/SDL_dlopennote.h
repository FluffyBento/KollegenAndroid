





#ifndef SDL_dlopennote_h
#define SDL_dlopennote_h


#define SDL_ELF_NOTE_DLOPEN_PRIORITY_SUGGESTED   "suggested"


#define SDL_ELF_NOTE_DLOPEN_PRIORITY_RECOMMENDED "recommended"


#define SDL_ELF_NOTE_DLOPEN_PRIORITY_REQUIRED    "required"


#if !defined(SDL_PLATFORM_UNIX) || defined(SDL_PLATFORM_ANDROID)

#ifndef SDL_DISABLE_DLOPEN_NOTES
#define SDL_DISABLE_DLOPEN_NOTES
#endif
#elif defined(__GNUC__) && (__GNUC__ < 3 || (__GNUC__ == 3 && __GNUC_MINOR__ < 1))

#ifndef SDL_DISABLE_DLOPEN_NOTES
#define SDL_DISABLE_DLOPEN_NOTES
#endif
#endif 

#if defined(__ELF__) && !defined(SDL_DISABLE_DLOPEN_NOTES)

#include <SDL3/SDL_stdinc.h>

#define SDL_ELF_NOTE_DLOPEN_VENDOR "FDO"
#define SDL_ELF_NOTE_DLOPEN_TYPE 0x407c0c0aU

#define SDL_ELF_NOTE_INTERNAL2(json, variable_name)                 \
    __attribute__((aligned(4), used, section(".note.dlopen")))      \
    static const struct {                                           \
        struct {                                                    \
            Uint32 n_namesz;                                        \
            Uint32 n_descsz;                                        \
            Uint32 n_type;                                          \
        } nhdr;                                                     \
        char name[4];                                               \
        __attribute__((aligned(4))) char dlopen_json[sizeof(json)]; \
    } variable_name = {                                             \
        {                                                           \
             sizeof(SDL_ELF_NOTE_DLOPEN_VENDOR),                    \
             sizeof(json),                                          \
             SDL_ELF_NOTE_DLOPEN_TYPE                               \
        },                                                          \
        SDL_ELF_NOTE_DLOPEN_VENDOR,                                 \
        json                                                        \
    }

#define SDL_ELF_NOTE_INTERNAL(json, variable_name) \
    SDL_ELF_NOTE_INTERNAL2(json, variable_name)

#define SDL_DLNOTE_JSON_ARRAY1(N1) "[\"" N1 "\"]"
#define SDL_DLNOTE_JSON_ARRAY2(N1,N2) "[\"" N1 "\",\"" N2 "\"]"
#define SDL_DLNOTE_JSON_ARRAY3(N1,N2,N3) "[\"" N1 "\",\"" N2 "\",\"" N3 "\"]"
#define SDL_DLNOTE_JSON_ARRAY4(N1,N2,N3,N4) "[\"" N1 "\",\"" N2 "\",\"" N3 "\",\"" N4 "\"]"
#define SDL_DLNOTE_JSON_ARRAY5(N1,N2,N3,N4,N5) "[\"" N1 "\",\"" N2 "\",\"" N3 "\",\"" N4 "\",\"" N5 "\"]"
#define SDL_DLNOTE_JSON_ARRAY6(N1,N2,N3,N4,N5,N6) "[\"" N1 "\",\"" N2 "\",\"" N3 "\",\"" N4 "\",\"" N5 "\",\"" N6 "\"]"
#define SDL_DLNOTE_JSON_ARRAY7(N1,N2,N3,N4,N5,N6,N7) "[\"" N1 "\",\"" N2 "\",\"" N3 "\",\"" N4 "\",\"" N5 "\",\"" N6 "\",\"" N7 "\"]"
#define SDL_DLNOTE_JSON_ARRAY8(N1,N2,N3,N4,N5,N6,N7,N8) "[\"" N1 "\",\"" N2 "\",\"" N3 "\",\"" N4 "\",\"" N5 "\",\"" N6 "\",\"" N7 "\",\"" N8 "\"]"
#define SDL_DLNOTE_JSON_ARRAY_GET(N1,N2,N3,N4,N5,N6,N7,N8,NAME,...) NAME
#define SDL_DLNOTE_JSON_ARRAY(...) \
    SDL_DLNOTE_JSON_ARRAY_GET(     \
         __VA_ARGS__,           \
         SDL_DLNOTE_JSON_ARRAY8,   \
         SDL_DLNOTE_JSON_ARRAY7,   \
         SDL_DLNOTE_JSON_ARRAY6,   \
         SDL_DLNOTE_JSON_ARRAY5,   \
         SDL_DLNOTE_JSON_ARRAY4,   \
         SDL_DLNOTE_JSON_ARRAY3,   \
         SDL_DLNOTE_JSON_ARRAY2,   \
         SDL_DLNOTE_JSON_ARRAY1    \
    )(__VA_ARGS__)


#define SDL_DLNOTE_JOIN2(A,B) A##B
#define SDL_DLNOTE_JOIN(A,B) SDL_DLNOTE_JOIN2(A,B)
#define SDL_DLNOTE_UNIQUE_NAME SDL_DLNOTE_JOIN(s_SDL_dlopen_note_, __LINE__)


#define SDL_ELF_NOTE_DLOPEN(feature, description, priority, ...) \
    SDL_ELF_NOTE_INTERNAL(                                       \
        "[{\"feature\":\"" feature                               \
        "\",\"description\":\"" description                      \
        "\",\"priority\":\"" priority                            \
        "\",\"soname\":" SDL_DLNOTE_JSON_ARRAY(__VA_ARGS__) "}]",   \
        SDL_DLNOTE_UNIQUE_NAME);

#elif defined(__GNUC__) && __GNUC__ < 3

#define SDL_ELF_NOTE_DLOPEN(args...)

#elif defined(_MSC_VER) && _MSC_VER < 1400



#else

#define SDL_ELF_NOTE_DLOPEN(...)

#endif

#endif 
