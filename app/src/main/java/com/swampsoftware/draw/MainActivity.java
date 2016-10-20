package com.swampsoftware.draw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    // Declaring Objects
    private RightFlag rf;
    private LeftFlag lf;

    // Declaring views
    private TextView TVtimer;
    private com.swampsoftware.draw.RightFlag rightFlag; // right flag custom view
    private com.swampsoftware.draw.LeftFlag leftFlag; // left flag custom view

    //Declaring variables
    private long startTime = 0L; // timer starts at 0
    private Handler customHandler = new Handler(); // handler for timer
    private int timer = 90; // timer ends at 90 seconds "could be added 15 seconds"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing views
        TVtimer = (TextView) findViewById(R.id.TVtimer);
        rightFlag = (com.swampsoftware.draw.RightFlag) findViewById(R.id.rightFlag);
        leftFlag = (com.swampsoftware.draw.LeftFlag) findViewById(R.id.leftFlag);

        // Initializing Objects
        rf = new RightFlag(getApplicationContext());
        lf = new LeftFlag(getApplicationContext());
    }

    /*****
     * **** *****
     * <p>
     * Runnable method for starting the timer, each second it will update from milliseconds and add 1 second
     * <p>
     * **** *****
     *****/
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            int secs = (int) (timeInMilliseconds / 1000); // each 1000 milli = 1 sec
            int milliseconds = (int) (timeInMilliseconds % 1000); // get the rest millis
            TVtimer.setText(String.format("%02d", secs) + ":" + String.format("%03d", milliseconds)); // print the time to text view
            customHandler.postDelayed(this, 0);
            if (secs >= timer) { // check if the timer reached the maximum, then check whether to add more 15 seconds or not using the method "stopTimer()"
                stopTimer();
            }
        }
    };


    /*****
     * **** *****
     * <p>
     * stopTimer method is triggered when the timer reaches the maximum, it then checks if the counters are different, if yes then update the status string inside the custom views and invalidate
     * if we have a tie then check if we are in the first chunk of time or 15 seconds had been added, if first time then add 15 seconds, if not then declare a TIE
     * then invalidate the custom views;
     * <p>
     * **** *****
     *****/
    private void stopTimer() {

        // get both counters of each flag
        int counterR = rf.getCounter();
        int counterL = lf.getCounter();
        String rf_status = "", lf_status = ""; // string for giving status

        // first check if both counters are equal and we are in the first chunk, if not then either they are not equal, or not in the first chink
        if (counterR == counterL && (timer >= 90 && timer < 105)) {
            timer += 15; // add 15 seconds to timer and don't do anything
        } else {
            if (counterR != counterL) { // if we get here then one is winner and the other is loser
                if (counterR > counterL) {
                    rf_status = "WIN";
                    lf_status = "LOSE";
                } else if (counterL > counterR) {
                    lf_status = "WIN";
                    rf_status = "LOSE";
                }

            } else if (counterR == counterL && timer >= 105) { // if we get here then its 100% tie
                lf_status = "TIE";
                rf_status = "TIE";

            }
            // update views method will invalidate custom views to the new values
            updateViews(counterR, counterL, rf_status, lf_status);
            // stop the counter
            customHandler.removeCallbacks(updateTimerThread);

        }
    }

    /*****
     * **** *****
     * <p>
     * Button view onclick listener will reset views, and reset timer, and start timer
     * <p>
     * **** *****
     *****/
    public void Bstart(View view) {

        timer = 90;
        updateViews(0, 0, "", "");
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

    }

    /*****
     * **** *****
     * <p>
     * updateViews method will either reset, or update after game is finished
     * <p>
     * **** *****
     *****/
    public void updateViews(int rf_counter, int lf_counter, String rf_status, String lf_status) {
        rf.setCounter(rf_counter);
        rf.setStatus(rf_status);
        lf.setCounter(lf_counter);
        lf.setStatus(lf_status);

        rightFlag.invalidate();
        leftFlag.invalidate();
    }

    /*****
     * **** *****
     * <p>
     * onPause will stop the timer
     * <p>
     * **** *****
     *****/
    @Override
    protected void onPause() {
        super.onPause();
        customHandler.removeCallbacks(updateTimerThread);
    }
}
