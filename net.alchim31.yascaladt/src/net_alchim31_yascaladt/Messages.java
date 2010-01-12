package net_alchim31_yascaladt;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

public class Messages {

    //private static final String RESOURCE_BUNDLE = "net_alchim31_eclipse_yascaladt.Messages";//$NON-NLS-1$

    private static ResourceBundle _resourceBundle = new ResourceBundle(){
        @Override
        public boolean containsKey(String key) {
            return true;
        }
        @Override
        public Enumeration<String> getKeys() {
            return new Vector<String>().elements();
            //return null;
        }

        @Override
        protected Object handleGetObject(String key) {
            return "xx_" + key;
        }
    };//ResourceBundle.getBundle(RESOURCE_BUNDLE);


    public static String getString(String key) {
        try {
            return _resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public static ResourceBundle getResourceBundle() {
        return _resourceBundle;
    }
}
