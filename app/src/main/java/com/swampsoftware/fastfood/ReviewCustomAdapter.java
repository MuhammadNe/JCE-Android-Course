package com.swampsoftware.fastfood;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Muhammad on 7/15/2016.
 */
public class ReviewCustomAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public ReviewCustomAdapter(Context context, Cursor c) {
        super(context, c);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.reviews_customview, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView TVname = (TextView) view.findViewById(R.id.TVname);
        TextView TVreview = (TextView) view.findViewById(R.id.TVreview);
        TextView TVrate = (TextView) view.findViewById(R.id.TVrate);

        int nameIndex = cursor.getColumnIndex("_name");
        int rateIndex = cursor.getColumnIndex("_rate");
        int reviewIndex = cursor.getColumnIndex("_review");

        String name = cursor.getString(nameIndex);
        String rate = cursor.getString(rateIndex);
        String review = cursor.getString(reviewIndex);

        TVname.setText(name + " :");
        TVrate.setText(rate);
        TVreview.setText(review);

    }
}
