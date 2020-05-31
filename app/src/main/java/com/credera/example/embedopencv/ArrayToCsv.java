package com.credera.example.embedopencv;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.content.ContextCompat.startActivity;

public class ArrayToCsv {

    private static final String FILE_NAME = "ExportedCSV.txt";
    private static String masterString = "";

    public static Intent exportCSV(String recipient, ArrayList<String> strings) throws IOException {
        String masterString = toCSV(strings);
        return send(recipient, masterString);
    }

    public static String toCSV(ArrayList<String> array) throws IOException{
        masterString = "";
        if (array.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                sb.append(s).append(",");
            }
            masterString = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return masterString;
    }

    public static Intent send(String recipient, String str) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Your CSV!");
        intent.putExtra(Intent.EXTRA_TEXT, str);

        intent.setType("message/rfc822");

        return intent;

    }

}
