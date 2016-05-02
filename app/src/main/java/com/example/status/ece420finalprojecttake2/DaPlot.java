package com.example.status.ece420finalprojecttake2;

import android.content.Context;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Status on 5/2/2016.
 */
public class DaPlot {

    public static void plot(String equation, Context context) {
        String equationStr;
        String evaluationStr;
        String valStr;
        int pointNum = 201;
        double[] xVals = new double[pointNum];
        double[] yVals = new double[pointNum];

        equationStr = EvaluateDeluxe.stringfixerooni(equation);

        for (int i = 0; i < pointNum; i++) {
            xVals[i] = 0.1 * i - 10;
            if (xVals[i] < 0)
                valStr = "(0" + Double.toString(xVals[i]) + ")";
            else
                valStr = Double.toString(xVals[i]);
            evaluationStr = equationStr.replace("x", valStr);
            yVals[i] = EvaluateDeluxe.parseEquation(evaluationStr);
        }

        View v = new View(context);
        GraphView graph = (GraphView) v.findViewById(R.id.graph);

        DataPoint[] datapoints = new DataPoint[pointNum];
        for (int i = 0; i < pointNum; i++) {
            datapoints[i] = new DataPoint(xVals[i],yVals[i]);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);

        graph.addSeries(series);
    }
}
