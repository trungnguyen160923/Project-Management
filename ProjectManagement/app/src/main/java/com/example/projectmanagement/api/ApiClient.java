package com.example.projectmanagement.api;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiClient {
    private static ApiClient instance;
    private RequestQueue requestQueue;

    private ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
