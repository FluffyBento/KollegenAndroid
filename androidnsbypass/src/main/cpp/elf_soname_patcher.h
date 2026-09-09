

#pragma once

#ifndef NSBYPASS_ELF_SONAME_PATCHER_H
#define NSBYPASS_ELF_SONAME_PATCHER_H

#include <errno.h>
#include <fcntl.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

#ifdef __cplusplus
extern "C" {
#endif


bool patch_elf_soname(int realfd, int patchfd, uint16_t patchid);


bool patch_elf_soname_path(const char *elfPath, int patchfd, uint16_t patchid);

#ifdef __cplusplus
}
#endif

#endif 
