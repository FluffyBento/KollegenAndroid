



#ifndef SDL_render_h_
#define SDL_render_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_blendmode.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_events.h>
#include <SDL3/SDL_pixels.h>
#include <SDL3/SDL_properties.h>
#include <SDL3/SDL_rect.h>
#include <SDL3/SDL_surface.h>
#include <SDL3/SDL_video.h>
#include <SDL3/SDL_gpu.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


#define SDL_SOFTWARE_RENDERER   "software"


#define SDL_GPU_RENDERER        "gpu"


typedef struct SDL_Vertex
{
    SDL_FPoint position;        
    SDL_FColor color;           
    SDL_FPoint tex_coord;       
} SDL_Vertex;


typedef enum SDL_TextureAccess
{
    SDL_TEXTUREACCESS_STATIC,    
    SDL_TEXTUREACCESS_STREAMING, 
    SDL_TEXTUREACCESS_TARGET     
} SDL_TextureAccess;


typedef enum SDL_TextureAddressMode
{
    SDL_TEXTURE_ADDRESS_INVALID = -1,
    SDL_TEXTURE_ADDRESS_AUTO,   
    SDL_TEXTURE_ADDRESS_CLAMP,  
    SDL_TEXTURE_ADDRESS_WRAP    
} SDL_TextureAddressMode;


typedef enum SDL_RendererLogicalPresentation
{
    SDL_LOGICAL_PRESENTATION_DISABLED,  
    SDL_LOGICAL_PRESENTATION_STRETCH,   
    SDL_LOGICAL_PRESENTATION_LETTERBOX, 
    SDL_LOGICAL_PRESENTATION_OVERSCAN,  
    SDL_LOGICAL_PRESENTATION_INTEGER_SCALE   
} SDL_RendererLogicalPresentation;


typedef struct SDL_Renderer SDL_Renderer;

#ifndef SDL_INTERNAL


struct SDL_Texture
{
    SDL_PixelFormat format;     
    int w;                      
    int h;                      

    int refcount;               
};
#endif 

typedef struct SDL_Texture SDL_Texture;




extern SDL_DECLSPEC int SDLCALL SDL_GetNumRenderDrivers(void);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetRenderDriver(int index);


extern SDL_DECLSPEC bool SDLCALL SDL_CreateWindowAndRenderer(const char *title, int width, int height, SDL_WindowFlags window_flags, SDL_Window **window, SDL_Renderer **renderer);


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_CreateRenderer(SDL_Window *window, const char *name);


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_CreateRendererWithProperties(SDL_PropertiesID props);

#define SDL_PROP_RENDERER_CREATE_NAME_STRING                                "SDL.renderer.create.name"
#define SDL_PROP_RENDERER_CREATE_WINDOW_POINTER                             "SDL.renderer.create.window"
#define SDL_PROP_RENDERER_CREATE_SURFACE_POINTER                            "SDL.renderer.create.surface"
#define SDL_PROP_RENDERER_CREATE_OUTPUT_COLORSPACE_NUMBER                   "SDL.renderer.create.output_colorspace"
#define SDL_PROP_RENDERER_CREATE_PRESENT_VSYNC_NUMBER                       "SDL.renderer.create.present_vsync"
#define SDL_PROP_RENDERER_CREATE_GPU_DEVICE_POINTER                         "SDL.renderer.create.gpu.device"
#define SDL_PROP_RENDERER_CREATE_GPU_SHADERS_SPIRV_BOOLEAN                  "SDL.renderer.create.gpu.shaders_spirv"
#define SDL_PROP_RENDERER_CREATE_GPU_SHADERS_DXIL_BOOLEAN                   "SDL.renderer.create.gpu.shaders_dxil"
#define SDL_PROP_RENDERER_CREATE_GPU_SHADERS_MSL_BOOLEAN                    "SDL.renderer.create.gpu.shaders_msl"
#define SDL_PROP_RENDERER_CREATE_VULKAN_INSTANCE_POINTER                    "SDL.renderer.create.vulkan.instance"
#define SDL_PROP_RENDERER_CREATE_VULKAN_SURFACE_NUMBER                      "SDL.renderer.create.vulkan.surface"
#define SDL_PROP_RENDERER_CREATE_VULKAN_PHYSICAL_DEVICE_POINTER             "SDL.renderer.create.vulkan.physical_device"
#define SDL_PROP_RENDERER_CREATE_VULKAN_DEVICE_POINTER                      "SDL.renderer.create.vulkan.device"
#define SDL_PROP_RENDERER_CREATE_VULKAN_GRAPHICS_QUEUE_FAMILY_INDEX_NUMBER  "SDL.renderer.create.vulkan.graphics_queue_family_index"
#define SDL_PROP_RENDERER_CREATE_VULKAN_PRESENT_QUEUE_FAMILY_INDEX_NUMBER   "SDL.renderer.create.vulkan.present_queue_family_index"


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_CreateGPURenderer(SDL_GPUDevice *device, SDL_Window *window);


