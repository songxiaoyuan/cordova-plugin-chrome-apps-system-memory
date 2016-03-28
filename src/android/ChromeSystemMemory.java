// Copyright (c) 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.net.MalformedURLException;  
import java.net.URL; 

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Build;
import android.util.Log;

public class ChromeSystemMemory extends CordovaPlugin {
    private static final String LOG_TAG = "ChromeSystemMemory";

    private ActivityManager activityManager;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Activity activity = cordova.getActivity();
        activityManager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("getInfo".equals(action)) {
            getInfo(args, callbackContext);
            return true;
        }
        return false;
    }


    private String getKernel() {
        Process process = null;  
        String kernelVersion = "";  
        try {  
            process = Runtime.getRuntime().exec("cat /proc/version");  
        } catch (IOException e) {  
        // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
          
          
        // get the output line  
        InputStream outs = process.getInputStream();  
        InputStreamReader isrout = new InputStreamReader(outs);  
        BufferedReader brout = new BufferedReader(isrout, 8 * 1024);  
          
          
        String result = "";  
        String line;  
        // get the whole standard output string  
        try {  
            while ((line = brout.readLine()) != null) {  
            result += line;  
        }  
        } catch (IOException e) {  
        // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
          
          
        try {  
            if (result != "") {  
                String Keyword = "version ";  
                int index = result.indexOf(Keyword);  
                line = result.substring(index + Keyword.length());  
                index = line.indexOf(" ");  
                kernelVersion = line.substring(0, index);  
            }  
        } catch (IndexOutOfBoundsException e) {  
            e.printStackTrace();  
        }  
        return kernelVersion;  
    }

    private void getInfo(final CordovaArgs args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject ret = new JSONObject();
                    MemoryInfo memoryInfo = new MemoryInfo();
                    activityManager.getMemoryInfo(memoryInfo);

                    ret.put("availableCapacity", memoryInfo.availMem);
                    ret.put("kernel", getKernel());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ret.put("capacity", memoryInfo.totalMem);
                    }

                    callbackContext.success(ret);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error occured while getting memory info", e);
                    callbackContext.error("Could not get memory info");
                }
            }
        });
    }
}
