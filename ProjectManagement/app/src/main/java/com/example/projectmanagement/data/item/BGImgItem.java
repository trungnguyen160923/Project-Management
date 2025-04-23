package com.example.projectmanagement.data.item;

public class BGImgItem {
    private Integer img;
    private String name;

    public BGImgItem(Integer img, String name) {
        this.img  = img;
        this.name = name;
    }
    public Integer getImg() { return img; }
    public String  getName(){ return name; }
}
