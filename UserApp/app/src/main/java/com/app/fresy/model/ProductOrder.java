package com.app.fresy.model;

import java.io.Serializable;

public class ProductOrder implements Serializable {

    public Long id;
    public Long product_id;
    public String name;
    public Long quantity;

    public String subtotal;
    public String total;
    public String price;

}
