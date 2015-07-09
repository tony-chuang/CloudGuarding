package com.example.cloudguarding_final;

public class IoTdevice {
	public String id;
	public String name;
	public String status;
	public String action;
	public String alarm;
	
	public IoTdevice(){}
	
	public IoTdevice(String id, String name, String status, String action, String alarm){
		this.id=id;
		this.name=name;
		this.status=status;
		this.action=action;
		this.alarm=alarm;
	}
	public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getStatus() {
        return status;
    }
 
    public void setStatus(String status) {
        this.status = status;
    }
 
    public String getAction() {
        return action;
    }
 
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getAlarm() {
        return alarm;
    }
 
    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }
}
