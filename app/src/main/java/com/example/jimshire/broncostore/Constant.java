package com.example.jimshire.broncostore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jimshire on 10/3/17.
 */

public final class Constant {
    public static final List<Integer> QUANTITY_LIST = new ArrayList<Integer>();

    static {
        for (int i = 1; i < 11; i++) QUANTITY_LIST.add(i);
    }

    public static final String MENU_REQUEST_URL = "http://54.191.37.235:8000/menu_item/Blurr002/";
    public static final String ORDER_POST_URL = "http://54.191.37.235:8000/order_item/";


    public static final String CURRENCY = "$";
}
