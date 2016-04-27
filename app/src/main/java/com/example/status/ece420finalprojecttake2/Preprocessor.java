package com.example.status.ece420finalprojecttake2;

/**
 * Created by Status on 4/16/2016.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.android.*;
//import org.opencv.imgcodecs.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preprocessor {
    public static void preprocess(String filename) {
        /********** Reading Input image **********/

        // Load an image from file - change this based on your image name
//        IplImage pInpImg = cvLoadImage(filename);
        Mat pInpImg = Highgui.imread(filename);
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
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0, 0, Imgproc.BORDER_DEFAULT);

        Log.d("Image Conversion", "Image Blurred");

        //Create threshold image
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255, 1, 1, 11, 2);

        Log.d("Image Conversion", "Image Thresholded");

        Highgui.imwrite(filename + "greyandthresh.jpg", thresh);

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
           Core.line(orig, approxCurve.toList().get(i%approxCurve.toList().size()), approxCurve.toList().get((i+1)%approxCurve.toList().size()), blue, thickness);
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
                Core.line(orig, largestrect.get(i%largestrect.size()), largestrect.get((i+1)%largestrect.size()), red, thickness);
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
        Highgui.imwrite(filename + "processed.jpg", dst);
        Highgui.imwrite(filename + "processed2.jpg", orig);
        Log.d("Preprocessor", "Image: " + orig);

        /**********  Map contour rectangle to bounding rectangle **********/
        List<Point> bounding_rectangle_points = new ArrayList<>();
        bounding_rectangle_points.add(new Point(bounding_rect.x, bounding_rect.y+bounding_rect.height));
        bounding_rectangle_points.add(new Point(bounding_rect.x+bounding_rect.width, bounding_rect.y+bounding_rect.height));
        bounding_rectangle_points.add(new Point(bounding_rect.x+bounding_rect.width, bounding_rect.y));
        bounding_rectangle_points.add(new Point(bounding_rect.x, bounding_rect.y));

//        vector<Point2f> rectangle_float = rectangletoPoint2f(largestrect);

        largestrect = rectangletoPoint2f(largestrect);

//        Mat transmtx = new Mat(4,1,Imgproc.CV_32FC2);
        Mat transmtx = new Mat(4,1,CvType.CV_32FC2);
        MatOfPoint largestrect_mat = new MatOfPoint();
        largestrect_mat.fromList(largestrect);
        largestrect_mat.convertTo(largestrect_mat,CvType.CV_32FC2);
        MatOfPoint bounding_rectangle_points_mat = new MatOfPoint();
        bounding_rectangle_points_mat.fromList(bounding_rectangle_points);
        bounding_rectangle_points_mat.convertTo(bounding_rectangle_points_mat,CvType.CV_32FC2);
        Log.d("Warping","Largest Rect: " + largestrect + "Bounding Rect: " + bounding_rectangle_points);
        transmtx = Imgproc.getPerspectiveTransform(largestrect_mat/*rectangle_float*/,bounding_rectangle_points_mat);
        Mat transformed = new Mat();
        Imgproc.warpPerspective(orig, transformed, transmtx, orig.size());


        /**********  Crop Image **********/
        bounding_rect.x += 0.035*bounding_rect.width;
        bounding_rect.y += 0.035*bounding_rect.height;
        bounding_rect.width -= 0.07*bounding_rect.width;
        bounding_rect.height -= 0.07*bounding_rect.height;
        Mat croppedImage = transformed.submat(bounding_rect);

        output = croppedImage;

        Highgui.imwrite(filename + "processed3.jpg", croppedImage);

        /**********  Binarize Image **********/
        //Convert to black and white
        Mat greyCroppedImage = new Mat();
        Imgproc.cvtColor(croppedImage, greyCroppedImage, Imgproc.COLOR_BGR2GRAY);
        Mat cropped_binary = new Mat();
        greyCroppedImage.convertTo(greyCroppedImage, CvType.CV_8UC1);

        //Median Blur to remove speckles and smooth
        Imgproc.medianBlur(greyCroppedImage, greyCroppedImage, 11);

        //Use adaptive threshhold to binarize image
        Imgproc.adaptiveThreshold(greyCroppedImage, cropped_binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 601, 100);

        // resize(cropped_binary, cropped_binary, SIZE??, 0, 0, interpolation);
        output = cropped_binary;

        Highgui.imwrite(filename + "processed4.jpg", cropped_binary);

        /**********  Freeing Memory **********/
