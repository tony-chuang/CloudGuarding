#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <libxml/xmlmemory.h>
#include <libxml/parser.h>
#include <libxml/encoding.h>
#include <libxml/xmlwriter.h>
#include <libxml/xmlversion.h>
#include "iot_device.h"

void getAttribute(xmlDocPtr doc, xmlNodePtr cur, struct iot_device *piot)
{

    xmlChar *id, *name, *status, *action, *alarm;
    cur = cur->xmlChildrenNode;
    while (cur != NULL) {
	if ((!xmlStrcmp(cur->name, (const xmlChar *) "device"))) {

	    strcpy(piot->id ,id = xmlGetProp(cur, "id"));
	    strcpy(piot->name, name = xmlGetProp(cur, "name"));
	    strcpy(piot->status, status = xmlGetProp(cur, "status"));
	    strcpy(piot->action, action = xmlGetProp(cur, "action"));
	    strcpy(piot->alarm, alarm = xmlGetProp(cur, "alarm"));

	    printf("id: %s name: %s status:%s action:%s alarm:%s\n", id, name,
		   status, action, alarm);

	    xmlFree(name);
	}
	cur = cur->next;
    }
    return;
}

struct iot_devices_xml_resolved *parseDoc(char *xmlFile)
{
    xmlDocPtr doc;
    xmlNodePtr cur;
    int idx; 
    //static struct iot_device device;
    struct iot_devices_xml_resolved *pdevices;
   
    //if (pdevices != NULL)
    //    free(pdevices);
    pdevices = malloc(sizeof(struct iot_devices_xml_resolved));
 
    printf("Parse in Memory !\n");
    doc = xmlReadMemory(xmlFile, strlen(xmlFile), NULL, "UTF-8", 0);
    if (doc == NULL) {
	fprintf(stderr, "Document not parsed successfully. \n");
	return NULL;
    }

    cur = xmlDocGetRootElement(doc);

    if (cur == NULL) {
	fprintf(stderr, "empty document\n");
	xmlFreeDoc(doc);
	return NULL;
    }

    if (xmlStrcmp(cur->name, (const xmlChar *) "iotDevices")) {
	fprintf(stderr,
		"document of the wrong type, root node != iotDevices");
	xmlFreeDoc(doc);
	return NULL;
    }

    cur = cur->xmlChildrenNode;
    idx = 0;
    while (cur != NULL) {
        if ((!xmlStrcmp(cur->name, (const xmlChar *) "device"))) {
            strcpy(pdevices->devices[idx].id ,xmlGetProp(cur, "id"));
            strcpy(pdevices->devices[idx].name, xmlGetProp(cur, "name"));
            strcpy(pdevices->devices[idx].status, xmlGetProp(cur, "status"));
            strcpy(pdevices->devices[idx].action, xmlGetProp(cur, "action"));
            strcpy(pdevices->devices[idx].alarm, xmlGetProp(cur, "alarm"));
            //printf("id = %s\n",pdevices->devices[idx].id);
            idx++;
            if (idx > IOT_DEVICE_MAX) break; 
        }
        cur = cur->next;
    }

    //getAttribute(doc, cur, &device);
    //return &device;
    return pdevices;
}

int writeToXML(struct iot_devices_xml_resolved *pdevices, char *buffer)
{
  int rc, idx;
  xmlTextWriterPtr writer;
  xmlBufferPtr buf;

  /* Create a new XML buffer */
  buf = xmlBufferCreate();
  if (buf == NULL) {
	fprintf(stderr, "Error creating the XML buffer !\n");
     return -1;
  }
  /* Create a new xmlWriter */
  writer = xmlNewTextWriterMemory(buf,0);
  if (writer == NULL) {
	fprintf(stderr, "Error creating the XML writer !\n");
     return -1;
  }
  /* Start the document with the xml */
  rc = xmlTextWriterStartDocument(writer, NULL, "UTF-8", NULL);
  if (rc < 0) {
	fprintf(stderr, "Error at xmlTextWriterStartDocument !\n");
     return -1;
  }
  /* Start an element, since this is the first element 
     will be the root element of the document. */    
  rc = xmlTextWriterStartElement(writer, BAD_CAST "iotDevices");
  if (rc < 0) {
	fprintf(stderr, "Error at xmlTextWriterStartElement !\n");
     return -1;
  }
  for (idx = 0; idx < IOT_DEVICE_MAX; idx++) {
     /* Start an element, as child of root */
     rc = xmlTextWriterStartElement(writer, BAD_CAST "device");
     if (rc < 0) {
	fprintf(stderr, "Error at xmlTextWriterStartElement !\n");
        return -1;
     }
     /*printf("write attribute %s\n",pdevices->devices[idx].id); 
     printf("write attribute %s\n",pdevices->devices[idx].name); 
     printf("write attribute %s\n",pdevices->devices[idx].status); 
     printf("write attribute %s\n",pdevices->devices[idx].action); 
     printf("write attribute %s\n",pdevices->devices[idx].alarm); 
     printf("\n"); */
     xmlTextWriterWriteAttribute(writer,BAD_CAST "id",BAD_CAST pdevices->devices[idx].id);
     xmlTextWriterWriteAttribute(writer,BAD_CAST "name",BAD_CAST pdevices->devices[idx].name);
     xmlTextWriterWriteAttribute(writer,BAD_CAST "status",BAD_CAST pdevices->devices[idx].status);
     xmlTextWriterWriteAttribute(writer,BAD_CAST "action",BAD_CAST pdevices->devices[idx].action);
     xmlTextWriterWriteAttribute(writer,BAD_CAST "alarm",BAD_CAST pdevices->devices[idx].alarm);

     /* Close the element */
     rc = xmlTextWriterEndElement(writer);
     if (rc < 0) {
	fprintf(stderr, "Error at xmlTextWriterEndElement !\n");
        return -1;
     }

  }
 

  /* Close the element */
  rc = xmlTextWriterEndElement(writer);
  if (rc < 0) {
     fprintf(stderr, "Error at xmlTextWriterEndElement !\n");
     return -1;
  }

  /* Close the document */
  rc = xmlTextWriterEndDocument(writer);
  if (rc < 0) {
	fprintf(stderr, "Error at xmlTextWriterEndDocument !\n");
     return -1;
  }
  xmlFreeTextWriter(writer);
  
  strcpy(buffer, (const char *) buf->content);
  xmlBufferFree(buf);
  //printf("%s\n",buffer);
  return 0;
}// end of writeToXML
