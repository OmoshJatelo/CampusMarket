package com.smart.campusmarket;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Compresses photos on the phone and uploads them to Cloudinary (free plan, no card).
 * Uses an UNSIGNED upload preset, so no secret key is stored in the app.
 */
public final class CloudinaryUploader {

    public interface Callback {
        void onProgress(int done, int total);
        void onSuccess(List<String> urls);
        void onError(String message);
    }

    private static final int MAX_SIDE_PX = 1280;
    private static final int JPEG_QUALITY = 80;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private CloudinaryUploader() { }

    /** Uploads the photos one after another and returns their https links in the same order. */
    public static void uploadAll(Context context, List<Uri> uris, Callback cb) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            List<String> urls = new ArrayList<>();
            try {
                if (AppConfig.CLOUDINARY_CLOUD_NAME.startsWith("YOUR_")
                        || AppConfig.CLOUDINARY_UPLOAD_PRESET.startsWith("YOUR_")) {
                    throw new IOException("Cloudinary is not set up yet. Fill in AppConfig.java.");
                }
                for (int i = 0; i < uris.size(); i++) {
                    byte[] jpeg = compress(app, uris.get(i));
                    urls.add(uploadBytes(jpeg));
                    int done = i + 1;
                    MAIN.post(() -> cb.onProgress(done, uris.size()));
                }
                MAIN.post(() -> cb.onSuccess(urls));
            } catch (Exception e) {
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                MAIN.post(() -> cb.onError(msg));
            }
        });
    }

    /** Smaller preview link for list cards (saves mobile data). Other links pass through. */
    public static String thumbnail(String url) {
        if (url == null) return null;
        String marker = "/upload/";
        int i = url.indexOf(marker);
        if (url.startsWith("https://res.cloudinary.com/") && i > 0) {
            int cut = i + marker.length();
            return url.substring(0, cut) + "w_300,h_300,c_fill,q_auto/" + url.substring(cut);
        }
        return url;
    }

    // ---------------------------------------------------------------- internals

    /** Glide decodes at reduced size and fixes camera rotation; then we save as ~80% JPEG. */
    private static byte[] compress(Context ctx, Uri uri) throws Exception {
        Bitmap bitmap = Glide.with(ctx)
                .asBitmap()
                .load(uri)
                .apply(RequestOptions.downsampleOf(DownsampleStrategy.AT_MOST))
                .override(MAX_SIDE_PX, MAX_SIDE_PX)
                .submit()
                .get();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return out.toByteArray();
    }

    private static String uploadBytes(byte[] data) throws Exception {
        String boundary = "----CampusMarket" + System.currentTimeMillis();
        URL url = new URL("https://api.cloudinary.com/v1_1/"
                + AppConfig.CLOUDINARY_CLOUD_NAME + "/image/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                writeText(os, "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n"
                        + AppConfig.CLOUDINARY_UPLOAD_PRESET + "\r\n");
                writeText(os, "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"file\"; filename=\"photo.jpg\"\r\n"
                        + "Content-Type: image/jpeg\r\n\r\n");
                os.write(data);
                writeText(os, "\r\n--" + boundary + "--\r\n");
            }

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String body = readAll(stream);
            if (code < 200 || code >= 300) {
                throw new IOException("Cloudinary error " + code + ": " + errorMessage(body));
            }
            return new JSONObject(body).getString("secure_url");
        } finally {
            conn.disconnect();
        }
    }

    private static void writeText(OutputStream os, String text) throws IOException {
        os.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return out.toString("UTF-8");
    }

    private static String errorMessage(String body) {
        try {
            return new JSONObject(body).getJSONObject("error").getString("message");
        } catch (Exception e) {
            return body.isEmpty() ? "no details" : body;
        }
    }
}