package com.credera.example.embedopencv;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import org.opencv.android.Utils;

import java.util.ArrayList;
import java.util.List;

public class HighlighterProcessing {

    //Outputs
    private static Mat source = new Mat();
    private static Mat blur0Output = new Mat();
    private static Mat maskOutput = new Mat();
    private static Mat hsvThresholdOutput = new Mat();
    private static Mat blur1Output = new Mat();
    private static ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
    private static ArrayList<MatOfPoint> filterContoursOutput = new ArrayList<MatOfPoint>();
    private static ArrayList<Mat> highlightedTextRectangles = new ArrayList<Mat>();

    public static ArrayList<Bitmap> findHighlightedWords(ImageView img) {

        source = imageviewToMat(img);
        process(source);

        ArrayList<Bitmap> textBitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < highlightedTextRectangles.size(); i++) {
            textBitmaps.add(matToBitmap(highlightedTextRectangles.get(i)));
        }

        return textBitmaps;

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
        Mat blur0Input = source0;
        BlurType blur0Type = BlurType.get("Box Blur");
        double blur0Radius = 12;
        blur(blur0Input, blur0Type, blur0Radius, blur0Output);

        // Step Mask0:
        Mat maskInput = source0;
        Mat maskMask = hsvThresholdOutput;
        mask(maskInput, maskMask, maskOutput);

        // Step HSV_Threshold0:
        Mat hsvThresholdInput = blur0Output;
        double[] hsvThresholdHue = {13, 30};
        double[] hsvThresholdSaturation = {29.81115107913669, 255.0};
        double[] hsvThresholdValue = {0.0, 255.0};
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

        // Step Blur1:
        Mat blur1Input = hsvThresholdOutput;
        BlurType blur1Type = BlurType.get("Box Blur");
        double blur1Radius = 5.405405405405403;
        blur(blur1Input, blur1Type, blur1Radius, blur1Output);

        // Step Find_Contours0:
        Mat findContoursInput = blur1Output;
        boolean findContoursExternalOnly = false;
        findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);

        // Step Filter_Contours0:
        ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 300.0;
        double filterContoursMinPerimeter = 0;
        double filterContoursMinWidth = 0;
        double filterContoursMaxWidth = 1.0E7;
        double filterContoursMinHeight = 0;
        double filterContoursMaxHeight = 1.0E7;
        double[] filterContoursSolidity = {50.35971223021583, 100.0};
        double filterContoursMaxVertices = 1000000;
        double filterContoursMinVertices = 0;
        double filterContoursMinRatio = 0;
        double filterContoursMaxRatio = 1000;
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);

        getHighlightedTextRectangles();
    }

    public static Mat blur0Output() {
        return blur0Output;
    }

    public static Mat blur1Output() {
        return blur1Output;
    }

    public static Mat hsvThresholdOutput() {
        return hsvThresholdOutput;
    }

    public static Mat maskOutput() {
        return maskOutput;
    }

    public static ArrayList<MatOfPoint> findContoursOutput() {
        return findContoursOutput;
    }

    public static ArrayList<MatOfPoint> filterContoursOutput() {
        return filterContoursOutput;
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

    private static void findContours(Mat input, boolean externalOnly, List<MatOfPoint> contours) {
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if (externalOnly) {
            mode = Imgproc.RETR_EXTERNAL;
        }
        else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);
    }

    private static void filterContours(List<MatOfPoint> inputContours, double minArea, double minPerimeter, double minWidth, double maxWidth, double minHeight, double
            maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double
            minRatio, double maxRatio, List<MatOfPoint> output) {

        final MatOfInt hull = new MatOfInt();
        output.clear();
        //operation
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) continue;
            if (bb.height < minHeight || bb.height > maxHeight) continue;
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) continue;
            if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) continue;
            Imgproc.convexHull(contour, hull);
            MatOfPoint mopHull = new MatOfPoint();
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int)hull.get(j, 0)[0];
                double[] point = new double[] { contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            final double solid = 100 * area / Imgproc.contourArea(mopHull);
            if (solid < solidity[0] || solid > solidity[1]) continue;
            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount)	continue;
            final double ratio = bb.width / (double)bb.height;
            if (ratio < minRatio || ratio > maxRatio) continue;
            output.add(contour);
        }
    }

    public static void getHighlightedTextRectangles() {

        for (int i = 0; i < filterContoursOutput.size(); i++) {
            MatOfPoint2f contourPolygon = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(filterContoursOutput.get(i).toArray()), contourPolygon, 3, true);
            Rect rect = Imgproc.boundingRect(new MatOfPoint(contourPolygon.toArray()));

            Mat croppedImg = new Mat(source, rect);
            highlightedTextRectangles.add(croppedImg);
        }

    }

}
