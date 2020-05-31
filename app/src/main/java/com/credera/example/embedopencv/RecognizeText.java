package com.credera.example.embedopencv;

import android.graphics.Bitmap;
import android.icu.text.StringPrepParseException;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;

public class RecognizeText {

    public static String extractTextFromImage(TextRecognizer recognizer, Bitmap img) {

        Frame frame = new Frame.Builder().setBitmap(img).build();
        SparseArray<TextBlock> sparseArray = recognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sparseArray.size(); i++) {
            TextBlock tx = sparseArray.get(i);
            String str = tx.getValue();

            stringBuilder.append(str);
        }

        return stringBuilder.toString();

    }

}
