package com.example.groupassignment2app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.example.groupassignment2app.R;
import com.example.groupassignment2app.model.Item;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ImageUtil {

    
    private static final int MAX_SIZE = 800;
    private static final int JPEG_QUALITY = 65;

    
    private static final int CHAT_MAX_SIZE = 600;
    private static final int CHAT_QUALITY = 50;

    public static String uriToChatBase64(Context context, Uri uri) {
        return encode(context, uri, CHAT_MAX_SIZE, CHAT_QUALITY);
    }

    
    public static String uriToBase64(Context context, Uri uri) {
        return encode(context, uri, MAX_SIZE, JPEG_QUALITY);
    }

    private static String encode(Context context, Uri uri, int maxSize, int quality) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(in);
            if (in != null) in.close();
            if (original == null) return null;

            Bitmap scaled = scaleDown(original, maxSize);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out);
            byte[] bytes = out.toByteArray();

            if (bytes.length > 900_000) {
                out.reset();
                scaled.compress(Bitmap.CompressFormat.JPEG, 40, out);
                bytes = out.toByteArray();
            }
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap scaleDown(Bitmap src, int maxSize) {
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float ratio = Math.min((float) maxSize / w, (float) maxSize / h);
        return Bitmap.createScaledBitmap(src, Math.round(w * ratio), Math.round(h * ratio), true);
    }

    public static Bitmap base64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    
    public static void loadInto(ImageView view, Item item) {
        if (item != null && item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            Bitmap bmp = base64ToBitmap(item.getImageBase64());
            if (bmp != null) {
                view.setImageBitmap(bmp);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            }
        }
        if (item != null && item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(view.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_item)
                    .error(R.drawable.ic_placeholder_item)
                    .centerCrop()
                    .into(view);
            return;
        }
        view.setImageResource(R.drawable.ic_placeholder_item);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}