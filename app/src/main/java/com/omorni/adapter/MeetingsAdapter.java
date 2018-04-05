package com.omorni.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.omorni.R;
import com.omorni.activity.AddEventActivity;
import com.omorni.activity.CalendarActivity;
import com.omorni.activity.OrderSummeryActivity;
import com.omorni.activity.OrderSummerySellerActivity;
import com.omorni.fragment.CalenderFragment;
import com.omorni.model.EventResponse;
import com.omorni.model.MeetingResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by V on 3/1/2017.
 */

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.MyViewHolder> {
    private Context context;
    private SavePref savePref;
    private ArrayList<EventResponse> arrayList;
    CalenderFragment calenderFragment;
    private boolean from_fragment = false;
    CalendarActivity activity;

    public MeetingsAdapter(Context context, ArrayList<EventResponse> arrayList, CalenderFragment calenderFragment, boolean from_fragment) {
        this.context = context;
        savePref = new SavePref(context);
        this.arrayList = arrayList;
        this.calenderFragment = calenderFragment;
        this.from_fragment = from_fragment;
    }

    public MeetingsAdapter(Context context, ArrayList<EventResponse> arrayList, CalendarActivity activity, boolean from_fragment) {
        this.context = context;
        savePref = new SavePref(context);
        this.arrayList = arrayList;
        this.activity = activity;
        this.from_fragment = from_fragment;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context))
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.calender_row_rtl, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.calender_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final EventResponse eventResponse = arrayList.get(position);

        if (eventResponse.getEven_type().equals("0")) {
            holder.swipe_layout.setLockDrag(false);
            holder.textView_meeting.setText(eventResponse.getEvent_name());
            holder.parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorViewSeller));
            holder.event_type.setText(context.getResources().getString(R.string.event));
            holder.event_price.setVisibility(View.GONE);
        } else {
            holder.swipe_layout.setLockDrag(true);
            //holder.textView_meeting.setText("Meeting With " + eventResponse.getName());
            holder.parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGrayMenu));

            holder.event_price.setVisibility(View.VISIBLE);
            if (eventResponse.getRequest_type().equals("1")) {
                holder.event_price.setText(context.getResources().getString(R.string.sar) + " " + eventResponse.getPrice());
                holder.event_type.setText(context.getResources().getString(R.string.post_job));
            } else {
                holder.event_price.setText(context.getResources().getString(R.string.sar) + " " + eventResponse.getNormal_charges());
                holder.event_type.setText(context.getResources().getString(R.string.scheduled));
            }
        }

        if (!eventResponse.getEnd_time().equalsIgnoreCase("")) {
            holder.textview_job_time.setText(eventResponse.getStart_time() + " - " + eventResponse.getEnd_time());
        } else {
            holder.textview_job_time.setText(eventResponse.getStart_time());
        }
        holder.textView_location.setText(eventResponse.getEvent_location());


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(position, eventResponse);
            }
        });
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (eventResponse.getEven_type().equals("0")) {
                    Intent intent = new Intent(context, AddEventActivity.class);
                    intent.putExtra("event_response", eventResponse);
                    intent.putExtra("update", true);
                    if (from_fragment)
                        calenderFragment.startActivityForResult(intent, 100);
                    else
                        activity.startActivityForResult(intent, 100);
                } else {

                    Intent intent = null;
                    if (savePref.getUserType().equals("1"))
                        intent = new Intent(context, OrderSummerySellerActivity.class);
                    else
                        intent = new Intent(context, OrderSummeryActivity.class);
                    intent.putExtra("req_id", eventResponse.getId());
                    intent.putExtra("from_my_order", true);
                    context.startActivity(intent);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_meeting, textview_job_time, textView_location, event_type, event_price;
        public RelativeLayout parent_layout;
        public SwipeRevealLayout swipe_layout;
        public ImageView delete;

        public MyViewHolder(View view) {
            super(view);
            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            textView_meeting = (TextView) view.findViewById(R.id.textView_meeting);
            textview_job_time = (TextView) view.findViewById(R.id.textview_job_time);
            textView_location = (TextView) view.findViewById(R.id.textView_location);
            event_type = (TextView) view.findViewById(R.id.event_type);
            event_price = (TextView) view.findViewById(R.id.event_price);
            delete = (ImageView) view.findViewById(R.id.delete);
            swipe_layout = (SwipeRevealLayout) view.findViewById(R.id.swipe_layout);
        }
    }

    public void showDeleteDialog(final int pos, final EventResponse eventResponse) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.want_delete))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteEventAPI(pos, eventResponse);
                            }
                        });
        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        android.support.v7.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void deleteEventAPI(final int pos, final EventResponse eventResponse) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.EVENT_ID, eventResponse.getId());

        RequestBody formBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.DELETE_EVENT_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();

                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObj = new JSONObject(result);
                        JSONObject status = jsonObj.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            if (from_fragment)
                                calenderFragment.removeItem(pos);
                            else
                                activity.removeItem(pos);
//                            arrayList.remove(pos);
//                            notifyDataSetChanged();
                        }
                        Utils.showToast(context, status.getString("message"));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }
}
