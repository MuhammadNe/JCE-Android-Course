package jceex1.example.com.camandrate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;

import java.io.File;

public class Preview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Declaring views
        ImageView imageView = (ImageView) findViewById(R.id.IVpreview);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.RBrating);

        // get the extra data transferred from main activity to here
        Float rate = getIntent().getExtras().getFloat("rating");
        String filePath = getIntent().getExtras().getString("bitmap");

        File bitmapFile = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());

        // set the rating and bitmap
        imageView.setImageBitmap(bitmap);
        ratingBar.setRating(rate);
    }
}
