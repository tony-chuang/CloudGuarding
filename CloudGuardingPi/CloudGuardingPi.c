#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <time.h>
#include <unistd.h>
#include "iot_device.h"
#include <wiringPi.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <string.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <libxml/xmlmemory.h>
#include <libxml/parser.h>
#include <wiringPiSPI.h>

#define SERVER_IP       "192.168.0.100"
#define PORT_NUMBER     8000
#define  MAXTIMINGS 85
#define  DHTPIN 7
#define BCM2708_PERI_BASE      0X20000000
#define GPIO_BASE              (BCM2708_PERI_BASE + 0X200000)
#define PAGE_SIZE              (4 * 1024)
#define BLOCK_SIZE            (4 * 1024)
#define INP_GPIO(g) *(gpio+((g)/10)) &= ~(7<<(((g)%10)*3))
#define OUT_GPIO(g) *(gpio+((g)/10)) |= (1<<(((g)%10)*3))
#define SET_GPIO_ALT(g,a) *(gpio+(((g)/10))) |= (((a)<=3?(a)+4:(a)==4?3:2)<<(((g)%10)*3))
#define GPIO_SET *(gpio+7)
#define GPIO_CLR *(gpio+10)
#define GET_GPIO(g) (*(gpio+13)&(1<<g))

uint8_t counter = 0;
int dht11_dat[5] = { 0, 0, 0, 0, 0 };
int mem_fd;
void *gpio_map;
volatile unsigned *gpio;

char xml_send[] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><iotDevices> \
		   <device id=\"A1\" name=\"磁簧\" status=\"\" action=\"disable\" alarm=\"true\"></device>\
		   <device id=\"B1\" name=\"紅外線\" status=\"disable\" action=\"\" alarm=\"true\"></device>\
		   <device id=\"C1\" name=\"按鈕\" status=\"disable\" action=\"\" alarm=\"false\"></device>\
		   <device id=\"D1\" name=\"溫度\" status=\"20\" action=\"\" alarm=\"false\"></device>\
		   <device id=\"E1\" name=\"溼度\" status=\"80\" action=\"\" alarm=\"false\"></device>\
		   <device id=\"F1\" name=\"一氧化碳\" status=\"disable\" action=\"\" alarm=\"false\"></device>\
		   <device id=\"G1\" name=\"蜂鳴器\" status=\"disable\" action=\"disable\" alarm=\"false\"></device></iotDevices>";

char *head_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
char *tag_start_iotDevices = "<iotDevices>";
char tag_device_with_att[100] = "<device id=\"A1\" name=\"磁簧\" status=\"\" action=\"enable\" alarm=\"true\"></device>";
char *tag_end_iotDevices = "</iotDevices>";

/******************************開始GPIO判斷***********************************/

/*adc參數設定*/
int readadc(int ch)
{
	int errno;
	unsigned char data[3];
	unsigned int adcout;
	wiringPiSPISetup(0, 500000);

	data[0] = 1;
	data[1] = (8 + ch) << 4;
	data[2] = 0;

	errno = wiringPiSPIDataRW(0, data, 3);
	adcout = ((data[1] & 0x03) << 8) + data[2];

	return adcout;
}

/*溫溼度參數設定*/
void read_dht11_dat(int *T,int *R)
{
	uint8_t laststate = HIGH;
	uint8_t counter = 0;
	uint8_t j = 0, i;
	float f;

	dht11_dat[0] = dht11_dat[1] = dht11_dat[2] = dht11_dat[3] = dht11_dat[4] = 0;

	pinMode(DHTPIN, OUTPUT);
	digitalWrite(DHTPIN, LOW);
	delay(18);

	digitalWrite(DHTPIN, HIGH);
	delayMicroseconds(40);

	pinMode(DHTPIN, INPUT);
	for (i = 0; i < MAXTIMINGS; i++) {
		counter = 0;
		while (digitalRead(DHTPIN) == laststate) {
			counter++;
			delayMicroseconds(1);
			if (counter == 255) {
				break;
			}
		}

		laststate = digitalRead(DHTPIN);

		if (counter == 255)
			break;

		if ((i >= 4) && (i % 2 == 0)) {
			dht11_dat[j / 8] <<= 1;
			if (counter > 16)
				dht11_dat[j / 8] |= 1;
			j++;
		}
	}

	if ((j >= 40) && (dht11_dat[4] == ((dht11_dat[0] + 
					    dht11_dat[1] + 
					    dht11_dat[2] + 
					    dht11_dat[3] & 0xFF)))) {
		f = dht11_dat[2] * 9. / 5. + 32;
		printf("Humidity = %d.%d %% Temperauure = %d.%d *C(%.1f*F)\n",
				dht11_dat[0], dht11_dat[1], dht11_dat[2], dht11_dat[3], f);

		if (dht11_dat[1] > 4){
			*T = dht11_dat[0]+1;
		}else{
			*T = dht11_dat[0];
		}
		if (dht11_dat[3] > 4){
			*R = dht11_dat[2]+1;
		}else{
			*R = dht11_dat[2];
		}
	} else {
		printf("Data not good, skip\n");
	}
}

