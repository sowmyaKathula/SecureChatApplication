package com.sowmya.securechat;

import java.util.Date;

/**
 * Created by sowmya on 5/12/17.
 */

public class Message {
    private String sender;
    private String receiver;
    private String msg_text;
    private Date date;

    public Message(){
    }


    public Message(String sender, String receiver, String msg_text, Date date) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg_text = msg_text;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg_text() {
        return msg_text;
    }

    public void setMsg_text(String msg_text) {
        this.msg_text = msg_text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
