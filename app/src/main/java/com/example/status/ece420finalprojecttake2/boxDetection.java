package com.example.status.ece420finalprojecttake2;

/**
 * Created by Status on 4/16/2016.
 */

import android.util.Log;

import org.opencv.core.*;
import org.opencv.android.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class boxDetection {
    public static void preprocess(String filename) {
        /********** Reading Input image **********/

        // Load an image from file - change this based on your image name
//        IplImage pInpImg = cvLoadImage(filename);
        Mat pInpImg = Imgcodecs.imread(filename);
        if (pInpImg == null) {
            Log.e("Image Error", "failed to load input image\n");
            return;
        }
        else {
            Log.d("Image Loading", "Image Loaded Successfully");
        }

        //Convert to mat (default is mat)
        Mat orig = pInpImg;  // default additional arguments: don't copy data.
        Mat output = orig;

        Log.d("Image Conversion", "Image Converted to Mat");

        /**********  Code for contour detection **********/
        //Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(orig, gray, Imgproc.COLOR_BGR2GRAY);

        Log.d("Image Conversion", "Image Converted to Gray");

        //Gaussian blur to remove noise
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0, 0, Core.BORDER_DEFAULT);

        Log.d("Image Conversion", "Image Blurred");

        //Create threshold image
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255, 1, 1, 11, 2);

        Log.d("Image Conversion", "Image Thresholded");

        Imgcodecs.imwrite(filename + "greyandthresh.jpg", thresh);

        Log.d("Image Conversion", "Thresholded image saved");

        /**********  Finding contours  **********/
//        vector<vector<Point> > contours;
//        vector<Vec4i> hierarchy;
//        findContours(thresh, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(thresh,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("Contours", "Contours Found: "+contours.size());

        /**********  Finding largest countour **********/
        //cout << "# of contours dectected: " << contours.size() << endl;

//        double largest_area=100;
//        int largest_contour_index=0;
//        double peri = 0;
//        vector<Point> approxCurve;
//
//        Rect bounding_rect;
//        for( int i = 0; i< contours.size(); i++ ){
//            //Find the area of contour
//            double a=contourArea(contours[i],false);
//            if(a>largest_area){
//                largest_area=a;
//                //Store the index of largest contour
//                largest_contour_index=i;
//                approxPolyDP(contours[i], approxCurve, 0.05*arcLength(contours[i], true), true);
//                bounding_rect=boundingRect(contours[i]); // Find the bounding rectangle for biggest contour
//            }
//        }

        double largest_area=100;
        int largest_contour_index=0;
        double peri = 0;
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        Rect bounding_rect = new Rect();
        for( int i = 0; i< contours.size(); i++ ){
            //Find the area of contour
            double a=Imgproc.contourArea(contours.get(i),false);
            if(a>largest_area){
                largest_area=a;
                //Store the index of largest contour
                largest_contour_index=i;
                /// New variable
                MatOfPoint2f contours2f = new MatOfPoint2f( contours.get(i).toArray() );
                Imgproc.approxPolyDP(contours2f, approxCurve, 0.05*Imgproc.arcLength(contours2f, true), true);
                bounding_rect=Imgproc.boundingRect(contours.get(i)); // Find the bounding rectangle for biggest contour
            }
        }
        //cout << "Size of largest area: " << (double)largest_area << endl;
        Log.d("Contours", "Largest Area: "+largest_area);

        /**********  Constants for drawing  **********/
        int thickness = 20;
        Scalar color = new Scalar(128,128,255);
        Scalar white = new Scalar(255,255,255);
        Scalar red = new Scalar(0,0,255);
        Scalar blue = new Scalar(255,0,0);
//        //cout << "Curve size: " << approxCurve.size() << endl;

        Log.d("Contours", "Curve size" + approxCurve.size());

        /**********  Draw around contour (Testing only)  **********/
         for(int i = 0; i < approxCurve.toList().size(); i++){
           Imgproc.line(orig, approxCurve.toList().get(i%approxCurve.toList().size()), approxCurve.toList().get((i+1)%approxCurve.toList().size()), blue, thickness);
         }


        /**********  Draw around largest rectangle **********/
