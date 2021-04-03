package com.app.fresy.model;

public class Coupon {

    public String status = null;
    public String code = null;
    public String message = null;

    public Long id = -1L;
    public String type;
    public String created_at;
    public String updated_at;
    public String amount;
    public boolean individual_use;
    public Integer usage_limit;
    public Integer usage_count;
    public String expiry_date;
    public boolean enable_free_shipping;
    public boolean exclude_sale_items;
    public String minimum_amount;
    public String maximum_amount;
    public String description;

}