extern SDL_DECLSPEC SDL_GPUDevice * SDLCALL SDL_GetGPURendererDevice(SDL_Renderer *renderer);


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_CreateSoftwareRenderer(SDL_Surface *surface);


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_GetRenderer(SDL_Window *window);


extern SDL_DECLSPEC SDL_Window * SDLCALL SDL_GetRenderWindow(SDL_Renderer *renderer);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetRendererName(SDL_Renderer *renderer);


extern SDL_DECLSPEC SDL_PropertiesID SDLCALL SDL_GetRendererProperties(SDL_Renderer *renderer);

#define SDL_PROP_RENDERER_NAME_STRING                               "SDL.renderer.name"
#define SDL_PROP_RENDERER_WINDOW_POINTER                            "SDL.renderer.window"
#define SDL_PROP_RENDERER_SURFACE_POINTER                           "SDL.renderer.surface"
#define SDL_PROP_RENDERER_VSYNC_NUMBER                              "SDL.renderer.vsync"
#define SDL_PROP_RENDERER_MAX_TEXTURE_SIZE_NUMBER                   "SDL.renderer.max_texture_size"
#define SDL_PROP_RENDERER_TEXTURE_FORMATS_POINTER                   "SDL.renderer.texture_formats"
#define SDL_PROP_RENDERER_TEXTURE_WRAPPING_BOOLEAN                  "SDL.renderer.texture_wrapping"
#define SDL_PROP_RENDERER_OUTPUT_COLORSPACE_NUMBER                  "SDL.renderer.output_colorspace"
#define SDL_PROP_RENDERER_HDR_ENABLED_BOOLEAN                       "SDL.renderer.HDR_enabled"
#define SDL_PROP_RENDERER_SDR_WHITE_POINT_FLOAT                     "SDL.renderer.SDR_white_point"
#define SDL_PROP_RENDERER_HDR_HEADROOM_FLOAT                        "SDL.renderer.HDR_headroom"
#define SDL_PROP_RENDERER_D3D9_DEVICE_POINTER                       "SDL.renderer.d3d9.device"
#define SDL_PROP_RENDERER_D3D11_DEVICE_POINTER                      "SDL.renderer.d3d11.device"
#define SDL_PROP_RENDERER_D3D11_SWAPCHAIN_POINTER                   "SDL.renderer.d3d11.swap_chain"
#define SDL_PROP_RENDERER_D3D12_DEVICE_POINTER                      "SDL.renderer.d3d12.device"
#define SDL_PROP_RENDERER_D3D12_SWAPCHAIN_POINTER                   "SDL.renderer.d3d12.swap_chain"
#define SDL_PROP_RENDERER_D3D12_COMMAND_QUEUE_POINTER               "SDL.renderer.d3d12.command_queue"
#define SDL_PROP_RENDERER_VULKAN_INSTANCE_POINTER                   "SDL.renderer.vulkan.instance"
#define SDL_PROP_RENDERER_VULKAN_SURFACE_NUMBER                     "SDL.renderer.vulkan.surface"
#define SDL_PROP_RENDERER_VULKAN_PHYSICAL_DEVICE_POINTER            "SDL.renderer.vulkan.physical_device"
#define SDL_PROP_RENDERER_VULKAN_DEVICE_POINTER                     "SDL.renderer.vulkan.device"
#define SDL_PROP_RENDERER_VULKAN_GRAPHICS_QUEUE_FAMILY_INDEX_NUMBER "SDL.renderer.vulkan.graphics_queue_family_index"
#define SDL_PROP_RENDERER_VULKAN_PRESENT_QUEUE_FAMILY_INDEX_NUMBER  "SDL.renderer.vulkan.present_queue_family_index"
#define SDL_PROP_RENDERER_VULKAN_SWAPCHAIN_IMAGE_COUNT_NUMBER       "SDL.renderer.vulkan.swapchain_image_count"
#define SDL_PROP_RENDERER_GPU_DEVICE_POINTER                        "SDL.renderer.gpu.device"


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderOutputSize(SDL_Renderer *renderer, int *w, int *h);


