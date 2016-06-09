/*
 * Copyright (C) 2016 Ari Morales arimoralesjordan@gmail.com
 *
 *
 */
package com.coreveillance.ionkepler;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 4/6/16.
 */
public class BarCodes {
    List<serial> serials = new ArrayList<serial>();
    public void add_serial(String number,String product){
        if (!this.exist_serial(number,product)){
            this.serials.add(new serial(number,product));
        }
    }
    private boolean exist_serial(String number,String product){
        boolean retorno=false;
        for (serial serial : serials) {
            if (serial.number.equals(number)){
                retorno=true;
            }
        }
        return retorno;
    }
    class serial{
        String number;
        String product;
        serial(String number,String product){
            this.number=number;
            this.product=product;
        }
    }
}