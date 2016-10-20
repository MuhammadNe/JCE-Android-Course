package com.swampsoftware.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.Path;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

/**
 * Created by Muhammad on 6/1/2016.
 */
public class RightFlag extends View {

    // Declaring draw variables
    private Paint paint_border, paint_flag, paint_text;
    private Path path;

    // Declaring variables
    private int left, top, width, height;
    private static int counter = 0; // flag counter
    private static long prevTime = 0, longClick = 0; // for managing clicking events
    private boolean firstTap = true; // for managing clicking events
    private static String status = ""; // after finish game status


    public RightFlag(Context context) {
        super(context);
        initView(null);

    }

    public RightFlag(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    // Getters and Setters
    public static String getStatus() {
        return status;
    }

    public static void setStatus(String status) {
        RightFlag.status = status;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        RightFlag.counter = counter;
    }

    /*****
     * **** *****
     * <p>
     * initView method will initialize all draw variables
     * <p>
     * **** *****
     *****/
    private void initView(AttributeSet attrs) {


        paint_border = new Paint();
        paint_flag = new Paint();
        paint_text = new Paint();
        path = new Path();

        paint_flag.setColor(Color.BLUE);
        paint_flag.setStrokeWidth(7);
        paint_flag.setAntiAlias(true);
        paint_flag.setStyle(Paint.Style.STROKE);

        paint_border.setColor(Color.BLUE);
        paint_border.setStrokeWidth(10);
        paint_border.setAntiAlias(true);
        paint_border.setStyle(Paint.Style.STROKE);

        paint_text.setColor(Color.BLACK);
        paint_text.setTextSize(40);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        left = getPaddingLeft();
        top = getPaddingTop();
        width = w - (getPaddingLeft() + getPaddingRight());
        height = h - (getPaddingBottom() + getPaddingTop());
    }

    /*****
     * **** *****
     * <p>
     * onDraw method for drawing flags - counter - status
     * <p>
     * **** *****
     *****/
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawLine(left, (float) (top + (height * 0.1)), left + width, (float) (top + (height * 0.1)), paint_border); // top border
        canvas.drawLine(left, (float) (top + (height * 0.9)), left + width, (float) (top + (height * 0.9)), paint_border); // bottom border

        // Upper triangle
        path.moveTo((left + width) / 2, (float) (top + (height * 0.2)));
        path.lineTo((float) (left + (width * 0.35)), (float) (top + (height * 0.65)));
        path.lineTo((float) (left + (width * 0.65)), (float) (top + (height * 0.65)));
        path.close();

        //Bottom triangle
        path.moveTo((left + width) / 2, (float) (top + (height * 0.8)));
        path.lineTo((float) (left + (width * 0.65)), (float) (top + (height * 0.35)));
        path.lineTo((float) (left + (width * 0.35)), (float) (top + (height * 0.35)));
        path.close();

        canvas.drawPath(path, paint_flag);

        canvas.drawText(counter + "", (float) (left + (width * 0.1)), (float) (top + (height * 0.3)), paint_text); // Counter text
        canvas.drawText(status, (float) (left + (width * 0.1)), (float) (top + (height * 0.7)), paint_text); // Status text

    }

    /*****
     * **** *****
     * <p>
     * onTouchEvents will handle the double click and long click according to time stamp
     * after successful clicks validate the view
     * <p>
     * **** *****
     *****/
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                longClick = Calendar.getInstance().getTimeInMillis(); // get time stamp for long click
                if (firstTap) { // if this is the first click then get the timestamp
                    //Log.d("TAP", "First Tap");
                    prevTime = Calendar.getInstance().getTimeInMillis();
                    firstTap = false;
                } else { // if not first click then
                    long currentTime = Calendar.getInstance().getTimeInMillis(); // get the comparison time stamp
                    // check that the time between the first click and the second is less than 600ms, if not then the second click will be treated as the first click
                    // also check that we got the time right
                    if (currentTime > prevTime) {
                        if (currentTime - prevTime <= 400) {
                            counter++;
                            firstTap = true;
                            //Log.d("TAP", "second click");
                        } else {
                            // here we tread this second click as a first click
                            prevTime = Calendar.getInstance().getTimeInMillis();
                            firstTap = false;
                            //Log.d("TAP", "second click is treated as first click");
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.e("action", "actionMove");
                break;
            case MotionEvent.ACTION_UP: // this case will check if the user kept ACTION_DOWN for 400milliseconds
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime > longClick) {
                    if (currentTime - longClick >= 400) {
                        counter = 0;
                    }
                }
                //Log.e("action", "actionUp");
                break;
        }

        invalidate();

        return true;
    }


}
