package com.weihuagu.receiptnotice.beans;

public class DeviceBean {
    public String deviceid;
    public String token;
    public String connectedtime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setDeviceid(String deviceid){
        this.deviceid=deviceid;
    }
    public void setConnectedtime(String connectedtime) {
        this.connectedtime = connectedtime;
    }
    public String getDeviceid() {
        return deviceid;
    }

    public String getConnectedtime() {
        return connectedtime;
    }

}
