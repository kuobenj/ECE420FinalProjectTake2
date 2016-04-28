package com.example.status.ece420finalprojecttake2;


/******************************************************************************
 *  Compilation:  javac EvaluateDeluxe.java
 *  Execution:    java EvaluateDeluxe
 *  Dependencies: Stack.java
 *
 *  Evaluates arithmetic expressions using Dijkstra's two-stack algorithm.
 *  Handles the following binary operators: +, -, *, / and parentheses.
 *
 *  % echo "3 + 5 * 6 - 7 * ( 8 + 5 )" | java EvaluateDeluxe
 *  -58.0
 *
 *
 *  Limitiations
 *  --------------
 *    -  can easily add additional operators and precedence orders, but they
 *       must be left associative (exponentiation is right associative)
 *    -  assumes whitespace between operators (including parentheses)
 *
 *  Remarks
 *  --------------
 *    -  can eliminate second phase if we enclose input expression
 *       in parentheses (and, then, could also remove the test
 *       for whether the operator stack is empty in the inner while loop)
 *    -  see http://introcs.cs.princeton.edu/java/11precedence/ for
 *       operator precedence in Java
 *
 ******************************************************************************/

import java.util.TreeMap;
import java.util.Stack;
import java.lang.Character;
import java.lang.Math;

public class EvaluateDeluxe {

    public static String stringfixerooni(String input){
        //fuck spaces
        input.replace(" ", "");

        //Minus sign boyz
        if(input.charAt(0) == '-'){
            input = "0" + input;
        }

        //multiply boogaloo
        for(int i = 0; i < input.length()-1; i++){

            if(input.charAt(i) >= '0' && input.charAt(i) <= '9' && (input.charAt(i+1) == '(' || input.charAt(i+1) == 'x' || input.charAt(i+1) == 'y')){
                input = input.substring(0, i+1) + "*" + input.substring(i+1, input.length());
            }
            else if((input.charAt(i+1) == '(' || input.charAt(i+1) == 'x' || input.charAt(i+1) == 'y') && input.charAt(i+1) >= '0' && input.charAt(i+1) <= '9'){
                input = input.substring(0, i+1) + "*" + input.substring(i+1, input.length());
            }
        }

        return input;
    }

    // result of applying binary operator op to two operands val1 and val2
    public static double eval(String op, double val1, double val2) {
        if (op.equals("+")) return val1 + val2;
        if (op.equals("-")) return val1 - val2;
        if (op.equals("/")) return val1 / val2;
        if (op.equals("*")) return val1 * val2;
        if (op.equals("^")) return Math.pow(val1,val2);
        throw new RuntimeException("Invalid operator");
    }

    public static double parseEquation(String equation) {
        // precedence order of operators
        TreeMap<String, Integer> precedence = new TreeMap<String, Integer>();
        precedence.put("(", 0);   // for convenience with algorithm
        precedence.put(")", 0);
        precedence.put("+", 1);   // + and - have lower precedence than * and /
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);
        precedence.put("^", 3);

        Stack<String> ops  = new Stack<String>();
        Stack<Double> vals = new Stack<Double>();

        for (int i = 0; i < equation.length(); i=i+1) {

            String s = "";

            // read in next token (operator or value)
            if((equation.charAt(i) >= '0' && equation.charAt(i) <= '9')){
                while(i < equation.length() && ((equation.charAt(i) >= '0' && equation.charAt(i) <= '9') || equation.charAt(i) == '.')) {
                    s = s + equation.charAt(i);
                    i++;
                }
                i--;
            }
            else{
                s = s + equation.charAt(i);
            }

            // token is a value
            if (!precedence.containsKey(s)) {
                vals.push(Double.parseDouble(s));
                continue;
            }

            // token is an operator
            while (true) {

                // the last condition ensures that the operator with higher precedence is evaluated first
                if (ops.isEmpty() || s.equals("(") || (precedence.get(s) > precedence.get(ops.peek()))) {
                    ops.push(s);
                    break;
                }

                // evaluate expression
                String op = ops.pop();

                // but ignore left parentheses
                if (op.equals("(")) {
                    assert s.equals(")");
                    break;
                }

                // evaluate operator and two operands and push result onto value stack
                else {
                    double val2 = vals.pop();
                    double val1 = vals.pop();

                    vals.push(eval(op, val1, val2));
                }
            }
        }

        // finished parsing string - evaluate operator and operands remaining on two stacks
        while (!ops.isEmpty()) {
            String op = ops.pop();
            double val2 = vals.pop();
            double val1 = vals.pop();
            vals.push(eval(op, val1, val2));
        }

        // last value on stack is value of expression
        assert vals.isEmpty();
        assert ops.isEmpty();
        return vals.pop();
    }

    public static void main(String[] args) {



        String equationStr = "2(x-1)^2";
        String evaluationStr;
        String valStr;
        int pointNum = 201;
        double[] xVals = new double[pointNum];
        double[] yVals = new double[pointNum];

        System.out.println("plsfix");
        equationStr = stringfixerooni(equationStr);
        System.out.println(equationStr);

        for (int i = 0; i < pointNum; i++) {
            xVals[i] = 0.1 * i - 10;
            if (xVals[i] < 0)
                valStr = "(0"+Double.toString(xVals[i])+")";
            else
                valStr = Double.toString(xVals[i]);
            evaluationStr = equationStr.replace("x", valStr);
            yVals[i] = parseEquation(evaluationStr);
            System.out.print(yVals[i] + " ");
        }

    }
}