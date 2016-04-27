package com.example.status.ece420finalprojecttake2;

/**
 * Created by Status on 4/19/2016.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.android.*;
import org.opencv.highgui.Highgui;
//import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
//import org.opencv.ml.SVM;
//import org.opencv.ml.StatModel;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvStatModel;
import org.opencv.objdetect.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OCR {

//    public static void generateFeatureData(String filename){
//        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
//
//    }

    public OCR(Context context){
        this.context = context;
    }

    public static void readChar(Context context) throws IOException {

        InputStream xml_file = context.getResources().openRawResource(R.raw.handwriting_svm);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/MyCameraApp", "handwriting_svm.xml");
        Log.d("HOG", "File1: " + file.getPath());
        OutputStream xml_copy = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int readvalue = 0;
        readvalue = xml_file.read(buffer);

        while (readvalue > 0) {
            xml_copy.write(buffer, 0, readvalue);
            readvalue = xml_file.read(buffer);
        }

//        xml_copy.close();



        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
        CvSVM svm = new CvSVM();
//        Uri xmlUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/raw/handwriting_svm");
        Uri xmlUri = Uri.parse("file:///android_asset/handwriting_svm.xml");
//        Uri xmlUri = Uri.fromFile(new File("android.resource://com.example.status.ece420finalprojecttake2/raw/handwriting_svm.xml"));
//        svm.load(xmlUri.getPath());
        svm.load(file.getAbsolutePath());
//        svm.load("file:///android_asset/handwriting_svm");
        Log.d("HOG", "SVM Count:" + svm.get_support_vector_count() + " VAR Count:" + svm.get_var_count());
        Log.d("HOG","xml location: " + xmlUri.getPath());
//        svm.getSupportVectors();
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/drawable/test_0");
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/"+R.drawable.test_0);
//        Log.d("HOG", "TEST IMAGE PATH: "+fileUri.toString());
//        Mat testImage = Imgcodecs.imread(fileUri.toString());
        Mat testImage = null;
        try {
            testImage = Utils.loadResource(context, R.drawable.test_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (testImage == null){
            Log.d("HOG","NULL MUTHAFUCKA");
        }
        Log.d("HOG", "Image: "+testImage);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return;
            }
        }

        // Create a media file name
        String media_string = mediaStorageDir.getPath() + File.separator +
                    "hog_test.jpg";
        File mediaFile = new File(media_string);
        Uri fileUri = Uri.fromFile(mediaFile);
        Highgui.imwrite(fileUri.getPath(),testImage);
        Log.d("HOG", "Image saved to: "+fileUri.getPath());
        MatOfFloat features = new MatOfFloat();
        hogDesc.compute(testImage,features);
        Log.d("HOG","Features"+features);

        float predict_result = svm.predict(features);

        Log.d("HOG", "Feature Prediction Result" + predict_result);
    }

    private Context context;
}
