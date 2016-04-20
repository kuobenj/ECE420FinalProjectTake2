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
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
import org.opencv.ml.SVM;
import org.opencv.ml.StatModel;
import org.opencv.objdetect.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OCR {

//    public static void generateFeatureData(String filename){
//        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
//
//    }

    public static void readChar(Context context){
        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
        SVM svm = SVM.create();
//        svm.getSupportVectors();
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/drawable/test_0");
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/"+R.drawable.test_0);
//        Log.d("HOG", "TEST IMAGE PATH: "+fileUri.toString());
//        Mat testImage = Imgcodecs.imread(fileUri.toString());
        Mat testImage = null;
        try {
            testImage = Utils.loadResource(context, R.drawable.test_0);
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
        Imgcodecs.imwrite(fileUri.toString(),testImage);
        Log.d("HOG", "Image saved to: "+fileUri.toString());
        MatOfFloat features = new MatOfFloat();
        hogDesc.compute(testImage,features);
        Log.d("HOG","Features"+features);
    }
}
