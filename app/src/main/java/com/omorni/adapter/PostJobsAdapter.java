package com.omorni.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.MainActivity;
import com.omorni.activity.PostedJobsDetailActivity;
import com.omorni.fragment.PostJobsFragment;

import com.omorni.model.PostedJobsResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by V on 3/1/2017.
 */

public class PostJobsAdapter extends RecyclerView.Adapter<PostJobsAdapter.MyViewHolder> implements Filterable {
    private Context context;
    SavePref savePref;
    ArrayList<PostedJobsResponse> postJobModelArrayList;
    ArrayList<PostedJobsResponse> searchList;
    private PostJobFilter mFilter;
    PostJobsFragment fragment;

    public PostJobsAdapter(Context context, ArrayList<PostedJobsResponse> postJobModelArrayList, PostJobsFragment fragment) {
        this.context = context;
        savePref = new SavePref(context);
        this.postJobModelArrayList = postJobModelArrayList;
        this.searchList = postJobModelArrayList;
        this.fragment = fragment;
    }

    @Override
    public PostJobsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            if (savePref.getUserType().equals("1")) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_jobs_row_seller_rtl, parent, false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_jobs_row_rtl, parent, false);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_job_row_seller, parent, false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_jobs_row, parent, false);
            }
        }
        return new PostJobsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PostJobsAdapter.MyViewHolder holder, final int position) {

        if (savePref.getUserType().equals("1")) {
            if (postJobModelArrayList.get(position).getJob_type().equals(context.getResources().getString(R.string.plumber_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_seller));
            } else if (postJobModelArrayList.get(position).getJob_type().equals(context.getResources().getString(R.string.electrician_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_seller));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.carpenter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_seller));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.ac_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_seller));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.satellite_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_seller));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.painter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_seller));
            }
        } else {
            if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.plumber_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_buyer));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.electrician_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_buyer));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.carpenter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_buyer));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.ac_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_buyer));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.satellite_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_buyer));
            } else if (postJobModelArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.painter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_buyer));
            }
        }

        // means buyer posted job
        if (postJobModelArrayList.get(position).getJob_status().equals("0")) {
            holder.job_status.setText(context.getResources().getString(R.string.open));
        }// means buyer paid to particular selected seller
        else if (postJobModelArrayList.get(position).getJob_status().equals("1")) {
            holder.job_status.setText(context.getResources().getString(R.string.seller_selected));
        }// means seller provide quotation on posted job
        else if (postJobModelArrayList.get(position).getJob_status().equals("2")) {
            holder.job_status.setText(context.getResources().getString(R.string.in_progress));
        }// means buyer not paid to seller
        else if (postJobModelArrayList.get(position).getJob_status().equals("3")) {
            holder.job_status.setText(context.getResources().getString(R.string.not_paid));
        }// means posted job expire
        else if (postJobModelArrayList.get(position).getJob_status().equals("4")) {
            holder.job_status.setText(context.getResources().getString(R.string.expire));
        }

        holder.job_name.setText(postJobModelArrayList.get(position).getRequest_title());

        String desc = "";

        byte[] data = Base64.decode(postJobModelArrayList.get(position).getJob_description(), Base64.DEFAULT);
        try {
            desc = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        holder.do_tv.setText(desc);
        holder.date.setText(postJobModelArrayList.get(position).getWork_date());
        holder.time.setText(postJobModelArrayList.get(position).getWork_time());

        if (Integer.parseInt(postJobModelArrayList.get(position).getTotal_quotes()) > 1) {
            holder.quote.setText(context.getResources().getString(R.string.quotes));
        } else {
            holder.quote.setText(context.getResources().getString(R.string.quote));
        }


        holder.number.setText(postJobModelArrayList.get(position).getTotal_quotes());
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostedJobsDetailActivity.class);
                intent.putExtra("job_id", postJobModelArrayList.get(position).getId());
                intent.putExtra("position", position);
                intent.putExtra("job_status", postJobModelArrayList.get(position).getJob_status());
                fragment.startActivityForResult(intent, 171);
                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {

        return postJobModelArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new PostJobsAdapter.PostJobFilter();
        }
        return mFilter;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView job_name, do_tv, date, time, number, job_status, quote;
        public RelativeLayout parent_layout;
        public CircleImageView image;
        public AppCompatRatingBar rating;
        public ImageView favourite;

        public MyViewHolder(View view) {
            super(view);
            image = (CircleImageView) view.findViewById(R.id.image);
            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            job_name = (TextView) view.findViewById(R.id.job_name);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            do_tv = (TextView) view.findViewById(R.id.do_tv);
            number = (TextView) view.findViewById(R.id.number);
            job_status = (TextView) view.findViewById(R.id.job_status);
            quote = (TextView) view.findViewById(R.id.quote);
        }
    }

    private class PostJobFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final ArrayList<PostedJobsResponse> list = searchList;
            int count = list.size();
            final ArrayList<PostedJobsResponse> nlist = new ArrayList<PostedJobsResponse>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getRequest_title();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            postJobModelArrayList = (ArrayList<PostedJobsResponse>) results.values;
            notifyDataSetChanged();
        }

    }
}
