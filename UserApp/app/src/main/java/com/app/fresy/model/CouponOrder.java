package com.app.fresy.model;

import java.io.Serializable;

public class CouponOrder implements Serializable {

    public Long id;
    public String code;
    public String discount;

    public CouponOrder() {
    }

    public CouponOrder(String code) {
        this.code = code;
    }
}
