package com.app.fresy.model;

import java.io.Serializable;

public class Notification implements Serializable {

    public Long id;
    public String title;
    public String content;
    public String type;
    public String data;

    // extra attribute
    public Boolean read = false;
    public Long created_at;

}
