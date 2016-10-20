
// EX1

package jceex1.example.com.camandrate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class MainActivity extends AppCompatActivity {


    //Declaring views
    private ImageView imageView1, imageView2, imageView3;
    private Button takePic, next, settings;
    private RatingBar ratingbar1, ratingbar2, ratingbar3;
    private boolean isClicked1, isClicked2, isClicked3;

    public static float textSize = 20; // static variable for assiging the button text size
    private boolean isOnCreate = true; // check if last activity was settings or not, this will help assigning text size into shared preferences

    private Bitmap bitmap; // bitmap for displaying inside image views
    private static final int CAMERA_REQUEST = 1888;

    private SharedPreferences sharedPref;       // shared preferences for storing elapsed time and best time
    private String filepath1;       // file path of Bitmap
    private String filepath2;       // file path of Bitmap
    private String filepath3;       // file path of Bitmap
    private String rbclick1;        // check if rating button is clickable or not
    private String rbclick2;        // check if rating button is clickable or not
    private String rbclick3;        // check if rating button is clickable or not
    private String rbrate1;         // getting the rate of rating bar
    private String rbrate2;         // getting the rate of rating bar
    private String rbrate3;         // getting the rate of rating bar


    /*****************************************************
     * \
     * <p/>
     * ON CREATE METHOD
     * <p/>
     * //
     ****************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing Variables
        settings = (Button) findViewById(R.id.Bsettings);
        next = (Button) findViewById(R.id.Bnext);
        takePic = (Button) findViewById(R.id.Bpic);

        imageView1 = (ImageView) findViewById(R.id.IVthumbnail1);
        imageView2 = (ImageView) findViewById(R.id.IVthumbnail2);
        imageView3 = (ImageView) findViewById(R.id.IVthumbnail3);

        ratingbar1 = (RatingBar) findViewById(R.id.RBrating1);
        ratingbar2 = (RatingBar) findViewById(R.id.RBrating2);
        ratingbar3 = (RatingBar) findViewById(R.id.RBrating3);

        // assiging false to isClicked that will check if rating bar has already been clicked, preventing second rating
        isClicked1 = false;
        isClicked2 = false;
        isClicked3 = false;

        filepath1 = "filepath1"; // for saving inside shared preferences
        filepath2 = "filepath2";
        filepath3 = "filepath3";

        rbclick1 = "rbclick1"; // for saving inside shared preferences
        rbclick2 = "rbclick2";
        rbclick3 = "rbclick3";

        rbrate1 = "rbrate1"; //  for saving inside shared preferences
        rbrate2 = "rbrate2";
        rbrate3 = "rbrate3";

        //Creating a shared preferences object
        String myPREFERENCES = "MyPrefs";
        sharedPref = getSharedPreferences(myPREFERENCES, 0);

        setBitMaps(); // updating the bitmaps
        setRating(); // updating the rating bars
        updateTextSize(); // updating the text size

        // event listener for taking pictures
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST); // this will call "onActivityResult"


            }
        });

        // event listener for settings button
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
            }
        });

        // event listener for previewing the bitmap with highest rate
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // first check if all the image views are empty, if yes dont switch activity
                if (imageView1.getDrawable() == null && imageView2.getDrawable() == null && imageView3.getDrawable() == null) {
                    Toast.makeText(getApplicationContext(), "No pictures to view", Toast.LENGTH_SHORT).show();
                    return;
                }

                // if there is at least one image view not NULL, then update the rating
                // this method is done here and not in RESUME or ONCREATE because it created a bug
                updateRating(true, true, true);

                String tempBitmap; // geting the bitmap path for transferring to the next activity
                float tempRate; // getting the rate for transferring to the nest activity

                // get the bitmap with the highest rate
                if (sharedPref.getFloat(rbrate1, -1f) >= sharedPref.getFloat(rbrate2, -1f)) {
                    tempRate = sharedPref.getFloat(rbrate1, -1f);
                    tempBitmap = sharedPref.getString(filepath1, "NULL");
                } else {
                    tempRate = sharedPref.getFloat(rbrate2, -1f);
                    tempBitmap = sharedPref.getString(filepath2, "NULL");

                }

                if (tempRate < sharedPref.getFloat(rbrate3, -1f)) {
                    tempRate = sharedPref.getFloat(rbrate3, -1f);
                    tempBitmap = sharedPref.getString(filepath3, "NULL");
                }

                // switch activity ro preview class along with the bitmap path and rate
                Intent intent = new Intent(getApplicationContext(), Preview.class);
                intent.putExtra("rating", tempRate);
                intent.putExtra("bitmap", tempBitmap);
                startActivity(intent);


            }
        });

        // event listener for each of the rating bars,
        // each listener will check if it was not clicked previously, and will get the result on action up
        // also it will lock if there is no bitmap assigned to its imageview
        ratingbar1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isClicked1 && event.getAction() == MotionEvent.ACTION_UP && imageView1.getDrawable() != null) {
                    isClicked1 = true;

                    return false;
                } else {
                    return true;
                }
            }
        });

        // same shit
        ratingbar2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isClicked2 && event.getAction() == MotionEvent.ACTION_UP && imageView2.getDrawable() != null) {
                    isClicked2 = true;

                    return false;
                } else {
                    return true;
                }
            }
        });

        // same shit
        ratingbar3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isClicked3 && event.getAction() == MotionEvent.ACTION_UP && imageView3.getDrawable() != null) {
                    isClicked3 = true;

                    return false;
                } else {
                    return true;
                }
            }
        });
    }

    /*****************************************************
     * \
     * <p/>
     * ON RESUME METHOD
     * <p/>
     * //
     ****************************************************/
    protected void onResume() {
        super.onResume();

        // check if the previous activity was Preview or starting from oncreate,
        // this will help assiging values to text size in shared preferences
        // if the activity was created then just get the saved text size
        // if the activity was resumed then get the new text size values and save them
        // bug : on resume was called before on start
        if (isOnCreate) {
            isOnCreate = false;
        } else {
            SharedPreferences.Editor editor;
            editor = sharedPref.edit();
            editor.putFloat("floatTextSize", textSize);
            editor.apply();
        }

        // update all values
        updateTextSize();
        setBitMaps();
        setRating();

    }

    /*****************************************************
     * \
     * <p/>
     * ON PAUSE METHOD
     * <p/>
     * //
     ****************************************************/
    protected void onPause() {
        super.onPause();
        updateRating(true, true, true);
    }


    /*****************************************************
     * \
     * <p/>
     * ON ACTIVITY RESULT METHOD
     * <p/>
     * //
     ****************************************************/

    // this method will be called after camera has taken a photo
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        SharedPreferences.Editor editor = sharedPref.edit(); // define shared preferences editor to store image bitmaps

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");

            Uri bitmapUri;
            File bitmapPath;

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            bitmapUri = getImageUri(getApplicationContext(), bitmap);
            // CALL THIS METHOD TO GET THE ACTUAL PATH
            bitmapPath = new File(getRealPathFromURI(bitmapUri));

            // The following logic :
            //
            // Check if the first imageview is null, if yes then assign the bitmap
            // if not null then check if the second one is null, if yes then copy the first place to the second place, and assign new value to first place
            // if 1 and 2 are null and 3 isnt, then do same thing as above with 3 places.
            // and save all to shared preferences
            if (sharedPref.getString(filepath1, "NULL").equals("NULL")) {
                editor.putString(filepath1, bitmapPath + "");

                editor.putFloat(rbrate1, 0.0f);
                editor.putBoolean(rbclick1, false);

                editor.apply();


            } else if (!sharedPref.getString(filepath1, "NULL").equals("NULL") && sharedPref.getString(filepath2, "NULL").equals("NULL")) {

                editor.putString(filepath2, sharedPref.getString(filepath1, "NULL"));
                editor.putString(filepath1, bitmapPath + "");

                editor.putFloat(rbrate2, sharedPref.getFloat(rbrate1, 0.0f));
                editor.putBoolean(rbclick2, sharedPref.getBoolean(rbclick1, false));

                editor.putFloat(rbrate1, 0.0f);
                editor.putBoolean(rbclick1, false);


                editor.apply();


            } else {
                editor.putString(filepath3, sharedPref.getString(filepath2, "NULL"));
                editor.putString(filepath2, sharedPref.getString(filepath1, "NULL"));
                editor.putString(filepath1, bitmapPath + "");

                editor.putFloat(rbrate3, sharedPref.getFloat(rbrate2, 0.0f));
                editor.putBoolean(rbclick3, sharedPref.getBoolean(rbclick2, false));

                editor.putFloat(rbrate2, sharedPref.getFloat(rbrate1, 0.0f));
                editor.putBoolean(rbclick2, sharedPref.getBoolean(rbclick1, false));

                editor.putFloat(rbrate1, 0.0f);
                editor.putBoolean(rbclick1, false);


                editor.apply();


            }

            bitmap = null;
        }
    }

    /*****************************************************
     * \
     * <p/>
     * GET IMAGE URI METHOD
     * <p/>
     * //
     ****************************************************/

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /*****************************************************
     * \
     * <p/>
     * GET PATH METHOD
     * <p/>
     * //
     ****************************************************/
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        return cursor.getString(idx);
    }

    /*****************************************************
     * \
     * <p/>
     * SET BITMAPS METHOD
     * <p/>
     * //
     ****************************************************/

    private void setBitMaps() {

        // this method will assign bitmaps to image views in case they are null
        if (!sharedPref.getString(filepath1, "NULL").equals("NULL")) {
            File bitmapFile = new File(sharedPref.getString(filepath1, "NULL")); // convert from string to File to BITMAP
            bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
            imageView1.setImageBitmap(bitmap);

        }
        if (!sharedPref.getString(filepath2, "NULL").equals("NULL")) {
            File bitmapFile = new File(sharedPref.getString(filepath2, "NULL"));
            bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
            imageView2.setImageBitmap(bitmap);


        }
        if (!sharedPref.getString(filepath3, "NULL").equals("NULL")) {
            File bitmapFile = new File(sharedPref.getString(filepath3, "NULL"));
            bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
            imageView3.setImageBitmap(bitmap);

        }
    }

    /*****************************************************
     * \
     * <p/>
     * UPDATE RATING METHOD
     * <p/>
     * //
     ****************************************************/
    //This method will update rating inside shared preferences
    private void updateRating(boolean ratingBar1, boolean ratingBar2, boolean ratingBar3) {

        SharedPreferences.Editor editor = sharedPref.edit();
        if (ratingBar1) {
            editor.putFloat(rbrate1, ratingbar1.getRating());
            if (isClicked1) // the user might take a photo and not rate, so the shared preferences will also save if he rated a bitmap or not
                editor.putBoolean(rbclick1, true);
            editor.apply();
        }
        if (ratingBar2) {
            editor.putFloat(rbrate2, ratingbar2.getRating());
            if (isClicked2)
                editor.putBoolean(rbclick2, true);
            editor.apply();
        }
        if (ratingBar3) {
            editor.putFloat(rbrate3, ratingbar3.getRating());
            if (isClicked3)
                editor.putBoolean(rbclick3, true);
            editor.apply();
        }
    }

    /*****************************************************
     * \
     * <p/>
     * SET RATING METHOD
     * <p/>
     * //
     ****************************************************/
    // This method will set rating to all rating bars
    private void setRating() {
        isClicked1 = sharedPref.getBoolean(rbclick1, false);
        isClicked2 = sharedPref.getBoolean(rbclick2, false);
        isClicked3 = sharedPref.getBoolean(rbclick3, false);

        ratingbar1.setRating(sharedPref.getFloat(rbrate1, 0.0f));
        ratingbar2.setRating(sharedPref.getFloat(rbrate2, 0.0f));
        ratingbar3.setRating(sharedPref.getFloat(rbrate3, 0.0f));

    }

    /*****************************************************
     * \
     * <p/>
     * UPDATE TEXT SIZE
     * <p/>
     * //
     ****************************************************/
    // This method will update text size
    private void updateTextSize() {

        takePic.setTextSize(sharedPref.getFloat("floatTextSize", 20f));
        settings.setTextSize(sharedPref.getFloat("floatTextSize", 20f));
        next.setTextSize(sharedPref.getFloat("floatTextSize", 20f));
    }


}


