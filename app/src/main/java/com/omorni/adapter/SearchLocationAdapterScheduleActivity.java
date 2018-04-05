package com.omorni.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.MapRequestNowActivity;
import com.omorni.activity.ScheduleServiceMapActivity;
import com.omorni.utils.SavePref;

import java.util.ArrayList;

/**
 * Created by V on 3/1/2017.
 */

public class SearchLocationAdapterScheduleActivity extends RecyclerView.Adapter<SearchLocationAdapterScheduleActivity.MyViewHolder> {
    private Context context;
    SavePref savePref;
    ArrayList<String[]> resultList;
    boolean is_schedule;

    public SearchLocationAdapterScheduleActivity(Context context, ArrayList<String[]> resultList, boolean is_schedule) {
        this.context = context;
        savePref = new SavePref(context);
        this.resultList = resultList;
        this.is_schedule = is_schedule;
    }


    @Override
    public SearchLocationAdapterScheduleActivity.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_location_row, parent, false);

        return new SearchLocationAdapterScheduleActivity.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchLocationAdapterScheduleActivity.MyViewHolder holder, final int position) {

        holder.search_location.setText(resultList.get(position)[0]);

        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_schedule)
                    ((ScheduleServiceMapActivity) context).SetSelectLocation(resultList.get(position)[0], position);
                else
                    ((MapRequestNowActivity) context).SetSelectLocation(resultList.get(position)[0], position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultList.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent_layout;
        public TextView search_location;

        public MyViewHolder(View view) {
            super(view);
            parent_layout = (LinearLayout) view.findViewById(R.id.parent_layout);
            search_location = (TextView) view.findViewById(R.id.search_location);
        }
    }


}
