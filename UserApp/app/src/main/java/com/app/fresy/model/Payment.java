package com.app.fresy.model;

import com.app.fresy.connection.response.SubSettings;

import java.io.Serializable;

public class Payment implements Serializable {

    public String id;
    public String title;
    public String description;
    public boolean enabled;
    public Settings settings;

    public class Settings implements Serializable {
        public SubSettings instructions;
    }

}
