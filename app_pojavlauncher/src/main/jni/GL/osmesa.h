





#ifndef OSMESA_H
#define OSMESA_H


#ifdef __cplusplus
extern "C" {
#endif


#include <GL/gl.h>


#define OSMESA_MAJOR_VERSION 11
#define OSMESA_MINOR_VERSION 2
#define OSMESA_PATCH_VERSION 0




#define OSMESA_COLOR_INDEX	GL_COLOR_INDEX
#define OSMESA_RGBA		GL_RGBA
#define OSMESA_BGRA		0x1
#define OSMESA_ARGB		0x2
#define OSMESA_RGB		GL_RGB
#define OSMESA_BGR		0x4
#define OSMESA_RGB_565		0x5



#define OSMESA_ROW_LENGTH	0x10
#define OSMESA_Y_UP		0x11



#define OSMESA_WIDTH		0x20
#define OSMESA_HEIGHT		0x21
#define OSMESA_FORMAT		0x22
#define OSMESA_TYPE		0x23
#define OSMESA_MAX_WIDTH	0x24  
#define OSMESA_MAX_HEIGHT	0x25  


#define OSMESA_DEPTH_BITS            0x30
#define OSMESA_STENCIL_BITS          0x31
#define OSMESA_ACCUM_BITS            0x32
#define OSMESA_PROFILE               0x33
#define OSMESA_CORE_PROFILE          0x34
#define OSMESA_COMPAT_PROFILE        0x35
#define OSMESA_CONTEXT_MAJOR_VERSION 0x36
#define OSMESA_CONTEXT_MINOR_VERSION 0x37


typedef struct osmesa_context *OSMesaContext;



GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContext( GLenum format, OSMesaContext sharelist );




GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContextExt( GLenum format, GLint depthBits, GLint stencilBits,
                        GLint accumBits, OSMesaContext sharelist);



GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContextAttribs( const int *attribList, OSMesaContext sharelist );




GLAPI void GLAPIENTRY
OSMesaDestroyContext( OSMesaContext ctx );




GLAPI GLboolean GLAPIENTRY
OSMesaMakeCurrent( OSMesaContext ctx, void *buffer, GLenum type,
                   GLsizei width, GLsizei height );





GLAPI OSMesaContext GLAPIENTRY
OSMesaGetCurrentContext( void );




GLAPI void GLAPIENTRY
OSMesaPixelStore( GLint pname, GLint value );




GLAPI void GLAPIENTRY
OSMesaGetIntegerv( GLint pname, GLint *value );




GLAPI GLboolean GLAPIENTRY
OSMesaGetDepthBuffer( OSMesaContext c, GLint *width, GLint *height,
                      GLint *bytesPerValue, void **buffer );




GLAPI GLboolean GLAPIENTRY
OSMesaGetColorBuffer( OSMesaContext c, GLint *width, GLint *height,
                      GLint *format, void **buffer );




typedef void (*OSMESAproc)();



GLAPI OSMESAproc GLAPIENTRY
OSMesaGetProcAddress( const char *funcName );




GLAPI void GLAPIENTRY
OSMesaColorClamp(GLboolean enable);



GLAPI void GLAPIENTRY
OSMesaPostprocess(OSMesaContext osmesa, const char *filter,
                  unsigned enable_value);


#ifdef __cplusplus
}
#endif


#endif
