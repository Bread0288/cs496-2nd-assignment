package com.example.myapplication.chat;

import android.graphics.Bitmap;

public class Message {
    private String name;
    String message ;
    String phonenumber;
    Bitmap image;

    public  Message(){    }


    public Message(String name, String message, String phonenumber/*, Bitmap image*/) {
        this.name = name;
        this.message = message;
        this.phonenumber = phonenumber;
        //this.image = image;
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
        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }

}