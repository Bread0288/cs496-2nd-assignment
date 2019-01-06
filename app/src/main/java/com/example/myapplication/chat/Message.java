package com.example.myapplication.chat;

public class Message {
    private String name;
    String message ;
    String phonenumber;

    public  Message(){    }

    public Message(String name, String message, String phonenumber) {
        this.name = name;
        this.message = message;
        this.phonenumber = phonenumber;
    }
        public String getName() {
            return name;
        }
        public void setname(String nickname) {
            this.name = name;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public void setPhonenumber(String phonenumber){
            this.phonenumber = phonenumber;

        }
        public String getPhonenumber() {
            return phonenumber;

        }
}