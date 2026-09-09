




#ifndef SDL_haptic_h_
#define SDL_haptic_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_joystick.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif 




typedef struct SDL_Haptic SDL_Haptic;




#define SDL_HAPTIC_INFINITY   4294967295U









typedef Uint16 SDL_HapticEffectType;


#define SDL_HAPTIC_CONSTANT     (1u<<0)


#define SDL_HAPTIC_SINE         (1u<<1)


#define SDL_HAPTIC_SQUARE       (1u<<2)


#define SDL_HAPTIC_TRIANGLE     (1u<<3)


#define SDL_HAPTIC_SAWTOOTHUP   (1u<<4)


#define SDL_HAPTIC_SAWTOOTHDOWN (1u<<5)


#define SDL_HAPTIC_RAMP         (1u<<6)


#define SDL_HAPTIC_SPRING       (1u<<7)


#define SDL_HAPTIC_DAMPER       (1u<<8)


#define SDL_HAPTIC_INERTIA      (1u<<9)


#define SDL_HAPTIC_FRICTION     (1u<<10)


#define SDL_HAPTIC_LEFTRIGHT    (1u<<11)


#define SDL_HAPTIC_RESERVED1    (1u<<12)


#define SDL_HAPTIC_RESERVED2    (1u<<13)


#define SDL_HAPTIC_RESERVED3    (1u<<14)


#define SDL_HAPTIC_CUSTOM       (1u<<15)






#define SDL_HAPTIC_GAIN       (1u<<16)


#define SDL_HAPTIC_AUTOCENTER (1u<<17)


#define SDL_HAPTIC_STATUS     (1u<<18)


#define SDL_HAPTIC_PAUSE      (1u<<19)






typedef Uint8 SDL_HapticDirectionType;


#define SDL_HAPTIC_POLAR      0


#define SDL_HAPTIC_CARTESIAN  1


#define SDL_HAPTIC_SPHERICAL  2


#define SDL_HAPTIC_STEERING_AXIS 3







typedef int SDL_HapticEffectID;



typedef struct SDL_HapticDirection
{
    SDL_HapticDirectionType type;  
    Sint32 dir[3];                 
} SDL_HapticDirection;



typedef struct SDL_HapticConstant
{
    
    SDL_HapticEffectType type;      
    SDL_HapticDirection direction;  

    
    Uint32 length;          
    Uint16 delay;           

    
    Uint16 button;          
    Uint16 interval;        

    
    Sint16 level;           

    
    Uint16 attack_length;   
    Uint16 attack_level;    
    Uint16 fade_length;     
    Uint16 fade_level;      
} SDL_HapticConstant;


typedef struct SDL_HapticPeriodic
{
    
    SDL_HapticEffectType type;      
    SDL_HapticDirection direction;  

    
    Uint32 length;      
    Uint16 delay;       

    
    Uint16 button;      
    Uint16 interval;    

    
    Uint16 period;      
    Sint16 magnitude;   
    Sint16 offset;      
    Uint16 phase;       

    
    Uint16 attack_length;   
    Uint16 attack_level;    
    Uint16 fade_length; 
    Uint16 fade_level;  
} SDL_HapticPeriodic;


typedef struct SDL_HapticCondition
{
    
    SDL_HapticEffectType type;      
    SDL_HapticDirection direction;  

    
    Uint32 length;          
    Uint16 delay;           

    
    Uint16 button;          
    Uint16 interval;        

    
    Uint16 right_sat[3];    
    Uint16 left_sat[3];     
    Sint16 right_coeff[3];  
    Sint16 left_coeff[3];   
    Uint16 deadband[3];     
    Sint16 center[3];       
} SDL_HapticCondition;


typedef struct SDL_HapticRamp
{
    
    SDL_HapticEffectType type;      
    SDL_HapticDirection direction;  

    
    Uint32 length;          
    Uint16 delay;           

    
    Uint16 button;          
    Uint16 interval;        

    
    Sint16 start;           
    Sint16 end;             

    
    Uint16 attack_length;   
    Uint16 attack_level;    
    Uint16 fade_length;     
    Uint16 fade_level;      
} SDL_HapticRamp;


typedef struct SDL_HapticLeftRight
{
    
    SDL_HapticEffectType type;  

    
    Uint32 length;          

    
    Uint16 large_magnitude; 
    Uint16 small_magnitude; 
} SDL_HapticLeftRight;


