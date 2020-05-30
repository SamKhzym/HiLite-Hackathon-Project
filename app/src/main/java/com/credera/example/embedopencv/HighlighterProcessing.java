package com.credera.example.embedopencv;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import org.opencv.android.Utils;

import java.util.ArrayList;

public class HighlighterProcessing {

    //Outputs
    private static Mat blurOutput = new Mat();
    private static Mat hsvThresholdOutput = new Mat();
    private static Mat maskOutput = new Mat();

    public static Bitmap findHighlightedWords(ImageView img) {

        process(imageviewToMat(img));

        return matToBitmap(maskOutput);

    }

    public static Mat imageviewToMat(ImageView img) {

        BitmapDrawable drawable = (BitmapDrawable) img.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        Mat finalMat = new Mat();
        Imgproc.cvtColor(mat, finalMat, Imgproc.COLOR_BGR2RGB);

        return finalMat;

    }

    public static Bitmap matToBitmap(Mat mat) {

        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }

        return bmp;

    }

    public static void process(Mat source0) {
        // Step Blur0:
        Mat blurInput = source0;
        BlurType blurType = BlurType.get("Box Blur");
        double blurRadius = 34.234234234234236;
        blur(blurInput, blurType, blurRadius, blurOutput);

        // Step HSV_Threshold0:
        Mat hsvThresholdInput = blurOutput;
        double[] hsvThresholdHue = {38.84892086330935, 78.63481228668942};
        double[] hsvThresholdSaturation = {29.81115107913669, 255.0};
        double[] hsvThresholdValue = {0.0, 255.0};
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

        // Step Mask0:
        Mat maskInput = source0;
        Mat maskMask = hsvThresholdOutput;
        mask(maskInput, maskMask, maskOutput);
    }

    public static Mat blurOutput() {
        return blurOutput;
    }

    public static Mat hsvThresholdOutput() {
        return hsvThresholdOutput;
    }

    public static Mat maskOutput() {
        return maskOutput;
    }

    enum BlurType{
        BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
        BILATERAL("Bilateral Filter");

        private final String label;

        BlurType(String label) {
            this.label = label;
        }

        public static BlurType get(String type) {
            if (BILATERAL.label.equals(type)) {
                return BILATERAL;
            }
            else if (GAUSSIAN.label.equals(type)) {
                return GAUSSIAN;
            }
            else if (MEDIAN.label.equals(type)) {
                return MEDIAN;
            }
            else {
                return BOX;
            }
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private static void blur(Mat input, BlurType type, double doubleRadius,
    Mat output) {
        int radius = (int)(doubleRadius + 0.5);
        int kernelSize;
        switch(type){
            case BOX:
                kernelSize = 2 * radius + 1;
                Imgproc.blur(input, output, new Size(kernelSize, kernelSize));
                break;
            case GAUSSIAN:
                kernelSize = 6 * radius + 1;
                Imgproc.GaussianBlur(input,output, new Size(kernelSize, kernelSize), radius);
                break;
            case MEDIAN:
                kernelSize = 2 * radius + 1;
                Imgproc.medianBlur(input, output, kernelSize);
                break;
            case BILATERAL:
                Imgproc.bilateralFilter(input, output, -1, radius, radius);
                break;
        }
    }

    private static void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
    Mat out) {
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
        Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
                new Scalar(hue[1], sat[1], val[1]), out);
    }

    private static void mask(Mat input, Mat mask, Mat output) {
        mask.convertTo(mask, CvType.CV_8UC1);
        Core.bitwise_xor(output, output, output);
        input.copyTo(output, mask);
    }

}