//        Mat dst = thresh;
//
//        // Draw the largest contour using previously stored index.
//        drawContours(dst, contours,largest_contour_index, color, CV_FILLED, 8, hierarchy);
//        vector<Point> largestrect(4);
//        if (approxCurve.size() >= 4 && (approxCurve.size() <= 10)){
//            largestrect = findLargestQuad(approxCurve);
//        }
//
//        if(!largestrect.empty()){
//            //cout << "Largest Rect Size: " << (double)contourArea(largestrect,false) << endl;
//            for(int i = 0; i < largestrect.size(); i ++){
//                //line(orig, approxCurve.at(i%approxCurve.size()), approxCurve.at((i+1)%approxCurve.size()), red, thickness);
//                line(orig, largestrect.at(i%largestrect.size()), largestrect.at((i+1)%largestrect.size()), red, thickness);
//            }
//        }
//        else{
//            cout << "No suitable rectangle detected" << endl;
//        }

        //Bounding rectangle (For testing)
        //rectangle(orig, bounding_rect,  white, thickness, 8,0);

//        output = orig;

        Mat dst = thresh;

        // Draw the largest contour using previously stored index.
        Imgproc.drawContours(dst, contours,largest_contour_index, color, Core.FILLED);
        List<Point> largestrect = new ArrayList<>(4);
        if (approxCurve.toList().size() >= 4 && (approxCurve.toList().size() <= 10)){
            largestrect = findLargestQuad(approxCurve.toList());
        }

        Log.d("Contours","largest rect data size: "+ largestrect.size() + "largest rect:" + largestrect);

        if(largestrect.size() != 0){
            //cout << "Largest Rect Size: " << (double)contourArea(largestrect,false) << endl;
            MatOfPoint largestrect_mat = new MatOfPoint();
            largestrect_mat.fromList(largestrect);
            Log.d("Countours", "Largest Rect Size" + Imgproc.contourArea(largestrect_mat,false));
            for(int i = 0; i < largestrect.size(); i ++){
                //line(orig, approxCurve.at(i%approxCurve.size()), approxCurve.at((i+1)%approxCurve.size()), red, thickness);
                Imgproc.line(orig, largestrect.get(i%largestrect.size()), largestrect.get((i+1)%largestrect.size()), red, thickness);
            }
        }
        else{
            Log.e("Contours","NO SUITABLE RECTANGLE DETECTED");
        }

        //Bounding rectangle (For testing) (not easy to translate)
//        Imgproc.rectangle(orig, bounding_rect,  white, thickness, 8,0);

//        output = orig;


        /**********  Writing output image **********/
        // using a different image format -- .png
//        if(!imwrite(argv[2], output)){
//            fprintf(stderr, "failed to write image file\n");
//        }
        Imgcodecs.imwrite(filename + "processed.jpg", dst);
        Imgcodecs.imwrite(filename + "processed2.jpg", orig);


        /**********  Freeing Memory **********/
//        Core.ReleaseImage(pInpImg);
        return;

    }
    /**********  HELPER FUNCTIONS **********/

    /**
     * findLargestQuad(vector<Point> input)
     * Inputs: vector of points for a Curve
     * Output: vector of 4 points
     * Description: given an input vector of points, returns a vector with 4 points
     * 					that form the largest rectangular area within those points.
     * 					If the input vector is less than 4 points, return empty vector.
     */
    private static List<Point> findLargestQuad(List<Point> input){
        List<Point> output = new ArrayList<Point>(4);
        List<Point> temp = new ArrayList<Point>(4);
        double largest_area = 0;

        if(input.size() < 4){
            return new ArrayList<Point>(4);
        }

        for(int a = 0; a < input.size(); a++){
            for(int b = 0; b < input.size(); b++){
                for(int c = 0; c < input.size(); c++){
                    for(int d = 0; d < input.size(); d++){
                        temp.clear();
                        temp.add(input.get(a));
                        temp.add(input.get(b));
                        temp.add(input.get(c));
                        temp.add(input.get(d));
                        MatOfPoint temp_mat = new MatOfPoint();
                        temp_mat.fromList(temp);
                        if(a != b && a != c && a != d && b != c && b != d && c != d){
                            double area = Imgproc.contourArea(temp_mat,false);
                            if(area > largest_area){
                                output.clear();
                                largest_area = area;
                                output = temp;
                                Log.d("Contours","New Largest quad");
                            }
                            Log.d("Contours","New Largest quad");
                        }
                    }
                }
            }
        }
        //cout << "Largest from findLargestQuad: " << largest_area <<endl;
        return output;
    }
}