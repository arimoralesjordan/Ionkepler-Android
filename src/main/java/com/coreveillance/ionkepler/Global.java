/*
 * Copyright (C) 2016 Ari Morales arimoralesjordan@gmail.com
 *
 *
 */
package com.coreveillance.ionkepler;

import android.app.Application;
import android.hardware.camera2.params.StreamConfigurationMap;

import org.json.JSONObject;

/**
 * Created by Ari Morales on 4/6/16.
 */
public class Global{
    static BarCodes barCodes;
    public static String Esignature="";
    public static JSONObject obj;
    Global(){
        barCodes = new BarCodes();
    }
    public BarCodes getBarCodes() {
        barCodes = new BarCodes();
        return barCodes;
    }
    public String getEsignature() {
        return Esignature;
    }
    public void setBarCodes(BarCodes barCodes) {
        Global.barCodes = barCodes;
    }
}
