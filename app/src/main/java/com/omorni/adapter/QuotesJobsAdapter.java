package com.omorni.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.AvailableJobDetail;
import com.omorni.activity.MainActivity;
import com.omorni.fragment.ProvideQuotesFragment;
import com.omorni.model.SellerAvailableJobsResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by V on 3/1/2017.
 */

public class QuotesJobsAdapter extends RecyclerView.Adapter<QuotesJobsAdapter.MyViewHolder> implements Filterable {
    private Context context;
    SavePref savePref;
    ArrayList<SellerAvailableJobsResponse> availableJobsResponseArrayList;
    ArrayList<SellerAvailableJobsResponse> searchList;
    private QuotesJobFilter mFilter;
    ProvideQuotesFragment provideQuotesFragment;

    public QuotesJobsAdapter(Context context, ArrayList<SellerAvailableJobsResponse> availableJobsResponseArrayList, ProvideQuotesFragment provideQuotesFragment) {
        this.context = context;
        savePref = new SavePref(context);
        this.availableJobsResponseArrayList = availableJobsResponseArrayList;
        this.searchList = availableJobsResponseArrayList;
        this.provideQuotesFragment = provideQuotesFragment;
        Log.e("AvailableJobsAdapter", "" + availableJobsResponseArrayList.size());
    }

    @Override
    public QuotesJobsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_jobs_row_rtl, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_jobs_row, parent, false);
        }

        return new QuotesJobsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuotesJobsAdapter.MyViewHolder holder, final int position) {

        if (savePref.getUserType().equals("1")) {
            if (availableJobsResponseArrayList.get(position).getJob_type().equals(context.getResources().getString(R.string.plumber_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.plumber_seller));
            } else if (availableJobsResponseArrayList.get(position).getJob_type().equals(context.getResources().getString(R.string.electrician_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.electrician_seller));
            } else if (availableJobsResponseArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.carpenter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.carpenter_seller));
            } else if (availableJobsResponseArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.ac_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ac_seller));
            } else if (availableJobsResponseArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.satellite_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.satellite_seller));
            } else if (availableJobsResponseArrayList.get(position).getJob_type().toLowerCase().equals(context.getResources().getString(R.string.painter_id))) {
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.painter_seller));
            }
        }

        holder.job_name.setText(availableJobsResponseArrayList.get(position).getRequest_title());
        holder.time.setText(availableJobsResponseArrayList.get(position).getWork_time());
        holder.date.setText(availableJobsResponseArrayList.get(position).getWork_date());
        holder.number.setText(availableJobsResponseArrayList.get(position).getDistance());


        String desc = "";

        byte[] data = Base64.decode(availableJobsResponseArrayList.get(position).getJob_description(), Base64.DEFAULT);
        try {
            desc = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        holder.job_description.setText(desc);


        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AvailableJobDetail.class);
                intent.putExtra("array", availableJobsResponseArrayList);
                intent.putExtra("selected_position", String.valueOf(position));
                intent.putExtra("flag", Utils.flag);
                provideQuotesFragment.startActivityForResult(intent, 203);
                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return availableJobsResponseArrayList.size();

    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new QuotesJobsAdapter.QuotesJobFilter();
        }
        return mFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView job_name, job_description, time, date, number;
        public RelativeLayout parent_layout;
        public CircleImageView user_image;
        public AppCompatRatingBar rating;
        public ImageView favourite, image;

        public MyViewHolder(View view) {
            super(view);
            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            image = (ImageView) view.findViewById(R.id.image);
            job_name = (TextView) view.findViewById(R.id.job_name);
            job_description = (TextView) view.findViewById(R.id.job_description);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            number = (TextView) view.findViewById(R.id.number);
        }
    }

    private class QuotesJobFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final ArrayList<SellerAvailableJobsResponse> list = searchList;
            int count = list.size();
            final ArrayList<SellerAvailableJobsResponse> nlist = new ArrayList<SellerAvailableJobsResponse>(count);

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
            availableJobsResponseArrayList = (ArrayList<SellerAvailableJobsResponse>) results.values;
            notifyDataSetChanged();
        }

    }
}
