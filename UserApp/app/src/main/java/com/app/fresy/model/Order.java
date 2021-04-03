package com.app.fresy.model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

    public Long id;
    public String number;
    public String order_key;
    public String status;
    public String currency;
    public String date_created;
    public String date_modified;
    public String discount_total;
    public String shipping_total;
    public String total;
    public String total_tax;
    public String customer_note;

    public BillingShipping billing;
    public BillingShipping shipping;
    public String payment_method;
    public String payment_method_title;
    public String date_paid;
    public String date_completed;

    public List<ProductOrder> line_items;
    public List<ShippingOrder> shipping_lines;
    public List<CouponOrder> coupon_lines;
}
