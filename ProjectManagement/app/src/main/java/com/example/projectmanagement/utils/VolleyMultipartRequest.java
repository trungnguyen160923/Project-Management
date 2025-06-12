package com.example.projectmanagement.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Map<String, String> headers;
    private final Response.Listener<NetworkResponse> listener;
    private final Map<String, DataPart> dataParts;

    public VolleyMultipartRequest(
            int method,
            String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener,
            Map<String, String> headers,
            Map<String, DataPart> dataParts
    ) {
        super(method, url, errorListener);
        this.listener = listener;
        this.headers = headers;
        this.dataParts = dataParts;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return buildMultipartBody(dataParts, getParams(), getBodyContentType());
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    // ------------------ Helper ----------------------
    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY = "----VolleyBoundary";

    private byte[] buildMultipartBody(
            Map<String, DataPart> dataParts,
            Map<String, String> params,
            String contentType
    ) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    outputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes());
                    outputStream.write((LINE_FEED).getBytes());
                    outputStream.write((entry.getValue() + LINE_FEED).getBytes());
                }
            }

            for (Map.Entry<String, DataPart> entry : dataParts.entrySet()) {
                outputStream.write(("--" + BOUNDARY + LINE_FEED).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + entry.getValue().getFileName() + "\"" + LINE_FEED).getBytes());
                outputStream.write(("Content-Type: " + entry.getValue().getType() + LINE_FEED).getBytes());
                outputStream.write((LINE_FEED).getBytes());
                outputStream.write(entry.getValue().getContent());
                outputStream.write((LINE_FEED).getBytes());
            }

            outputStream.write(("--" + BOUNDARY + "--" + LINE_FEED).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}
