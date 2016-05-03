package com.example.status.ece420finalprojecttake2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Start up", "Package name: "+getApplicationContext().getPackageName());

        setTitle("#420yolOCR");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.take_pic) {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

            return true;
        }

        if (id == R.id.plot_eqn){
//            GraphView graph = (GraphView) findViewById(R.id.graph);
//            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
//                    new DataPoint(0, 1),
//                    new DataPoint(1, 5),
//                    new DataPoint(2, 3),
//                    new DataPoint(3, 2),
//                    new DataPoint(4, 6)
//            });
//            graph.addSeries(series);


            String equationStr;
            String evaluationStr;
            String valStr;
            int pointNum = 201;
            double[] xVals = new double[pointNum];
            double[] yVals = new double[pointNum];

            Log.d("Plotting","Last equation: "+last_equation);
            equationStr = EvaluateDeluxe.stringfixerooni(last_equation);
            Log.d("Plotting","equationStr: "+equationStr);
            for (int i = 0; i < pointNum; i++) {
                xVals[i] = 0.1 * i - 10;
                if (xVals[i] < 0)
                    valStr = "(0" + Double.toString(xVals[i]) + ")";
                else
                    valStr = Double.toString(xVals[i]);
                evaluationStr = equationStr.replace("x", valStr);
                yVals[i] = EvaluateDeluxe.parseEquation(evaluationStr);
            }

            GraphView graph = (GraphView) findViewById(R.id.graph);
            graph.removeAllSeries();
            graph = (GraphView) findViewById(R.id.graph);

            DataPoint[] datapoints = new DataPoint[pointNum];
            for (int i = 0; i < pointNum; i++) {
                datapoints[i] = new DataPoint(xVals[i],yVals[i]);
            }

            Log.d("Plotting","x's: "+xVals.length);
            Log.d("Plotting","y's: "+yVals.length);
            Log.d("Plotting","points: "+datapoints.length);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);

            Log.d("Plotting","series: "+series);
            graph.addSeries(series);
        }

//        if (id == R.id.test_char){
//            try {
//                OCR.readChar(this);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (id == R.id.test_segmentation){
//            my_ocr = new OCR(this);
//            try {
//                my_ocr.testSegmentation();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (id == R.id.test_img){
//            Preprocessor.testBinarization(this);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        fileUri.toString(), Toast.LENGTH_LONG).show();
                // Display the extras
//                Log.d("Getting Extras", (data.getExtras()).toString());

                //display image
                ImageView jpgView = (ImageView)findViewById(R.id.jpgview);
                File myFile = new File(fileUri.getPath());
                Bitmap d = BitmapFactory.decodeFile(myFile.getAbsolutePath());
//                Bitmap d = BitmapFactory.decodeFile(String.valueOf(data.getExtras().get(MediaStore.EXTRA_OUTPUT)));
//                jpgView.setImageBitmap(d);

                last_file = myFile.getAbsolutePath();

                Preprocessor preprocessor = new Preprocessor();
                Preprocessor.preprocess(myFile.getAbsolutePath());

                OCR my_ocr = new OCR(this);

                String equation = null;
                try {
                    equation = my_ocr.getEquation(myFile.getAbsolutePath()+"processed4.jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                d = BitmapFactory.decodeFile(myFile.getAbsolutePath()+"processed4.jpg");
                jpgView.setImageBitmap(d);

                TextView textView = (TextView)findViewById(R.id.textView1);
                textView.setText(equation);

                last_equation = equation;

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Image Capture Canceled", Toast.LENGTH_LONG).show();
            } else {
                // Image capture failed, advise user
                Toast.makeText(this, "Image Capture FAILED", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        Log.d("Path Test",(Uri.fromFile(getOutputMediaFile(type))).getEncodedPath());
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            String media_string = mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(media_string);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private OCR my_ocr;
    private String last_file = null;
    private String last_equation = null;


    static{ System.loadLibrary("opencv_java"); }
}
