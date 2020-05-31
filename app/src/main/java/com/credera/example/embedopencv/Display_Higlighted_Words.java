package com.credera.example.embedopencv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Display_Higlighted_Words extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_highlighted_words);

    }
    private void Rows(int m){
        for (int i = 0; i < m; i++) {
            ImageView newImg = new ImageView(this);
          //  newImg.setImageBitmap(highlightedTexts.get(i));
           // LinearLayoutaddView(newImg);

        }
    }

}
