// File: FileLogger.java
package com.example.projectmanagement.utils;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Helpers {
    public static String parseError(VolleyError error) throws JSONException, UnsupportedEncodingException {
        String errorMessage = "Lỗi không xác định";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            String errorBody = new String(error.networkResponse.data, "UTF-8");
            JSONObject json = new JSONObject(errorBody);
            errorMessage = "Lỗi: " + json.optString("error");
        } else if (error.getMessage() != null) {
            errorMessage = error.getMessage();
        }
        return errorMessage;
    }

    public static String createImageUrlEndpoint(String filename) {
        return ApiConfig.BASE_URL+"/files/images/" + filename;
    }
}
