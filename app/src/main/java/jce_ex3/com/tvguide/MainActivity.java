package jce_ex3.com.tvguide;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // Declaring objects
    private List<Show> showList = new ArrayList<>();
    private CustomListAdapter adapter;
    private SharedPreferencesHelper sharedPreferencesHelper; // shared preferences for saving the counter, so if the app is turned off then on, the counter will NOT set back to 0 and override older alarms

    // Declaring Views
    private EditText searchED;
    private Button searchB;
    private ListView listView;

    // Declaring variables
    private boolean checkSearch = false; // this variable checks if the user has the shows lists or episode lists, if shows lists then he can click a show and search for episodes, if not then alarm
    private boolean clickable = true; // this variable checks if the alarm opened the app, then the list will be unclickable.

    private static int counter = 0; // this variable works as an ID for the alarm manager, each alarm got his own id.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // if this is true then the alarm opened the app, set list clickable to false, and load the episode.
        if (getIntent().getBooleanExtra("alarm", false)) {
            clickable = false;
            Show show = new Show();
            show.setName(getIntent().getStringExtra("name"));
            show.setSummary(getIntent().getStringExtra("summary"));
            show.setThumbnailUrl(getIntent().getStringExtra("image"));
            showList.add(show);
        }

        // Get the counter from shared preferences
        sharedPreferencesHelper = new SharedPreferencesHelper(getApplicationContext());
        sharedPreferencesHelper.getCounter("counter");

        // Defining views
        searchED = (EditText) findViewById(R.id.searchED);
        searchB = (Button) findViewById(R.id.searchB);
        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(this); // makes the list items clickable
        adapter = new CustomListAdapter(this, showList);
        listView.setAdapter(adapter);

        // Search Button click listener
        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if the search edit text is not empty
                if (!searchED.getText().toString().isEmpty() || !searchED.getText().toString().matches("")) {

                    clickable = true; // if the user searched, then he is in the first list, he can click the items and search for episodes
                    checkSearch = true;
                    String searchQ = searchED.getText().toString(); // search query
                    show_getHttp(searchQ); // get all episode for selected show

                }
            }
        });

    }


    /*
    *
    *  If the list is clickable that means the user is getting all episode for a show.
    *  When an episode is clicked, checktime() will check when the time will be displayed in milliseconds,
    *  and then starts the alarm
    *
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (clickable) {
            if (checkSearch) {
                episode_getHttp(showList.get(position).getShowID());
                checkSearch = false;
            } else {
                scheduleAlarm(position, checkTime(position));

            }
        }
    }

    /*
    *
    * This function will calculate the display time of an episode
    *
    * @params : int position : position of episode in showList<>
    *
    * @returns : long delay : the time in which the episode will be displayed
    *
     */
    private long checkTime(int position) {

        Long time = new GregorianCalendar().getTimeInMillis(); // gets the current time
        int episodeYear, episodeMonth, episodeDay, episodeHour;

        if (showList.get(position).getAir_time() == "" || showList.get(position).getAir_time().isEmpty() || showList.get(position).getAir_time() == null) {
            episodeHour = 0;
        } else {
            String[] EpisodeTime = showList.get(position).getAir_time().split(":"); // parse time according to :    12:00
            episodeHour = Integer.parseInt(EpisodeTime[0]);
        }

        String[] EpisodeDate = showList.get(position).getAir_date().split("-"); // parse date according to -    2016-05-017
        episodeYear = Integer.parseInt(EpisodeDate[0]);
        episodeMonth = Integer.parseInt(EpisodeDate[1]);
        episodeDay = Integer.parseInt(EpisodeDate[2]);


        String tempDate = episodeMonth + "." + episodeDay + "." + episodeYear;
        SimpleDateFormat tempDateConvert = new SimpleDateFormat("MM.dd.yyyy");


        //  subtract the current time from the episode show time, if the answer is < 0 then the episode is finished.
        try {
            Date date = tempDateConvert.parse(tempDate); // this will return day, month, year...hours will be added
            // get the episode time, and then add the hours : numOfHours * Minutes * Seconds * MilliSeconds to complete the day
            if ((date.getTime() + (episodeHour * 60 * 60 * 1000)) - time > 0) {
                return (date.getTime() + (episodeHour * 60 * 60 * 1000));
            } else {
                return -1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }


    }

    /*
    *
    *  Function that creates an alarm, according to delay and episode
    *
    *  @Params : int position : episode position in showList<>
    *  @Params : long delay : time in milliseconds that will be set to alarm
    *
    *
     */
    public void scheduleAlarm(int position, long delay) {

        if (delay > 0) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(delay);


            // create an Intent and set the class which will execute when Alarm triggers, here we have
            // given AlarmReceiver in the Intent, the onRecieve() method of this class will execute when
            // alarm triggers
            // we pass the episode info to the alarm class
            Intent intentAlarm = new Intent(this, AlarmReceiver.class);
            intentAlarm.putExtra("name", showList.get(position).getName());
            intentAlarm.putExtra("summary", showList.get(position).getSummary());
            intentAlarm.putExtra("image", showList.get(position).getThumbnailUrl());

            // create the object
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            //set the alarm for particular time
            alarmManager.set(AlarmManager.RTC_WAKEUP, delay, PendingIntent.getBroadcast(this, counter, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            Toast.makeText(this, "Alarm Scheduled for " + calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR)
                    + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
            counter++; // increment counter by 1 to make it different for each alarm
            sharedPreferencesHelper.setCounter("counter", counter); // save it in the shared preferences
        } else {
            Toast.makeText(getApplicationContext(), "Episode is finished.", Toast.LENGTH_SHORT).show();
        }

    }

    /*
    *
    * Volley request for getting episodes of show
    *
    * @Params : String id : if of the show
     */
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
                            for (int i = 0; i < response.getJSONObject("_embedded").getJSONArray("episodes").length(); i++) {

                                // create a new object for each episode, set all the data and add it to the showList<>
                                Show show = new Show();

                                String name = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("name");
                                String season = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("season");
                                String episode = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("number");
                                String airdate = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("airdate");
                                String airtime = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("airtime");
                                String summary = response.getJSONObject("_embedded").getJSONArray("episodes").getJSONObject(i).getString("summary");
                                String image = ""; // there might not be an image!!!!
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
                            adapter.notifyDataSetChanged(); // change the list
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

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
                            ArrayList<String> days = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                Show show = new Show();

                                String name = response.getJSONObject(i).getJSONObject("show").getString("name");
                                String showID = response.getJSONObject(i).getJSONObject("show").getString("id");
                                String summary = response.getJSONObject(i).getJSONObject("show").getString("summary");

                                show.setName(name);
                                show.setShowID(showID);
                                show.setSummary(summary);
                                String image = ""; // There might not be an image...
                                try {
                                    image = response.getJSONObject(i).getJSONObject("show").getJSONObject("image").getString("medium");
                                } catch (Exception e) {
                                    image = "null";
                                }
                                show.setThumbnailUrl(image);

                                showList.add(show);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                    }
                }
        );

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

    }
}
