package com.example.geoindoor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.geoindoor.models.Agenda;

import java.util.ArrayList;

public class AgendaListAdapter extends ArrayAdapter<Agenda> {

    private static final String TAG = "AgendaListAdapter";
    private final Context mContext;
    int mResource;

    public AgendaListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Agenda> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String date = getItem(position).getDate();
        String time = getItem(position).getTime();

        Agenda agenda = new Agenda(name, date, time);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.textView);
        TextView tvDate = convertView.findViewById(R.id.textView2);
        TextView tvTime = convertView.findViewById(R.id.textView3);

        tvName.setText(name);
        tvDate.setText(date);
        tvTime.setText(time);

        return convertView;


    }
}
