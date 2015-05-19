package com.rmathur.cumtd.ui.adapters.list;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.model.Departure;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ResultsAdapter extends BaseAdapter {

    private List<Departure> departures;
    private LayoutInflater inflater;
    private Context context;

    public ResultsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        departures = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        if (departures != null) {
            return departures.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return departures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_result, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Departure departure = departures.get(position);
        String busName = departure.getBusName();
        if (busName.equals("120E Teal Orchard Downs"))
            busName = "120E Teal";
        else if (busName.equals("100S Yellow First & Greg"))
            busName = "100S Yellow";
        else if (busName.equals("1S YellowHOPPER Gerty"))
            busName = "1S YellowHOPPER";
        else if (busName.equals("1S YellowHOPPER E-14"))
            busName = "1S YellowHOPPER";
        else if (busName.equals("100S Yellow E14"))
            busName = "100S Yellow";
        else if (busName.equals("12E Teal Orchard Downs"))
            busName = "12E Teal";
        else if (busName.equals("12E Teal PAR"))
            busName = "12E Teal";

        holder.title.setText(busName);
        String minsLeft = departure.getMinsLeft();
        if (minsLeft.equals("0"))
            minsLeft = "DUE";
        else if (minsLeft.equals("1"))
            minsLeft += " minute";
        else
            minsLeft += " minutes";
        holder.description.setText(minsLeft);
        String long_name = departure.getLongName();
        long_name = long_name.substring(long_name.indexOf("-") + 2);
        if (long_name.toLowerCase().equals("arkland college"))
            long_name = "PARKLAND COLLEGE";
        holder.time.setText(long_name);
        holder.sideBar.setBackgroundColor(Color.parseColor("#" + departure.getColor()));
        Picasso.with(context)
                .load(R.drawable.bus)
                .into(holder.icon);
        return convertView;
    }

    public void setData(List<Departure> newDepartures) {
        this.departures = newDepartures;
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @InjectView(R.id.alert_item_side_bar)
        View sideBar;

        @InjectView(R.id.alert_description)
        TextView description;

        @InjectView(R.id.alert_image)
        ImageView icon;

        @InjectView(R.id.alert_time)
        TextView time;

        @InjectView(R.id.alert_title)
        TextView title;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
