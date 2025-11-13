// File: src/main/java/com/example/braingate/MyApplication.java
package com.example.braingate;

import android.app.Application;

public class MyApplication extends Application {

    // This is the string that will be accessible everywhere.
    private String opApp;

   // @Override                                                                                                                                       `
    public void onCreate() {
        super.onCreate();
        // Initialize the string with a default value if needed.
       opApp = "";
    }

    public String getGlobalString() {
        return opApp;
    }

    public void setGlobalString(String newString) {
        this.opApp = newString;
    }
}
