

#ifndef SDL_time_h_
#define SDL_time_h_



#include <SDL3/SDL_error.h>
#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_DateTime
{
    int year;                  
    int month;                 
    int day;                   
    int hour;                  
    int minute;                
    int second;                
    int nanosecond;            
    int day_of_week;           
    int utc_offset;            
} SDL_DateTime;


typedef enum SDL_DateFormat
{
    SDL_DATE_FORMAT_YYYYMMDD = 0, 
    SDL_DATE_FORMAT_DDMMYYYY = 1, 
    SDL_DATE_FORMAT_MMDDYYYY = 2  
} SDL_DateFormat;


typedef enum SDL_TimeFormat
{
    SDL_TIME_FORMAT_24HR = 0, 
    SDL_TIME_FORMAT_12HR = 1  
} SDL_TimeFormat;


extern SDL_DECLSPEC bool SDLCALL SDL_GetDateTimeLocalePreferences(SDL_DateFormat *dateFormat, SDL_TimeFormat *timeFormat);


extern SDL_DECLSPEC bool SDLCALL SDL_GetCurrentTime(SDL_Time *ticks);


extern SDL_DECLSPEC bool SDLCALL SDL_TimeToDateTime(SDL_Time ticks, SDL_DateTime *dt, bool localTime);


extern SDL_DECLSPEC bool SDLCALL SDL_DateTimeToTime(const SDL_DateTime *dt, SDL_Time *ticks);


extern SDL_DECLSPEC void SDLCALL SDL_TimeToWindows(SDL_Time ticks, Uint32 *dwLowDateTime, Uint32 *dwHighDateTime);


extern SDL_DECLSPEC SDL_Time SDLCALL SDL_TimeFromWindows(Uint32 dwLowDateTime, Uint32 dwHighDateTime);


extern SDL_DECLSPEC int SDLCALL SDL_GetDaysInMonth(int year, int month);


extern SDL_DECLSPEC int SDLCALL SDL_GetDayOfYear(int year, int month, int day);


extern SDL_DECLSPEC int SDLCALL SDL_GetDayOfWeek(int year, int month, int day);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
