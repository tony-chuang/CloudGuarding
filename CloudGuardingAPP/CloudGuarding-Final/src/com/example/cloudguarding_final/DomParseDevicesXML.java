package com.example.cloudguarding_final;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DomParseDevicesXML {
	public static List<IoTdevice> ReadEquipmentXML(InputStream inStream) throws Exception{
		List<IoTdevice> equipments=new ArrayList<IoTdevice>();
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder=factory.newDocumentBuilder();
		Document document=builder.parse(inStream);  
		Element root=document.getDocumentElement();
		NodeList nodes=root.getElementsByTagName("device");
		
		for(int i=0;i<nodes.getLength();i++){
			Element equipmentElement=(Element)nodes.item(i);
			IoTdevice equipment=new IoTdevice();
			equipment.setId(equipmentElement.getAttribute("id"));
			equipment.setName(equipmentElement.getAttribute("name"));
			equipment.setStatus(equipmentElement.getAttribute("status"));
			equipment.setAction(equipmentElement.getAttribute("action"));
			equipment.setAlarm(equipmentElement.getAttribute("alarm"));
			equipments.add(equipment);
		}
		
		return equipments;
	}

}
