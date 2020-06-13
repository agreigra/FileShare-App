#ifndef __TRACKER_H__
#define __TRACKER_H__

#define BUFFER_SIZE 256
#define LOG_FILE "log"
#define VERBOSE 1==0

#include <pthread.h>
#include "struct.h"

void * socketThread(void *arg);

void error(char* msg);

void filelog(char* msg, char* info);

void seed_err_check(char* token,char* buffer);


#endif
