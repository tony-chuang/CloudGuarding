#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/shm.h>
#include "iot_device.h"


#define PORT_GATEWAY	8000	//connect prot number for gateway
#define PORT_MOBILE	9000	//connect prot number for mobile

char xml_send[] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices> \
   <device id=\"A1\" name=\"磁簧\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"B1\" name=\"紅外線\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"C1\" name=\"按鈕\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"D1\" name=\"溫度\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"E1\" name=\"溼度\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"F1\" name=\"一氧化碳\" status=\"\" action=\"\" alarm=\"\"> \
   </device><device id=\"G1\" name=\"蜂鳴器\" status=\"\" action=\"\" alarm=\"\"></device> \
   </iotDevices>";

struct shared_use_st {
    int isEnabled;
    char from_mobile [4096];
    char from_gateway[4096];
};

int main(int argc, char *argv[])
{
    int sockfd, newsockfd, oldsockfd;
    int listen_sockfd, connection_sockfd;
    struct sockaddr_in addr;
    int addr_len = sizeof(struct sockaddr_in);
    char buffer[4096];
    char connectedMsg[] = "Welcome to server !";
    char responseMsg[] = "Got it !";
    int ret;
    struct iot_devices_xml_resolved *pdevices;
    pid_t pid;
    int segment_id;
    char *shared_memory = (char *) 0;
    struct shared_use_st *guard;

    /* Allocate a shared memory segment */
    segment_id = shmget((key_t) 1112, sizeof(struct shared_use_st), 0666 | IPC_CREAT);
    if (segment_id == -1) {
	perror("shmget");
	exit(1);
    }

    /* Attach the shared memory segment */
    shared_memory = shmat(segment_id, (char *) 0, 0);
    if (shared_memory == (void *) -1) {
	perror("shmat");
	exit(1);
    }
    printf("Shareed memory attached at address %p\n", shared_memory);
    guard = (struct shared_use_st *) shared_memory;

    /* initial xml */
    strcpy(guard->from_mobile,  xml_send);
    strcpy(guard->from_gateway, xml_send);

    if (fork() == 0) {

	/*********************/
        /* socket for Mobile */
        /*********************/

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
	    perror("socket");
	    exit(1);
	}
	/* set SO_REUSEADDR on a socket to true (1): */
	int optval1 = 1;
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval1, sizeof optval1);
	if (sockfd < 0) {
	    perror("ERROR opening socket");
	    exit(1);
	}

	bzero(&addr, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_port = htons(PORT_MOBILE);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(sockfd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
	    perror("connect");
	    exit(1);
	}

	if (listen(sockfd, 5) < 0) {
	    perror("listen");
	    exit(1);
	}

	while (1) {

	    static int cnt = 0;

	    connection_sockfd = accept(sockfd, (struct sockaddr *) &addr, &addr_len);
	    if (connection_sockfd < 0) {
		perror("ERROR on accept");
		exit(1);
	    }
	    printf("Mobile Connect form %s\n", inet_ntoa(addr.sin_addr));

	    printf("Run: %d-th -----------------------------------------\n", ++cnt);

	    //printf("Server send: %s \n", guard->from_gateway);

	    send(connection_sockfd, guard->from_gateway, sizeof guard->from_gateway, 0);
	    send(connection_sockfd, "\n", sizeof "\n", 0);

	    bzero(buffer, sizeof(buffer));
	    ret = recv(connection_sockfd, buffer, sizeof buffer, 0);

	    //printf("Mobile send: %s\n", buffer);

            memset(guard->from_mobile, 0, sizeof guard->from_mobile);
	    strcpy (guard->from_mobile, buffer);

	    close(connection_sockfd);
	}

    } else {

	/**********************/
	/* socket for Gateway */
	/**********************/

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
	    perror("socket");
	    exit(1);
	}
	// set SO_REUSEADDR on a socket to true (1):
	int optval1 = 1;
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval1, sizeof optval1);
	if (sockfd < 0) {
	    perror("ERROR opening socket");
	    exit(1);
	}

	bzero(&addr, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_port = htons(PORT_GATEWAY);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(sockfd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
	    perror("connect");
	    exit(1);
	}

	if (listen(sockfd, 5) < 0) {
	    perror("listen");
	    exit(1);
	}

	while (1) {
	    static int cnt = 0;

	    connection_sockfd = accept(sockfd, (struct sockaddr *) &addr, &addr_len);
	    if (connection_sockfd < 0) {
		perror("ERROR on accept");
		exit(1);
	    }

	    printf("Gateway Connect from %s\n", inet_ntoa(addr.sin_addr));

	    printf ("Run: %d-th -----------------------------------------\n", ++cnt);
	    bzero(buffer, sizeof(buffer));
	    ret = recv(connection_sockfd, buffer, sizeof buffer, 0);
	    if (ret < 0 || ret == 0) {
		close(connection_sockfd);
		printf("Connection closed ! \n");
		continue ;
	    }

	    //printf("Gateway send: %s\n", buffer);

	    memset(guard->from_gateway, 0, sizeof guard->from_gateway);
	    strcpy (guard->from_gateway, buffer);

	    //printf("Server send to Gateway: %s \n", guard->from_mobile);

	    send(connection_sockfd, guard->from_mobile, sizeof guard->from_mobile, 0);
	}
     }
    return 0;
}