extern SDL_DECLSPEC bool SDLCALL SDL_GetCurrentRenderOutputSize(SDL_Renderer *renderer, int *w, int *h);


extern SDL_DECLSPEC SDL_Texture * SDLCALL SDL_CreateTexture(SDL_Renderer *renderer, SDL_PixelFormat format, SDL_TextureAccess access, int w, int h);


extern SDL_DECLSPEC SDL_Texture * SDLCALL SDL_CreateTextureFromSurface(SDL_Renderer *renderer, SDL_Surface *surface);


extern SDL_DECLSPEC SDL_Texture * SDLCALL SDL_CreateTextureWithProperties(SDL_Renderer *renderer, SDL_PropertiesID props);

#define SDL_PROP_TEXTURE_CREATE_COLORSPACE_NUMBER               "SDL.texture.create.colorspace"
#define SDL_PROP_TEXTURE_CREATE_FORMAT_NUMBER                   "SDL.texture.create.format"
#define SDL_PROP_TEXTURE_CREATE_ACCESS_NUMBER                   "SDL.texture.create.access"
#define SDL_PROP_TEXTURE_CREATE_WIDTH_NUMBER                    "SDL.texture.create.width"
#define SDL_PROP_TEXTURE_CREATE_HEIGHT_NUMBER                   "SDL.texture.create.height"
#define SDL_PROP_TEXTURE_CREATE_PALETTE_POINTER                 "SDL.texture.create.palette"
#define SDL_PROP_TEXTURE_CREATE_SDR_WHITE_POINT_FLOAT           "SDL.texture.create.SDR_white_point"
#define SDL_PROP_TEXTURE_CREATE_HDR_HEADROOM_FLOAT              "SDL.texture.create.HDR_headroom"
#define SDL_PROP_TEXTURE_CREATE_D3D11_TEXTURE_POINTER           "SDL.texture.create.d3d11.texture"
#define SDL_PROP_TEXTURE_CREATE_D3D11_TEXTURE_U_POINTER         "SDL.texture.create.d3d11.texture_u"
#define SDL_PROP_TEXTURE_CREATE_D3D11_TEXTURE_V_POINTER         "SDL.texture.create.d3d11.texture_v"
#define SDL_PROP_TEXTURE_CREATE_D3D12_TEXTURE_POINTER           "SDL.texture.create.d3d12.texture"
#define SDL_PROP_TEXTURE_CREATE_D3D12_TEXTURE_U_POINTER         "SDL.texture.create.d3d12.texture_u"
#define SDL_PROP_TEXTURE_CREATE_D3D12_TEXTURE_V_POINTER         "SDL.texture.create.d3d12.texture_v"
#define SDL_PROP_TEXTURE_CREATE_METAL_PIXELBUFFER_POINTER       "SDL.texture.create.metal.pixelbuffer"
#define SDL_PROP_TEXTURE_CREATE_OPENGL_TEXTURE_NUMBER           "SDL.texture.create.opengl.texture"
#define SDL_PROP_TEXTURE_CREATE_OPENGL_TEXTURE_UV_NUMBER        "SDL.texture.create.opengl.texture_uv"
#define SDL_PROP_TEXTURE_CREATE_OPENGL_TEXTURE_U_NUMBER         "SDL.texture.create.opengl.texture_u"
#define SDL_PROP_TEXTURE_CREATE_OPENGL_TEXTURE_V_NUMBER         "SDL.texture.create.opengl.texture_v"
#define SDL_PROP_TEXTURE_CREATE_OPENGLES2_TEXTURE_NUMBER        "SDL.texture.create.opengles2.texture"
#define SDL_PROP_TEXTURE_CREATE_OPENGLES2_TEXTURE_UV_NUMBER     "SDL.texture.create.opengles2.texture_uv"
#define SDL_PROP_TEXTURE_CREATE_OPENGLES2_TEXTURE_U_NUMBER      "SDL.texture.create.opengles2.texture_u"
#define SDL_PROP_TEXTURE_CREATE_OPENGLES2_TEXTURE_V_NUMBER      "SDL.texture.create.opengles2.texture_v"
#define SDL_PROP_TEXTURE_CREATE_VULKAN_TEXTURE_NUMBER           "SDL.texture.create.vulkan.texture"
#define SDL_PROP_TEXTURE_CREATE_VULKAN_LAYOUT_NUMBER            "SDL.texture.create.vulkan.layout"
#define SDL_PROP_TEXTURE_CREATE_GPU_TEXTURE_POINTER             "SDL.texture.create.gpu.texture"
#define SDL_PROP_TEXTURE_CREATE_GPU_TEXTURE_UV_POINTER          "SDL.texture.create.gpu.texture_uv"
#define SDL_PROP_TEXTURE_CREATE_GPU_TEXTURE_U_POINTER           "SDL.texture.create.gpu.texture_u"
#define SDL_PROP_TEXTURE_CREATE_GPU_TEXTURE_V_POINTER           "SDL.texture.create.gpu.texture_v"


