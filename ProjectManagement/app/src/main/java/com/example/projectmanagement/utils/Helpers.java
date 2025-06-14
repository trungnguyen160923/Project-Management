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
        return ApiConfig.BASE_URL + "/files/images/" + filename;
    }

    public static String getFileExtensionFromMimeType(String mimeType) {
        if (mimeType == null) return "bin";

        mimeType = mimeType.toLowerCase(); // phòng khi server trả viết hoa

        if (mimeType.contains("pdf")) return "pdf";
        if (mimeType.contains("word") || mimeType.contains("msword") || mimeType.contains("officedocument.wordprocessingml"))
            return "doc";
        if (mimeType.contains("excel") || mimeType.contains("spreadsheet") || mimeType.contains("officedocument.spreadsheetml"))
            return "xls";
        if (mimeType.contains("powerpoint") || mimeType.contains("presentation") || mimeType.contains("officedocument.presentationml"))
            return "ppt";
        if (mimeType.contains("text")) return "txt";
        if (mimeType.contains("zip") || mimeType.contains("compressed") || mimeType.contains("application/x-zip-compressed"))
            return "zip";
        if (mimeType.contains("image/jpeg")) return "jpg";
        if (mimeType.contains("image/png")) return "png";
        if (mimeType.contains("image")) return "img"; // fallback cho các định dạng ảnh khác

        return "bin"; // không xác định
    }

    public static EnumFileType getFileCategoryFromMimeType(String mimeType) {
        if (mimeType == null) return EnumFileType.OTHER;

        mimeType = mimeType.toLowerCase();

        if (mimeType.startsWith("image/") || mimeType.contains("jpg") || mimeType.contains("jpeg") ||
                mimeType.contains("png") || mimeType.contains("webp")) {
            return EnumFileType.IMAGE;
        }

        if (
                mimeType.contains("pdf") ||
                        mimeType.contains("word") || mimeType.contains("msword") || mimeType.contains("officedocument.wordprocessingml") ||
                        mimeType.contains("excel") || mimeType.contains("spreadsheet") || mimeType.contains("officedocument.spreadsheetml") ||
                        mimeType.contains("powerpoint") || mimeType.contains("presentation") || mimeType.contains("officedocument.presentationml") ||
                        mimeType.contains("text")
        ) {
            return EnumFileType.DOCUMENT;
        }

        return EnumFileType.OTHER;
    }
}
