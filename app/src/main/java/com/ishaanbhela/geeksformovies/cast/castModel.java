package com.ishaanbhela.geeksformovies.cast;

public class castModel {

    String name, imgPath, character;

    public castModel(String name, String imgPath, String character){
        this.name = name;
        this.imgPath = imgPath;
        this.character = character;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setCharacter(String character){
        this.character = character;
    }

    public String getCharacter(){
        return character;
    }
}