extern SDL_DECLSPEC SDL_PropertiesID SDLCALL SDL_GetTextureProperties(SDL_Texture *texture);

#define SDL_PROP_TEXTURE_COLORSPACE_NUMBER                  "SDL.texture.colorspace"
#define SDL_PROP_TEXTURE_FORMAT_NUMBER                      "SDL.texture.format"
#define SDL_PROP_TEXTURE_ACCESS_NUMBER                      "SDL.texture.access"
#define SDL_PROP_TEXTURE_WIDTH_NUMBER                       "SDL.texture.width"
#define SDL_PROP_TEXTURE_HEIGHT_NUMBER                      "SDL.texture.height"
#define SDL_PROP_TEXTURE_SDR_WHITE_POINT_FLOAT              "SDL.texture.SDR_white_point"
#define SDL_PROP_TEXTURE_HDR_HEADROOM_FLOAT                 "SDL.texture.HDR_headroom"
#define SDL_PROP_TEXTURE_D3D11_TEXTURE_POINTER              "SDL.texture.d3d11.texture"
#define SDL_PROP_TEXTURE_D3D11_TEXTURE_U_POINTER            "SDL.texture.d3d11.texture_u"
#define SDL_PROP_TEXTURE_D3D11_TEXTURE_V_POINTER            "SDL.texture.d3d11.texture_v"
#define SDL_PROP_TEXTURE_D3D12_TEXTURE_POINTER              "SDL.texture.d3d12.texture"
#define SDL_PROP_TEXTURE_D3D12_TEXTURE_U_POINTER            "SDL.texture.d3d12.texture_u"
#define SDL_PROP_TEXTURE_D3D12_TEXTURE_V_POINTER            "SDL.texture.d3d12.texture_v"
#define SDL_PROP_TEXTURE_OPENGL_TEXTURE_NUMBER              "SDL.texture.opengl.texture"
#define SDL_PROP_TEXTURE_OPENGL_TEXTURE_UV_NUMBER           "SDL.texture.opengl.texture_uv"
#define SDL_PROP_TEXTURE_OPENGL_TEXTURE_U_NUMBER            "SDL.texture.opengl.texture_u"
#define SDL_PROP_TEXTURE_OPENGL_TEXTURE_V_NUMBER            "SDL.texture.opengl.texture_v"
#define SDL_PROP_TEXTURE_OPENGL_TEXTURE_TARGET_NUMBER       "SDL.texture.opengl.target"
#define SDL_PROP_TEXTURE_OPENGL_TEX_W_FLOAT                 "SDL.texture.opengl.tex_w"
#define SDL_PROP_TEXTURE_OPENGL_TEX_H_FLOAT                 "SDL.texture.opengl.tex_h"
#define SDL_PROP_TEXTURE_OPENGLES2_TEXTURE_NUMBER           "SDL.texture.opengles2.texture"
#define SDL_PROP_TEXTURE_OPENGLES2_TEXTURE_UV_NUMBER        "SDL.texture.opengles2.texture_uv"
#define SDL_PROP_TEXTURE_OPENGLES2_TEXTURE_U_NUMBER         "SDL.texture.opengles2.texture_u"
#define SDL_PROP_TEXTURE_OPENGLES2_TEXTURE_V_NUMBER         "SDL.texture.opengles2.texture_v"
#define SDL_PROP_TEXTURE_OPENGLES2_TEXTURE_TARGET_NUMBER    "SDL.texture.opengles2.target"
#define SDL_PROP_TEXTURE_VULKAN_TEXTURE_NUMBER              "SDL.texture.vulkan.texture"
#define SDL_PROP_TEXTURE_GPU_TEXTURE_POINTER                "SDL.texture.gpu.texture"
#define SDL_PROP_TEXTURE_GPU_TEXTURE_UV_POINTER             "SDL.texture.gpu.texture_uv"
#define SDL_PROP_TEXTURE_GPU_TEXTURE_U_POINTER              "SDL.texture.gpu.texture_u"
#define SDL_PROP_TEXTURE_GPU_TEXTURE_V_POINTER              "SDL.texture.gpu.texture_v"


