package com.app.fresy.model;

import java.io.Serializable;

public class Category implements Serializable {

    public Long id;
    public String name;
    public Integer menu_order = 0;
    public Image image;
    public boolean selected;

}
