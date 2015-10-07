package com.stayfprod.utter.model.chat;


public class PhotoMsg extends AbstractMainMsg {

    //info thumbIndex = -1 Значит нет фамба
    public int thumbIndex;
    public int photoIndex;
    public int photoWidth;
    public int photoHeight;

    public int progressStartX;
    public int progressStartY;
}
