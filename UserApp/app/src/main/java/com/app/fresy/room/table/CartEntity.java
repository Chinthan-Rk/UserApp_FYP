package com.app.fresy.room.table;

import com.app.fresy.model.Image;
import com.app.fresy.model.Product;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartEntity {

    @PrimaryKey
    private long id = -1;

    @ColumnInfo(name = "name")
    private String name = null;

    @ColumnInfo(name = "short_description")
    private String short_description = null;

    // price
    @ColumnInfo(name = "price")
    private String price = null;

    // image
    @ColumnInfo(name = "image")
    private String image = null;

    // stock
    @ColumnInfo(name = "manage_stock")
    private boolean manage_stock = false;

    @ColumnInfo(name = "stock_status")
    private String stock_status = null;

    @ColumnInfo(name = "stock_quantity")
    private Long stock_quantity = 0L;

    @ColumnInfo(name = "amount")
    private long amount = 0;

    @ColumnInfo(name = "saved_date")
    private long saved_date = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isManage_stock() {
        return manage_stock;
    }

    public void setManage_stock(boolean manage_stock) {
        this.manage_stock = manage_stock;
    }

    public String getStock_status() {
        return stock_status;
    }

    public void setStock_status(String stock_status) {
        this.stock_status = stock_status;
    }

    public Long getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(Long stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public long getSaved_date() {
        return saved_date;
    }

    public void setSaved_date(long saved_date) {
        this.saved_date = saved_date;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public static CartEntity entity(Product p) {
        CartEntity entity = new CartEntity();
        entity.setId(p.id);
        entity.setName(p.name);
        entity.setShort_description(p.short_description);
        entity.setPrice(p.price);
        if (p.images != null && p.images.size() > 0) {
            entity.setImage(p.images.get(0).src);
        } else {
            entity.setImage(null);
        }
        entity.setManage_stock(p.manage_stock);
        entity.setStock_status(p.stock_status);
        entity.setStock_quantity(p.stock_quantity);
        return entity;
    }

    public Product original() {
        Product p = new Product();
        p.id = getId();
        p.name = getName();
        p.short_description = getShort_description();
        p.price = getPrice();
        if(getImage() != null) p.images = new ArrayList<>(Arrays.asList(new Image(getImage())));
        p.manage_stock = isManage_stock();
        p.stock_status = getStock_status();
        p.stock_quantity = getStock_quantity();
        return p;
    }
}
