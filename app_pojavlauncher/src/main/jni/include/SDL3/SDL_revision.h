





#ifndef SDL_revision_h_
#define SDL_revision_h_

#ifdef SDL_WIKI_DOCUMENTATION_SECTION


#define SDL_REVISION "Some arbitrary string decided at SDL build time"
#elif defined(SDL_VENDOR_INFO)
#define SDL_REVISION SDL_VENDOR_INFO
#else
#define SDL_REVISION ""
#endif

#endif 
