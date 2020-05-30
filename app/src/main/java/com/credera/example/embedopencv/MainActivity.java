package com.credera.example.embedopencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

import butterknife.internal.DebouncingOnClickListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    private LinearLayout layout;
    private ImageView uploadedImage;
    private int imageMaxHeight;
    private Button uploadImageButton, convertImageButton;

    private ArrayList<Bitmap> highlightedTexts = new ArrayList<Bitmap>();

    static {
        if (!OpenCVLoader.initDebug()){
            Log.d("TEST", "Failed to load OpenCV :(");
        } else {
            Log.d("TEST", "Loaded OpenCV :)");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (LinearLayout) findViewById(R.id.layout);
        uploadedImage = (ImageView) findViewById(R.id.imageToUpload);
        imageMaxHeight = uploadedImage.getHeight();
        uploadImageButton = (Button) findViewById(R.id.uploadImageButton);
        convertImageButton = (Button) findViewById(R.id.convertPicture);

        uploadImageButton.setOnClickListener(this);
        convertImageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadImageButton:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;

            case R.id.convertPicture:
                displayHighlightedTexts();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            uploadedImage.setImageURI(selectedImage);

            //FIGURE OUT HOW TO RESIZE IMAGE AND NOT EAT THE BUTTON HERE

        }
    }

    private void displayHighlightedTexts() {

        highlightedTexts = HighlighterProcessing.findHighlightedWords(uploadedImage);

        for (int i = 0; i < highlightedTexts.size(); i++) {
            ImageView newImg = new ImageView(this);
            newImg.setImageBitmap(highlightedTexts.get(i));
            layout.addView(newImg);
        }

    }

    /*private ImageView imageView;
    private Bitmap processedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context context = this;

        imageView = (ImageView) findViewById(R.id.testImage);

        GlideApp.with(context)
                .load(R.drawable.test_image)
                .into(imageView);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final DebouncingOnClickListener fabClickListener = new DebouncingOnClickListener() {
            Boolean fabToggle = true;

            @Override
            public void doClick(View v) {
                if (!fabToggle) {
                    fabToggle = true;

                    fab.setImageResource(android.R.drawable.ic_menu_edit);

                    GlideApp.with(context)
                            .load(R.drawable.test_image)
                            .into(imageView);

                    return;
                }

                fabToggle = false;
                fab.setImageResource(android.R.drawable.ic_menu_gallery);

                new ConvertToGrayAsyncTask(context, imageView).execute();
            }
        };

        fab.setOnClickListener(fabClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId,
                                                         int reqWidth,
                                                         int reqHeight) {

        // First decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private class ConvertToGrayAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private WeakReference<Context> contextRef;
        private WeakReference<ImageView> imageViewRef;

        ConvertToGrayAsyncTask(Context context, ImageView imageView) {
            contextRef = new WeakReference<>(context);
            imageViewRef = new WeakReference<>(imageView);
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap == null || contextRef.get() == null || imageViewRef.get() == null) {
                return;
            }

            Context context = contextRef.get();

            GlideApp.with(context)
                    .load(bitmap)
                    .into(imageViewRef.get());
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (contextRef.get() == null || imageViewRef.get() == null) {
                return null;
            }

            Context context = contextRef.get();
            ImageView imageView = imageViewRef.get();

            if (processedBitmap != null) {
                processedBitmap.recycle();
            }

            @SuppressLint("WrongThread") final Bitmap src = decodeSampledBitmapFromResource(context.getResources(),
                    R.drawable.test_image,
                    imageView.getWidth(),
                    imageView.getHeight());

            Mat image = new Mat();
            Utils.bitmapToMat(src, image);

            src.recycle();

            Mat grayMat = new Mat();
            Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY, CvType.CV_32S);

            processedBitmap = Bitmap.createBitmap(grayMat.cols(),
                    grayMat.rows(),
                    Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(grayMat, processedBitmap);

            return processedBitmap;
        }
    }*/

}