extern SDL_DECLSPEC SDL_Renderer * SDLCALL SDL_GetRendererFromTexture(SDL_Texture *texture);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureSize(SDL_Texture *texture, float *w, float *h);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTexturePalette(SDL_Texture *texture, SDL_Palette *palette);


extern SDL_DECLSPEC SDL_Palette * SDLCALL SDL_GetTexturePalette(SDL_Texture *texture);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureColorMod(SDL_Texture *texture, Uint8 r, Uint8 g, Uint8 b);



extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureColorModFloat(SDL_Texture *texture, float r, float g, float b);



extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureColorMod(SDL_Texture *texture, Uint8 *r, Uint8 *g, Uint8 *b);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureColorModFloat(SDL_Texture *texture, float *r, float *g, float *b);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureAlphaMod(SDL_Texture *texture, Uint8 alpha);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureAlphaModFloat(SDL_Texture *texture, float alpha);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureAlphaMod(SDL_Texture *texture, Uint8 *alpha);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureAlphaModFloat(SDL_Texture *texture, float *alpha);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureBlendMode(SDL_Texture *texture, SDL_BlendMode blendMode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureBlendMode(SDL_Texture *texture, SDL_BlendMode *blendMode);


extern SDL_DECLSPEC bool SDLCALL SDL_SetTextureScaleMode(SDL_Texture *texture, SDL_ScaleMode scaleMode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetTextureScaleMode(SDL_Texture *texture, SDL_ScaleMode *scaleMode);


extern SDL_DECLSPEC bool SDLCALL SDL_UpdateTexture(SDL_Texture *texture, const SDL_Rect *rect, const void *pixels, int pitch);


extern SDL_DECLSPEC bool SDLCALL SDL_UpdateYUVTexture(SDL_Texture *texture,
                                                 const SDL_Rect *rect,
                                                 const Uint8 *Yplane, int Ypitch,
                                                 const Uint8 *Uplane, int Upitch,
                                                 const Uint8 *Vplane, int Vpitch);


extern SDL_DECLSPEC bool SDLCALL SDL_UpdateNVTexture(SDL_Texture *texture,
                                                 const SDL_Rect *rect,
                                                 const Uint8 *Yplane, int Ypitch,
                                                 const Uint8 *UVplane, int UVpitch);


extern SDL_DECLSPEC bool SDLCALL SDL_LockTexture(SDL_Texture *texture,
                                            const SDL_Rect *rect,
                                            void **pixels, int *pitch);


extern SDL_DECLSPEC bool SDLCALL SDL_LockTextureToSurface(SDL_Texture *texture, const SDL_Rect *rect, SDL_Surface **surface);


extern SDL_DECLSPEC void SDLCALL SDL_UnlockTexture(SDL_Texture *texture);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderTarget(SDL_Renderer *renderer, SDL_Texture *texture);


extern SDL_DECLSPEC SDL_Texture * SDLCALL SDL_GetRenderTarget(SDL_Renderer *renderer);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderLogicalPresentation(SDL_Renderer *renderer, int w, int h, SDL_RendererLogicalPresentation mode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderLogicalPresentation(SDL_Renderer *renderer, int *w, int *h, SDL_RendererLogicalPresentation *mode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderLogicalPresentationRect(SDL_Renderer *renderer, SDL_FRect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderCoordinatesFromWindow(SDL_Renderer *renderer, float window_x, float window_y, float *x, float *y);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderCoordinatesToWindow(SDL_Renderer *renderer, float x, float y, float *window_x, float *window_y);


extern SDL_DECLSPEC bool SDLCALL SDL_ConvertEventToRenderCoordinates(SDL_Renderer *renderer, SDL_Event *event);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderViewport(SDL_Renderer *renderer, const SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderViewport(SDL_Renderer *renderer, SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderViewportSet(SDL_Renderer *renderer);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderSafeArea(SDL_Renderer *renderer, SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderClipRect(SDL_Renderer *renderer, const SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderClipRect(SDL_Renderer *renderer, SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderClipEnabled(SDL_Renderer *renderer);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderScale(SDL_Renderer *renderer, float scaleX, float scaleY);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderScale(SDL_Renderer *renderer, float *scaleX, float *scaleY);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderDrawColor(SDL_Renderer *renderer, Uint8 r, Uint8 g, Uint8 b, Uint8 a);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderDrawColorFloat(SDL_Renderer *renderer, float r, float g, float b, float a);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderDrawColor(SDL_Renderer *renderer, Uint8 *r, Uint8 *g, Uint8 *b, Uint8 *a);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderDrawColorFloat(SDL_Renderer *renderer, float *r, float *g, float *b, float *a);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderColorScale(SDL_Renderer *renderer, float scale);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderColorScale(SDL_Renderer *renderer, float *scale);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderDrawBlendMode(SDL_Renderer *renderer, SDL_BlendMode blendMode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderDrawBlendMode(SDL_Renderer *renderer, SDL_BlendMode *blendMode);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderClear(SDL_Renderer *renderer);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderPoint(SDL_Renderer *renderer, float x, float y);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderPoints(SDL_Renderer *renderer, const SDL_FPoint *points, int count);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderLine(SDL_Renderer *renderer, float x1, float y1, float x2, float y2);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderLines(SDL_Renderer *renderer, const SDL_FPoint *points, int count);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderRect(SDL_Renderer *renderer, const SDL_FRect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderRects(SDL_Renderer *renderer, const SDL_FRect *rects, int count);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderFillRect(SDL_Renderer *renderer, const SDL_FRect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderFillRects(SDL_Renderer *renderer, const SDL_FRect *rects, int count);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTexture(SDL_Renderer *renderer, SDL_Texture *texture, const SDL_FRect *srcrect, const SDL_FRect *dstrect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTextureRotated(SDL_Renderer *renderer, SDL_Texture *texture,
                                                     const SDL_FRect *srcrect, const SDL_FRect *dstrect,
                                                     double angle, const SDL_FPoint *center,
                                                     SDL_FlipMode flip);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTextureAffine(SDL_Renderer *renderer, SDL_Texture *texture,
                                                     const SDL_FRect *srcrect, const SDL_FPoint *origin,
                                                     const SDL_FPoint *right, const SDL_FPoint *down);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTextureTiled(SDL_Renderer *renderer, SDL_Texture *texture, const SDL_FRect *srcrect, float scale, const SDL_FRect *dstrect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTexture9Grid(SDL_Renderer *renderer, SDL_Texture *texture, const SDL_FRect *srcrect, float left_width, float right_width, float top_height, float bottom_height, float scale, const SDL_FRect *dstrect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderTexture9GridTiled(SDL_Renderer *renderer, SDL_Texture *texture, const SDL_FRect *srcrect, float left_width, float right_width, float top_height, float bottom_height, float scale, const SDL_FRect *dstrect, float tileScale);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderGeometry(SDL_Renderer *renderer,
                                               SDL_Texture *texture,
                                               const SDL_Vertex *vertices, int num_vertices,
                                               const int *indices, int num_indices);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderGeometryRaw(SDL_Renderer *renderer,
                                               SDL_Texture *texture,
                                               const float *xy, int xy_stride,
                                               const SDL_FColor *color, int color_stride,
                                               const float *uv, int uv_stride,
                                               int num_vertices,
                                               const void *indices, int num_indices, int size_indices);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderTextureAddressMode(SDL_Renderer *renderer, SDL_TextureAddressMode u_mode, SDL_TextureAddressMode v_mode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderTextureAddressMode(SDL_Renderer *renderer, SDL_TextureAddressMode *u_mode, SDL_TextureAddressMode *v_mode);


extern SDL_DECLSPEC SDL_Surface * SDLCALL SDL_RenderReadPixels(SDL_Renderer *renderer, const SDL_Rect *rect);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderPresent(SDL_Renderer *renderer);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyTexture(SDL_Texture *texture);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyRenderer(SDL_Renderer *renderer);


extern SDL_DECLSPEC bool SDLCALL SDL_FlushRenderer(SDL_Renderer *renderer);


extern SDL_DECLSPEC void * SDLCALL SDL_GetRenderMetalLayer(SDL_Renderer *renderer);


extern SDL_DECLSPEC void * SDLCALL SDL_GetRenderMetalCommandEncoder(SDL_Renderer *renderer);



extern SDL_DECLSPEC bool SDLCALL SDL_AddVulkanRenderSemaphores(SDL_Renderer *renderer, Uint32 wait_stage_mask, Sint64 wait_semaphore, Sint64 signal_semaphore);


extern SDL_DECLSPEC bool SDLCALL SDL_SetRenderVSync(SDL_Renderer *renderer, int vsync);

#define SDL_RENDERER_VSYNC_DISABLED 0
#define SDL_RENDERER_VSYNC_ADAPTIVE (-1)


extern SDL_DECLSPEC bool SDLCALL SDL_GetRenderVSync(SDL_Renderer *renderer, int *vsync);


#define SDL_DEBUG_TEXT_FONT_CHARACTER_SIZE 8


extern SDL_DECLSPEC bool SDLCALL SDL_RenderDebugText(SDL_Renderer *renderer, float x, float y, const char *str);


extern SDL_DECLSPEC bool SDLCALL SDL_RenderDebugTextFormat(SDL_Renderer *renderer, float x, float y, SDL_PRINTF_FORMAT_STRING const char *fmt, ...) SDL_PRINTF_VARARG_FUNC(4);


extern SDL_DECLSPEC bool SDLCALL SDL_SetDefaultTextureScaleMode(SDL_Renderer *renderer, SDL_ScaleMode scale_mode);


extern SDL_DECLSPEC bool SDLCALL SDL_GetDefaultTextureScaleMode(SDL_Renderer *renderer, SDL_ScaleMode *scale_mode);


typedef struct SDL_GPURenderStateCreateInfo
{
    SDL_GPUShader *fragment_shader; 

    Sint32 num_sampler_bindings;    
    const SDL_GPUTextureSamplerBinding *sampler_bindings;   

    Sint32 num_storage_textures;    
    SDL_GPUTexture *const *storage_textures;    

    Sint32 num_storage_buffers;     
    SDL_GPUBuffer *const *storage_buffers;      

    SDL_PropertiesID props;         
} SDL_GPURenderStateCreateInfo;


typedef struct SDL_GPURenderState SDL_GPURenderState;


extern SDL_DECLSPEC SDL_GPURenderState * SDLCALL SDL_CreateGPURenderState(SDL_Renderer *renderer, const SDL_GPURenderStateCreateInfo *createinfo);


extern SDL_DECLSPEC bool SDLCALL SDL_SetGPURenderStateFragmentUniforms(SDL_GPURenderState *state, Uint32 slot_index, const void *data, Uint32 length);


extern SDL_DECLSPEC bool SDLCALL SDL_SetGPURenderState(SDL_Renderer *renderer, SDL_GPURenderState *state);


extern SDL_DECLSPEC void SDLCALL SDL_DestroyGPURenderState(SDL_GPURenderState *state);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
