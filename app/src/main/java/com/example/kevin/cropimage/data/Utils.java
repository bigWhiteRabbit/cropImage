package com.example.kevin.cropimage.data;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 16-6-15.
 */
public class Utils {
    private static final String TAG = "Utils";

    public static final String DIR_ROOT = Environment.getExternalStorageDirectory().getPath();
    public static final String DIR_SRC = DIR_ROOT + "/DCIM/crop/src";
    public static final String DIR_CROP = DIR_ROOT + "/DCIM/crop/crop";

    public static final int CART_WIDTH = 1440;
    public static final int CART_HEIGHT = 908;
    public static final int CART_CORNER_RADIUS = 60;

    public static List<String> listFiles(String path) {
        List<String> filePath = new ArrayList<String>();
        File file = new File(path);

        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                if (f.isDirectory()) {
                    filePath.addAll(listFiles(f.getPath()));
                } else {
                    filePath.add(f.getPath());
                }
            }
        } else {
            filePath.add(file.getPath());
        }
        return filePath;
    }

    public static void save(Bitmap bitmap, String savePath) {
        File cropDir = new File(savePath);
        if (!cropDir.getParentFile().exists()) {
            cropDir.getParentFile().mkdirs();
        }

        File file = new File(savePath);
        BufferedOutputStream bos = null;
        boolean isSuccess = false;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
        } catch (IOException e) {
            isSuccess = false;
        } finally {
            Log.d(TAG, "save file " + file.getName() + " " + isSuccess);
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bitmap.recycle();
        }
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix mx = new Matrix();
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        mx.postScale(scaleWidth, scaleHeight);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mx, true);
        if (scaledBitmap != bitmap) {
            bitmap.recycle();
        }
        return scaledBitmap;
    }

    public static Bitmap cropCorner(Bitmap bitmap, float radius) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        Bitmap roundCornerBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundCornerBitmap);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        Paint paint = new Paint();
        paint.setAntiAlias(true);   // 抗锯齿
        paint.setXfermode(null);
        paint.setFilterBitmap(true);

        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rectF, paint);

        bitmap.recycle();
        return roundCornerBitmap;
    }

    public static void cleanFile(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                cleanFile(f.getPath());
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static void saveErrorLog(List<String> errorFiles) {
        try {
            FileWriter fileWriter = new FileWriter(DIR_CROP + File.separator + "errorLog", true);
            for (String str : errorFiles) {
                fileWriter.write(str + "\n\t");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
