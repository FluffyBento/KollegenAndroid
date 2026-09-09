



#ifndef SDL_bits_h_
#define SDL_bits_h_

#include <SDL3/SDL_stdinc.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif

#if defined(__WATCOMC__) && defined(__386__)
extern __inline int _SDL_bsr_watcom(Uint32);
#pragma aux _SDL_bsr_watcom = \
    "bsr eax, eax" \
    parm [eax] nomemory \
    value [eax] \
    modify exact [eax] nomemory;
#endif


SDL_FORCE_INLINE int SDL_MostSignificantBitIndex32(Uint32 x)
{
#if defined(__GNUC__) && (__GNUC__ >= 4 || (__GNUC__ == 3 && __GNUC_MINOR__ >= 4))
    
    if (x == 0) {
        return -1;
    }
    return 31 - __builtin_clz(x);
#elif defined(__WATCOMC__) && defined(__386__)
    if (x == 0) {
        return -1;
    }
    return _SDL_bsr_watcom(x);
#elif defined(_MSC_VER) && _MSC_VER >= 1400
    unsigned long index;
    if (_BitScanReverse(&index, x)) {
        return (int)index;
    }
    return -1;
#else
    
    const Uint32 b[] = {0x2, 0xC, 0xF0, 0xFF00, 0xFFFF0000};
    const int    S[] = {1, 2, 4, 8, 16};

    int msbIndex = 0;
    int i;

    if (x == 0) {
        return -1;
    }

    for (i = 4; i >= 0; i--)
    {
        if (x & b[i])
        {
            x >>= S[i];
            msbIndex |= S[i];
        }
    }

    return msbIndex;
#endif
}


SDL_FORCE_INLINE bool SDL_HasExactlyOneBitSet32(Uint32 x)
{
    if (x && !(x & (x - 1))) {
        return true;
    }
    return false;
}


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
