/* A simple server in the internet domain using TCP
The port number is passed as an argument */
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <unistd.h>
#include "Tracker.h"
#include "parser.h"
#include "struct.h"

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;


void error(char *msg)
{
  perror(msg);
  exit(1);
}

void filelog(char* msg, char* info)
{
  time_t t = time(NULL);
  char * time_s = ctime(&t);
  time_s[strlen(time_s)-1] = 0;
  int pid = pthread_self();
  FILE* fp;
  fp = fopen(LOG_FILE,"a");
  if (VERBOSE) printf("LOG : %u : %s : %s\n",pid, msg, info);
  fprintf(fp, "%s : %u : %s : %s\n", time_s, pid, msg, info);
  fclose(fp);
}



void * socketThread(void *arg)
{
  int active_connection = 1;
  filelog("New thread", "/");
  // arg est un tableau de deux pointeurs d'entiers
  int newsockfd = *(((int **)arg)[0]);
  struct sockaddr_in cli_addr = *(((struct sockaddr_in **)arg)[1]);
  int ip_addr = cli_addr.sin_addr.s_addr;
  int cli_port = -1; // client port has a global variable. It has to be reuse in the update command.
  struct fichiers* fichiers = ((struct fichiers**)arg)[2];


  while (active_connection) {
    struct buffer* local_buffer=new_buffer();
    //printf("DEBUG : (re)create buffer %p %s %s \n", local_buffer, local_buffer->buf, local_buffer->cursor);
    //char local_buffer[BUFFER_SIZE];
    int r=read(newsockfd,local_buffer->cursor,BUFFER_SIZE);
    if (r==0) {
      filelog("Message vide recus","/");
      active_connection = 0;
    } else {
      while(r==BUFFER_SIZE){
        local_buffer->cursor+=BUFFER_SIZE;
        lengthen_buffer(local_buffer);
        r=read(newsockfd,local_buffer->cursor,BUFFER_SIZE);
      }
      local_buffer->cursor+=r;
      if(r<0)
      error("ERROR reading from socket");
      *(local_buffer->cursor - 1) = 0; //enleve le retour chariot
      filelog("Nouveau message reçu ","");
      printf("> %s\n",local_buffer->buf);
      // printf("DEBUG : %d %p\n", cli_port, &cli_port);
      parser(local_buffer,ip_addr, &cli_port, fichiers);
      if(local_buffer->buf+local_buffer->length==local_buffer->cursor)
      lengthen_buffer(local_buffer);
      strcat(local_buffer->buf,"\n");
      local_buffer->length = strlen(local_buffer->buf);
      if (write(newsockfd,local_buffer->buf,local_buffer->length) < 0)
      error("ERROR writing to socket");
      printf("< %s\n",local_buffer->buf);
    }
    //send(newSocket,buffer,13,0);
    free_buffer(local_buffer);
  }
  close(newsockfd);
  filelog("Exit socketThread","/");
  pthread_exit(NULL);
}

int main(int argc, char *argv[])
{

  struct fichiers * fichiers = init_fichiers();

  int sockfd; /*discripteur de la socket du serveur*/
  int newsockfd; /*discripteur de la socket du client */
  int portno; /*numero du port*/
  unsigned int clientlen;

  //char buffer[BUFFER_SIZE];
  struct sockaddr_in serv_addr; /*pointeur sur le contexte d'adressage du serveur*/
  struct sockaddr_in cli_addr; /*pointeur sur le contexte d'adressage du client*/
  if (argc < 2) {
    fprintf(stderr,"ERROR, no port provided\n");
    exit(1);
  }

  sockfd = socket(AF_INET, SOCK_STREAM, 0); //création de la socket

  if (sockfd < 0)
  error("ERROR opening socket");

  bzero((char *) &serv_addr, sizeof(serv_addr)); //
  portno = atoi(argv[1]); //numero de port

  /* configurer la socket*/
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(portno);

  /* associer à la socket ces informations (discripteur de la socket, pointeur socketadd, taille de la structure socketadd) */

  if (bind(sockfd, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0)
  error("ERROR on binding");
  pthread_t tid[60];
  int i = 0;
  while (1) {
    /*mettre la socket dans un état d'écoute*/
    filelog("socket","listening");
    int err = listen(sockfd,5);
    if(err< 0)
    {error("ERROR on listening");}
    /*accepter un appel de connexion*/
    clientlen = sizeof(cli_addr);
    newsockfd = accept(sockfd,(struct sockaddr *) &cli_addr,&clientlen);
    if (newsockfd < 0)
    error("ERROR on accept");
    void* arg[3]={&newsockfd,&cli_addr,fichiers};
    if( pthread_create(&tid[i++], NULL, socketThread, arg) != 0 )
    error("Failed to create thread");
    else filelog("Created new thread","/");
    if( i >= 50)
    {
      i = 0;
      while(i < 50)
      {pthread_join(tid[i++],NULL);}
      i = 0;
    }

  }
  return 0;
}
