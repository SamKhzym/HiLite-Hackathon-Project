package com.credera.example.embedopencv;
import android.widget.Toast;

import java.io.*;

public class ArrayToCsv {


    public static void toCSV(String[] array) throws IOException{
        String result = "";
        if (array.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        FileOutputStream out = null;

        try {
            out = new FileOutputStream("output.txt");
            out.write(result.getBytes());
            } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
