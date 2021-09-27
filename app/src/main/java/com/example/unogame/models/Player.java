package com.example.unogame.models;

import java.io.Serializable;

public class Player implements Serializable {

    String id, name, photoref;

    public Player(){

    }
    public Player(String id, String name, String photoref) {
        this.id = id;
        this.name = name;
        this.photoref = photoref;
    }

    public String getPhotoref() {
        return photoref;
    }

    public void setPhotoref(String photoref) {
        this.photoref = photoref;
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

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", photoref='" + photoref + '\'' +
                '}';
    }
}
