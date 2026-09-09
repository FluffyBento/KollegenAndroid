



#ifndef SDL_log_h_
#define SDL_log_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef enum SDL_LogCategory
{
    SDL_LOG_CATEGORY_APPLICATION,
    SDL_LOG_CATEGORY_ERROR,
    SDL_LOG_CATEGORY_ASSERT,
    SDL_LOG_CATEGORY_SYSTEM,
    SDL_LOG_CATEGORY_AUDIO,
    SDL_LOG_CATEGORY_VIDEO,
    SDL_LOG_CATEGORY_RENDER,
    SDL_LOG_CATEGORY_INPUT,
    SDL_LOG_CATEGORY_TEST,
    SDL_LOG_CATEGORY_GPU,

    
    SDL_LOG_CATEGORY_RESERVED2,
    SDL_LOG_CATEGORY_RESERVED3,
    SDL_LOG_CATEGORY_RESERVED4,
    SDL_LOG_CATEGORY_RESERVED5,
    SDL_LOG_CATEGORY_RESERVED6,
    SDL_LOG_CATEGORY_RESERVED7,
    SDL_LOG_CATEGORY_RESERVED8,
    SDL_LOG_CATEGORY_RESERVED9,
    SDL_LOG_CATEGORY_RESERVED10,

    
    SDL_LOG_CATEGORY_CUSTOM
} SDL_LogCategory;


typedef enum SDL_LogPriority
{
    SDL_LOG_PRIORITY_INVALID,
    SDL_LOG_PRIORITY_TRACE,
    SDL_LOG_PRIORITY_VERBOSE,
    SDL_LOG_PRIORITY_DEBUG,
    SDL_LOG_PRIORITY_INFO,
    SDL_LOG_PRIORITY_WARN,
    SDL_LOG_PRIORITY_ERROR,
    SDL_LOG_PRIORITY_CRITICAL,
    SDL_LOG_PRIORITY_COUNT
} SDL_LogPriority;



extern SDL_DECLSPEC void SDLCALL SDL_SetLogPriorities(SDL_LogPriority priority);


extern SDL_DECLSPEC void SDLCALL SDL_SetLogPriority(int category, SDL_LogPriority priority);


extern SDL_DECLSPEC SDL_LogPriority SDLCALL SDL_GetLogPriority(int category);


extern SDL_DECLSPEC void SDLCALL SDL_ResetLogPriorities(void);


extern SDL_DECLSPEC bool SDLCALL SDL_SetLogPriorityPrefix(SDL_LogPriority priority, const char *prefix);


extern SDL_DECLSPEC void SDLCALL SDL_Log(SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(1);


extern SDL_DECLSPEC void SDLCALL SDL_LogTrace(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogVerbose(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogDebug(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogInfo(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogWarn(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogError(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogCritical(int category, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(2);


extern SDL_DECLSPEC void SDLCALL SDL_LogMessage(int category,
                                            SDL_LogPriority priority,
                                            SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(3);


extern SDL_DECLSPEC void SDLCALL SDL_LogMessageV(int category,
                                             SDL_LogPriority priority,
                                             SDL_PRINTF_FORMAT_STRING const char *fmt, va_list ap) SDL_PRINTF_VARARG_FUNCV(3);


typedef void (SDLCALL *SDL_LogOutputFunction)(void *userdata, int category, SDL_LogPriority priority, const char *message);


extern SDL_DECLSPEC SDL_LogOutputFunction SDLCALL SDL_GetDefaultLogOutputFunction(void);


extern SDL_DECLSPEC void SDLCALL SDL_GetLogOutputFunction(SDL_LogOutputFunction *callback, void **userdata);


extern SDL_DECLSPEC void SDLCALL SDL_SetLogOutputFunction(SDL_LogOutputFunction callback, void *userdata);



#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
