package com.iisysgroup.androidlite.login;

import java.io.Serializable;

/**
 * Created by Bamitale@Itex on 04/05/2017.
 */

public class Service implements Serializable {

    public String name;
    public int icon;
    public Type type = Type.VALUE;
    public Product products[];


    public Service(String name, int icon, Product[] products) {
        this.name = name;
        this.icon = icon;
        this.products = products;
    }


    public Service(String name, int icon, Type type, Product[] products) {
        this(name, icon, products);
        this.type = type;
    }


    @Override
    public String toString() {
        return name;
    }

    public enum Type {
        AIRTIME, PIN, PLAN, VALUE, PLAN_VALUE, PIN_VALUE
    }


    public static class Product implements Serializable {
        public String name, requestCode, proxyCode;

        public Product(String name, String requestCode, String proxyCode) {
            this.name = name;
            this.requestCode = requestCode;
            this.proxyCode = proxyCode;
        }

        @Override
        public String toString() {
            return requestCode;
        }
    }


}