//        Core.ReleaseImage(pInpImg);
        return;

    }

    public static void preprocess_test(String filename, Mat pInpImg) {
        /********** Reading Input image **********/

        // Load an image from file - change this based on your image name
//        IplImage pInpImg = cvLoadImage(filename);
//        Mat pInpImg = Imgcodecs.imread(filename);
//        if (pInpImg == null) {
//            Log.e("Image Error", "failed to load input image\n");
//            return;
//        }
//        else {
//            Log.d("Image Loading", "Image Loaded Successfully");
//        }

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
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0, 0, Imgproc.BORDER_DEFAULT);

        Log.d("Image Conversion", "Image Blurred");

        //Create threshold image
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(gray, thresh, 255, 1, 1, 11, 2);

        Log.d("Image Conversion", "Image Thresholded");

        Highgui.imwrite(filename + "greyandthresh.jpg", thresh);

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
            Core.line(orig, approxCurve.toList().get(i%approxCurve.toList().size()), approxCurve.toList().get((i+1)%approxCurve.toList().size()), blue, thickness);
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
                Core.line(orig, largestrect.get(i%largestrect.size()), largestrect.get((i+1)%largestrect.size()), red, thickness);
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
        Highgui.imwrite(filename + "processed.jpg", dst);
        Highgui.imwrite(filename + "processed2.jpg", orig);
        Log.d("Preprocessor", "Image: " + orig);

        /**********  Map contour rectangle to bounding rectangle **********/
        List<Point> bounding_rectangle_points = new ArrayList<>();
        bounding_rectangle_points.add(new Point(bounding_rect.x, bounding_rect.y+bounding_rect.height));
        bounding_rectangle_points.add(new Point(bounding_rect.x+bounding_rect.width, bounding_rect.y+bounding_rect.height));
        bounding_rectangle_points.add(new Point(bounding_rect.x+bounding_rect.width, bounding_rect.y));
        bounding_rectangle_points.add(new Point(bounding_rect.x, bounding_rect.y));

//        vector<Point2f> rectangle_float = rectangletoPoint2f(largestrect);

        largestrect = rectangletoPoint2f(largestrect);

