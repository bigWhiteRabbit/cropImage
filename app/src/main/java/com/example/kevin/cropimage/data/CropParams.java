package com.example.kevin.cropimage.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by kevin on 16-6-15.
 */
public class CropParams {
    public static final String CROP_TYPE = "image/*";
    public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG.toString();

    public static final int DEFAULT_ASPECT = 1;
    public static final int DEFAULT_OUTPUT_WIDTH = 1440;
    public static final int DEFAULT_OUTPUT_HEIGHT = 908;
//    public static final int DEFAULT_COMPRESS_WIDTH = 640;
//    public static final int DEFAULT_COMPRESS_HEIGHT = 854;
//    public static final int DEFAULT_COMPRESS_QUALITY = 90;

    public Uri uri;

    public String type;
    public String outputFormat;
    public String crop;          // 发送裁剪信号

    public boolean scale;        // 是否保留比例
    public boolean returnData;   // 是否将数据保留在Bitmap中返回
    public boolean noFaceDetection;
    public boolean scaleUpIfNeeded;

    /**
     * Default is true, if set false, crop function will not work,
     * it will only pick up images from gallery or take pictures from camera.
     */
//    public boolean enable;

    /**
     * Default is false, if it is from capture and without crop, the image could be large
     * enough to trigger OOM, it is better to compress image while enable is false
     */
//    public boolean compress;

//    public boolean rotateToCorrectDirection;

//    public int compressWidth;
//    public int compressHeight;
//    public int compressQuality;

    public int aspectX;
    public int aspectY;

    public int outputX;
    public int outputY;

    public Context context;

    public CropParams(Context context) {
        this.context = context;
        type = CROP_TYPE;
        outputFormat = OUTPUT_FORMAT;
        crop = "true";
        scale = true;
        returnData = false;
        noFaceDetection = true;
        scaleUpIfNeeded = true;
//        enable = true;
//        rotateToCorrectDirection = false;
//        compress = false;
//        compressQuality = DEFAULT_COMPRESS_QUALITY;
//        compressWidth = DEFAULT_COMPRESS_WIDTH;
//        compressHeight = DEFAULT_COMPRESS_HEIGHT;
        aspectX = DEFAULT_ASPECT;
        aspectY = DEFAULT_ASPECT;
        outputX = DEFAULT_OUTPUT_WIDTH;
        outputY = DEFAULT_OUTPUT_HEIGHT;
        refreshUri();
    }

    public void refreshUri() {
        uri = CropHelper.generateUri();
    }
}
