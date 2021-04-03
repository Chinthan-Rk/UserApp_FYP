package com.app.fresy.model;

import java.io.Serializable;

public class Image implements Serializable {

    public Image() {
    }

    public Image(String src) {
        this.src = src;
    }
    public String src;
}