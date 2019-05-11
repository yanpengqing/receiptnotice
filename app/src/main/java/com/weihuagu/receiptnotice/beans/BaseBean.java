package com.weihuagu.receiptnotice.beans;

public class BaseBean {


    /**
     * message : 已经绑定过了
     * data :
     * status : 0
     */

    private String message="";
    private String data;
    private int status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
