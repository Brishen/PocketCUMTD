package com.rmathur.cumtd.ui.adapters.list;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rmathur.cumtd.R;
import com.rmathur.cumtd.data.StopDataSource;
import com.rmathur.cumtd.data.model.Stop;

import java.util.ArrayList;

public class FavoritesAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<Stop> list = new ArrayList<Stop>();
    private Context context;

    public FavoritesAdapter(ArrayList<Stop> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_favorites, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView) view.findViewById(R.id.list_item_string);
        listItemText.setText(list.get(position).getStopName());
        listItemText.setTextColor(Color.BLACK);

        //Handle buttons and add onClickListeners
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_btn);
        Drawable d = context.getResources().getDrawable(android.R.drawable.ic_delete);
        ImageView image = (ImageView) view.findViewById(R.id.delete_btn);
        image.setImageDrawable(d);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopDataSource dataSource = new StopDataSource(context);
                dataSource.open();
                dataSource.deleteStop(list.get(position));
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }
}