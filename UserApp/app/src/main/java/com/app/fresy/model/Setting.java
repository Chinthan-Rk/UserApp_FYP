package com.app.fresy.model;

import java.io.Serializable;

public class Setting implements Serializable {

    public String status;
    public String code;
    public String currency_symbol;
    public String currency_position;
    public String thousand_separator;
    public String decimal_separator;
    public Integer number_of_decimals = 0;

}