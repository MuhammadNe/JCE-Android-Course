package jce_ex3.com.tvguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 *  AlarmReceiver class for when the alarm is triggered, it will open the main activity and pass the episode parameters
 */
public class AlarmReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated method stub

        Toast.makeText(context, "Episode Ready!", Toast.LENGTH_LONG).show();

        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("alarm", true);
        i.putExtra("name", intent.getStringExtra("name"));
        i.putExtra("summary", intent.getStringExtra("summary"));
        i.putExtra("image", intent.getStringExtra("image"));
        context.startActivity(i);
    }

}