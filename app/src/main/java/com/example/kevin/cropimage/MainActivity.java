package com.example.kevin.cropimage;

import com.example.kevin.cropimage.data.Utils;


import org.w3c.dom.Text;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    private Button mCropButton;
    private TextView mDescText;
    private TextView mCurText;
    private ProgressBar mProgressBar;
    private TextView mTextProgress;
    private View mProgressContainer;
    private Button mOpenCropButton;

    private TextView mSrcTipsView;
    private EditText mWidthView;
    private EditText mHeightView;
    private EditText mRadiusView;


    private List<String> mFilePaths;
    private List<String> mErrorFiles;

    private AsyncTask mCropTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        mCropButton = (Button) findViewById(R.id.button_crop);
        mDescText = (TextView) findViewById(R.id.desc);
        mCurText = (TextView) findViewById(R.id.cur);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextProgress = (TextView) findViewById(R.id.text_progress);
        mProgressContainer = findViewById(R.id.progress_container);
        mOpenCropButton = (Button) findViewById(R.id.open_crop);

        mSrcTipsView = (TextView) findViewById(R.id.src_tips);
        mWidthView = (EditText) findViewById(R.id.width);
        mHeightView = (EditText) findViewById(R.id.height);
        mRadiusView = (EditText) findViewById(R.id.radius);

        mFilePaths = Utils.listFiles(Utils.DIR_SRC);
        if (mFilePaths.size() < 1) {
            mDescText.setVisibility(View.INVISIBLE);
            mCropButton.setText(getString(R.string.text_refresh));
            mCropButton.setOnClickListener(mRefreshListener);
        } else {
            mCropButton.setText(getString(R.string.text_start_crop));
            mCropButton.setOnClickListener(mStartCropListener);
            mDescText.setText(getString(R.string.text_description, mFilePaths.size()));
        }

        mProgressBar.setMax(mFilePaths.size() * 10);

        File cropFile = new File(Utils.DIR_CROP);
        if (!cropFile.exists()) {
            cropFile.mkdir();
        }

        mSrcTipsView.setText(getString(R.string.text_src_tips, Utils.DIR_SRC, Utils.DIR_CROP));
        mOpenCropButton.setOnClickListener(mOpenCropListener);
        mOpenCropButton.setVisibility(View.GONE);
        mErrorFiles = new ArrayList<String>();
    }

    private View.OnClickListener mStartCropListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProgressContainer.setVisibility(View.VISIBLE);
            startCrop();
            showStopButton();
        }
    };

    private View.OnClickListener mStopCropListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCropTask != null) {
                mCropTask.cancel(true);
            }
        }
    };

    private View.OnClickListener mOpenCropListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            Uri uri = Uri.parse(Utils.DIR_CROP);
            intent.setData(uri);
            intent.setType("*/*");
            intent.setComponent(new ComponentName("com.android.fileexplorer", "com.android.fileexplorer.FileExplorerTabActivity"));
            startActivity(intent);
        }
    };

    private View.OnClickListener mRefreshListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFilePaths = Utils.listFiles(Utils.DIR_SRC);
            if (!mFilePaths.isEmpty()) {
                mCropButton.setText(getString(R.string.text_start_crop));
                mCropButton.setOnClickListener(mStartCropListener);
                mDescText.setVisibility(View.VISIBLE);
                mDescText.setText(getString(R.string.text_description, mFilePaths.size()));
            }
        }
    };

    private void startCrop() {
        mErrorFiles.clear();
        Utils.cleanFile(Utils.DIR_CROP);

        if (mFilePaths.isEmpty()) {
            return;
        }

        mDescText.setVisibility(View.VISIBLE);

        int width,height,radius;
        try {
            width = Integer.parseInt(mWidthView.getText().toString());
            height = Integer.parseInt(mHeightView.getText().toString());
            radius = Integer.parseInt(mRadiusView.getText().toString());
        } catch (Exception e) {
            width = -1;
            height = -1;
            radius = -1;
        }

        if (width == -1 || height == -1 || radius == -1) {
            width = Utils.CART_WIDTH;
            width = Utils.CART_HEIGHT;
            radius = Utils.CART_CORNER_RADIUS;
        }
        final int w = width;
        final int h = height;
        final int r = radius;

        mCropTask = new AsyncTask<Object, Integer, String>() {
            private int index = 0;
            private String mCurFile;

            @Override
            protected String doInBackground(Object... params) {

                for (Object object : params) {
                    String path = (String) object;
                    String fileName = path.substring(path.lastIndexOf(File.separator));

                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    Log.d("lsw", "cur file name = " + fileName + " bitmap = " + bitmap);
                    if (bitmap == null || bitmap.isRecycled()) {
                        index++;
                        SimpleDateFormat df = new SimpleDateFormat();
                        String time = df.format(new Date());
                        mErrorFiles.add(time + " : " + path + "\n");
                        continue;
                    }
                    mCurFile = fileName;
                    publishProgress(index++);

                    String saveDir = path.replace(Utils.DIR_SRC, Utils.DIR_CROP);
                    saveDir = saveDir.substring(0, saveDir.lastIndexOf('/'));

                    Bitmap scaledBitmap = Utils.scaleBitmap(bitmap, w, h);
                    String savePath = saveDir + fileName;
                    Bitmap roundBitmap = Utils.cropCorner(scaledBitmap, r);
                    Utils.save(roundBitmap, savePath);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mCurText.setText(mCurFile);
                mProgressBar.setProgress(values[0] * 10);
                mTextProgress.setText(getString(R.string.text_num_progress, index, mFilePaths.size()));
            }

            @Override
            protected void onPostExecute(String s) {
                if (!mErrorFiles.isEmpty()) {
                    Utils.saveErrorLog(mErrorFiles);
                }
                showStartButton();
                mProgressContainer.setVisibility(View.GONE);
                mCurText.setText(getString(R.string.text_crop_complete));
                Toast.makeText(MainActivity.this, "complete!!!!", Toast.LENGTH_LONG).show();
                mOpenCropButton.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onCancelled() {
                if (!mErrorFiles.isEmpty()) {
                    Utils.saveErrorLog(mErrorFiles);
                }
                showStartButton();
                mOpenCropButton.setVisibility(View.VISIBLE);
            }
        }.execute(mFilePaths.toArray());
    }

    private void showStartButton() {
        mCropButton.setText(R.string.text_start_crop);
        mCropButton.setOnClickListener(mStartCropListener);
    }

    private void showStopButton() {
        mCropButton.setText(R.string.text_stop_crop);
        mCropButton.setOnClickListener(mStopCropListener);
    }
}
