package com.swampsoftware.fastfood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Muhammad on 7/8/2016.
 */
public class HTTPVolley extends Activity {


    private Context context;
    private String urlQuery;
    private JSONObject jsonObject = new JSONObject();

    public HTTPVolley(Context context, String urlQuery) {
        this.context = context;
        this.urlQuery = urlQuery;
    }


    public void volleyGetHttp() {

        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        final ProgressDialog dialog = ProgressDialog.show(context, "",
                "Loading. Please wait...", true);
        String url = "https://api.backand.com/1/query/data/" + urlQuery;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        //Log.d("Response ", response.toString());
                        try {
                            jsonObject.put("restaurants", response);
                            if(context.toString().contains("Launcher")) {
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        try {
                            jsonObject.put("error", error);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String encodedCredentials = Base64.encodeToString((MASTER_TOKEN + ":" + USER_TOKEN).getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + encodedCredentials);
                return headers;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

    }
}