//        Mat transmtx = new Mat(4,1,Imgproc.CV_32FC2);
        Mat transmtx = new Mat(4,1,CvType.CV_32FC2);
        MatOfPoint largestrect_mat = new MatOfPoint();
        largestrect_mat.fromList(largestrect);
        largestrect_mat.convertTo(largestrect_mat,CvType.CV_32FC2);
        MatOfPoint bounding_rectangle_points_mat = new MatOfPoint();
        bounding_rectangle_points_mat.fromList(bounding_rectangle_points);
        bounding_rectangle_points_mat.convertTo(bounding_rectangle_points_mat,CvType.CV_32FC2);
        Log.d("Warping","Largest Rect: " + largestrect + "Bounding Rect: " + bounding_rectangle_points);
        transmtx = Imgproc.getPerspectiveTransform(largestrect_mat/*rectangle_float*/,bounding_rectangle_points_mat);
        Mat transformed = new Mat();
        Imgproc.warpPerspective(orig, transformed, transmtx, orig.size());


        /**********  Crop Image **********/
        bounding_rect.x += 0.035*bounding_rect.width;
        bounding_rect.y += 0.035*bounding_rect.height;
        bounding_rect.width -= 0.07*bounding_rect.width;
        bounding_rect.height -= 0.07*bounding_rect.height;
        Mat croppedImage = transformed.submat(bounding_rect);

        output = croppedImage;

        Highgui.imwrite(filename + "processed3.jpg", croppedImage);

        /**********  Binarize Image **********/
        //Convert to black and white
        Mat greyCroppedImage = new Mat();
        Imgproc.cvtColor(croppedImage, greyCroppedImage, Imgproc.COLOR_BGR2GRAY);
        Mat cropped_binary = new Mat();
        greyCroppedImage.convertTo(greyCroppedImage, CvType.CV_8UC1);

        //Median Blur to remove speckles and smooth
        Imgproc.medianBlur(greyCroppedImage, greyCroppedImage, 11);

        //Use adaptive threshhold to binarize image
        Imgproc.adaptiveThreshold(greyCroppedImage, cropped_binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 601, 100);

        // resize(cropped_binary, cropped_binary, SIZE??, 0, 0, interpolation);
        output = cropped_binary;

        Highgui.imwrite(filename + "processed4.jpg", cropped_binary);

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
//        List<Point> temp = new ArrayList<Point>(4);
        double largest_area = 0;

        if(input.size() < 4){
            return new ArrayList<Point>(4);
        }
        MatOfPoint temp_mat = new MatOfPoint();

        for(int a = 0; a < input.size(); a++){
            for(int b = 0; b < input.size(); b++){
                for(int c = 0; c < input.size(); c++){
                    for(int d = 0; d < input.size(); d++){
                        List<Point> temp = new ArrayList<>();
                        temp.clear();
                        temp.removeAll(temp);
                        temp.add(input.get(a));
                        temp.add(input.get(b));
                        temp.add(input.get(c));
                        temp.add(input.get(d));
                        temp_mat.release();
                        temp_mat.fromList(temp);
                        if(a != b && a != c && a != d && b != c && b != d && c != d){
                            double area = Imgproc.contourArea(temp_mat,false);
                            if(area > largest_area){
                                output.removeAll(output);
                                output.clear();
                                largest_area = area;
                                output = temp;
                                Log.d("Contours","New Largest Quad: " + output + "New Largest Input" + temp + "Area: " + area);
                            }
//                            Log.d("Contours","New Largest quad");
                        }
                    }
                }
            }
        }
        //cout << "Largest from findLargestQuad: " << largest_area <<endl;
        return output;
    }

    /**
     * vector<Point2f> rectangletoPoint2f(vector<Point> input)
     * Inputs: vector of points for a Curve
     * Output: vector of 4 Point2f
     * Description: given an input vector of points in the Point datatype, return
     * 			a vector of points in the Point2f format. Also, arrange points
     * 			in specific order: Top left, top right, bottom right, bottom left.
     * 			within the vector.
     */
    private static List<Point> rectangletoPoint2f(List<Point> input){
        double centerx = ((float)input.get(0).x+input.get(1).x+input.get(2).x+input.get(3).x)/4;
        double centery = ((float)input.get(0).y+input.get(1).y+input.get(2).y+input.get(3).y)/4;
//        Point2f center(centerx, centery);
//
//        vector<Point2f> returnvalue(4, Point2f());

        List<Point> returnvalue = new ArrayList<>(4);

        returnvalue.add(new Point());
        returnvalue.add(new Point());
        returnvalue.add(new Point());
        returnvalue.add(new Point());

        for(int i = 0; i < 4; i++){
//            Point2f temp((float)input[i].x, (float)input[i].y);
            Point temp = new Point(input.get(i).x, input.get(i).y);

            if(temp.x < centerx && temp.y > centery){
                returnvalue.set(0,temp);
            }
            else if(temp.x > centerx && temp.y > centery){
                returnvalue.set(1,temp);
            }
            else if(temp.x > centerx && temp.y < centery){
                returnvalue.set(2,temp);
            }
            else{
                returnvalue.set(3,temp);
            }

        }

        return returnvalue;
    }

    public static void testBinarization(Context context) {
//        HOGDescriptor hogDesc = new HOGDescriptor(new Size(28,28), new Size(28,28), new Size(28,28), new Size(4,4), 12);
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/drawable/b_test_0.jpg");
//        Uri fileUri = Uri.parse("android.resource://com.example.status.ece420finalprojecttake2/"+R.drawable.b_test_0);
//        Log.d("HOG", "TEST IMAGE PATH: " + fileUri.toString());
//        Mat testImage = Imgcodecs.imread(fileUri.toString());
        Mat testImage = null;
        try {
            testImage = Utils.loadResource(context, R.drawable.b_test_0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (testImage == null) {
            Log.d("BINARY", "NULL MUTHAFUCKA");
        }

        Mat testImage2 = testImage;

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
                "binary_test.jpg";
        File mediaFile = new File(media_string);
        Uri fileUri = Uri.fromFile(mediaFile);
        Boolean bool  = Highgui.imwrite(fileUri.getPath(),testImage2);
        if (bool == true)
            Log.d("BINARY", "SUCCESS writing image to external storage");
        else
            Log.d("BINARY", "Fail writing image to external storage");

        Log.d("BINARY", "Image saved to: "+fileUri.getPath());
//        MatOfFloat features = new MatOfFloat();
//        hogDesc.compute(testImage,features);
//        Log.d("HOG","Features"+features);
        Log.d("BINARY", "Image: " + testImage);

//        preprocess(fileUri.toString());
        preprocess_test(mediaFile.getAbsolutePath(), testImage);
    }
}
