package com.example.projectmanagement.api;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.utils.UserPreferences;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static ApiClient instance;
    private final RequestQueue requestQueue;
    private final Context context;

    private ApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(this.context);
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

    public void addToRequestQueue(Request<?> request) {
        requestQueue.add(request);
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        UserPreferences prefs = new UserPreferences(context);
        String token = prefs.getJwtToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Cookie", "user_auth_token=" + token);
            Log.d(TAG, "Adding JWT token to request");
        } else {
            Log.w(TAG, "No JWT token found in preferences");
        }
        return headers;
    }

}
