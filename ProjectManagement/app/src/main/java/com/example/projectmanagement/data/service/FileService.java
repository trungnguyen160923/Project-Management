package com.example.projectmanagement.data.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.projectmanagement.utils.ApiConfig;
import com.example.projectmanagement.utils.UserPreferences;
import com.example.projectmanagement.utils.VolleyMultipartRequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileService {
    public static final String TAG = "FileService";

    public static void uploadFile(Context context, int taskId, InputStream inputStream, String fileName, String mimeType, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        try {
            byte[] fileData = getBytes(inputStream);

            // Lấy token
            UserPreferences prefs = new UserPreferences(context);
            String token = prefs.getJwtToken();

            // Chuẩn bị headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", "user_auth_token=" + token);

            Map<String, VolleyMultipartRequest.DataPart> files = new HashMap<>();
            files.put("file", new VolleyMultipartRequest.DataPart(fileName, fileData, mimeType));

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, ApiConfig.BASE_URL + "/files/upload/" + taskId, // URL của API upload
                    listener, errorListener, headers, files);

            Volley.newRequestQueue(context).add(multipartRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static Uri downloadImageToMediaStore(Context context, String imageUrl, String fileName, String mimeType) {
        try {
            // 1. Mở kết nối và tải dữ liệu từ URL

            URL url = new URL(imageUrl);
            return convertJavaUriToAndroidUri(url.toURI());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    static public Uri downloadImageAndGetUri(Context context, String imageUrl) {
        try {
            // Step 1: Open connection to image URL
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Step 2: Create a file in cache dir
            File file = new File(context.getCacheDir(), "downloaded_image_" + System.currentTimeMillis() + ".jpg");
            file.createNewFile();

            // Step 3: Write input stream to file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream output = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            // Step 4: Convert file to Uri using FileProvider (safe way)
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Uri convertJavaUriToAndroidUri(URI javaUri) {
        if (javaUri == null) {
            Log.e(TAG, "Java URI is null");
            return null;
        }
        try {
            return Uri.parse(javaUri.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error converting URI to Android Uri: " + e.getMessage());
            return null;
        }
    }
}
