package com.example.andrea.myapplication.other_class;

import android.text.TextUtils;

/*
 * Created by Andrea on 21/12/2014.
 */
public class StringManager {

    public static final String COMMA = "__";

    public String merge (String[] array){
        return TextUtils.join(COMMA, array);
    }

    public String[] divide (String str){
        return str.split(COMMA);
    }

    public String cut (String str){
        return str.split(COMMA)[0];
    }

}
