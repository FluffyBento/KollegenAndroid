



#ifndef SDL_process_h_
#define SDL_process_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_iostream.h>
#include <SDL3/SDL_properties.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_Process SDL_Process;


extern SDL_DECLSPEC SDL_Process * SDLCALL SDL_CreateProcess(const char * const *args, bool pipe_stdio);


typedef enum SDL_ProcessIO
{
    SDL_PROCESS_STDIO_INHERITED,    
    SDL_PROCESS_STDIO_NULL,         
    SDL_PROCESS_STDIO_APP,          
    SDL_PROCESS_STDIO_REDIRECT      
} SDL_ProcessIO;


extern SDL_DECLSPEC SDL_Process * SDLCALL SDL_CreateProcessWithProperties(SDL_PropertiesID props);

#define SDL_PROP_PROCESS_CREATE_ARGS_POINTER                "SDL.process.create.args"
#define SDL_PROP_PROCESS_CREATE_ENVIRONMENT_POINTER         "SDL.process.create.environment"
#define SDL_PROP_PROCESS_CREATE_WORKING_DIRECTORY_STRING    "SDL.process.create.working_directory"
#define SDL_PROP_PROCESS_CREATE_STDIN_NUMBER                "SDL.process.create.stdin_option"
#define SDL_PROP_PROCESS_CREATE_STDIN_POINTER               "SDL.process.create.stdin_source"
#define SDL_PROP_PROCESS_CREATE_STDOUT_NUMBER               "SDL.process.create.stdout_option"
#define SDL_PROP_PROCESS_CREATE_STDOUT_POINTER              "SDL.process.create.stdout_source"
#define SDL_PROP_PROCESS_CREATE_STDERR_NUMBER               "SDL.process.create.stderr_option"
#define SDL_PROP_PROCESS_CREATE_STDERR_POINTER              "SDL.process.create.stderr_source"
#define SDL_PROP_PROCESS_CREATE_STDERR_TO_STDOUT_BOOLEAN    "SDL.process.create.stderr_to_stdout"
#define SDL_PROP_PROCESS_CREATE_BACKGROUND_BOOLEAN          "SDL.process.create.background"
#define SDL_PROP_PROCESS_CREATE_CMDLINE_STRING              "SDL.process.create.cmdline"


extern SDL_DECLSPEC SDL_PropertiesID SDLCALL SDL_GetProcessProperties(SDL_Process *process);

#define SDL_PROP_PROCESS_PID_NUMBER         "SDL.process.pid"
#define SDL_PROP_PROCESS_STDIN_POINTER      "SDL.process.stdin"
#define SDL_PROP_PROCESS_STDOUT_POINTER     "SDL.process.stdout"
#define SDL_PROP_PROCESS_STDERR_POINTER     "SDL.process.stderr"
#define SDL_PROP_PROCESS_BACKGROUND_BOOLEAN "SDL.process.background"


extern SDL_DECLSPEC void * SDLCALL SDL_ReadProcess(SDL_Process *process, size_t *datasize, int *exitcode);


extern SDL_DECLSPEC SDL_IOStream * SDLCALL SDL_GetProcessInput(SDL_Process *process);


extern SDL_DECLSPEC SDL_IOStream * SDLCALL SDL_GetProcessOutput(SDL_Process *process);


extern SDL_DECLSPEC bool SDLCALL SDL_KillProcess(SDL_Process *process, bool force);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitProcess(SDL_Process *process, bool block, int *exitcode);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyProcess(SDL_Process *process);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
