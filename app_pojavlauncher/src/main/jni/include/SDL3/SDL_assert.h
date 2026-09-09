



#ifndef SDL_assert_h_
#define SDL_assert_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_ASSERT_LEVEL SomeNumberBasedOnVariousFactors

#elif !defined(SDL_ASSERT_LEVEL)
#ifdef SDL_DEFAULT_ASSERT_LEVEL
#define SDL_ASSERT_LEVEL SDL_DEFAULT_ASSERT_LEVEL
#elif defined(_DEBUG) || defined(DEBUG) || \
      (defined(__GNUC__) && !defined(__OPTIMIZE__))
#define SDL_ASSERT_LEVEL 2
#else
#define SDL_ASSERT_LEVEL 1
#endif
#endif

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_TriggerBreakpoint() TriggerABreakpointInAPlatformSpecificManner

#elif defined(_MSC_VER) && _MSC_VER >= 1310
    
    extern void __cdecl __debugbreak(void);
    #define SDL_TriggerBreakpoint() __debugbreak()
#elif defined(__MINGW32__)
    #include <intrin.h>
    #define SDL_TriggerBreakpoint() __debugbreak()
#elif defined(_MSC_VER) && defined(_M_IX86)
    #define SDL_TriggerBreakpoint() { _asm { int 0x03 }  }
#elif SDL_HAS_BUILTIN(__builtin_debugtrap)
    #define SDL_TriggerBreakpoint() __builtin_debugtrap()
#elif SDL_HAS_BUILTIN(__builtin_trap)
    #define SDL_TriggerBreakpoint() __builtin_trap()
#elif (defined(__GNUC__) || defined(__clang__) || defined(__TINYC__) || defined(__slimcc__)) && (defined(__i386__) || defined(__x86_64__))
    #define SDL_TriggerBreakpoint() __asm__ __volatile__ ( "int $3\n\t" )
#elif (defined(__GNUC__) || defined(__clang__)) && defined(__riscv)
    #define SDL_TriggerBreakpoint() __asm__ __volatile__ ( "ebreak\n\t" )
#elif ( defined(SDL_PLATFORM_APPLE) && (defined(__arm64__) || defined(__aarch64__)) )  
    #define SDL_TriggerBreakpoint() __asm__ __volatile__ ( "brk #22\n\t" )
#elif defined(SDL_PLATFORM_APPLE) && defined(__arm__)
    #define SDL_TriggerBreakpoint() __asm__ __volatile__ ( "bkpt #22\n\t" )
#elif defined(_WIN32) && ((defined(__GNUC__) || defined(__clang__)) && (defined(__arm64__) || defined(__aarch64__)) )
    #define SDL_TriggerBreakpoint() __asm__ __volatile__ ( "brk #0xF000\n\t" )
#elif defined(__GNUC__) || defined(__clang__)
    #define SDL_TriggerBreakpoint() __builtin_trap()  
#elif defined(__386__) && defined(__WATCOMC__)
    #define SDL_TriggerBreakpoint() { _asm { int 0x03 } }
#elif defined(HAVE_SIGNAL_H) && !defined(__WATCOMC__)
    #include <signal.h>
    #define SDL_TriggerBreakpoint() raise(SIGTRAP)
#else
    
#endif

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_FUNCTION __FUNCTION__

#elif !defined(SDL_FUNCTION)
#if defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 199901L) 
#   define SDL_FUNCTION __func__
#elif ((defined(__GNUC__) && (__GNUC__ >= 2)) || defined(_MSC_VER) || defined (__WATCOMC__))
#   define SDL_FUNCTION __FUNCTION__
#else
#   define SDL_FUNCTION "???"
#endif
#endif

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_FILE    __FILE_NAME__

#elif !defined(SDL_FILE)
#ifdef __FILE_NAME__
#define SDL_FILE    __FILE_NAME__
#else
#define SDL_FILE    __FILE__
#endif
#endif

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_ASSERT_FILE SDL_FILE

#elif !defined(SDL_ASSERT_FILE)
#define SDL_ASSERT_FILE SDL_FILE
#endif



#define SDL_LINE    __LINE__



#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_NULL_WHILE_LOOP_CONDITION (0)

#elif defined(_MSC_VER) && !defined(__clang__)  

#define SDL_NULL_WHILE_LOOP_CONDITION (0,0)
#else
#define SDL_NULL_WHILE_LOOP_CONDITION (0)
#endif


