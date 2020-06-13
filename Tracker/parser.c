#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <limits.h>
#include <math.h>

#include "parser.h"

/*  data manipulation functions */
int ip_to_bytes(int ip, int i){
  unsigned char bytes[4];
  bytes[0] = ip & 0xFF;
  bytes[1] = (ip >> 8) & 0xFF;
  bytes[2] = (ip >> 16) & 0xFF;
  bytes[3] = (ip >> 24) & 0xFF;
  return bytes[i];
};

/* Parser function */
void parser(struct buffer* buffer, int ip_src, int * cli_port, struct fichiers * fichiers) {

  char *token = strtok(buffer->buf," ");
  if (token != NULL && strcmp(token,"announce") == 0){
    filelog("new command",buffer->buf);
    announce(token,ip_src,cli_port,buffer->buf,fichiers);
  } else if (token != NULL && strcmp(token,"getfile") == 0){
    filelog("new command",buffer->buf);
    getfile(token,buffer,fichiers);
    // getfile
  } else if (token != NULL && strcmp(token,"look") == 0){
    filelog("new command",buffer->buf );
    look(token,buffer->buf,fichiers);
    // look
  } else if (token != NULL && strcmp(token,"update") == 0){
    filelog("new command",buffer->buf );
    update(token,ip_src,cli_port,buffer->buf,fichiers);
    // update
  }  else if (token != NULL && strncmp(token,"list",4) == 0){
    filelog("new command",buffer->buf);
    // list all file saved
    printf("LIST : fichiers:%p filename:%p keys:%p\n", fichiers, fichiers->filenames, fichiers->keys);
    struct filename_queue * f = fichiers->filenames;
    while (f != NULL) {
      printf("LIST : FILENAME \"%s\" is %s at %p\n", f->filename, (f->key)->key, f->key);
      f = f->next;
    }
    struct key_queue * k = fichiers->keys;
    while (k != NULL) {
      printf("LIST : KEY %s at %p next %p\n", k->key, k, k->next);
      struct peer * p = k->peers;
      while (p != NULL) {
        printf("LIST :  %s─ %s %d.%d.%d.%d:%d\n", (p->next == NULL) ? "└" : "├" , (p->type == SEED) ? "SEED " : "LEECH", ip_to_bytes(p->ip,0),ip_to_bytes(p->ip,1),ip_to_bytes(p->ip,2),ip_to_bytes(p->ip,3), p->port);
        p = p->next;
      }
      k = k->next;
    }
    bzero(buffer->buf,BUFFER_SIZE);
    strcpy(buffer->buf,"ok\n");
  } else {
    filelog("unknow command",buffer->buf);
    bzero(buffer->buf,BUFFER_SIZE);
    strcpy(buffer->buf,"nok\n");
  }
};

/* command functions*/
void announce(char * token, int ip, int * port, char* buffer, struct fichiers * fichiers){
  token = strtok(NULL, " ");

  token = strtok(NULL, " ");

  if (token != NULL) {
    *port = atoi(token);
    token = strtok(NULL, " ");
  } else {
    filelog("Malformed announce command", "missing port");
  }



  //while (token != NULL) {
  if (token != NULL && strcmp(token,"seed") == 0) {
    token = strtok(NULL," ");
    //token = strtok(NULL,"] ");
    if (token != NULL) token = token + 1;
    while (token != NULL && strcmp(token,"leech") != 0) {
      char length[BUFFER_SIZE];
      char piece_size[BUFFER_SIZE];
      char key[BUFFER_SIZE];


      //token = strtok(NULL," ]");
      char filename[BUFFER_SIZE];
      if (token != NULL && strcmp(token,"leech") != 0) {
        strcpy(filename,token);
        token = strtok(NULL," ]");
        if (token != NULL && strcmp(token,"leech") != 0) {
          strcpy(length,token);
          token = strtok(NULL," ]");
          if (token != NULL && strcmp(token,"leech") != 0) {
            strcpy(piece_size,token);
            token = strtok(NULL," ]");

            if (token != NULL && strcmp(token,"leech") != 0) {
              strcpy(key,token);
              add(fichiers,key,SEED, ip, *port, filename, atoi(length), atoi(piece_size));
              filelog("record new seed",token);
              token = strtok(NULL," ]");

            } else {
              filelog("skipping malformed seed","no key");
            }
          } else {
            filelog("skipping malformed seed","no piece_size");
          }
        } else {
          filelog("skipping malformed seed","no length");
        }
      } else {
        filelog("skipping malformed seed","no filename");
      }


    }

  }

  if (token != NULL && strcmp(token,"leech") == 0) {
    token = strtok(NULL," ]");
    //token = strtok(NULL,"] ");
    if (token != NULL) token = token + 1;
    while (token != NULL && strcmp(token,"seed") != 0) {
      char key[BUFFER_SIZE];
      strcpy(key,token);
      if (token != NULL) {
        add(fichiers,key,LEECH, ip, *port, NULL, 0, 0);
        filelog("record new leech",key);
        token = strtok(NULL," ]");
      }

    }
  }

  bzero(buffer,BUFFER_SIZE);
  strcpy(buffer,"ok");

};

