package com.app.fresy.model;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {

    public Long id;
    public String name;
    public String short_description;
    public String description;

    // price
    public String price;
    public String sale_price;
    public String regular_price;

    // stock
    public Boolean manage_stock;
    public String stock_status;
    public Long stock_quantity;

    // others
    public List<Image> images;
    public List<Long> related_ids;

}