/***GPIO腳位設定****/
void setup_io()
{
	//open /dev/mem
	if ((mem_fd = open("/dev/mem", O_RDWR | O_SYNC)) < 0) {
		printf("can't open /dev/mem \n");
		exit(-1);
	}

	// map GPIO
	gpio_map = mmap(NULL,
			BLOCK_SIZE,
			PROT_READ | PROT_WRITE, MAP_SHARED, mem_fd, GPIO_BASE);

	close(mem_fd);

	if (gpio_map == MAP_FAILED) {
		printf("mmap error %d\n", (int) gpio_map);
		exit(-1);
	}

	gpio = (volatile unsigned *) gpio_map;
}

/*主程式*/
int main(int argc, char **argv)
{
	int sockfd;
	struct sockaddr_in addr;
	char buffer[4096];
	char VSbuffer[4096]="";
	int ret, idx;
	struct iot_devices_xml_resolved *pdevices;

	setup_io();
	INP_GPIO(13);//PIR led output
	OUT_GPIO(13);
	INP_GPIO(25);//Relay output
	OUT_GPIO(25);
	INP_GPIO(26);//PIR input
	INP_GPIO(23);//button input
	INP_GPIO(21);
	OUT_GPIO(21);
	INP_GPIO(22);  //Reed input
	INP_GPIO(3);   //Read power
	OUT_GPIO(3);

	GPIO_SET = 1 << 21 ;

	int rst=0;
	int ch;
	char *s1 = "enable";
	char *s2 = "disable";
	int pirflag = 0;
	int pirtimeflag = 0;
	int buttonflag=0;
	int buttontimeflag=0;
	int T=0;
	int R=0;
	int firstsendflag = 0; 
	char humi[16] = "";
	char tmper[16] = "";
	char enable[] = "enable";
	char disable[] = "disable";
	char truemark[] = "true";
	char falsemark[] = "false";
	char null[16] = "";
	int Reedwarnningflag = 0;
	int speakerflag = 0;
	char speakerlastaction[16] = "";
	char reedlastaction[16] = "";

	pdevices = parseDoc(xml_send);
	wiringPiSetupPhys();

	while (1) {
		/* Create socket */
		sockfd = socket(AF_INET, SOCK_STREAM, 0);
		if (sockfd < 0) {
			perror("socket");
			exit(1);
		}
		bzero(&addr, sizeof(addr));
		addr.sin_family = AF_INET;
		addr.sin_port = htons(PORT_NUMBER);
		addr.sin_addr.s_addr = inet_addr(SERVER_IP);

		/* try connect to server */
		ret = connect(sockfd, (struct sockaddr *) &addr, sizeof(addr));
		if (ret < 0) {
			perror("connect");
			exit(1);
		}
		printf("Success connect to Server !\n");


		/**********************開始週邊判斷***********************/
#if 1
		//read smoke
		ch = 1;
		rst = readadc(ch);
		printf("smoke value is %d\n", rst);
		if(rst > 400)
			strcpy(pdevices->devices[5].alarm, "true");
		else
			strcpy(pdevices->devices[5].alarm, "false");
#endif		
#if 1
		//read temp and humidity
		read_dht11_dat(&T, &R);
		int TT = T+20;
		sprintf(humi,"%d",R);
		sprintf(tmper,"%d",TT);
		strcpy(pdevices->devices[4].status, tmper);
		strcpy(pdevices->devices[3].status, humi);
#endif
#if 1
		//Reed control
		GPIO_SET = 1 << 3;

		int Reed_action_enable = strcmp(pdevices->devices[0].action, enable);
		int Reed_action_disable = strcmp(pdevices->devices[0].action, disable);
		int Reed_action_null = strcmp(pdevices->devices[0].action, null);

		if (Reed_action_null == 0){
			strcpy(pdevices->devices[0].status, reedlastaction);
			goto reed_action;
		}
		if (Reed_action_null != 0){
			strcpy(reedlastaction, pdevices->devices[0].action);
reed_action:
			if (Reed_action_disable == 0){
				strcpy(pdevices->devices[0].status, "disable");
				Reedwarnningflag=0;
				if (GET_GPIO(22)){
					strcpy(pdevices->devices[0].alarm, "true");
				}else{
					strcpy(pdevices->devices[0].alarm, "false");
				}
			}
			if (Reed_action_enable == 0){
				strcpy(pdevices->devices[0].status, "enable");
				if (GET_GPIO(22)){
reed_warnning_conti:
					GPIO_SET = 1 << 25;
					strcpy(pdevices->devices[0].alarm, "true");
					strcpy(pdevices->devices[6].alarm, "true");
					Reedwarnningflag=1;
				}else{
					if (Reedwarnningflag == 0){
						strcpy(pdevices->devices[0].alarm, "false");
					}
					if (Reedwarnningflag == 1){
						goto reed_warnning_conti;
					}
				}
			}
		}
#endif
#if 1
		//蜂鳴器
		int speaker_action_null = strcmp(pdevices->devices[6].action, null);
		int speaker_action_disable = 0;
		int speaker_action_enable = 0;

		if (speaker_action_null == 0){
			strcpy(pdevices->devices[6].action, speakerlastaction);
			goto speaker_action;
		}
		if (speaker_action_null != 0){
			strcpy(speakerlastaction, pdevices->devices[6].action);
speaker_action:
			speaker_action_disable = strcmp(pdevices->devices[6].action, disable);
			speaker_action_enable = strcmp(pdevices->devices[6].action, enable);
			if (speaker_action_disable == 0){
				GPIO_CLR = 1 << 25;
				strcpy(pdevices->devices[6].alarm, "false");
			}
			if (speaker_action_enable == 0){
				GPIO_SET = 1 << 25;
				strcpy(pdevices->devices[6].alarm, "true");
			}
		}
#endif
#if 1
		//PIR control
		if(!buttonflag){
			if(GET_GPIO(26)&& (pirflag<10)){
				strcpy(pdevices->devices[1].alarm, "false");
				pirflag++;
				GPIO_SET = 1 << 13;
				printf("prowler confirming.....\n");
			}else if (GET_GPIO(26)&& (pirflag>=10)){
				strcpy(pdevices->devices[1].alarm, "true");
				GPIO_SET = 1 << 13;
				printf("===* Prowler Detected!! *===\n");
				pirflag = 0;
			}else{
				strcpy(pdevices->devices[1].alarm, "false");
				GPIO_CLR = 1 << 13;
				printf("===**** PIR standby!! ****===\n");
				pirflag = 0;
			}
		}else{
			strcpy(pdevices->devices[1].status, "disable");

		}
#endif
#if 1
		//button control via 60 seconds timer
		if (GET_GPIO(23)) {
			strcpy(pdevices->devices[2].alarm, "true");
			buttonflag = 1;		    
		}else
			strcpy(pdevices->devices[2].alarm, "false");
		if (buttonflag && buttontimeflag<60)
			buttontimeflag++;
		if (buttontimeflag >= 60){
			buttonflag = 0;
			buttontimeflag = 0;
			strcpy(pdevices->devices[2].alarm, "false");
		}
#endif
		/**********************結束週邊判斷***********************/

		/**********************開始傳送接收***********************/
		/*PI包裝XML,傳送資料*/
		bzero(buffer, sizeof(buffer));
		strcat(buffer, head_xml);
		strcat(buffer, tag_start_iotDevices);
		for (idx=0; idx<7; idx++) { 
			memset (tag_device_with_att, 0, sizeof tag_device_with_att);
			sprintf (tag_device_with_att, "<device id=\"%s\" name=\"%s\" status=\"%s\" action=\"%s\" alarm=\"%s\"></device>", 
					pdevices->devices[idx].id, 
					pdevices->devices[idx].name, 
					pdevices->devices[idx].status, 
					pdevices->devices[idx].action, 
					pdevices->devices[idx].alarm);
			strcat(buffer, tag_device_with_att);
		} 
		strcat(buffer, tag_end_iotDevices);
		ret = send(sockfd, buffer, sizeof buffer, 0);
		if (ret < 0) {
			perror("ERROR writing to socket");
			exit(1);
		}

		/*PI接收server資料*/
		recv(sockfd, buffer, sizeof(buffer), 0);
		int VS = strcmp(buffer, VSbuffer);
		if ( VS == 0)
			printf("NULL\n");
		else{
			pdevices = parseDoc(buffer);
		}
		close(sockfd);
		sleep(1);
	}
	return 0 ;
}
