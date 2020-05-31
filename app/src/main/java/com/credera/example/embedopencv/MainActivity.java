package com.credera.example.embedopencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

import butterknife.internal.DebouncingOnClickListener;
import me.bendik.simplerangeview.SimpleRangeView;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    private LinearLayout masterLayout;
    private ImageView uploadedImage;
    private int imageMaxHeight;
    private Button uploadImageButton, convertImageButton, exportCSVBtn;
    private TextRecognizer recognizer;
    SimpleRangeView rangeBar;
    private RadioButton yellowO,blueO,orangeO,greenO,pinkO;

    private ArrayList<Bitmap> highlightedTexts = new ArrayList<Bitmap>();
    private ArrayList<String> recognizedText = new ArrayList<String>();
    public static double[] hueFilter;
    private ArrayList<LinearLayout> layouts = new ArrayList<LinearLayout>();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("TEST", "Failed to load OpenCV :(");
        } else {
            Log.d("TEST", "Loaded OpenCV :)");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        masterLayout = (LinearLayout) findViewById(R.id.masterLayout);
        uploadedImage = (ImageView) findViewById(R.id.imageToUpload);
        imageMaxHeight = uploadedImage.getHeight();
        uploadImageButton = (Button) findViewById(R.id.uploadImageButton);
        convertImageButton = (Button) findViewById(R.id.convertPicture);
        exportCSVBtn = (Button) findViewById(R.id.exportCSV);
        yellowO = (RadioButton) findViewById((R.id.yellowO));
        blueO = (RadioButton) findViewById((R.id.blueO));
        orangeO = (RadioButton) findViewById((R.id.orangeO));
        greenO = (RadioButton) findViewById((R.id.greenO));
        pinkO = (RadioButton) findViewById((R.id.pinkO));

        uploadImageButton.setOnClickListener(this);
        convertImageButton.setOnClickListener(this);
        exportCSVBtn.setOnClickListener(this);

        recognizer = new TextRecognizer.Builder(MainActivity.this).build();
        hueFilter = Slider();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadImageButton:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                Log.d("button", "press upload");
                break;

            case R.id.convertPicture:
                findHighlightedTexts();
                Log.d("button", "press convert");
                displayAllHighlights();
                break;

            case R.id.exportCSV:
                getTextFromBitmaps();
                try {
                    exportCSV();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            uploadedImage.setImageURI(selectedImage);
        }
    }

    public void onRadioButtonClicked(View v) {

        boolean checked = ((RadioButton) v).isChecked();

        yellowO.setTextColor(Color.GRAY);
        blueO.setTextColor(Color.GRAY);
        orangeO.setTextColor(Color.GRAY);
        greenO.setTextColor(Color.GRAY);
        pinkO.setTextColor(Color.GRAY);
        switch (v.getId()) {
            case R.id.yellowO:
                if (checked)
                    yellowO.setTextColor(Color.YELLOW);
                break;
            case R.id.blueO:
                if (checked)
                    blueO.setTextColor(Color.BLUE);
                break;
            case R.id.orangeO:
                if (checked)
                    blueO.setTextColor(Color.RED);
                break;
            case R.id.greenO:
                if (checked)
                    blueO.setTextColor(Color.GREEN);
                break;
            case R.id.pinkO:
                if (checked)
                    blueO.setTextColor(Color.RED);
                break;
        }
    }

    private void findHighlightedTexts() {
        highlightedTexts = HighlighterProcessing.findHighlightedWords(uploadedImage);// opencv colour filter

    }

    private void getTextFromBitmaps() {

        for (int i = 0; i < highlightedTexts.size(); i++) {
            String str = RecognizeText.extractTextFromImage(recognizer, highlightedTexts.get(i));
            recognizedText.add(str);
            Log.d("TESTING RECOGNITION", str);
        }

    }
    private void displayAllHighlights() {

        exportCSVBtn.setVisibility(View.VISIBLE);

        for (int i = 0; i < highlightedTexts.size(); i++) {

            LinearLayout newLayout = new LinearLayout(this);
            newLayout.setGravity(Gravity.CENTER);
            newLayout.setPadding(10, 10, 10, 10);

            ImageView newImg = new ImageView(this);
            newImg.setLayoutParams(new FrameLayout.LayoutParams(700, 100));
            newImg.setImageBitmap(highlightedTexts.get(i));

            Button btn = new Button(this);
            btn.setBackgroundColor(Color.RED);
            btn.setLayoutParams(new FrameLayout.LayoutParams(100, 100));
            btn.setText("X");
            btn.setPadding(10, 10, 10, 10);

            final int j = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    highlightedTexts.remove(j);
                    masterLayout.removeView(layouts.get(j));
                }
            });

            ImageView buffer = new ImageView(this);
            buffer.setLayoutParams(new FrameLayout.LayoutParams(30, 100));

            newLayout.addView(newImg);
            newLayout.addView(buffer);
            newLayout.addView(btn);

            layouts.add(newLayout);

            masterLayout.addView(newLayout);

        }
    }

    private void exportCSV() throws IOException {
        ArrayToCsv.exportCSV(recognizedText, this);
    }

    private double[] Slider() {
        final double[] a = {0, 0};

        rangeBar = findViewById(R.id.rang_bar);
        rangeBar.setOnChangeRangeListener(new SimpleRangeView.OnChangeRangeListener() {
            @Override
            public void onRangeChanged(@NotNull SimpleRangeView simpleRangeView, int i, int i1) {
                a[0] = i;
            }
        });
        rangeBar.setOnChangeRangeListener(new SimpleRangeView.OnChangeRangeListener() {
            @Override
            public void onRangeChanged(@NotNull SimpleRangeView simpleRangeView, int i, int i1) {
                a[1] = i;
            }
        });
        rangeBar.setOnRangeLabelsListener(new SimpleRangeView.OnRangeLabelsListener() {
            @Nullable
            @Override
            public String getLabelTextForPosition(@NotNull SimpleRangeView simpleRangeView, int i, @NotNull SimpleRangeView.State state) {
                return String.valueOf(i);
            }
        });
        return a;
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
// cock
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
