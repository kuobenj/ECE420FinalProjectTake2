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

//    public static void generateFeatureData(String filename){
//        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
//
//    }

    public OCR(Context context){
        this.context = context;
    }

    public void testSegmentation() throws IOException {
        Mat testImage = null;
        try {
            testImage = Utils.loadResource(context, R.drawable.testvect1);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private static List<MatOfFloat> generateFeatureData(Mat input_image){
        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
        List<Mat> characterData = trainingSetFromFile(input_image);
        List<MatOfFloat> features = new ArrayList<>();
        for(int i = 0; i < characterData.size(); i++){
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

        Mat characterLines_inv = new Mat();
        Imgproc.cvtColor(characterLines,characterLines_inv,Imgproc.COLOR_BGR2GRAY);
        characterLines_inv.convertTo(characterLines_inv, CvType.CV_8UC1);
        Core.invert(characterLines_inv,characterLines_inv);
        Mat charFinder = new Mat();
        Core.reduce(characterLines_inv,charFinder,0,Core.REDUCE_SUM);

        int start = -1;
        float[] charFind = new float[0];
        float[] charFindPrev = new float[0];
        for(int i = 1; i < charFinder.width(); i++){
            charFinder.get(i,0,charFind);
            charFinder.get(i-1,0,charFindPrev);
            if ((charFind[0] > 0) && ((charFindPrev[0] == 0)||(start == -1))){
                start = i;
            }
            else if ((charFind[0] == 0) && ((charFindPrev[0] > 0)&&(start != -1))){
                characterRaw.add(characterLines.submat(0,characterLines.height(),start,i-1));
                start = -1;
            }
        }

        return characterRaw;
    }

    private static List<Mat> generateCharacterFormatted(List<Mat> characterRaw) {
        List<Mat> characterData = new ArrayList<>();
        for(int i = 0; i < characterData.size(); i++){
            Mat charInv = new Mat();
            Core.invert(characterRaw.get(i),charInv);
            Mat charFinder = new Mat();
            Core.reduce(charInv,charFinder,1,Core.REDUCE_SUM);
            float[] placefind = new float[0];
            int top = 0;
            int bottom = charInv.height()-1;

            charInv.get(top,0,placefind);
            while (placefind[0] == 0){
                top++;
                charInv.get(top,0,placefind);
            }

            charInv.get(bottom,0,placefind);
            while (placefind[0] == 0){
                bottom--;
                charInv.get(bottom,0,placefind);
            }

            Mat characterBound = characterRaw.get(i).submat(top,bottom,0,charInv.width());

            //?? what the hell is character
            // character = np.full((28, 28), 255.0, dtype = np.uint8)

            float height = characterBound.height();
            float width = characterBound.width();

            float newHeight = 0;
            float newWidth = 0;

            if (height > width) {
                newHeight = 20;
                newWidth = ((int) width * newHeight / height / 2) / 2;
                if (newWidth <= 0) {
                    newWidth = 2;
                }
            }
            else {
                newWidth = 20;
                newHeight = ((int) height * newWidth / width / 2) / 2;
                if (newHeight <= 0){
                    newHeight = 2;
                }
            }

            Mat characterScaled = new Mat();
            Imgproc.resize(characterBound,characterScaled,new Size(newWidth,newHeight));

            Mat character = new Mat(28,28,CvType.CV_8UC1,new Scalar(255));

            characterScaled.copyTo(character.rowRange(Math.round(14 - newHeight / 2),Math.round(14 + newHeight / 2)).colRange(Math.round((14 - newWidth / 2)),Math.round(14 + newWidth / 2)));

            float getfloat[] = new float[0];
            for (int j = 0; j < character.width(); j++){
                for (int k = 0; k < character.height(); k++){
                    character.get(j,k,getfloat);
                    if (getfloat[0] >= 200) {
                        getfloat[0] = 255;
                        character.put(j, k, getfloat);
                    }
                    else {
                        getfloat[0] = 0;
                        character.put(j,k,getfloat);
                    }

                }
            }

            characterData.add(character);
        }

        return characterData;
    }


    private CvSVM getSVMFromFile() throws IOException {
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

    private Context context;
}
