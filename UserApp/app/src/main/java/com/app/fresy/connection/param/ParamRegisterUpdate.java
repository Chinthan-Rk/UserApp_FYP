package com.app.fresy.connection.param;

import com.app.fresy.model.BillingShipping;

public class ParamRegisterUpdate {

    public String email = null;
    public String username = null;
    public String password = null;
    public String first_name = null;
    public String last_name = null;

    public BillingShipping billing = null;
    public BillingShipping shipping = null;

    public ParamRegisterUpdate() {
    }

    public ParamRegisterUpdate(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
