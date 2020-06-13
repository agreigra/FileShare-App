#ifndef __STRUCT_H__
#define __STRUCT_H__

#define FILE_NAME_SIZE 256
#define KEY_LEN 64

enum info_type {SEED, LEECH};

struct key_queue
{
  char key[KEY_LEN];
  int length;
  int piece_size;
  struct peer * peers;
  struct key_queue * next;
};

struct filename_queue
{
  char filename[FILE_NAME_SIZE];
  struct key_queue * key;
  struct filename_queue * next;
};

struct peer
{
  int ip;
  int port;
  enum info_type type;
  struct peer * next;
};

struct fichiers
{
  struct key_queue * keys;
  struct filename_queue * filenames;
};

struct buffer{
  char* buf;
  char* cursor;
  int length;
};


struct key_queue * add_to_key_queue(char * key, int length, int piece_size, struct fichiers * fichiers);
struct filename_queue * add_to_filename_queue(struct fichiers * fichiers, char * filename, struct key_queue * key_cell);
struct peer * add_peer(int ip, int port, enum info_type type, struct key_queue * key_cell);

struct fichiers * init_fichiers();

struct buffer* new_buffer();
void free_buffer(struct buffer*);
void lengthen_buffer(struct buffer*);

void add(struct fichiers * first, char * key, enum info_type type,int ip, int port, char*filename, int length, int piece_size);

#endif
