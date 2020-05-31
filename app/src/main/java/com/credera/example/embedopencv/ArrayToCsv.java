package com.credera.example.embedopencv;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ArrayToCsv {

    private static final String FILE_NAME = "ExportedCSV.txt";
    private static String masterString = "";

    public static void exportCSV(ArrayList<String> strings) throws IOException {
        String masterString = toCSV(strings);
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

    public void save(Context ctx) {

        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(masterString.getBytes());

            Log.d("TEST", ("Saved to " + ctx.getFilesDir() + "/" + FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
