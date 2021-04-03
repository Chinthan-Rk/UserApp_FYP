package com.app.fresy.connection.param;

import com.app.fresy.model.BillingShipping;
import com.app.fresy.model.CouponOrder;
import com.app.fresy.model.ProductOrder;
import com.app.fresy.model.ShippingOrder;

import java.io.Serializable;
import java.util.List;

public class ParamOrder implements Serializable {

    public Long customer_id;
    public String payment_method;
    public String payment_method_title;
    public String customer_note;
    public String status;

    public BillingShipping billing = null;
    public BillingShipping shipping = null;

    public List<ProductOrder> line_items = null;
    public List<ShippingOrder> shipping_lines = null;
    public List<CouponOrder> coupon_lines = null;

}
