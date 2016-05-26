package jce_ex3.com.tvguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Muhammad on 5/23/2016.
 */
public class AlarmReceiver extends BroadcastReceiver
{

    private static Show show;

    public AlarmReceiver() {

    }
    public AlarmReceiver(Show show) {
        this.show = show;
    }
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated method stub

        // Show the toast  like in above screen shot
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