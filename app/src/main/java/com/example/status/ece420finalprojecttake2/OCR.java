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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OCR {

    public OCR(Context context){
        this.context = context;
    }

    public static String getEquation(String filename) throws IOException {
        Mat equation_image = Highgui.imread(filename);
        CvSVM svm = getSVMFromFile();
        List<MatOfFloat> features = generateFeatureData(equation_image);
        String return_string = "";

        for (int i = 0; i<features.size(); i++){
            Log.d("SVM Predict Results","Value = "+svm.predict(features.get(i)));
            return_string = return_string + charDict[(int)svm.predict(features.get(i))];
        }
        return return_string;
    }

    public void testSegmentation() throws IOException {
        Mat testImage = null;
        try {
            testImage = Utils.loadResource(context, R.drawable.testvect1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("testSeg","Image = "+ testImage);
        CvSVM svm = getSVMFromFile();
        List<MatOfFloat> features = generateFeatureData(testImage);

        for (int i = 0; i<features.size(); i++){
            Log.d("SVM Predict Results","Value = "+svm.predict(features.get(i)));
        }
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
            testImage = Utils.loadResource(context, R.drawable.test_10);
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

    private static List<MatOfFloat> generateFeatureData(Mat input_image){
        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
        List<Mat> characterData = trainingSetFromFile(input_image);
        List<MatOfFloat> features = new ArrayList<>();
        for(int i = 0; i < characterData.size(); i++){

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String media_string = mediaStorageDir.getPath() + File.separator +
                    "characterData"+i+".jpg";
            File mediaFile = new File(media_string);
            Uri fileUri = Uri.fromFile(mediaFile);
            Highgui.imwrite(fileUri.getPath(),characterData.get(i));

            MatOfFloat hog_feature = new MatOfFloat();
            hogDesc.compute(characterData.get(i),hog_feature);
            features.add(hog_feature);
        }
        return features;
    }

    private static List<Mat> trainingSetFromFile(Mat input_image){
        List<Mat> characterData = generateCharacterFormatted(generateCharacterFromLines(input_image));
        return characterData;
    }

    private static List<Mat> generateCharacterFromLines(Mat characterLines) {
        List<Mat> characterRaw = new ArrayList<>();
        Log.d("genCharLines","characterLines = "+characterLines);
        Mat characterLines_inv = new Mat();
        Imgproc.cvtColor(characterLines,characterLines_inv,Imgproc.COLOR_BGR2GRAY);
//        characterLines_inv.convertTo(characterLines_inv, CvType.CV_8UC1);
        Core.bitwise_not(characterLines,characterLines_inv);
 //        Mat charFinder = new Mat(1,characterLines.height(),CvType.CV_32FC1);
        Mat charFinder = new Mat();
        Core.reduce(characterLines_inv,charFinder,0,Core.REDUCE_SUM,CvType.CV_32SC1);
//        Core.reduce(characterLines_inv,charFinder,0,Core.REDUCE_MAX);
        Log.d("genCharLines","Character Finder Size: "+charFinder);
        int start = -1;
        int[] charFind = new int[(int) (charFinder.total()*charFinder.channels())];
        charFinder.get(0,0,charFind);
//        int[] charFindPrev = new int[1];
        Log.d("genCharLines","charFind length"+charFind.length);
        for(int i = 1; i < charFind.length; i++){
//            charFinder.get(i,0,charFind);
//            charFinder.get(i-1,0,charFindPrev);
//            Log.d("genCharLines","charFind: "+charFind[i]);
//            Log.d("genCharLines","charFindPrev: "+charFind[i-1]);
            if ((charFind[i] > 0) && ((charFind[i-1] == 0)||(start == -1))){
                start = i;
            }
            else if ((charFind[i] == 0) && ((charFind[i-1] > 0)&&(start != -1))){
                characterRaw.add(characterLines.submat(0,characterLines.height(),start,i-1));

                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "MyCameraApp");

                // Create the storage directory if it does not exist
                if (! mediaStorageDir.exists()){
                    if (! mediaStorageDir.mkdirs()){
                        Log.d("MyCameraApp", "failed to create directory");
                        return null;
                    }
                }

                // Create a media file name
                String media_string = mediaStorageDir.getPath() + File.separator +
                        "rawcharacterimage"+i+".jpg";
                File mediaFile = new File(media_string);
                Uri fileUri = Uri.fromFile(mediaFile);
                Highgui.imwrite(fileUri.getPath(),characterLines.submat(0,characterLines.height(),start,i-1));

                start = -1;
            }
        }
        Log.d("genCharLines","Done Generating Raw Characters, characterRaw size: "+characterRaw.size());
        return characterRaw;
    }

    private static List<Mat> generateCharacterFormatted(List<Mat> characterRaw) {
        List<Mat> characterData = new ArrayList<>();

        Log.d("genCharForm","Entering generateCharacterFormatted, characterRaw size: "+characterRaw.size());

        for(int i = 0; i < characterRaw.size(); i++){
            Mat charInv = new Mat();
            Core.bitwise_not(characterRaw.get(i),charInv);
//            Mat charFinder = new Mat(characterRaw.get(i).width(),1,CvType.CV_32SC1);
            Mat charFinder = new Mat();
            Core.reduce(charInv,charFinder,1,Core.REDUCE_SUM,CvType.CV_32SC1);
            int[] placefind = new int[(int) (charFinder.total()*charFinder.channels())];
            charFinder.get(0,0,placefind);
            int top = 0;
            int bottom = charInv.height()-1;

            while ((placefind[top] == 0)&&(top < bottom)){
                top++;
            }

            while ((placefind[bottom] == 0)&&(bottom > top)){
                bottom--;
            }

            Mat characterBound = characterRaw.get(i).submat(top,bottom,0,charInv.width());

//            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
//
//            // Create the storage directory if it does not exist
//            if (! mediaStorageDir.exists()){
//                if (! mediaStorageDir.mkdirs()){
//                    Log.d("MyCameraApp", "failed to create directory");
//                    return null;
//                }
//            }
//
//            // Create a media file name
//            String media_string = mediaStorageDir.getPath() + File.separator +
//                    "characterboundimage"+i+".jpg";
//            File mediaFile = new File(media_string);
//            Uri fileUri = Uri.fromFile(mediaFile);
//            Highgui.imwrite(fileUri.getPath(),characterBound);


            float height = characterBound.height();
            float width = characterBound.width();

            Log.d("genCharForm", "Image Height: "+height+" Image Width: "+width);

            float newHeight = 0;
            float newWidth = 0;

            if (height > width) {
                newHeight = 20;
                newWidth = width * newHeight / height;
                if (newWidth <= 0) {
                    newWidth = 2;
                }
            }
            else {
                newWidth = 20;
                newHeight = height * newWidth / width;
                if (newHeight <= 0){
                    newHeight = 2;
                }
            }

            Log.d("genCharForm", "New Image Height: "+newHeight+" New Image Width: "+newWidth);

            Mat characterScaled = new Mat();
            Imgproc.resize(characterBound,characterScaled,new Size(newWidth,newHeight));

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String media_string = mediaStorageDir.getPath() + File.separator +
                    "characterscaledimage"+i+".jpg";
            File mediaFile = new File(media_string);
            Uri fileUri = Uri.fromFile(mediaFile);
            Highgui.imwrite(fileUri.getPath(),characterScaled);


            Mat character = new Mat(28,28,CvType.CV_8UC1,new Scalar(255));

//            Mat poop = new Mat(20,20,CvType.CV_8UC1,new Scalar(0));

            Log.d("genCharForm","Row range and col range bounds: ["+Math.round(14 - newHeight / 2)+","+Math.round(14 + newHeight / 2)+","+Math.round(14 - newWidth / 2)+","+Math.round(14 + newWidth / 2)+"]");

            characterScaled.copyTo(character.submat(new Rect(14-(int)newWidth/2,14-(int)newHeight/2,(int)newWidth,(int)newHeight)));

            byte getbyte[] = new byte[(int) (character.total()*character.channels())];

//            character.get(0,0,getbyte);
//
//            for (int j = 0; j < getbyte.length; j++){
//                if (getbyte[j] >= 150) {
//                    getbyte[j] = (byte) 255;
//                }
//                else {
//                    getbyte[0] = 0;
//
//                }
//            }
//            character.put(0,0,getbyte);

            characterData.add(character);
        }

        Log.d("genCharForm","Finished with generating Formatted characters, Length: "+characterData.size());
        return characterData;
    }


    private static CvSVM getSVMFromFile() throws IOException {
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

        CvSVM svm = new CvSVM();
        svm.load(file.getAbsolutePath());

        return svm;
    }

    private static Context context;

    private static String charDict[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9","x", "y", "(", ")", "+", "-", "="};
}
