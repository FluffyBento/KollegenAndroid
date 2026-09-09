





#ifndef SDL_intrin_h_
#define SDL_intrin_h_

#include <SDL3/SDL_stdinc.h>

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_LSX_INTRINSICS 1


#define SDL_LASX_INTRINSICS 1


#define SDL_NEON_INTRINSICS 1


#define SDL_ALTIVEC_INTRINSICS 1


#define SDL_MMX_INTRINSICS 1


#define SDL_SSE_INTRINSICS 1


#define SDL_SSE2_INTRINSICS 1


#define SDL_SSE3_INTRINSICS 1


#define SDL_SSE4_1_INTRINSICS 1


#define SDL_SSE4_2_INTRINSICS 1


#define SDL_AVX_INTRINSICS 1


#define SDL_AVX2_INTRINSICS 1


#define SDL_AVX512F_INTRINSICS 1
#endif



#if defined(_MSC_VER) && (_MSC_VER >= 1500) && (defined(_M_IX86) || defined(_M_X64))

#if defined(__clang__) && !SDL_HAS_BUILTIN(_m_prefetch)
#ifndef __PRFCHWINTRIN_H
#define __PRFCHWINTRIN_H
static __inline__ void __attribute__((__always_inline__, __nodebug__))
_m_prefetch(void *__P)
{
  __builtin_prefetch (__P, 0, 3 );
}
#endif 
#endif 
#include <intrin.h>

#elif defined(__MINGW64_VERSION_MAJOR)
#include <intrin.h>
#if defined(__ARM_NEON) && !defined(SDL_DISABLE_NEON)
#  define SDL_NEON_INTRINSICS 1
#  include <arm_neon.h>
#endif

#else

#if defined(__ALTIVEC__) && defined(SDL_ENABLE_ALTIVEC)
#define SDL_ALTIVEC_INTRINSICS 1
#include <altivec.h>
#endif
#ifndef SDL_DISABLE_NEON
#  ifdef __ARM_NEON
#    define SDL_NEON_INTRINSICS 1
#    include <arm_neon.h>
#  elif defined(SDL_PLATFORM_WINDOWS)

#    ifdef _M_ARM
#      define SDL_NEON_INTRINSICS 1
#      include <armintr.h>
#      include <arm_neon.h>
#      define __ARM_NEON 1 
#    endif
#    if defined (_M_ARM64)
#      define SDL_NEON_INTRINSICS 1
#      include <arm64intr.h>
#      include <arm64_neon.h>
#      define __ARM_NEON 1 
#      define __ARM_ARCH 8
#    endif
#  endif
#endif
#endif 

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_HAS_TARGET_ATTRIBS
#elif defined(__loongarch64) && defined(__GNUC__) && (__GNUC__ >= 15)

# define SDL_HAS_TARGET_ATTRIBS
#elif defined(__clang__) && defined(__has_attribute)
# if __has_attribute(target)
# define SDL_HAS_TARGET_ATTRIBS
# endif
#elif defined(__GNUC__) && !defined(__loongarch64) && (__GNUC__ + (__GNUC_MINOR__ >= 9) > 4) 
# define SDL_HAS_TARGET_ATTRIBS
#elif defined(__ICC) && __ICC >= 1600
# define SDL_HAS_TARGET_ATTRIBS
#endif


#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_TARGETING(x) __attribute__((target(x)))

#elif defined(SDL_HAS_TARGET_ATTRIBS)
# define SDL_TARGETING(x) __attribute__((target(x)))
#else
# define SDL_TARGETING(x)
#endif

#ifdef __loongarch64
# ifndef SDL_DISABLE_LSX
#  define SDL_LSX_INTRINSICS 1
#  include <lsxintrin.h>
# endif
# ifndef SDL_DISABLE_LASX
#  define SDL_LASX_INTRINSICS 1
#  include <lasxintrin.h>
# endif
#endif

#if defined(__x86_64__) || defined(_M_X64) || defined(__i386__) || defined(_M_IX86)
# if ((defined(_MSC_VER) && !defined(_M_X64)) || defined(__MMX__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_MMX)
#  define SDL_MMX_INTRINSICS 1
#  include <mmintrin.h>
# endif
# if (defined(_MSC_VER) || defined(__SSE__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_SSE)
#  define SDL_SSE_INTRINSICS 1
#  include <xmmintrin.h>
# endif
# if (defined(_MSC_VER) || defined(__SSE2__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_SSE2)
#  define SDL_SSE2_INTRINSICS 1
#  include <emmintrin.h>
# endif
# if (defined(_MSC_VER) || defined(__SSE3__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_SSE3)
#  define SDL_SSE3_INTRINSICS 1
#  include <pmmintrin.h>
# endif
# if (defined(_MSC_VER) || defined(__SSE4_1__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_SSE4_1)
#  define SDL_SSE4_1_INTRINSICS 1
#  include <smmintrin.h>
# endif
# if (defined(_MSC_VER) || defined(__SSE4_2__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(SDL_DISABLE_SSE4_2)
#  define SDL_SSE4_2_INTRINSICS 1
#  include <nmmintrin.h>
# endif
# if defined(__clang__) && (defined(_MSC_VER) || defined(__SCE__)) && !defined(__AVX__) && !defined(SDL_DISABLE_AVX)
#  define SDL_DISABLE_AVX       
# endif
# if (defined(_MSC_VER) || defined(__AVX__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(_M_ARM64EC) && !defined(SDL_DISABLE_AVX)
#  define SDL_AVX_INTRINSICS 1
#  include <immintrin.h>
# endif
# if defined(__clang__) && (defined(_MSC_VER) || defined(__SCE__)) && !defined(__AVX2__) && !defined(SDL_DISABLE_AVX2)
#  define SDL_DISABLE_AVX2      
# endif
# if (defined(_MSC_VER) || defined(__AVX2__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(_M_ARM64EC) && !defined(SDL_DISABLE_AVX2)
#  define SDL_AVX2_INTRINSICS 1
#  include <immintrin.h>
# endif
# if defined(__clang__) && (defined(_MSC_VER) || defined(__SCE__)) && !defined(__AVX512F__) && !defined(SDL_DISABLE_AVX512F)
#  define SDL_DISABLE_AVX512F   
# endif
# if (defined(_MSC_VER) || defined(__AVX512F__) || defined(SDL_HAS_TARGET_ATTRIBS)) && !defined(_M_ARM64EC) && !defined(SDL_DISABLE_AVX512F)
#  define SDL_AVX512F_INTRINSICS 1
#  include <immintrin.h>
# endif
#endif 

#endif 
