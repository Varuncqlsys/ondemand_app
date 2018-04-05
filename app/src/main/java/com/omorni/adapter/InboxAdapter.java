package com.omorni.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.Util;
import com.omorni.R;
import com.omorni.activity.ChattingActivity;
import com.omorni.activity.MainActivity;
import com.omorni.model.GetChatListResponse;
import com.omorni.model.NotificationResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by V on 3/1/2017.
 */

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MyViewHolder> implements Filterable {
    private Context context;
    private SavePref savePref;
    private ArrayList<GetChatListResponse> arrayList = new ArrayList<>();
    private ArrayList<GetChatListResponse> searchList = new ArrayList<>();
    private InboxFilter mFilter;

    public InboxAdapter(Context context, ArrayList<GetChatListResponse> arrayList) {
        this.context = context;
        savePref = new SavePref(context);
        this.arrayList = arrayList;
        this.searchList = arrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            if (savePref.getUserType().equals("1"))
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_row_seller_rtl, parent, false);
            else
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_row_rtl, parent, false);
        } else {
            if (savePref.getUserType().equals("1"))
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_row_seller, parent, false);
            else
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_row, parent, false);
        }

        return new InboxAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        GetChatListResponse getChatListResponse = arrayList.get(position);
        if (savePref.getUserType().equals("1"))
            Glide.with(context).load(getChatListResponse.getUser_image()).placeholder(R.drawable.user_placeholder_seller).override(150, 150).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);
        else
            Glide.with(context).load(getChatListResponse.getUser_image()).placeholder(R.drawable.user_placeholder).override(150, 150).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);


        holder.username.setText(getChatListResponse.getName());

        if (getChatListResponse.getMessage_type().equals("0"))
            holder.message.setText(getChatListResponse.getMessage());
        else if (getChatListResponse.getMessage_type().equals("1"))
            holder.message.setText(context.getResources().getString(R.string.image));
        else if (getChatListResponse.getMessage_type().equals("2"))
            holder.message.setText(context.getResources().getString(R.string.video));

        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChattingActivity.class);
                intent.putExtra("thread_id", arrayList.get(position).getThread_id());
                intent.putExtra("req_id", arrayList.get(position).getRequest_id());
                intent.putExtra("opponent_id", arrayList.get(position).getUser());
                intent.putExtra("opponent_image", arrayList.get(position).getUser_image());
                intent.putExtra("opponent_name", arrayList.get(position).getName());
                intent.putExtra("job_status", arrayList.get(position).getJob_status());
                context.startActivity(intent);
                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new InboxAdapter.InboxFilter();
        }
        return mFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView user_image;
        public TextView username, message, online_status;
        public ImageView favourite;
        public RelativeLayout parent_layout;

        public MyViewHolder(View view) {
            super(view);
            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            user_image = (CircleImageView) view.findViewById(R.id.user_image);
            username = (TextView) view.findViewById(R.id.username);
            message = (TextView) view.findViewById(R.id.message);
            online_status = (TextView) view.findViewById(R.id.online_status);
            favourite = (ImageView) view.findViewById(R.id.favourite);
        }
    }

    private class InboxFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final ArrayList<GetChatListResponse> list = searchList;
            int count = list.size();
            final ArrayList<GetChatListResponse> nlist = new ArrayList<GetChatListResponse>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
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
            arrayList = (ArrayList<GetChatListResponse>) results.values;
            notifyDataSetChanged();
        }

    }
}
