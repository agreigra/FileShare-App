#ifndef __PARSER_H__
#define __PARSER_H__

#include "struct.h"
#include "Tracker.h"

#define MAX_PARAM 10

int ip_to_bytes(int ip, int i);

void parser(struct buffer * buffer, int ip_src, int * cli_port, struct fichiers * fichiers);

void announce(char * token, int ip, int * cli_port, char* buffer, struct fichiers * fichiers);

void getfile(char* token, struct buffer* buffer, struct fichiers * fichiers);

void update(char* token, int ip, int * cli_port, char* buffer, struct fichiers * fichiers);

void look(char* token, char* buffer, struct fichiers * fichiers);

#endif
