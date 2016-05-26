package jce_ex3.com.tvguide;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private List<Show> showList = new ArrayList<>();
    private List<Show> tmpList = new ArrayList<>();
    private CustomListAdapter adapter;

    // Declaring Views
    private EditText searchED;
    private Button searchB;
    private ListView listView;

    private boolean checkSearch = false;
    private boolean clickable = true;

    final private int PERMISSION_REQUEST_CODE = 1; // request code for permissions

    private static int counter = 0;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getIntent().getBooleanExtra("alarm", false)) {
            clickable = false;
            Show show = new Show();

            show.setName(getIntent().getStringExtra("name"));
            System.out.println("111111111111111111111111 : " + show.getName());


            show.setSummary(getIntent().getStringExtra("summary"));
            System.out.println("222222222222222222222222 : " + show.getSummary());


            show.setThumbnailUrl(getIntent().getStringExtra("image"));

            System.out.println("333333333333333333333333: " + show.getThumbnailUrl());

            showList.add(show);
        }

        sharedPreferencesHelper = new SharedPreferencesHelper(getApplicationContext());
        sharedPreferencesHelper.getCounter("counter");

        // Defining views
        searchED = (EditText) findViewById(R.id.searchED);
        searchB = (Button) findViewById(R.id.searchB);
        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(this);
        adapter = new CustomListAdapter(this, showList);
        listView.setAdapter(adapter);

        // Search Button click listener
        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if the search edit text is not empty
                if (!searchED.getText().toString().isEmpty() || !searchED.getText().toString().matches("")) {

                    clickable = true;
                    checkSearch = true;
                    String searchQ = searchED.getText().toString();
                    show_getHttp(searchQ);

                }
            }
        });

    }

    // This is done using onResume instead of onStart to overRide when the user pauses the app to allow permissions from settings.
    // Or if user paused the app and gets back to it for any reason.
    protected void onResume() {

        super.onResume();


        /*
        // Check if user granted permissions
        if (checkPermissions()) {

            //TODO some shit
        } else {
            System.out.println("> Permissions are DENIED, Requesting from user : ");
            String[] permissions = {Manifest.permission.SET_ALARM};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
        */
    }

    // Function to check permissions
    // If all permissions are granted then return true;
    // If false then grant the user to allow permissions
    private boolean checkPermissions() {

        int set_alarm_permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SET_ALARM);

        if (set_alarm_permissionCheck == 0) {
            return true;
        } else {
            return false;
        }
    }

    // This method as called a result for interacting with the user, check if the user allowed or denied permissions
    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] results) {
        switch (request_code) {
            case PERMISSION_REQUEST_CODE: {
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (clickable) {
            if (checkSearch) {
                episode_getHttp(showList.get(position).getShowID());
                checkSearch = false;
            } else {
                //SchedualAlarm schedualAlarm = new SchedualAlarm(getApplicationContext(), showList.get(position));
                //schedualAlarm.scheduleAlarm(checkTime(position));
                scheduleAlarm(position, checkTime(position));

            }
        }
    }

    private long checkTime(int position) {

        Long time = new GregorianCalendar().getTimeInMillis();

        String[] EpisodeDate = showList.get(position).getAir_date().split("-");
        String[] EpisodeTime = showList.get(position).getAir_time().split(":");

        int episodeYear;
        int episodeMonth;
        int episodeDay;
        int episodeHour;


        episodeYear = Integer.parseInt(EpisodeDate[0]);
        episodeMonth = Integer.parseInt(EpisodeDate[1]);
        episodeDay = Integer.parseInt(EpisodeDate[2]);
        episodeHour = Integer.parseInt(EpisodeTime[0]);


        //if (episodeYear >= currentYear) {
        //    if (episodeMonth >= currentMonth) {
        //        if (episodeDay >= currentDay) {
        //            if (episodeHour > currentHour) {

        String tempDate = episodeMonth + "." + episodeDay + "." + episodeYear;
        SimpleDateFormat tempDateConvert = new SimpleDateFormat("MM.dd.yyyy");


        try {
            Date date = tempDateConvert.parse(tempDate);
            System.out.println("date.getTime : " + date.getTime());
            if ((date.getTime() + (episodeHour * 60 * 60 * 1000)) - time > 0) {
                return (date.getTime() + (episodeHour * 60 * 60 * 1000));
            } else {
                return -1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }

        //          }
        //        }
        //    }
        //}

    }

    public void scheduleAlarm(int position, long delay) {
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
            intentAlarm.putExtra("name", showList.get(position).getName());
            intentAlarm.putExtra("summary", showList.get(position).getSummary());
            intentAlarm.putExtra("image", showList.get(position).getThumbnailUrl());


            //Show show = new Show();
            //show.setName(showList.get(position).getName());
            //show.setSummary(showList.get(position).getSummary());
            //show.setThumbnailUrl(showList.get(position).getThumbnailUrl());
            //AlarmReceiver alarmReceiver = new AlarmReceiver(show);

            // create the object
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            System.out.println("=======222 " + delay);


            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, delay, PendingIntent.getBroadcast(this, counter, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            Toast.makeText(this, "Alarm Scheduled for " + calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR)
                    + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
            counter++;
            sharedPreferencesHelper.setCounter("counter", counter);
        } else {
            Toast.makeText(getApplicationContext(), "Episode is finished.", Toast.LENGTH_SHORT).show();
        }


    }


    public void episode_getHttp(String id) {

        String url = "http://api.tvmaze.com/shows/" + id + "?embed=episodes";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            showList.clear();


                            System.out.println("----------------- Response START -----------------");
                            for (int i = 0; i < response.getJSONObject("_embedded").getJSONArray("episodes").length(); i++) {

                                Show show = new Show();

                                String name = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("name");
                                String season = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("season");
                                String episode = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("number");
                                String airdate = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("airdate");
                                String airtime = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("airtime");
                                String summary = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("summary");
                                //summary = summary.replaceAll("<.*?>", "");
                                String image = "";
                                try {
                                    image = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getJSONObject("image").getString("medium");

                                } catch (Exception e) {
                                    image = "null";
                                }

                                show.setName(name);
                                show.setSeason_num(season);
                                show.setEpisode_num(episode);
                                show.setAir_date(airdate);
                                show.setAir_time(airtime);
                                show.setThumbnailUrl(image);
                                show.setSummary(summary);
                                showList.add(show);

                            }
                            System.out.println("----------------- Response END   -----------------");

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            System.out.println("Error");
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("----------------- Error START -----------------");
                        error.printStackTrace();
                        System.out.println("----------------- Error END   -----------------");

                    }
                }
        );

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    /*
    * Sends http post request to "api.tvmaze" using a search query, and gets back the result as json array.
    *
    * @Type : void
    *
    * @param : searchQ -  search field that is added to the url.
    */

    public void show_getHttp(String searchQ) {

        String url = "http://api.tvmaze.com/search/shows?q=" + searchQ;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {

                            showList.clear();
                            tmpList.clear();
                            ArrayList<String> days = new ArrayList<>();
                            System.out.println("----------------- Response START -----------------");
                            for (int i = 0; i < response.length(); i++) {
                                Show show = new Show();
                                System.out.println("----------------- Json Parse START -----------------");

                                String name = response.getJSONObject(i).getJSONObject("show").getString("name");
                                name = name.replaceAll("<.*?>", "");
                                System.out.println("> Name : " + name);
                                show.setName(name);

                                System.out.println("> ID : " + response.getJSONObject(i).getJSONObject("show").getString("id"));

                                String showID = response.getJSONObject(i).getJSONObject("show").getString("id");
                                show.setShowID(showID);

                                String summary = response.getJSONObject(i).getJSONObject("show").getString("summary");
                                //summary = summary.replaceAll("<.*?>", "");

                                System.out.println("> Summary : " + summary);
                                show.setSummary(summary);

                                try {
                                    String image = response.getJSONObject(i).getJSONObject("show").getJSONObject("image").getString("medium");
                                    System.out.println("> Image : " + response.getJSONObject(i).getJSONObject("show").getJSONObject("image").getString("medium"));
                                    show.setThumbnailUrl(image);


                                } catch (Exception e) {
                                    show.setThumbnailUrl("null");
                                }

                                System.out.println("> time : " + response.getJSONObject(i).getJSONObject("show").getJSONObject("schedule").getString("time"));

                                JSONArray daysJsonArray = response.getJSONObject(i).getJSONObject("show").getJSONObject("schedule").getJSONArray("days");
                                for (int j = 0; j < daysJsonArray.length(); j++) {
                                    days.add(daysJsonArray.getString(j));
                                    System.out.println("> Days : " + days.get(j));
                                }
                                System.out.println("----------------- Json Parse END -----------------");

                                showList.add(show);
                            }
                            System.out.println("----------------- Response END   -----------------");

                            tmpList = showList; // If we go from shows to episodes and then we get back, the list will be displayed without http request because it is backed up in tmpList
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            System.out.println("Error");
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("----------------- Error START -----------------");
                        error.printStackTrace();
                        System.out.println("----------------- Error END   -----------------");

                    }
                }
        );

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

    }
}
