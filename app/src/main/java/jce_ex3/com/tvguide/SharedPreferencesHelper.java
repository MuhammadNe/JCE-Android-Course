package jce_ex3.com.tvguide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by Muhammad on 5/26/2016.
 */
public class SharedPreferencesHelper extends Activity{

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    public SharedPreferencesHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("shows", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setCounter(String key, int counter) {

        editor.remove(key);
        editor.putInt(key, counter);
        editor.commit();
    }

    public int getCounter(String key) {


        int counter = sharedPreferences.getInt(key, 0);
        return counter;
    }
}
