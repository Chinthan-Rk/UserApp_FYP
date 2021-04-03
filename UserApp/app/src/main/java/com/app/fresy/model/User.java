package com.app.fresy.model;

import android.text.TextUtils;

import com.app.fresy.connection.param.ParamRegisterUpdate;

import java.io.Serializable;

public class User implements Serializable {

    public long id = -1;
    public String email = "";
    public String username = "";
    public String first_name = "";
    public String last_name = "";
    public String avatar_url = "";
    public String date_modified = "";

    public BillingShipping billing = null;
    public BillingShipping shipping = null;

    public String getName() {
        String name = first_name + " " + last_name;
        if (TextUtils.isEmpty(name.trim())) {
            return username;
        }
        return name.trim();
    }

}