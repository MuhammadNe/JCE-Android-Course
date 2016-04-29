package ex1.jce.com.jce_ex2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Muhammad on 4/29/2016.
 */
public class CustomCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;

    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c);
        inflater = LayoutInflater.from(context);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.items, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView latTV = (TextView) view.findViewById(R.id.lat);
        TextView lngTV = (TextView) view.findViewById(R.id.lng);
        TextView timeTV = (TextView) view.findViewById(R.id.time);

        latTV.setText(cursor.getString(1));
        lngTV.setText(cursor.getString(2));
        timeTV.setText(cursor.getString(3));
    }
}
