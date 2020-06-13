#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <stdio.h>
#include "Tracker.h"
#include "struct.h"


struct fichiers * init_fichiers()
{
    struct fichiers * f = malloc(sizeof(struct fichiers));
    f->filenames = NULL;
    f->keys = NULL;
    return f;
};

struct filename_queue * add_to_filename_queue(struct fichiers * fichiers, char * filename, struct key_queue * key_cell) {
  if (strcmp(filename,"") == 0) {
    filelog("skipping an empty filename","");
    return fichiers->filenames;
  } else if (fichiers->filenames == NULL) {
    struct filename_queue * fn = malloc(sizeof(struct filename_queue));
    strcpy(fn->filename, filename);
    fn->key = key_cell;
    fn->next = NULL;
    fichiers->filenames = fn;
    filelog("created first filename", filename);
    return fn;
  } else {
    struct filename_queue * head = fichiers->filenames;
    while (head->next != NULL && ((strcmp(head->filename,filename) != 0) || head->key != key_cell)) {
      head = head->next;
    }
    if ( (strcmp(head->filename,filename) == 0) && head->key == key_cell) {
      filelog("filename already present with same key", filename);
      return head;
    } else {
      filelog("creation of new filename", filename);
      struct filename_queue * fn = malloc(sizeof(struct filename_queue));
      strcpy(fn->filename, filename);
      fn->key = key_cell;
      fn->next = NULL;
      head->next = fn;
      return fn;
    }
  }
}



struct key_queue * add_to_key_queue(char * key, int length, int piece_size, struct fichiers * fichiers)
{
  if (fichiers->keys == NULL) {
    struct key_queue * ks = malloc(sizeof(struct key_queue));
    strcpy(ks->key, key);
    ks->length = length;
    ks->piece_size = piece_size;
    ks->peers = NULL;
    ks->next = NULL;
    fichiers->keys = ks;
    filelog("created first key", key);
    return ks;
  } else {
    struct key_queue * head = fichiers->keys;
    while (head->next != NULL && (strcmp(head->key,key) != 0)) {
      head = head->next;
    }
    if  (strcmp(head->key,key) == 0) {
      filelog("key already present", key);
      return head;
    } else {
      filelog("creation of new key", key);
      struct key_queue * ks = malloc(sizeof(struct key_queue));
      strcpy(ks->key, key);
      ks->length = length;
      ks->piece_size = piece_size;
      ks->peers = NULL;
      ks->next = NULL;
      head->next = ks;
      return ks;
    }
  }
};

struct peer * create_peer(int ip, int port, enum info_type type)
{
  struct peer * s = malloc(sizeof(struct peer));
  s->ip = ip;
  s->port = port;
  s->type = type;
  s->next = NULL;
  return s;
};

struct peer * add_peer(int ip, int port, enum info_type type, struct key_queue * key_cell)
{
    struct peer * peer_queue = key_cell->peers;
    if (peer_queue == NULL) {
      struct peer * p = malloc(sizeof(struct peer));
      p->ip = ip;
      p->port = port;
      p->type = type;
      p->next = NULL;
      key_cell->peers = p;
      filelog("created first peer", "/");
      return p;
    } else {
      struct peer * head = key_cell->peers;
      while (head->next != NULL && ( head->ip != ip || head->port != port)) {
        head = head->next;
      }
      if  ( head->ip == ip && head->port == port) {
        filelog("peer already present", "/");
        if (head->type == LEECH) head->type = type; // on passe le peer en seed si c'etait un leech mais pas l'inverse
        return head;
      } else {
        filelog("creation of new peer", "/");
        struct peer * p = malloc(sizeof(struct peer));
        p->ip = ip;
        p->port = port;
        p->type = type;
        p->next = NULL;
        head->next = p;
        return p;
      }
    }

}


void add(struct fichiers * fichiers, char * key, enum info_type t, int ip, int port, char*filename, int length, int piece_size) {
  struct key_queue * key_cell = add_to_key_queue(key,length,piece_size,fichiers);
  add_to_filename_queue(fichiers,filename,key_cell);
  add_peer(ip,port,t,key_cell);
}

struct buffer* new_buffer(){
    struct buffer * buffer=malloc(sizeof(struct buffer));
    buffer->buf=(char*) malloc(sizeof(char)*BUFFER_SIZE);
    buffer->cursor=buffer->buf;
    buffer->length=BUFFER_SIZE;
    bzero(buffer->buf,buffer->length);
    return buffer;
}
void free_buffer(struct buffer* buffer){
  free(buffer->buf);
  free(buffer);
}
void lengthen_buffer(struct buffer* buffer){
  buffer->length=buffer->length+BUFFER_SIZE;
  buffer->buf=(char*) realloc(buffer->buf,sizeof(char)*buffer->length);
  bzero(buffer->cursor,BUFFER_SIZE);
}
