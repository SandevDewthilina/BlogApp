package com.javapoint.blogapp;


import java.util.Date;

public class BlogPost extends BlogPostId{

    public String user_id, image_uri, desc, image_thumb;

    public Date timestamp;



    public BlogPost () {
    }

    public BlogPost(String user_id, String image_uri, String desc, String image_thumb, Date timestamp) {

        this.user_id = user_id;
        this.image_uri = image_uri;
        this.desc = desc;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
