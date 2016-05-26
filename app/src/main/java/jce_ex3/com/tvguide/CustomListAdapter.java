package jce_ex3.com.tvguide;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

/**
 * Created by Muhammad on 5/23/2016.
 */
public class CustomListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Show> showItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public CustomListAdapter(Activity activity, List<Show> showItems) {
        this.activity = activity;
        this.showItems = showItems;
    }


    @Override
    public int getCount() {
        return showItems.size();
    }

    @Override
    public Object getItem(int location) {
        return showItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.customlist, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.icon);
        TextView nameTV = (TextView) convertView.findViewById(R.id.nameTV);
        TextView summaryTV = (TextView) convertView.findViewById(R.id.summaryTV);
        TextView moreinfoTV = (TextView) convertView.findViewById(R.id.moreinfoTV);


        // getting show data for the row
        Show show = showItems.get(position);

        // thumbnail image
        if (!show.getThumbnailUrl().equals("null")) {
            //thumbNail.setImageResource(android.R.drawable.ic_menu_report_image);
            thumbNail.setImageUrl(show.getThumbnailUrl(), imageLoader);
        } else {
            thumbNail.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // title
        nameTV.setText(show.getName());

        summaryTV.setText(Html.fromHtml(show.getSummary()));

        if (show.getSeason_num() != null && show.getEpisode_num() != null && show.getAir_time() != null && show.getAir_date() != null) {
            moreinfoTV.setText(show.getAllInfo());
        } else {
            moreinfoTV.setText("");
        }
        return convertView;
    }
}
