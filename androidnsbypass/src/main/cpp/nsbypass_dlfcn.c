







































#include <android/log.h>
#include <dlfcn.h>
#include <elf.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>
#include <inttypes.h>

#include "fasthook/nsbypass_dlfcn.h"
#include "utils.h"

struct ctx {
	void *load_addr;
	void *dynstr;
	void *dynsym;
    size_t dynsym_num;
	void *strtab;
	void *symtab;
    size_t symtab_num;

};

int nsbypass_dlclose(void *handle) {
	if (is_android_6_or_lower()) return dlclose(handle);
	if (handle) {
		struct ctx *ctx = (struct ctx *) handle;
		if (ctx->dynsym) free(ctx->dynsym);    
		if (ctx->dynstr) free(ctx->dynstr);    
		if (ctx->symtab) free(ctx->symtab);
		if (ctx->strtab) free(ctx->strtab);
		free(ctx);
	}
	return 0;
}

void *nsbypass_dlopen(const char *libPath, int flags) {
	if (is_android_6_or_lower()) return dlopen(libPath, flags);
	FILE *maps;
	
	
	char mapsSearchBuff[2048];
	struct ctx *ctx = 0;
	uintptr_t load_addr, size;
	int k, fd = -1, found = 0;
	void *shoff;
    ELF_EHDR *elf = (ELF_EHDR *) MAP_FAILED;

#define fatal(fmt, args...) do { LOGE(fmt,##args); goto err_exit; } while(0)

	maps = fopen("/proc/self/maps", "r");
	if (!maps) fatal("failed to open maps");

	while (!found && fgets(mapsSearchBuff, sizeof(mapsSearchBuff), maps))
		if (strstr(mapsSearchBuff, libPath)) found = 1;

	fclose(maps);

	
	
	if (libPath[0] != '/') {
		
		
		
		char *libPathStart = strstr(mapsSearchBuff, libPath); 
		if (libPathStart != NULL) {
			char *pathStart = libPathStart;
			
			while (pathStart > mapsSearchBuff &&
					pathStart[-1] != ' ' &&
					pathStart[-1] != '\t') {
				--pathStart;
			}
			
			if (*pathStart == '/') {
				libPath = pathStart;
                
                
                char *pathEnd = strchr(libPath, '\n');
                if (pathEnd != NULL) {
                    *pathEnd = '\0';
                }
			} else {
				fatal(
						"An error happened while resolving the full path of %s; "
						"searching stopped at %s",
						libPath,
						pathStart
				);
			}
		}
	}

	if (!found) fatal("%s not found in my userspace", libPath);

	unsigned long p_offset;

	
	

	
	
	if (sscanf(mapsSearchBuff,
			"%" SCNxPTR "-%*" SCNxPTR " %*4s %lx",
			&load_addr,
			&p_offset) != 2) {
		fatal("failed to parse maps entry for %s", libPath);
	}

	load_addr -= p_offset;

	LOGI("%s loaded in Android at 0x%" PRIxPTR, libPath, load_addr);
	

	fd = open(libPath, O_RDONLY);
	if (fd < 0) fatal("failed to open %s", libPath);

	size = lseek(fd, 0, SEEK_END);
	if (size <= 0) fatal("lseek() failed for %s", libPath);

	elf = (ELF_EHDR *) mmap(0, size, PROT_READ, MAP_SHARED, fd, 0);
	close(fd);
	fd = -1;

	if (elf == MAP_FAILED) fatal("mmap() failed for %s", libPath);

	ctx = (struct ctx *) calloc(1, sizeof(struct ctx));
	if (!ctx) fatal("no memory for %s", libPath);

	ctx->load_addr = (void *) load_addr;
	shoff = ((void *) elf) + elf->e_shoff;

	ELF_SHDR *shstrtab = (ELF_SHDR *)(shoff + elf->e_shstrndx * elf->e_shentsize);
	char * shstr = malloc(shstrtab->sh_size);
	memcpy(shstr, ((void *) elf) + shstrtab->sh_offset, shstrtab->sh_size);

	for (k = 0; k < elf->e_shnum; k++, shoff += elf->e_shentsize) {

        ELF_SHDR *sh = (ELF_SHDR *) shoff;
		LOGD("%s: k=%d shdr=%p type=%d", __func__, k, sh, sh->sh_type);

		switch (sh->sh_type) {

			case SHT_DYNSYM:
				if (ctx->dynsym) fatal("%s: duplicate DYNSYM sections", libPath); 
				ctx->dynsym = malloc(sh->sh_size);
				if (!ctx->dynsym) fatal("%s: no memory for .dynsym", libPath);
				memcpy(ctx->dynsym, ((void *) elf) + sh->sh_offset, sh->sh_size);
				ctx->dynsym_num = (sh->sh_size / sizeof(ELF_SYM));
				break;

			case SHT_SYMTAB:
				if (ctx->symtab) fatal("%s: duplicate SYMTAB sections", libPath); 
				ctx->symtab = malloc(sh->sh_size);
				if (!ctx->symtab) fatal("%s: no memory for .symtab", libPath);
				memcpy(ctx->symtab, ((void *) elf) + sh->sh_offset, sh->sh_size);
				ctx->symtab_num = (sh->sh_size / sizeof(ELF_SYM));
				break;

			case SHT_STRTAB:
				if(!strcmp(shstr+sh->sh_name,".dynstr")) {
					if (ctx->dynstr) break;    
					ctx->dynstr = malloc(sh->sh_size);
					if (!ctx->dynstr) fatal("%s: no memory for .dynstr", libPath);
					memcpy(ctx->dynstr, ((void *) elf) + sh->sh_offset, sh->sh_size);
				}else if(!strcmp(shstr+sh->sh_name,".strtab")) {
					if (ctx->strtab) break;
					ctx->strtab = malloc(sh->sh_size);
					if (!ctx->strtab) fatal("%s: no memory for .strtab", libPath);
					memcpy(ctx->strtab, ((void *) elf) + sh->sh_offset, sh->sh_size);
				}
				break;
		






		}
	}

	munmap(elf, size);
	elf = 0;

	if (!ctx->dynstr || !ctx->dynsym) fatal("dynamic sections not found in %s", libPath);

#undef fatal

	LOGD("%s: ok, dynsym = %p, dynstr = %p symtab = %p strtab = %p", libPath, ctx->dynsym, ctx->dynstr, ctx->symtab, ctx->strtab);

	return ctx;

	err_exit:
	if (fd >= 0) close(fd);
	if (elf != MAP_FAILED) munmap(elf, size);
	nsbypass_dlclose(ctx);
	return 0;
}

void *nsbypass_dlsym(void *handle, const char *name) {
	if (is_android_6_or_lower()) return dlsym(handle, name);
	int k;
	struct ctx *ctx = (struct ctx *) handle;
    ELF_SYM *dynsym = (ELF_SYM *) ctx->dynsym;
    ELF_SYM *symtab = (ELF_SYM *) ctx->symtab;
	char *dynstr = (char *) ctx->dynstr;
	char *strtab = (char *) ctx->strtab;

	for (k = 0; k < ctx->dynsym_num; k++, dynsym++) {
		if (strcmp(dynstr + dynsym->st_name, name) == 0) {
			

			
			
			void *ret = ctx->load_addr + dynsym->st_value;
			return ret;
		}
	}

	if(symtab) {
		for (k = 0; k < ctx->symtab_num; k++, symtab++) {
			
			if (strcmp(strtab + symtab->st_name, name) == 0) {
				

				
				
				void *ret = ctx->load_addr + symtab->st_value;
				return ret;
			}
		}
	}
	return 0;
}