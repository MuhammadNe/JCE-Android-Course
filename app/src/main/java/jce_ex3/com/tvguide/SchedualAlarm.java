package jce_ex3.com.tvguide;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Muhammad on 5/26/2016.
 */
public class SchedualAlarm extends Activity {

    private Context context;
    private Show show;
    private static int counter = 0;

    public SchedualAlarm(Context context, Show show) {
        this.context  = context;
        this.show = show;
    }

    public void scheduleAlarm(long delay) {
        // time at which alarm will be scheduled here alarm is scheduled at 1 day from current time,
        // we fetch  the current time in milliseconds and added 1 day time
        // i.e. 24*60*60*1000= 86,400,000  milliseconds in a day

        System.out.println("======= " + delay);
        if (delay > 0) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(delay);


            // create an Intent and set the class which will execute when Alarm triggers, here we have
            // given AlarmReceiver in the Intent, the onRecieve() method of this class will execute when
            // alarm triggers and
            //we will write the code to send SMS inside onRecieve() method pf Alarmreceiver class
            Intent intentAlarm = new Intent(this, AlarmReceiver.class);
            intentAlarm.putExtra("name", show.getName());
            intentAlarm.putExtra("summary", show.getSummary());
            intentAlarm.putExtra("image", show.getThumbnailUrl());


            //Show show = new Show();
            //show.setName(showList.get(position).getName());
            //show.setSummary(showList.get(position).getSummary());
            //show.setThumbnailUrl(showList.get(position).getThumbnailUrl());
            //AlarmReceiver alarmReceiver = new AlarmReceiver(show);

            // create the object
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            System.out.println("=======222 " + delay);

            Long time = new GregorianCalendar().getTimeInMillis() + 1 * 1 * 5 * 1000;

            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, counter, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            Toast.makeText(context, "Alarm Scheduled for " + calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR)
                    + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
            counter++;
        } else {
            Toast.makeText(context, "Episode is finished.", Toast.LENGTH_SHORT).show();
        }


    }
}
