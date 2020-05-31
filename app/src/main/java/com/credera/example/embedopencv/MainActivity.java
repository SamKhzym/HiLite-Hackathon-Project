package com.credera.example.embedopencv;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

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
import java.util.Collections;
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
    private Button uploadImageButton, convertImageButton, exportCSVBtn, clearAllBtn;
    private TextRecognizer recognizer;
    private EditText emailRecipient;
    SimpleRangeView rangeBar;
    private RadioButton yellowO,blueO,orangeO,greenO,pinkO,sliderBut;
    boolean[] radBut = new boolean[6];
    boolean emptySearch = false;

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
        clearAllBtn = (Button) findViewById(R.id.clearAll);
        emailRecipient = (EditText) findViewById(R.id.sendTo);

        yellowO = (RadioButton) findViewById((R.id.yellowO));
        blueO = (RadioButton) findViewById((R.id.blueO));
        orangeO = (RadioButton) findViewById((R.id.orangeO));
        greenO = (RadioButton) findViewById((R.id.greenO));
        pinkO = (RadioButton) findViewById((R.id.pinkO));
        sliderBut= (RadioButton) findViewById(R.id.sliderbut);

        uploadImageButton.setOnClickListener(this);
        convertImageButton.setOnClickListener(this);
        exportCSVBtn.setOnClickListener(this);
        clearAllBtn.setOnClickListener(this);

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
                Log.d("STAGE 1", "STAGE 1");
                if (colourMaker()[0] != -1) {
                    Log.d("STAGE 2", "STAGE 2");
                    findHighlightedTexts();
                    if (!emptySearch) {
                        Log.d("STAGE 3", "STAGE 3");
                        displayAllHighlights();
                        Log.d("How many text entries?", highlightedTexts.size() + "");
                        Log.d("How many layouts?", layouts.size() + "");
                    }
                    else {
                        emptySearch = false;
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please pick a colour to filter", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.exportCSV:
                getTextFromBitmaps();
                try {
                    exportCSV();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.clearAll:
                for (int i = 0; i < layouts.size(); i++) {
                    masterLayout.removeView(layouts.get(i));
                }
                layouts.clear();
                highlightedTexts.clear();
                Log.d("How many text entries?", highlightedTexts.size() + "");
                Log.d("How many layouts?", layouts.size() + "");
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

    private void findHighlightedTexts() {
        /*ArrayList<Bitmap> newTexts = HighlighterProcessing.findHighlightedWords(uploadedImage,colourMaker());// opencv colour filter
        if (newTexts.size() == 0) {
            Toast.makeText(getApplicationContext(),"No highlights found. :(", Toast.LENGTH_LONG).show();
            emptySearch = true;
        }
        Collections.reverse(newTexts);
        highlightedTexts.addAll(newTexts);*/

        highlightedTexts = HighlighterProcessing.findHighlightedWords(uploadedImage,colourMaker());
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
        emailRecipient.setVisibility(View.VISIBLE);
        clearAllBtn.setVisibility(View.VISIBLE);

        for (int i = 0; i < layouts.size(); i++) {
            masterLayout.removeView(layouts.get(i));
        }
        layouts.clear();

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
        Intent sendEmail = ArrayToCsv.exportCSV(emailRecipient.getText().toString(), recognizedText);
        startActivity(Intent.createChooser(sendEmail, "Choose an email client: "));
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
    public void radButtons(View view) {
        radBut[0] = blueO.isChecked();
        radBut[1] = orangeO.isChecked();
        radBut[2] = greenO.isChecked();
        radBut[3] = pinkO.isChecked();
        radBut[4] = yellowO.isChecked();
        radBut[5] = sliderBut.isChecked();
        if(radBut[5]){
            rangeBar.setVisibility(View.VISIBLE);
        }
        else {rangeBar.setVisibility(View.INVISIBLE);}

    }
    private double[]colourMaker(){
        if(radBut[0]){return new double[]{79,112}; }
        if(radBut[1]){return new double[]{10,23}; }
        if(radBut[2]){return new double[]{50,71}; }
        if(radBut[3]){return new double[]{141,168}; }
        if(radBut[4]){return new double[]{28,40}; }
        if(radBut[5]){return Slider();}
        return new double[]{-1,-1};
    }

}