#define SDL_disabled_assert(condition) \
    do { (void) sizeof ((condition)); } while (SDL_NULL_WHILE_LOOP_CONDITION)


typedef enum SDL_AssertState
{
    SDL_ASSERTION_RETRY,  
    SDL_ASSERTION_BREAK,  
    SDL_ASSERTION_ABORT,  
    SDL_ASSERTION_IGNORE,  
    SDL_ASSERTION_ALWAYS_IGNORE  
} SDL_AssertState;


typedef struct SDL_AssertData
{
    bool always_ignore;  
    unsigned int trigger_count; 
    const char *condition;  
    const char *filename;  
    int linenum;  
    const char *function;  
    const struct SDL_AssertData *next;  
} SDL_AssertData;


extern SDL_DECLSPEC SDL_AssertState SDLCALL SDL_ReportAssertion(SDL_AssertData *data,
                                                            const char *func,
                                                            const char *file, int line) SDL_ANALYZER_NORETURN;


#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_AssertBreakpoint() SDL_TriggerBreakpoint()

#elif !defined(SDL_AssertBreakpoint)
#  if defined(ANDROID) && defined(assert)
     
#    define SDL_AssertBreakpoint()
#  else
#    define SDL_AssertBreakpoint() SDL_TriggerBreakpoint()
#  endif
#endif 


#define SDL_enabled_assert(condition) \
    do { \
        while ( !(condition) ) { \
            static struct SDL_AssertData sdl_assert_data = { false, 0, #condition, NULL, 0, NULL, NULL }; \
            const SDL_AssertState sdl_assert_state = SDL_ReportAssertion(&sdl_assert_data, SDL_FUNCTION, SDL_ASSERT_FILE, SDL_LINE); \
            if (sdl_assert_state == SDL_ASSERTION_RETRY) { \
                continue;  \
            } else if (sdl_assert_state == SDL_ASSERTION_BREAK) { \
                SDL_AssertBreakpoint(); \
            } \
            break;  \
        } \
    } while (SDL_NULL_WHILE_LOOP_CONDITION)

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_assert(condition) if (assertion_enabled && (condition)) { trigger_assertion; }


#define SDL_assert_release(condition) SDL_disabled_assert(condition)


#define SDL_assert_paranoid(condition) SDL_disabled_assert(condition)


#elif SDL_ASSERT_LEVEL == 0   
#   define SDL_assert(condition) SDL_disabled_assert(condition)
#   define SDL_assert_release(condition) SDL_disabled_assert(condition)
#   define SDL_assert_paranoid(condition) SDL_disabled_assert(condition)
#elif SDL_ASSERT_LEVEL == 1  
#   define SDL_assert(condition) SDL_disabled_assert(condition)
#   define SDL_assert_release(condition) SDL_enabled_assert(condition)
#   define SDL_assert_paranoid(condition) SDL_disabled_assert(condition)
#elif SDL_ASSERT_LEVEL == 2  
#   define SDL_assert(condition) SDL_enabled_assert(condition)
#   define SDL_assert_release(condition) SDL_enabled_assert(condition)
#   define SDL_assert_paranoid(condition) SDL_disabled_assert(condition)
#elif SDL_ASSERT_LEVEL == 3  
#   define SDL_assert(condition) SDL_enabled_assert(condition)
#   define SDL_assert_release(condition) SDL_enabled_assert(condition)
#   define SDL_assert_paranoid(condition) SDL_enabled_assert(condition)
#else
#   error Unknown assertion level.
#endif


#define SDL_assert_always(condition) SDL_enabled_assert(condition)



typedef SDL_AssertState (SDLCALL *SDL_AssertionHandler)(
                                 const SDL_AssertData *data, void *userdata);


extern SDL_DECLSPEC void SDLCALL SDL_SetAssertionHandler(
                                            SDL_AssertionHandler handler,
                                            void *userdata);


extern SDL_DECLSPEC SDL_AssertionHandler SDLCALL SDL_GetDefaultAssertionHandler(void);


extern SDL_DECLSPEC SDL_AssertionHandler SDLCALL SDL_GetAssertionHandler(void **puserdata);


extern SDL_DECLSPEC const SDL_AssertData * SDLCALL SDL_GetAssertionReport(void);


extern SDL_DECLSPEC void SDLCALL SDL_ResetAssertionReport(void);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
