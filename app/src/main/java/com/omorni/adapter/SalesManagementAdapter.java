package com.omorni.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;

import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.ArrayList;


/**
 * Created by V on 4/4/2017.
 */

public class SalesManagementAdapter extends RecyclerView.Adapter<SalesManagementAdapter.MyViewHolder> {
    private Context context;
    ArrayList<String[]> arrayList;
    SavePref savePref;

    public SalesManagementAdapter(Context context, ArrayList<String[]> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        savePref = new SavePref(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_management_list_row_rtl, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_management_list_row, parent, false);
        }

        return new MyViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.userName.setText(arrayList.get(position)[0] + " " + arrayList.get(position)[1]);

        holder.salesDate.setText(Utils.convertTimeStampDate(Long.parseLong(arrayList.get(position)[7])));
        holder.time_sales.setText(Utils.convertTimeStampTime(Long.parseLong(arrayList.get(position)[7])));

        if (arrayList.get(position)[8].equals("0")) {
            holder.salesAmount.setText(arrayList.get(position)[4]);
            if (arrayList.get(position)[6].equals("0")) {
                holder.job_type.setText(context.getResources().getString(R.string.request_now));
            } else {
                holder.job_type.setText(context.getResources().getString(R.string.scheduled));
            }
        } else {
            holder.salesAmount.setText(arrayList.get(position)[9]);
            holder.job_type.setText(context.getResources().getString(R.string.post_job));
        }
        Glide.with(context).load(arrayList.get(position)[2]).override(150, 150).centerCrop().placeholder(R.drawable.user_placeholder_seller).dontAnimate().diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.user_image);
//        holder.rating.setRating(Float.parseFloat(arrayList.get(position)[3]));
        if (Integer.parseInt(arrayList.get(position)[3]) > 1) {
            holder.rating_text.setText(context.getResources().getString(R.string.gives_you) + " " + arrayList.get(position)[3] + " " + context.getResources().getString(R.string.stars));
        } else if (Integer.parseInt(arrayList.get(position)[3]) == 1) {
            holder.rating_text.setText(context.getResources().getString(R.string.gives_you) + " " + arrayList.get(position)[3] + " " + context.getResources().getString(R.string.star));
        } else {
            holder.rating_text.setText(context.getResources().getString(R.string.not_give_rating));
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public de.hdodenhof.circleimageview.CircleImageView user_image;
        public TextView userName, salesDate, salesAmount, job_type, time_sales, rating_text;
        public android.support.v7.widget.AppCompatRatingBar rating;


        public MyViewHolder(View view) {
            super(view);
            user_image = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.seller_image);
            userName = (TextView) view.findViewById(R.id.name);
            salesDate = (TextView) view.findViewById(R.id.date_sales);
            salesAmount = (TextView) view.findViewById(R.id.sales_amount);
            job_type = (TextView) view.findViewById(R.id.job_type);
            time_sales = (TextView) view.findViewById(R.id.time_sales);
            rating_text = (TextView) view.findViewById(R.id.rating_text);
            rating = (android.support.v7.widget.AppCompatRatingBar) view.findViewById(R.id.rating);

        }
    }
}
