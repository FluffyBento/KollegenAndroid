





#ifndef SDL_hidapi_h_
#define SDL_hidapi_h_

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_error.h>
#include <SDL3/SDL_properties.h>

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef struct SDL_hid_device SDL_hid_device;


typedef enum SDL_hid_bus_type {
    
    SDL_HID_API_BUS_UNKNOWN = 0x00,

    
    SDL_HID_API_BUS_USB = 0x01,

    
    SDL_HID_API_BUS_BLUETOOTH = 0x02,

    
    SDL_HID_API_BUS_I2C = 0x03,

    
    SDL_HID_API_BUS_SPI = 0x04

} SDL_hid_bus_type;




typedef struct SDL_hid_device_info
{
    
    char *path;
    
    unsigned short vendor_id;
    
    unsigned short product_id;
    
    wchar_t *serial_number;
    
    unsigned short release_number;
    
    wchar_t *manufacturer_string;
    
    wchar_t *product_string;
    
    unsigned short usage_page;
    
    unsigned short usage;
    
    int interface_number;

    
    int interface_class;
    int interface_subclass;
    int interface_protocol;

    
    SDL_hid_bus_type bus_type;

    
    struct SDL_hid_device_info *next;

} SDL_hid_device_info;



extern SDL_DECLSPEC int SDLCALL SDL_hid_init(void);


extern SDL_DECLSPEC int SDLCALL SDL_hid_exit(void);


extern SDL_DECLSPEC Uint32 SDLCALL SDL_hid_device_change_count(void);


extern SDL_DECLSPEC SDL_hid_device_info * SDLCALL SDL_hid_enumerate(unsigned short vendor_id, unsigned short product_id);


extern SDL_DECLSPEC void SDLCALL SDL_hid_free_enumeration(SDL_hid_device_info *devs);


extern SDL_DECLSPEC SDL_hid_device * SDLCALL SDL_hid_open(unsigned short vendor_id, unsigned short product_id, const wchar_t *serial_number);


extern SDL_DECLSPEC SDL_hid_device * SDLCALL SDL_hid_open_path(const char *path);


extern SDL_DECLSPEC SDL_PropertiesID SDLCALL SDL_hid_get_properties(SDL_hid_device *dev);

#define SDL_PROP_HIDAPI_LIBUSB_DEVICE_HANDLE_POINTER   "SDL.hidapi.libusb.device.handle"


extern SDL_DECLSPEC int SDLCALL SDL_hid_write(SDL_hid_device *dev, const unsigned char *data, size_t length);


extern SDL_DECLSPEC int SDLCALL SDL_hid_read_timeout(SDL_hid_device *dev, unsigned char *data, size_t length, int milliseconds);


extern SDL_DECLSPEC int SDLCALL SDL_hid_read(SDL_hid_device *dev, unsigned char *data, size_t length);


extern SDL_DECLSPEC int SDLCALL SDL_hid_set_nonblocking(SDL_hid_device *dev, int nonblock);


extern SDL_DECLSPEC int SDLCALL SDL_hid_send_feature_report(SDL_hid_device *dev, const unsigned char *data, size_t length);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_feature_report(SDL_hid_device *dev, unsigned char *data, size_t length);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_input_report(SDL_hid_device *dev, unsigned char *data, size_t length);


extern SDL_DECLSPEC int SDLCALL SDL_hid_close(SDL_hid_device *dev);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_manufacturer_string(SDL_hid_device *dev, wchar_t *string, size_t maxlen);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_product_string(SDL_hid_device *dev, wchar_t *string, size_t maxlen);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_serial_number_string(SDL_hid_device *dev, wchar_t *string, size_t maxlen);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_indexed_string(SDL_hid_device *dev, int string_index, wchar_t *string, size_t maxlen);


extern SDL_DECLSPEC SDL_hid_device_info * SDLCALL SDL_hid_get_device_info(SDL_hid_device *dev);


extern SDL_DECLSPEC int SDLCALL SDL_hid_get_report_descriptor(SDL_hid_device *dev, unsigned char *buf, size_t buf_size);


extern SDL_DECLSPEC void SDLCALL SDL_hid_ble_scan(bool active);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