void getfile(char* token, struct buffer *buffer, struct fichiers * fichiers){
  token = strtok(NULL, " ");
  printf("DEBUG : token:%s:\n", token);
  if (token != NULL) {
    char key[32];
    strcpy(key,token);
    token = strtok(NULL, " ");
    struct key_queue * f = fichiers->keys;
    while (f != NULL) {
      if (strcmp(f->key ,key) == 0) {
        bzero(buffer->buf,buffer->length);
        buffer->cursor=buffer->buf;
        sprintf(buffer->buf,"peer %s [ ",key);
        buffer->cursor=buffer->cursor+strlen("peer  [ ")+strlen(key);
        filelog("getfile found match",key);
        struct peer * s = f->peers;
        char addr[20];
        while (s != NULL) {
          if (s->type == SEED) {
            sprintf(addr,"%d.%d.%d.%d:%d ",ip_to_bytes(s->ip,0),ip_to_bytes(s->ip,1),ip_to_bytes(s->ip,2),ip_to_bytes(s->ip,3), s->port);
            if((unsigned int) (buffer->buf + buffer->length - buffer->cursor) < strlen(addr))
            lengthen_buffer(buffer);
            strcpy(buffer->cursor,addr);
            buffer->cursor+=strlen(addr);
          }
          s = s->next;
        }
        struct peer * l = f->peers;
        while (l != NULL) {
          if (l->type == LEECH) {
            sprintf(addr,"%d.%d.%d.%d:%d ",ip_to_bytes(l->ip,0),ip_to_bytes(l->ip,1),ip_to_bytes(l->ip,2),ip_to_bytes(l->ip,3), l->port);
            if((unsigned int) (buffer->buf + buffer->length - buffer->cursor) < strlen(addr))
            lengthen_buffer(buffer);
            strcpy(buffer->cursor,addr);
            buffer->cursor+=strlen(addr);
          }
          l = l->next;
        }
        if(buffer->buf+buffer->length==buffer->cursor)
          lengthen_buffer(buffer);
        strcat(buffer->buf,"]");
      }
      f = f->next;
    }
  } else {
    bzero(buffer->buf,buffer->length);
    strcpy(buffer->buf,"missing key");
    filelog("Malformed getfile command", "missing key");
  }

};


void look(char* token, char* buffer, struct fichiers * fichiers){
  char local_buffer[BUFFER_SIZE];
  strcpy (local_buffer,"list [");
  token = strtok(NULL, "\" ");
  token +=1;
  while (token != NULL) {
    if (strncmp(token,"filename=",9)==0) {
      struct filename_queue * filename_cell = fichiers->filenames;
      char filename[FILE_NAME_SIZE];
      token = strtok(NULL,"\" ");
      strcpy(filename,token);
      filelog("Looking for filename", filename);
      while (filename_cell != NULL) {
        if (strcmp(filename_cell->filename,filename) == 0){
          filelog("found matching file",(filename_cell->key)->key);
          char newfile[345];
          sprintf(newfile,"%s %d %d %s ",filename_cell->filename, filename_cell->key->length, filename_cell->key->piece_size, (filename_cell->key)->key);
          strcat(local_buffer, newfile);

        }
        filename_cell = filename_cell->next;

      }

      token = strtok(NULL,"] ");
    } else {
      //strcpy(local_buffer,"nok");
      filelog("look command not suported", token);
      token = strtok(NULL,"] ");
    }

  }
  int p = strlen(local_buffer);
  if (local_buffer[p-1] == ' ') p += -1;
  strcpy(local_buffer+p,"]\0");
  bzero(buffer,BUFFER_SIZE);
  strcpy(buffer,local_buffer);
};

void update(char* token, int ip, int * port, char* buffer, struct fichiers * fichiers){
  token = strtok(NULL, " ");

  if ( *port == -1) filelog("Port not assigned", "should not happend if the announce command is propely made");

  //while (token != NULL) {
  if (token != NULL && strcmp(token,"seed") == 0) {
    token = strtok(NULL," ]");
    //token = strtok(NULL,"] ");
    if (token != NULL) token = token + 1;
    while (token != NULL && strcmp(token,"leech") != 0) {
      char key[BUFFER_SIZE];
      strcpy(key,token);
      if (token != NULL) {
        if (strcmp(key,"") != 0){
          add(fichiers,key,SEED, ip, *port, "", -1, -1);
          filelog("updated new seed",key);
        }
        token = strtok(NULL," ]");
      }
    }

  }

  if (token != NULL && strcmp(token,"leech") == 0) {
    token = strtok(NULL," ]");
    if (token != NULL) token = token + 1;
    while (token != NULL && strcmp(token,"seed") != 0) {
      char key[BUFFER_SIZE];
      strcpy(key,token);
      if (token != NULL) {
        if (strcmp(key,"") != 0){
          add(fichiers,key,LEECH, ip, *port, "", -1, -1);
          filelog("updated new leech",key);
      }
      token = strtok(NULL," ]");}

    }
  }

  bzero(buffer,BUFFER_SIZE);
  strcpy(buffer,"ok");

}
