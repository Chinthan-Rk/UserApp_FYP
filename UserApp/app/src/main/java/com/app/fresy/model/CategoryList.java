package com.app.fresy.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryList {

    public List<Category> categories = new ArrayList<>();

    public CategoryList() {
    }

    public CategoryList(List<Category> categories) {
        this.categories = categories;
    }

}