typedef struct SDL_HapticCustom
{
    
    SDL_HapticEffectType type;      
    SDL_HapticDirection direction;  

    
    Uint32 length;          
    Uint16 delay;           

    
    Uint16 button;          
    Uint16 interval;        

    
    Uint8 channels;         
    Uint16 period;          
    Uint16 samples;         
    Uint16 *data;           

    
    Uint16 attack_length;   
    Uint16 attack_level;    
    Uint16 fade_length;     
    Uint16 fade_level;      
} SDL_HapticCustom;


typedef union SDL_HapticEffect
{
    
    SDL_HapticEffectType type;      
    SDL_HapticConstant constant;    
    SDL_HapticPeriodic periodic;    
    SDL_HapticCondition condition;  
    SDL_HapticRamp ramp;            
    SDL_HapticLeftRight leftright;  
    SDL_HapticCustom custom;        
} SDL_HapticEffect;


typedef Uint32 SDL_HapticID;





extern SDL_DECLSPEC SDL_HapticID * SDLCALL SDL_GetHaptics(int *count);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetHapticNameForID(SDL_HapticID instance_id);


extern SDL_DECLSPEC SDL_Haptic * SDLCALL SDL_OpenHaptic(SDL_HapticID instance_id);



extern SDL_DECLSPEC SDL_Haptic * SDLCALL SDL_GetHapticFromID(SDL_HapticID instance_id);


extern SDL_DECLSPEC SDL_HapticID SDLCALL SDL_GetHapticID(SDL_Haptic *haptic);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetHapticName(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_IsMouseHaptic(void);


extern SDL_DECLSPEC SDL_Haptic * SDLCALL SDL_OpenHapticFromMouse(void);


extern SDL_DECLSPEC bool SDLCALL SDL_IsJoystickHaptic(SDL_Joystick *joystick);


extern SDL_DECLSPEC SDL_Haptic * SDLCALL SDL_OpenHapticFromJoystick(SDL_Joystick *joystick);


extern SDL_DECLSPEC void SDLCALL SDL_CloseHaptic(SDL_Haptic *haptic);


extern SDL_DECLSPEC int SDLCALL SDL_GetMaxHapticEffects(SDL_Haptic *haptic);


extern SDL_DECLSPEC int SDLCALL SDL_GetMaxHapticEffectsPlaying(SDL_Haptic *haptic);


extern SDL_DECLSPEC Uint32 SDLCALL SDL_GetHapticFeatures(SDL_Haptic *haptic);


extern SDL_DECLSPEC int SDLCALL SDL_GetNumHapticAxes(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_HapticEffectSupported(SDL_Haptic *haptic, const SDL_HapticEffect *effect);


extern SDL_DECLSPEC SDL_HapticEffectID SDLCALL SDL_CreateHapticEffect(SDL_Haptic *haptic, const SDL_HapticEffect *effect);


extern SDL_DECLSPEC bool SDLCALL SDL_UpdateHapticEffect(SDL_Haptic *haptic, SDL_HapticEffectID effect, const SDL_HapticEffect *data);


extern SDL_DECLSPEC bool SDLCALL SDL_RunHapticEffect(SDL_Haptic *haptic, SDL_HapticEffectID effect, Uint32 iterations);


extern SDL_DECLSPEC bool SDLCALL SDL_StopHapticEffect(SDL_Haptic *haptic, SDL_HapticEffectID effect);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyHapticEffect(SDL_Haptic *haptic, SDL_HapticEffectID effect);


extern SDL_DECLSPEC bool SDLCALL SDL_GetHapticEffectStatus(SDL_Haptic *haptic, SDL_HapticEffectID effect);


extern SDL_DECLSPEC bool SDLCALL SDL_SetHapticGain(SDL_Haptic *haptic, int gain);


extern SDL_DECLSPEC bool SDLCALL SDL_SetHapticAutocenter(SDL_Haptic *haptic, int autocenter);


extern SDL_DECLSPEC bool SDLCALL SDL_PauseHaptic(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_ResumeHaptic(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_StopHapticEffects(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_HapticRumbleSupported(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_InitHapticRumble(SDL_Haptic *haptic);


extern SDL_DECLSPEC bool SDLCALL SDL_PlayHapticRumble(SDL_Haptic *haptic, float strength, Uint32 length);


extern SDL_DECLSPEC bool SDLCALL SDL_StopHapticRumble(SDL_Haptic *haptic);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
