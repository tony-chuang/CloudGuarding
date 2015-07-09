#ifndef __IOT_DEVICE__H
#define __IOT_DEVICE__H

#define IOT_DEVICE_MAX	7

struct iot_device {
	char id[16];
	char name[16];
	char status[16];
	char action[16];
	char alarm[16];
};

struct iot_devices_xml_resolved {

	struct iot_device devices[IOT_DEVICE_MAX];
	int num;
};
#endif
