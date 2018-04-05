package com.omorni.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.activity.WithdrawlRequestDetailActivity;
import com.omorni.model.ViewRefundCancelResponse;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by V on 4/4/2017.
 */

public class WithdrawHistoryAdapter extends RecyclerView.Adapter<WithdrawHistoryAdapter.MyViewHolder> {
    private Context context;
    ArrayList<ViewRefundCancelResponse> arrayList;
    SavePref savePref;
    String type;

    public WithdrawHistoryAdapter(Context context, ArrayList<ViewRefundCancelResponse> arrayList, String type) {
        this.context = context;
        this.arrayList = arrayList;
        savePref = new SavePref(context);
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_history_list_row_rtl, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_history_list_row, parent, false);
        }
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ViewRefundCancelResponse viewRefundCancelResponse = arrayList.get(position);
        holder.transac_id.setText(viewRefundCancelResponse.getTransaction_id());
        holder.date.setText(Utils.convertTimeStampDate(Long.parseLong(viewRefundCancelResponse.getCreatedDate())));
        holder.time.setText(Utils.convertTimeStampTime(Long.parseLong(viewRefundCancelResponse.getCreatedDate())));
        holder.amount.setText(viewRefundCancelResponse.getAmount() + " " + context.getResources().getString(R.string.sar));

        if (viewRefundCancelResponse.getStatus().equals("1")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            holder.status.setText(context.getResources().getString(R.string.under_processing));
        } else if (viewRefundCancelResponse.getStatus().equals("4")) {
            holder.status.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            holder.status.setText(context.getResources().getString(R.string.transfer_completed));
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WithdrawlRequestDetailActivity.class);
                intent.putExtra("transaction_id", viewRefundCancelResponse.getTransaction_id());
                Log.e("id", "after " + viewRefundCancelResponse.getTransaction_id());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView transac_id, status, date, time, amount;
        public LinearLayout parent;

        public MyViewHolder(View view) {
            super(view);
            transac_id = (TextView) view.findViewById(R.id.transac_id);
            status = (TextView) view.findViewById(R.id.status);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            amount = (TextView) view.findViewById(R.id.amount);
            parent = (LinearLayout) view.findViewById(R.id.parent);
        }
    }
}
