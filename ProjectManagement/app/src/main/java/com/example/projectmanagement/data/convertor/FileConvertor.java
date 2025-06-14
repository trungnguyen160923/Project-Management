package com.example.projectmanagement.data.convertor;

import com.example.projectmanagement.data.model.File;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileConvertor {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    public static File fromJson(JSONObject jsonObject) {
        File file = new File();

        file.setId(jsonObject.optInt("id"));
        file.setFileName(jsonObject.optString("fileName", null));
        file.setFilePath(jsonObject.optString("filePath", null));
        file.setFileSize(jsonObject.has("fileSize") ? jsonObject.optLong("fileSize") : null);
        file.setFileType(jsonObject.optString("fileType", null));
        file.setTaskID(jsonObject.optInt("taskID"));
        file.setUserID(jsonObject.optInt("userID"));

        String createdAtStr = jsonObject.optString("createdAt", null);
        String updatedAtStr = jsonObject.optString("updateAt", null);

        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            file.setCreatedAt(parseDate(createdAtStr));
        }

        if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
            file.setUpdateAt(parseDate(updatedAtStr));
        }

        return file;
    }

    private static Date parseDate(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
