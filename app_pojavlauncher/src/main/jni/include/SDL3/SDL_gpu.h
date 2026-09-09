





#ifndef SDL_gpu_h_
#define SDL_gpu_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_pixels.h>
#include <SDL3/SDL_properties.h>
#include <SDL3/SDL_rect.h>
#include <SDL3/SDL_surface.h>
#include <SDL3/SDL_video.h>

#include <SDL3/SDL_begin_code.h>
#ifdef __cplusplus
extern "C" {
#endif 




typedef struct SDL_GPUDevice SDL_GPUDevice;


typedef struct SDL_GPUBuffer SDL_GPUBuffer;


typedef struct SDL_GPUTransferBuffer SDL_GPUTransferBuffer;


typedef struct SDL_GPUTexture SDL_GPUTexture;


typedef struct SDL_GPUSampler SDL_GPUSampler;


typedef struct SDL_GPUShader SDL_GPUShader;


typedef struct SDL_GPUComputePipeline SDL_GPUComputePipeline;


typedef struct SDL_GPUGraphicsPipeline SDL_GPUGraphicsPipeline;


typedef struct SDL_GPUCommandBuffer SDL_GPUCommandBuffer;


typedef struct SDL_GPURenderPass SDL_GPURenderPass;


typedef struct SDL_GPUComputePass SDL_GPUComputePass;


typedef struct SDL_GPUCopyPass SDL_GPUCopyPass;


typedef struct SDL_GPUFence SDL_GPUFence;


typedef enum SDL_GPUPrimitiveType
{
    SDL_GPU_PRIMITIVETYPE_TRIANGLELIST,  
    SDL_GPU_PRIMITIVETYPE_TRIANGLESTRIP, 
    SDL_GPU_PRIMITIVETYPE_LINELIST,      
    SDL_GPU_PRIMITIVETYPE_LINESTRIP,     
    SDL_GPU_PRIMITIVETYPE_POINTLIST      
} SDL_GPUPrimitiveType;


typedef enum SDL_GPULoadOp
{
    SDL_GPU_LOADOP_LOAD,      
    SDL_GPU_LOADOP_CLEAR,     
    SDL_GPU_LOADOP_DONT_CARE  
} SDL_GPULoadOp;


typedef enum SDL_GPUStoreOp
{
    SDL_GPU_STOREOP_STORE,             
    SDL_GPU_STOREOP_DONT_CARE,         
    SDL_GPU_STOREOP_RESOLVE,           
    SDL_GPU_STOREOP_RESOLVE_AND_STORE  
} SDL_GPUStoreOp;


typedef enum SDL_GPUIndexElementSize
{
    SDL_GPU_INDEXELEMENTSIZE_16BIT, 
    SDL_GPU_INDEXELEMENTSIZE_32BIT  
} SDL_GPUIndexElementSize;


typedef enum SDL_GPUTextureFormat
{
    SDL_GPU_TEXTUREFORMAT_INVALID,

    
    SDL_GPU_TEXTUREFORMAT_A8_UNORM,
    SDL_GPU_TEXTUREFORMAT_R8_UNORM,
    SDL_GPU_TEXTUREFORMAT_R8G8_UNORM,
    SDL_GPU_TEXTUREFORMAT_R8G8B8A8_UNORM,
    SDL_GPU_TEXTUREFORMAT_R16_UNORM,
    SDL_GPU_TEXTUREFORMAT_R16G16_UNORM,
    SDL_GPU_TEXTUREFORMAT_R16G16B16A16_UNORM,
    SDL_GPU_TEXTUREFORMAT_R10G10B10A2_UNORM,
    SDL_GPU_TEXTUREFORMAT_B5G6R5_UNORM,
    SDL_GPU_TEXTUREFORMAT_B5G5R5A1_UNORM,
    SDL_GPU_TEXTUREFORMAT_B4G4R4A4_UNORM,
    SDL_GPU_TEXTUREFORMAT_B8G8R8A8_UNORM,
    
    SDL_GPU_TEXTUREFORMAT_BC1_RGBA_UNORM,
    SDL_GPU_TEXTUREFORMAT_BC2_RGBA_UNORM,
    SDL_GPU_TEXTUREFORMAT_BC3_RGBA_UNORM,
    SDL_GPU_TEXTUREFORMAT_BC4_R_UNORM,
    SDL_GPU_TEXTUREFORMAT_BC5_RG_UNORM,
    SDL_GPU_TEXTUREFORMAT_BC7_RGBA_UNORM,
    
    SDL_GPU_TEXTUREFORMAT_BC6H_RGB_FLOAT,
    
    SDL_GPU_TEXTUREFORMAT_BC6H_RGB_UFLOAT,
    
    SDL_GPU_TEXTUREFORMAT_R8_SNORM,
    SDL_GPU_TEXTUREFORMAT_R8G8_SNORM,
    SDL_GPU_TEXTUREFORMAT_R8G8B8A8_SNORM,
    SDL_GPU_TEXTUREFORMAT_R16_SNORM,
    SDL_GPU_TEXTUREFORMAT_R16G16_SNORM,
    SDL_GPU_TEXTUREFORMAT_R16G16B16A16_SNORM,
    
    SDL_GPU_TEXTUREFORMAT_R16_FLOAT,
    SDL_GPU_TEXTUREFORMAT_R16G16_FLOAT,
    SDL_GPU_TEXTUREFORMAT_R16G16B16A16_FLOAT,
    SDL_GPU_TEXTUREFORMAT_R32_FLOAT,
    SDL_GPU_TEXTUREFORMAT_R32G32_FLOAT,
    SDL_GPU_TEXTUREFORMAT_R32G32B32A32_FLOAT,
    
    SDL_GPU_TEXTUREFORMAT_R11G11B10_UFLOAT,
    
    SDL_GPU_TEXTUREFORMAT_R8_UINT,
    SDL_GPU_TEXTUREFORMAT_R8G8_UINT,
    SDL_GPU_TEXTUREFORMAT_R8G8B8A8_UINT,
    SDL_GPU_TEXTUREFORMAT_R16_UINT,
    SDL_GPU_TEXTUREFORMAT_R16G16_UINT,
    SDL_GPU_TEXTUREFORMAT_R16G16B16A16_UINT,
    SDL_GPU_TEXTUREFORMAT_R32_UINT,
    SDL_GPU_TEXTUREFORMAT_R32G32_UINT,
    SDL_GPU_TEXTUREFORMAT_R32G32B32A32_UINT,
    
    SDL_GPU_TEXTUREFORMAT_R8_INT,
    SDL_GPU_TEXTUREFORMAT_R8G8_INT,
    SDL_GPU_TEXTUREFORMAT_R8G8B8A8_INT,
    SDL_GPU_TEXTUREFORMAT_R16_INT,
    SDL_GPU_TEXTUREFORMAT_R16G16_INT,
    SDL_GPU_TEXTUREFORMAT_R16G16B16A16_INT,
    SDL_GPU_TEXTUREFORMAT_R32_INT,
    SDL_GPU_TEXTUREFORMAT_R32G32_INT,
    SDL_GPU_TEXTUREFORMAT_R32G32B32A32_INT,
    
    SDL_GPU_TEXTUREFORMAT_R8G8B8A8_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_B8G8R8A8_UNORM_SRGB,
    
    SDL_GPU_TEXTUREFORMAT_BC1_RGBA_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_BC2_RGBA_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_BC3_RGBA_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_BC7_RGBA_UNORM_SRGB,
    
    SDL_GPU_TEXTUREFORMAT_D16_UNORM,
    SDL_GPU_TEXTUREFORMAT_D24_UNORM,
    SDL_GPU_TEXTUREFORMAT_D32_FLOAT,
    SDL_GPU_TEXTUREFORMAT_D24_UNORM_S8_UINT,
    SDL_GPU_TEXTUREFORMAT_D32_FLOAT_S8_UINT,
    
    SDL_GPU_TEXTUREFORMAT_ASTC_4x4_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x4_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x5_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x5_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x6_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x5_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x6_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x8_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x5_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x6_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x8_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x10_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x10_UNORM,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x12_UNORM,
    
    SDL_GPU_TEXTUREFORMAT_ASTC_4x4_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x4_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x5_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x5_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x6_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x5_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x6_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x8_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x5_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x6_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x8_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x10_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x10_UNORM_SRGB,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x12_UNORM_SRGB,
    
    SDL_GPU_TEXTUREFORMAT_ASTC_4x4_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x4_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_5x5_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x5_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_6x6_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x5_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x6_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_8x8_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x5_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x6_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x8_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_10x10_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x10_FLOAT,
    SDL_GPU_TEXTUREFORMAT_ASTC_12x12_FLOAT
} SDL_GPUTextureFormat;


typedef Uint32 SDL_GPUTextureUsageFlags;

#define SDL_GPU_TEXTUREUSAGE_SAMPLER                                 (1u << 0) 
#define SDL_GPU_TEXTUREUSAGE_COLOR_TARGET                            (1u << 1) 
#define SDL_GPU_TEXTUREUSAGE_DEPTH_STENCIL_TARGET                    (1u << 2) 
#define SDL_GPU_TEXTUREUSAGE_GRAPHICS_STORAGE_READ                   (1u << 3) 
#define SDL_GPU_TEXTUREUSAGE_COMPUTE_STORAGE_READ                    (1u << 4) 
#define SDL_GPU_TEXTUREUSAGE_COMPUTE_STORAGE_WRITE                   (1u << 5) 
#define SDL_GPU_TEXTUREUSAGE_COMPUTE_STORAGE_SIMULTANEOUS_READ_WRITE (1u << 6) 


typedef enum SDL_GPUTextureType
{
    SDL_GPU_TEXTURETYPE_2D,         
    SDL_GPU_TEXTURETYPE_2D_ARRAY,   
    SDL_GPU_TEXTURETYPE_3D,         
    SDL_GPU_TEXTURETYPE_CUBE,       
    SDL_GPU_TEXTURETYPE_CUBE_ARRAY  
} SDL_GPUTextureType;


typedef enum SDL_GPUSampleCount
{
    SDL_GPU_SAMPLECOUNT_1,  
    SDL_GPU_SAMPLECOUNT_2,  
    SDL_GPU_SAMPLECOUNT_4,  
    SDL_GPU_SAMPLECOUNT_8   
} SDL_GPUSampleCount;



typedef enum SDL_GPUCubeMapFace
{
    SDL_GPU_CUBEMAPFACE_POSITIVEX,
    SDL_GPU_CUBEMAPFACE_NEGATIVEX,
    SDL_GPU_CUBEMAPFACE_POSITIVEY,
    SDL_GPU_CUBEMAPFACE_NEGATIVEY,
    SDL_GPU_CUBEMAPFACE_POSITIVEZ,
    SDL_GPU_CUBEMAPFACE_NEGATIVEZ
} SDL_GPUCubeMapFace;


typedef Uint32 SDL_GPUBufferUsageFlags;

#define SDL_GPU_BUFFERUSAGE_VERTEX                                  (1u << 0) 
#define SDL_GPU_BUFFERUSAGE_INDEX                                   (1u << 1) 
#define SDL_GPU_BUFFERUSAGE_INDIRECT                                (1u << 2) 
#define SDL_GPU_BUFFERUSAGE_GRAPHICS_STORAGE_READ                   (1u << 3) 
#define SDL_GPU_BUFFERUSAGE_COMPUTE_STORAGE_READ                    (1u << 4) 
#define SDL_GPU_BUFFERUSAGE_COMPUTE_STORAGE_WRITE                   (1u << 5) 


typedef enum SDL_GPUTransferBufferUsage
{
    SDL_GPU_TRANSFERBUFFERUSAGE_UPLOAD,
    SDL_GPU_TRANSFERBUFFERUSAGE_DOWNLOAD
} SDL_GPUTransferBufferUsage;


typedef enum SDL_GPUShaderStage
{
    SDL_GPU_SHADERSTAGE_VERTEX,
    SDL_GPU_SHADERSTAGE_FRAGMENT
} SDL_GPUShaderStage;


typedef Uint32 SDL_GPUShaderFormat;

#define SDL_GPU_SHADERFORMAT_INVALID  0
#define SDL_GPU_SHADERFORMAT_PRIVATE  (1u << 0) 
#define SDL_GPU_SHADERFORMAT_SPIRV    (1u << 1) 
#define SDL_GPU_SHADERFORMAT_DXBC     (1u << 2) 
#define SDL_GPU_SHADERFORMAT_DXIL     (1u << 3) 
#define SDL_GPU_SHADERFORMAT_MSL      (1u << 4) 
#define SDL_GPU_SHADERFORMAT_METALLIB (1u << 5) 


typedef enum SDL_GPUVertexElementFormat
{
    SDL_GPU_VERTEXELEMENTFORMAT_INVALID,

    
    SDL_GPU_VERTEXELEMENTFORMAT_INT,
    SDL_GPU_VERTEXELEMENTFORMAT_INT2,
    SDL_GPU_VERTEXELEMENTFORMAT_INT3,
    SDL_GPU_VERTEXELEMENTFORMAT_INT4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_UINT,
    SDL_GPU_VERTEXELEMENTFORMAT_UINT2,
    SDL_GPU_VERTEXELEMENTFORMAT_UINT3,
    SDL_GPU_VERTEXELEMENTFORMAT_UINT4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_FLOAT,
    SDL_GPU_VERTEXELEMENTFORMAT_FLOAT2,
    SDL_GPU_VERTEXELEMENTFORMAT_FLOAT3,
    SDL_GPU_VERTEXELEMENTFORMAT_FLOAT4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_BYTE2,
    SDL_GPU_VERTEXELEMENTFORMAT_BYTE4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_UBYTE2,
    SDL_GPU_VERTEXELEMENTFORMAT_UBYTE4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_BYTE2_NORM,
    SDL_GPU_VERTEXELEMENTFORMAT_BYTE4_NORM,

    
    SDL_GPU_VERTEXELEMENTFORMAT_UBYTE2_NORM,
    SDL_GPU_VERTEXELEMENTFORMAT_UBYTE4_NORM,

    
    SDL_GPU_VERTEXELEMENTFORMAT_SHORT2,
    SDL_GPU_VERTEXELEMENTFORMAT_SHORT4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_USHORT2,
    SDL_GPU_VERTEXELEMENTFORMAT_USHORT4,

    
    SDL_GPU_VERTEXELEMENTFORMAT_SHORT2_NORM,
    SDL_GPU_VERTEXELEMENTFORMAT_SHORT4_NORM,

    
    SDL_GPU_VERTEXELEMENTFORMAT_USHORT2_NORM,
    SDL_GPU_VERTEXELEMENTFORMAT_USHORT4_NORM,

    
    SDL_GPU_VERTEXELEMENTFORMAT_HALF2,
    SDL_GPU_VERTEXELEMENTFORMAT_HALF4
} SDL_GPUVertexElementFormat;


typedef enum SDL_GPUVertexInputRate
{
    SDL_GPU_VERTEXINPUTRATE_VERTEX,   
    SDL_GPU_VERTEXINPUTRATE_INSTANCE  
} SDL_GPUVertexInputRate;


typedef enum SDL_GPUFillMode
{
    SDL_GPU_FILLMODE_FILL,  
    SDL_GPU_FILLMODE_LINE   
} SDL_GPUFillMode;


typedef enum SDL_GPUCullMode
{
    SDL_GPU_CULLMODE_NONE,   
    SDL_GPU_CULLMODE_FRONT,  
    SDL_GPU_CULLMODE_BACK    
} SDL_GPUCullMode;


typedef enum SDL_GPUFrontFace
{
    SDL_GPU_FRONTFACE_COUNTER_CLOCKWISE,  
    SDL_GPU_FRONTFACE_CLOCKWISE           
} SDL_GPUFrontFace;


typedef enum SDL_GPUCompareOp
{
    SDL_GPU_COMPAREOP_INVALID,
    SDL_GPU_COMPAREOP_NEVER,             
    SDL_GPU_COMPAREOP_LESS,              
    SDL_GPU_COMPAREOP_EQUAL,             
    SDL_GPU_COMPAREOP_LESS_OR_EQUAL,     
    SDL_GPU_COMPAREOP_GREATER,           
    SDL_GPU_COMPAREOP_NOT_EQUAL,         
    SDL_GPU_COMPAREOP_GREATER_OR_EQUAL,  
    SDL_GPU_COMPAREOP_ALWAYS             
} SDL_GPUCompareOp;


typedef enum SDL_GPUStencilOp
{
    SDL_GPU_STENCILOP_INVALID,
    SDL_GPU_STENCILOP_KEEP,                 
    SDL_GPU_STENCILOP_ZERO,                 
    SDL_GPU_STENCILOP_REPLACE,              
    SDL_GPU_STENCILOP_INCREMENT_AND_CLAMP,  
    SDL_GPU_STENCILOP_DECREMENT_AND_CLAMP,  
    SDL_GPU_STENCILOP_INVERT,               
    SDL_GPU_STENCILOP_INCREMENT_AND_WRAP,   
    SDL_GPU_STENCILOP_DECREMENT_AND_WRAP    
} SDL_GPUStencilOp;


typedef enum SDL_GPUBlendOp
{
    SDL_GPU_BLENDOP_INVALID,
    SDL_GPU_BLENDOP_ADD,               
    SDL_GPU_BLENDOP_SUBTRACT,          
    SDL_GPU_BLENDOP_REVERSE_SUBTRACT,  
    SDL_GPU_BLENDOP_MIN,               
    SDL_GPU_BLENDOP_MAX                
} SDL_GPUBlendOp;


typedef enum SDL_GPUBlendFactor
{
    SDL_GPU_BLENDFACTOR_INVALID,
    SDL_GPU_BLENDFACTOR_ZERO,                      
    SDL_GPU_BLENDFACTOR_ONE,                       
    SDL_GPU_BLENDFACTOR_SRC_COLOR,                 
    SDL_GPU_BLENDFACTOR_ONE_MINUS_SRC_COLOR,       
    SDL_GPU_BLENDFACTOR_DST_COLOR,                 
    SDL_GPU_BLENDFACTOR_ONE_MINUS_DST_COLOR,       
    SDL_GPU_BLENDFACTOR_SRC_ALPHA,                 
    SDL_GPU_BLENDFACTOR_ONE_MINUS_SRC_ALPHA,       
    SDL_GPU_BLENDFACTOR_DST_ALPHA,                 
    SDL_GPU_BLENDFACTOR_ONE_MINUS_DST_ALPHA,       
    SDL_GPU_BLENDFACTOR_CONSTANT_COLOR,            
    SDL_GPU_BLENDFACTOR_ONE_MINUS_CONSTANT_COLOR,  
    SDL_GPU_BLENDFACTOR_SRC_ALPHA_SATURATE         
} SDL_GPUBlendFactor;


typedef Uint8 SDL_GPUColorComponentFlags;

#define SDL_GPU_COLORCOMPONENT_R (1u << 0) 
#define SDL_GPU_COLORCOMPONENT_G (1u << 1) 
#define SDL_GPU_COLORCOMPONENT_B (1u << 2) 
#define SDL_GPU_COLORCOMPONENT_A (1u << 3) 


typedef enum SDL_GPUFilter
{
    SDL_GPU_FILTER_NEAREST,  
    SDL_GPU_FILTER_LINEAR    
} SDL_GPUFilter;


typedef enum SDL_GPUSamplerMipmapMode
{
    SDL_GPU_SAMPLERMIPMAPMODE_NEAREST,  
    SDL_GPU_SAMPLERMIPMAPMODE_LINEAR    
} SDL_GPUSamplerMipmapMode;


typedef enum SDL_GPUSamplerAddressMode
{
    SDL_GPU_SAMPLERADDRESSMODE_REPEAT,           
    SDL_GPU_SAMPLERADDRESSMODE_MIRRORED_REPEAT,  
    SDL_GPU_SAMPLERADDRESSMODE_CLAMP_TO_EDGE     
} SDL_GPUSamplerAddressMode;


typedef enum SDL_GPUPresentMode
{
    SDL_GPU_PRESENTMODE_VSYNC,
    SDL_GPU_PRESENTMODE_IMMEDIATE,
    SDL_GPU_PRESENTMODE_MAILBOX
} SDL_GPUPresentMode;


typedef enum SDL_GPUSwapchainComposition
{
    SDL_GPU_SWAPCHAINCOMPOSITION_SDR,
    SDL_GPU_SWAPCHAINCOMPOSITION_SDR_LINEAR,
    SDL_GPU_SWAPCHAINCOMPOSITION_HDR_EXTENDED_LINEAR,
    SDL_GPU_SWAPCHAINCOMPOSITION_HDR10_ST2084
} SDL_GPUSwapchainComposition;




typedef struct SDL_GPUViewport
{
    float x;          
    float y;          
    float w;          
    float h;          
    float min_depth;  
    float max_depth;  
} SDL_GPUViewport;


typedef struct SDL_GPUTextureTransferInfo
{
    SDL_GPUTransferBuffer *transfer_buffer;  
    Uint32 offset;                           
    Uint32 pixels_per_row;                   
    Uint32 rows_per_layer;                   
} SDL_GPUTextureTransferInfo;


typedef struct SDL_GPUTransferBufferLocation
{
    SDL_GPUTransferBuffer *transfer_buffer;  
    Uint32 offset;                           
} SDL_GPUTransferBufferLocation;


typedef struct SDL_GPUTextureLocation
{
    SDL_GPUTexture *texture;  
    Uint32 mip_level;         
    Uint32 layer;             
    Uint32 x;                 
    Uint32 y;                 
    Uint32 z;                 
} SDL_GPUTextureLocation;


typedef struct SDL_GPUTextureRegion
{
    SDL_GPUTexture *texture;  
    Uint32 mip_level;         
    Uint32 layer;             
    Uint32 x;                 
    Uint32 y;                 
    Uint32 z;                 
    Uint32 w;                 
    Uint32 h;                 
    Uint32 d;                 
} SDL_GPUTextureRegion;


typedef struct SDL_GPUBlitRegion
{
    SDL_GPUTexture *texture;      
    Uint32 mip_level;             
    Uint32 layer_or_depth_plane;  
    Uint32 x;                     
    Uint32 y;                     
    Uint32 w;                     
    Uint32 h;                     
} SDL_GPUBlitRegion;


typedef struct SDL_GPUBufferLocation
{
    SDL_GPUBuffer *buffer;  
    Uint32 offset;          
} SDL_GPUBufferLocation;


typedef struct SDL_GPUBufferRegion
{
    SDL_GPUBuffer *buffer;  
    Uint32 offset;          
    Uint32 size;            
} SDL_GPUBufferRegion;


typedef struct SDL_GPUIndirectDrawCommand
{
    Uint32 num_vertices;   
    Uint32 num_instances;  
    Uint32 first_vertex;   
    Uint32 first_instance; 
} SDL_GPUIndirectDrawCommand;


typedef struct SDL_GPUIndexedIndirectDrawCommand
{
    Uint32 num_indices;    
    Uint32 num_instances;  
    Uint32 first_index;    
    Sint32 vertex_offset;  
    Uint32 first_instance; 
} SDL_GPUIndexedIndirectDrawCommand;


typedef struct SDL_GPUIndirectDispatchCommand
{
    Uint32 groupcount_x;  
    Uint32 groupcount_y;  
    Uint32 groupcount_z;  
} SDL_GPUIndirectDispatchCommand;




typedef struct SDL_GPUSamplerCreateInfo
{
    SDL_GPUFilter min_filter;                  
    SDL_GPUFilter mag_filter;                  
    SDL_GPUSamplerMipmapMode mipmap_mode;      
    SDL_GPUSamplerAddressMode address_mode_u;  
    SDL_GPUSamplerAddressMode address_mode_v;  
    SDL_GPUSamplerAddressMode address_mode_w;  
    float mip_lod_bias;                        
    float max_anisotropy;                      
    SDL_GPUCompareOp compare_op;               
    float min_lod;                             
    float max_lod;                             
    bool enable_anisotropy;                    
    bool enable_compare;                       
    Uint8 padding1;
    Uint8 padding2;

    SDL_PropertiesID props;                    
} SDL_GPUSamplerCreateInfo;


typedef struct SDL_GPUVertexBufferDescription
{
    Uint32 slot;                        
    Uint32 pitch;                       
    SDL_GPUVertexInputRate input_rate;  
    Uint32 instance_step_rate;          
} SDL_GPUVertexBufferDescription;


typedef struct SDL_GPUVertexAttribute
{
    Uint32 location;                    
    Uint32 buffer_slot;                 
    SDL_GPUVertexElementFormat format;  
    Uint32 offset;                      
} SDL_GPUVertexAttribute;


typedef struct SDL_GPUVertexInputState
{
    const SDL_GPUVertexBufferDescription *vertex_buffer_descriptions; 
    Uint32 num_vertex_buffers;                                        
    const SDL_GPUVertexAttribute *vertex_attributes;                  
    Uint32 num_vertex_attributes;                                     
} SDL_GPUVertexInputState;


typedef struct SDL_GPUStencilOpState
{
    SDL_GPUStencilOp fail_op;        
    SDL_GPUStencilOp pass_op;        
    SDL_GPUStencilOp depth_fail_op;  
    SDL_GPUCompareOp compare_op;     
} SDL_GPUStencilOpState;


typedef struct SDL_GPUColorTargetBlendState
{
    SDL_GPUBlendFactor src_color_blendfactor;     
    SDL_GPUBlendFactor dst_color_blendfactor;     
    SDL_GPUBlendOp color_blend_op;                
    SDL_GPUBlendFactor src_alpha_blendfactor;     
    SDL_GPUBlendFactor dst_alpha_blendfactor;     
    SDL_GPUBlendOp alpha_blend_op;                
    SDL_GPUColorComponentFlags color_write_mask;  
    bool enable_blend;                            
    bool enable_color_write_mask;                 
    Uint8 padding1;
    Uint8 padding2;
} SDL_GPUColorTargetBlendState;



typedef struct SDL_GPUShaderCreateInfo
{
    size_t code_size;             
    const Uint8 *code;            
    const char *entrypoint;       
    SDL_GPUShaderFormat format;   
    SDL_GPUShaderStage stage;     
    Uint32 num_samplers;          
    Uint32 num_storage_textures;  
    Uint32 num_storage_buffers;   
    Uint32 num_uniform_buffers;   

    SDL_PropertiesID props;       
} SDL_GPUShaderCreateInfo;


typedef struct SDL_GPUTextureCreateInfo
{
    SDL_GPUTextureType type;          
    SDL_GPUTextureFormat format;      
    SDL_GPUTextureUsageFlags usage;   
    Uint32 width;                     
    Uint32 height;                    
    Uint32 layer_count_or_depth;      
    Uint32 num_levels;                
    SDL_GPUSampleCount sample_count;  

    SDL_PropertiesID props;           
} SDL_GPUTextureCreateInfo;


typedef struct SDL_GPUBufferCreateInfo
{
    SDL_GPUBufferUsageFlags usage;  
    Uint32 size;                    

    SDL_PropertiesID props;         
} SDL_GPUBufferCreateInfo;


typedef struct SDL_GPUTransferBufferCreateInfo
{
    SDL_GPUTransferBufferUsage usage;  
    Uint32 size;                       

    SDL_PropertiesID props;            
} SDL_GPUTransferBufferCreateInfo;




typedef struct SDL_GPURasterizerState
{
    SDL_GPUFillMode fill_mode;         
    SDL_GPUCullMode cull_mode;         
    SDL_GPUFrontFace front_face;       
    float depth_bias_constant_factor;  
    float depth_bias_clamp;            
    float depth_bias_slope_factor;     
    bool enable_depth_bias;            
    bool enable_depth_clip;            
    Uint8 padding1;
    Uint8 padding2;
} SDL_GPURasterizerState;


typedef struct SDL_GPUMultisampleState
{
    SDL_GPUSampleCount sample_count;  
    Uint32 sample_mask;               
    bool enable_mask;                 
    bool enable_alpha_to_coverage;    
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUMultisampleState;


typedef struct SDL_GPUDepthStencilState
{
    SDL_GPUCompareOp compare_op;                
    SDL_GPUStencilOpState back_stencil_state;   
    SDL_GPUStencilOpState front_stencil_state;  
    Uint8 compare_mask;                         
    Uint8 write_mask;                           
    bool enable_depth_test;                     
    bool enable_depth_write;                    
    bool enable_stencil_test;                   
    Uint8 padding1;
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUDepthStencilState;


typedef struct SDL_GPUColorTargetDescription
{
    SDL_GPUTextureFormat format;               
    SDL_GPUColorTargetBlendState blend_state;  
} SDL_GPUColorTargetDescription;


typedef struct SDL_GPUGraphicsPipelineTargetInfo
{
    const SDL_GPUColorTargetDescription *color_target_descriptions;  
    Uint32 num_color_targets;                                        
    SDL_GPUTextureFormat depth_stencil_format;                       
    bool has_depth_stencil_target;                                   
    Uint8 padding1;
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUGraphicsPipelineTargetInfo;


typedef struct SDL_GPUGraphicsPipelineCreateInfo
{
    SDL_GPUShader *vertex_shader;                   
    SDL_GPUShader *fragment_shader;                 
    SDL_GPUVertexInputState vertex_input_state;     
    SDL_GPUPrimitiveType primitive_type;            
    SDL_GPURasterizerState rasterizer_state;        
    SDL_GPUMultisampleState multisample_state;      
    SDL_GPUDepthStencilState depth_stencil_state;   
    SDL_GPUGraphicsPipelineTargetInfo target_info;  

    SDL_PropertiesID props;                         
} SDL_GPUGraphicsPipelineCreateInfo;


typedef struct SDL_GPUComputePipelineCreateInfo
{
    size_t code_size;                       
    const Uint8 *code;                      
    const char *entrypoint;                 
    SDL_GPUShaderFormat format;             
    Uint32 num_samplers;                    
    Uint32 num_readonly_storage_textures;   
    Uint32 num_readonly_storage_buffers;    
    Uint32 num_readwrite_storage_textures;  
    Uint32 num_readwrite_storage_buffers;   
    Uint32 num_uniform_buffers;             
    Uint32 threadcount_x;                   
    Uint32 threadcount_y;                   
    Uint32 threadcount_z;                   

    SDL_PropertiesID props;                 
} SDL_GPUComputePipelineCreateInfo;


typedef struct SDL_GPUColorTargetInfo
{
    SDL_GPUTexture *texture;         
    Uint32 mip_level;                
    Uint32 layer_or_depth_plane;     
    SDL_FColor clear_color;          
    SDL_GPULoadOp load_op;           
    SDL_GPUStoreOp store_op;         
    SDL_GPUTexture *resolve_texture; 
    Uint32 resolve_mip_level;        
    Uint32 resolve_layer;            
    bool cycle;                      
    bool cycle_resolve_texture;      
    Uint8 padding1;
    Uint8 padding2;
} SDL_GPUColorTargetInfo;


typedef struct SDL_GPUDepthStencilTargetInfo
{
    SDL_GPUTexture *texture;               
    float clear_depth;                     
    SDL_GPULoadOp load_op;                 
    SDL_GPUStoreOp store_op;               
    SDL_GPULoadOp stencil_load_op;         
    SDL_GPUStoreOp stencil_store_op;       
    bool cycle;                            
    Uint8 clear_stencil;                   
    Uint8 mip_level;                       
    Uint8 layer;                           
} SDL_GPUDepthStencilTargetInfo;


typedef struct SDL_GPUBlitInfo {
    SDL_GPUBlitRegion source;       
    SDL_GPUBlitRegion destination;  
    SDL_GPULoadOp load_op;          
    SDL_FColor clear_color;         
    SDL_FlipMode flip_mode;         
    SDL_GPUFilter filter;           
    bool cycle;                     
    Uint8 padding1;
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUBlitInfo;




typedef struct SDL_GPUBufferBinding
{
    SDL_GPUBuffer *buffer;  
    Uint32 offset;          
} SDL_GPUBufferBinding;


typedef struct SDL_GPUTextureSamplerBinding
{
    SDL_GPUTexture *texture;  
    SDL_GPUSampler *sampler;  
} SDL_GPUTextureSamplerBinding;


typedef struct SDL_GPUStorageBufferReadWriteBinding
{
    SDL_GPUBuffer *buffer;  
    bool cycle;             
    Uint8 padding1;
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUStorageBufferReadWriteBinding;


typedef struct SDL_GPUStorageTextureReadWriteBinding
{
    SDL_GPUTexture *texture;  
    Uint32 mip_level;         
    Uint32 layer;             
    bool cycle;               
    Uint8 padding1;
    Uint8 padding2;
    Uint8 padding3;
} SDL_GPUStorageTextureReadWriteBinding;






extern SDL_DECLSPEC bool SDLCALL SDL_GPUSupportsShaderFormats(
    SDL_GPUShaderFormat format_flags,
    const char *name);


extern SDL_DECLSPEC bool SDLCALL SDL_GPUSupportsProperties(
    SDL_PropertiesID props);


extern SDL_DECLSPEC SDL_GPUDevice * SDLCALL SDL_CreateGPUDevice(
    SDL_GPUShaderFormat format_flags,
    bool debug_mode,
    const char *name);


extern SDL_DECLSPEC SDL_GPUDevice * SDLCALL SDL_CreateGPUDeviceWithProperties(
    SDL_PropertiesID props);

#define SDL_PROP_GPU_DEVICE_CREATE_DEBUGMODE_BOOLEAN                            "SDL.gpu.device.create.debugmode"
#define SDL_PROP_GPU_DEVICE_CREATE_PREFERLOWPOWER_BOOLEAN                       "SDL.gpu.device.create.preferlowpower"
#define SDL_PROP_GPU_DEVICE_CREATE_VERBOSE_BOOLEAN                              "SDL.gpu.device.create.verbose"
#define SDL_PROP_GPU_DEVICE_CREATE_NAME_STRING                                  "SDL.gpu.device.create.name"
#define SDL_PROP_GPU_DEVICE_CREATE_FEATURE_CLIP_DISTANCE_BOOLEAN                "SDL.gpu.device.create.feature.clip_distance"
#define SDL_PROP_GPU_DEVICE_CREATE_FEATURE_DEPTH_CLAMPING_BOOLEAN               "SDL.gpu.device.create.feature.depth_clamping"
#define SDL_PROP_GPU_DEVICE_CREATE_FEATURE_INDIRECT_DRAW_FIRST_INSTANCE_BOOLEAN "SDL.gpu.device.create.feature.indirect_draw_first_instance"
#define SDL_PROP_GPU_DEVICE_CREATE_FEATURE_ANISOTROPY_BOOLEAN                   "SDL.gpu.device.create.feature.anisotropy"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_PRIVATE_BOOLEAN                      "SDL.gpu.device.create.shaders.private"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_SPIRV_BOOLEAN                        "SDL.gpu.device.create.shaders.spirv"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_DXBC_BOOLEAN                         "SDL.gpu.device.create.shaders.dxbc"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_DXIL_BOOLEAN                         "SDL.gpu.device.create.shaders.dxil"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_MSL_BOOLEAN                          "SDL.gpu.device.create.shaders.msl"
#define SDL_PROP_GPU_DEVICE_CREATE_SHADERS_METALLIB_BOOLEAN                     "SDL.gpu.device.create.shaders.metallib"
#define SDL_PROP_GPU_DEVICE_CREATE_D3D12_ALLOW_FEWER_RESOURCE_SLOTS_BOOLEAN     "SDL.gpu.device.create.d3d12.allowtier1resourcebinding"
#define SDL_PROP_GPU_DEVICE_CREATE_D3D12_SEMANTIC_NAME_STRING                   "SDL.gpu.device.create.d3d12.semantic"
#define SDL_PROP_GPU_DEVICE_CREATE_D3D12_AGILITY_SDK_VERSION_NUMBER             "SDL.gpu.device.create.d3d12.agility_sdk_version"
#define SDL_PROP_GPU_DEVICE_CREATE_D3D12_AGILITY_SDK_PATH_STRING                "SDL.gpu.device.create.d3d12.agility_sdk_path"
#define SDL_PROP_GPU_DEVICE_CREATE_VULKAN_REQUIRE_HARDWARE_ACCELERATION_BOOLEAN "SDL.gpu.device.create.vulkan.requirehardwareacceleration"
#define SDL_PROP_GPU_DEVICE_CREATE_VULKAN_OPTIONS_POINTER                       "SDL.gpu.device.create.vulkan.options"
#define SDL_PROP_GPU_DEVICE_CREATE_METAL_ALLOW_MACFAMILY1_BOOLEAN               "SDL.gpu.device.create.metal.allowmacfamily1"



typedef struct SDL_GPUVulkanOptions
{
    Uint32 vulkan_api_version; 
    void *feature_list; 
	void *vulkan_10_physical_device_features; 
	Uint32 device_extension_count; 
	const char **device_extension_names; 
	Uint32 instance_extension_count; 
	const char **instance_extension_names; 
} SDL_GPUVulkanOptions;


extern SDL_DECLSPEC void SDLCALL SDL_DestroyGPUDevice(SDL_GPUDevice *device);


extern SDL_DECLSPEC int SDLCALL SDL_GetNumGPUDrivers(void);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetGPUDriver(int index);


extern SDL_DECLSPEC const char * SDLCALL SDL_GetGPUDeviceDriver(SDL_GPUDevice *device);


extern SDL_DECLSPEC SDL_GPUShaderFormat SDLCALL SDL_GetGPUShaderFormats(SDL_GPUDevice *device);


extern SDL_DECLSPEC SDL_PropertiesID SDLCALL SDL_GetGPUDeviceProperties(SDL_GPUDevice *device);

#define SDL_PROP_GPU_DEVICE_NAME_STRING               "SDL.gpu.device.name"
#define SDL_PROP_GPU_DEVICE_DRIVER_NAME_STRING        "SDL.gpu.device.driver_name"
#define SDL_PROP_GPU_DEVICE_DRIVER_VERSION_STRING     "SDL.gpu.device.driver_version"
#define SDL_PROP_GPU_DEVICE_DRIVER_INFO_STRING        "SDL.gpu.device.driver_info"





extern SDL_DECLSPEC SDL_GPUComputePipeline * SDLCALL SDL_CreateGPUComputePipeline(
    SDL_GPUDevice *device,
    const SDL_GPUComputePipelineCreateInfo *createinfo);

#define SDL_PROP_GPU_COMPUTEPIPELINE_CREATE_NAME_STRING "SDL.gpu.computepipeline.create.name"


extern SDL_DECLSPEC SDL_GPUGraphicsPipeline * SDLCALL SDL_CreateGPUGraphicsPipeline(
    SDL_GPUDevice *device,
    const SDL_GPUGraphicsPipelineCreateInfo *createinfo);

#define SDL_PROP_GPU_GRAPHICSPIPELINE_CREATE_NAME_STRING "SDL.gpu.graphicspipeline.create.name"


extern SDL_DECLSPEC SDL_GPUSampler * SDLCALL SDL_CreateGPUSampler(
    SDL_GPUDevice *device,
    const SDL_GPUSamplerCreateInfo *createinfo);

#define SDL_PROP_GPU_SAMPLER_CREATE_NAME_STRING "SDL.gpu.sampler.create.name"


extern SDL_DECLSPEC SDL_GPUShader * SDLCALL SDL_CreateGPUShader(
    SDL_GPUDevice *device,
    const SDL_GPUShaderCreateInfo *createinfo);

#define SDL_PROP_GPU_SHADER_CREATE_NAME_STRING "SDL.gpu.shader.create.name"


extern SDL_DECLSPEC SDL_GPUTexture * SDLCALL SDL_CreateGPUTexture(
    SDL_GPUDevice *device,
    const SDL_GPUTextureCreateInfo *createinfo);

#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_R_FLOAT         "SDL.gpu.texture.create.d3d12.clear.r"
#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_G_FLOAT         "SDL.gpu.texture.create.d3d12.clear.g"
#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_B_FLOAT         "SDL.gpu.texture.create.d3d12.clear.b"
#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_A_FLOAT         "SDL.gpu.texture.create.d3d12.clear.a"
#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_DEPTH_FLOAT     "SDL.gpu.texture.create.d3d12.clear.depth"
#define SDL_PROP_GPU_TEXTURE_CREATE_D3D12_CLEAR_STENCIL_NUMBER  "SDL.gpu.texture.create.d3d12.clear.stencil"
#define SDL_PROP_GPU_TEXTURE_CREATE_NAME_STRING                 "SDL.gpu.texture.create.name"


extern SDL_DECLSPEC SDL_GPUBuffer * SDLCALL SDL_CreateGPUBuffer(
    SDL_GPUDevice *device,
    const SDL_GPUBufferCreateInfo *createinfo);

#define SDL_PROP_GPU_BUFFER_CREATE_NAME_STRING "SDL.gpu.buffer.create.name"


extern SDL_DECLSPEC SDL_GPUTransferBuffer * SDLCALL SDL_CreateGPUTransferBuffer(
    SDL_GPUDevice *device,
    const SDL_GPUTransferBufferCreateInfo *createinfo);

#define SDL_PROP_GPU_TRANSFERBUFFER_CREATE_NAME_STRING "SDL.gpu.transferbuffer.create.name"




extern SDL_DECLSPEC void SDLCALL SDL_SetGPUBufferName(
    SDL_GPUDevice *device,
    SDL_GPUBuffer *buffer,
    const char *text);


extern SDL_DECLSPEC void SDLCALL SDL_SetGPUTextureName(
    SDL_GPUDevice *device,
    SDL_GPUTexture *texture,
    const char *text);


extern SDL_DECLSPEC void SDLCALL SDL_InsertGPUDebugLabel(
    SDL_GPUCommandBuffer *command_buffer,
    const char *text);


extern SDL_DECLSPEC void SDLCALL SDL_PushGPUDebugGroup(
    SDL_GPUCommandBuffer *command_buffer,
    const char *name);


extern SDL_DECLSPEC void SDLCALL SDL_PopGPUDebugGroup(
    SDL_GPUCommandBuffer *command_buffer);




extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUTexture(
    SDL_GPUDevice *device,
    SDL_GPUTexture *texture);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUSampler(
    SDL_GPUDevice *device,
    SDL_GPUSampler *sampler);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUBuffer(
    SDL_GPUDevice *device,
    SDL_GPUBuffer *buffer);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUTransferBuffer(
    SDL_GPUDevice *device,
    SDL_GPUTransferBuffer *transfer_buffer);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUComputePipeline(
    SDL_GPUDevice *device,
    SDL_GPUComputePipeline *compute_pipeline);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUShader(
    SDL_GPUDevice *device,
    SDL_GPUShader *shader);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUGraphicsPipeline(
    SDL_GPUDevice *device,
    SDL_GPUGraphicsPipeline *graphics_pipeline);


extern SDL_DECLSPEC SDL_GPUCommandBuffer * SDLCALL SDL_AcquireGPUCommandBuffer(
    SDL_GPUDevice *device);




extern SDL_DECLSPEC void SDLCALL SDL_PushGPUVertexUniformData(
    SDL_GPUCommandBuffer *command_buffer,
    Uint32 slot_index,
    const void *data,
    Uint32 length);


extern SDL_DECLSPEC void SDLCALL SDL_PushGPUFragmentUniformData(
    SDL_GPUCommandBuffer *command_buffer,
    Uint32 slot_index,
    const void *data,
    Uint32 length);


extern SDL_DECLSPEC void SDLCALL SDL_PushGPUComputeUniformData(
    SDL_GPUCommandBuffer *command_buffer,
    Uint32 slot_index,
    const void *data,
    Uint32 length);




extern SDL_DECLSPEC SDL_GPURenderPass * SDLCALL SDL_BeginGPURenderPass(
    SDL_GPUCommandBuffer *command_buffer,
    const SDL_GPUColorTargetInfo *color_target_infos,
    Uint32 num_color_targets,
    const SDL_GPUDepthStencilTargetInfo *depth_stencil_target_info);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUGraphicsPipeline(
    SDL_GPURenderPass *render_pass,
    SDL_GPUGraphicsPipeline *graphics_pipeline);


extern SDL_DECLSPEC void SDLCALL SDL_SetGPUViewport(
    SDL_GPURenderPass *render_pass,
    const SDL_GPUViewport *viewport);


extern SDL_DECLSPEC void SDLCALL SDL_SetGPUScissor(
    SDL_GPURenderPass *render_pass,
    const SDL_Rect *scissor);


extern SDL_DECLSPEC void SDLCALL SDL_SetGPUBlendConstants(
    SDL_GPURenderPass *render_pass,
    SDL_FColor blend_constants);


extern SDL_DECLSPEC void SDLCALL SDL_SetGPUStencilReference(
    SDL_GPURenderPass *render_pass,
    Uint8 reference);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUVertexBuffers(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    const SDL_GPUBufferBinding *bindings,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUIndexBuffer(
    SDL_GPURenderPass *render_pass,
    const SDL_GPUBufferBinding *binding,
    SDL_GPUIndexElementSize index_element_size);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUVertexSamplers(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    const SDL_GPUTextureSamplerBinding *texture_sampler_bindings,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUVertexStorageTextures(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    SDL_GPUTexture *const *storage_textures,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUVertexStorageBuffers(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    SDL_GPUBuffer *const *storage_buffers,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUFragmentSamplers(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    const SDL_GPUTextureSamplerBinding *texture_sampler_bindings,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUFragmentStorageTextures(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    SDL_GPUTexture *const *storage_textures,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUFragmentStorageBuffers(
    SDL_GPURenderPass *render_pass,
    Uint32 first_slot,
    SDL_GPUBuffer *const *storage_buffers,
    Uint32 num_bindings);




extern SDL_DECLSPEC void SDLCALL SDL_DrawGPUIndexedPrimitives(
    SDL_GPURenderPass *render_pass,
    Uint32 num_indices,
    Uint32 num_instances,
    Uint32 first_index,
    Sint32 vertex_offset,
    Uint32 first_instance);


extern SDL_DECLSPEC void SDLCALL SDL_DrawGPUPrimitives(
    SDL_GPURenderPass *render_pass,
    Uint32 num_vertices,
    Uint32 num_instances,
    Uint32 first_vertex,
    Uint32 first_instance);


extern SDL_DECLSPEC void SDLCALL SDL_DrawGPUPrimitivesIndirect(
    SDL_GPURenderPass *render_pass,
    SDL_GPUBuffer *buffer,
    Uint32 offset,
    Uint32 draw_count);


extern SDL_DECLSPEC void SDLCALL SDL_DrawGPUIndexedPrimitivesIndirect(
    SDL_GPURenderPass *render_pass,
    SDL_GPUBuffer *buffer,
    Uint32 offset,
    Uint32 draw_count);


extern SDL_DECLSPEC void SDLCALL SDL_EndGPURenderPass(
    SDL_GPURenderPass *render_pass);




extern SDL_DECLSPEC SDL_GPUComputePass * SDLCALL SDL_BeginGPUComputePass(
    SDL_GPUCommandBuffer *command_buffer,
    const SDL_GPUStorageTextureReadWriteBinding *storage_texture_bindings,
    Uint32 num_storage_texture_bindings,
    const SDL_GPUStorageBufferReadWriteBinding *storage_buffer_bindings,
    Uint32 num_storage_buffer_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUComputePipeline(
    SDL_GPUComputePass *compute_pass,
    SDL_GPUComputePipeline *compute_pipeline);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUComputeSamplers(
    SDL_GPUComputePass *compute_pass,
    Uint32 first_slot,
    const SDL_GPUTextureSamplerBinding *texture_sampler_bindings,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUComputeStorageTextures(
    SDL_GPUComputePass *compute_pass,
    Uint32 first_slot,
    SDL_GPUTexture *const *storage_textures,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_BindGPUComputeStorageBuffers(
    SDL_GPUComputePass *compute_pass,
    Uint32 first_slot,
    SDL_GPUBuffer *const *storage_buffers,
    Uint32 num_bindings);


extern SDL_DECLSPEC void SDLCALL SDL_DispatchGPUCompute(
    SDL_GPUComputePass *compute_pass,
    Uint32 groupcount_x,
    Uint32 groupcount_y,
    Uint32 groupcount_z);


extern SDL_DECLSPEC void SDLCALL SDL_DispatchGPUComputeIndirect(
    SDL_GPUComputePass *compute_pass,
    SDL_GPUBuffer *buffer,
    Uint32 offset);


extern SDL_DECLSPEC void SDLCALL SDL_EndGPUComputePass(
    SDL_GPUComputePass *compute_pass);




extern SDL_DECLSPEC void * SDLCALL SDL_MapGPUTransferBuffer(
    SDL_GPUDevice *device,
    SDL_GPUTransferBuffer *transfer_buffer,
    bool cycle);


extern SDL_DECLSPEC void SDLCALL SDL_UnmapGPUTransferBuffer(
    SDL_GPUDevice *device,
    SDL_GPUTransferBuffer *transfer_buffer);




extern SDL_DECLSPEC SDL_GPUCopyPass * SDLCALL SDL_BeginGPUCopyPass(
    SDL_GPUCommandBuffer *command_buffer);


extern SDL_DECLSPEC void SDLCALL SDL_UploadToGPUTexture(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUTextureTransferInfo *source,
    const SDL_GPUTextureRegion *destination,
    bool cycle);


extern SDL_DECLSPEC void SDLCALL SDL_UploadToGPUBuffer(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUTransferBufferLocation *source,
    const SDL_GPUBufferRegion *destination,
    bool cycle);


extern SDL_DECLSPEC void SDLCALL SDL_CopyGPUTextureToTexture(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUTextureLocation *source,
    const SDL_GPUTextureLocation *destination,
    Uint32 w,
    Uint32 h,
    Uint32 d,
    bool cycle);


extern SDL_DECLSPEC void SDLCALL SDL_CopyGPUBufferToBuffer(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUBufferLocation *source,
    const SDL_GPUBufferLocation *destination,
    Uint32 size,
    bool cycle);


extern SDL_DECLSPEC void SDLCALL SDL_DownloadFromGPUTexture(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUTextureRegion *source,
    const SDL_GPUTextureTransferInfo *destination);


extern SDL_DECLSPEC void SDLCALL SDL_DownloadFromGPUBuffer(
    SDL_GPUCopyPass *copy_pass,
    const SDL_GPUBufferRegion *source,
    const SDL_GPUTransferBufferLocation *destination);


extern SDL_DECLSPEC void SDLCALL SDL_EndGPUCopyPass(
    SDL_GPUCopyPass *copy_pass);


extern SDL_DECLSPEC void SDLCALL SDL_GenerateMipmapsForGPUTexture(
    SDL_GPUCommandBuffer *command_buffer,
    SDL_GPUTexture *texture);


extern SDL_DECLSPEC void SDLCALL SDL_BlitGPUTexture(
    SDL_GPUCommandBuffer *command_buffer,
    const SDL_GPUBlitInfo *info);




extern SDL_DECLSPEC bool SDLCALL SDL_WindowSupportsGPUSwapchainComposition(
    SDL_GPUDevice *device,
    SDL_Window *window,
    SDL_GPUSwapchainComposition swapchain_composition);


extern SDL_DECLSPEC bool SDLCALL SDL_WindowSupportsGPUPresentMode(
    SDL_GPUDevice *device,
    SDL_Window *window,
    SDL_GPUPresentMode present_mode);


extern SDL_DECLSPEC bool SDLCALL SDL_ClaimWindowForGPUDevice(
    SDL_GPUDevice *device,
    SDL_Window *window);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseWindowFromGPUDevice(
    SDL_GPUDevice *device,
    SDL_Window *window);


extern SDL_DECLSPEC bool SDLCALL SDL_SetGPUSwapchainParameters(
    SDL_GPUDevice *device,
    SDL_Window *window,
    SDL_GPUSwapchainComposition swapchain_composition,
    SDL_GPUPresentMode present_mode);


extern SDL_DECLSPEC bool SDLCALL SDL_SetGPUAllowedFramesInFlight(
    SDL_GPUDevice *device,
    Uint32 allowed_frames_in_flight);


extern SDL_DECLSPEC SDL_GPUTextureFormat SDLCALL SDL_GetGPUSwapchainTextureFormat(
    SDL_GPUDevice *device,
    SDL_Window *window);


extern SDL_DECLSPEC bool SDLCALL SDL_AcquireGPUSwapchainTexture(
    SDL_GPUCommandBuffer *command_buffer,
    SDL_Window *window,
    SDL_GPUTexture **swapchain_texture,
    Uint32 *swapchain_texture_width,
    Uint32 *swapchain_texture_height);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitForGPUSwapchain(
    SDL_GPUDevice *device,
    SDL_Window *window);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitAndAcquireGPUSwapchainTexture(
    SDL_GPUCommandBuffer *command_buffer,
    SDL_Window *window,
    SDL_GPUTexture **swapchain_texture,
    Uint32 *swapchain_texture_width,
    Uint32 *swapchain_texture_height);


extern SDL_DECLSPEC bool SDLCALL SDL_SubmitGPUCommandBuffer(
    SDL_GPUCommandBuffer *command_buffer);


extern SDL_DECLSPEC SDL_GPUFence * SDLCALL SDL_SubmitGPUCommandBufferAndAcquireFence(
    SDL_GPUCommandBuffer *command_buffer);


extern SDL_DECLSPEC bool SDLCALL SDL_CancelGPUCommandBuffer(
    SDL_GPUCommandBuffer *command_buffer);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitForGPUIdle(
    SDL_GPUDevice *device);


extern SDL_DECLSPEC bool SDLCALL SDL_WaitForGPUFences(
    SDL_GPUDevice *device,
    bool wait_all,
    SDL_GPUFence *const *fences,
    Uint32 num_fences);


extern SDL_DECLSPEC bool SDLCALL SDL_QueryGPUFence(
    SDL_GPUDevice *device,
    SDL_GPUFence *fence);


extern SDL_DECLSPEC void SDLCALL SDL_ReleaseGPUFence(
    SDL_GPUDevice *device,
    SDL_GPUFence *fence);




extern SDL_DECLSPEC Uint32 SDLCALL SDL_GPUTextureFormatTexelBlockSize(
    SDL_GPUTextureFormat format);


extern SDL_DECLSPEC bool SDLCALL SDL_GPUTextureSupportsFormat(
    SDL_GPUDevice *device,
    SDL_GPUTextureFormat format,
    SDL_GPUTextureType type,
    SDL_GPUTextureUsageFlags usage);


extern SDL_DECLSPEC bool SDLCALL SDL_GPUTextureSupportsSampleCount(
    SDL_GPUDevice *device,
    SDL_GPUTextureFormat format,
    SDL_GPUSampleCount sample_count);


extern SDL_DECLSPEC Uint32 SDLCALL SDL_CalculateGPUTextureFormatSize(
    SDL_GPUTextureFormat format,
    Uint32 width,
    Uint32 height,
    Uint32 depth_or_layer_count);


extern SDL_DECLSPEC SDL_PixelFormat SDLCALL SDL_GetPixelFormatFromGPUTextureFormat(SDL_GPUTextureFormat format);


extern SDL_DECLSPEC SDL_GPUTextureFormat SDLCALL SDL_GetGPUTextureFormatFromPixelFormat(SDL_PixelFormat format);

#ifdef SDL_PLATFORM_GDK


extern SDL_DECLSPEC void SDLCALL SDL_GDKSuspendGPU(SDL_GPUDevice *device);


extern SDL_DECLSPEC void SDLCALL SDL_GDKResumeGPU(SDL_GPUDevice *device);

#endif 

#ifdef __cplusplus
}
#endif 
#include <SDL3/SDL_close_code.h>

#endif 
