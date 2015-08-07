package com.qianmi.common.config;

/**
 *
 * Created by aqlu on 15/5/19.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("config.zk.connection.str", "172.19.65.33:2181");
        System.setProperty("config.zk.rootPath", "/config");
        System.setProperty("config.version", "1.0");

        PropertiesConfig propertiesConfig = PropertiesConfig.getInstance();

        String int_property_key = String.valueOf(propertiesConfig.get("property-group1", "int_property_key"));
        String string_property_key = String.valueOf(propertiesConfig.get("property-group1", "string_property_key"));

        System.out.println("int_property_key:" + int_property_key);
        System.out.println("string_property_key:" + string_property_key);

//        Thread.sleep(200 * 1000);
//        int_property_key = String.valueOf(propertiesConfig.get("property-group1", "int_property_key"));
//        System.out.println("int_property_key:" + int_property_key);
    }
}
