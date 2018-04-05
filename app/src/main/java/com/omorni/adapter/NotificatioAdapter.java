package com.omorni.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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
import com.omorni.activity.LoginActivity;
import com.omorni.activity.MainActivity;
import com.omorni.activity.OrderSummeryActivity;
import com.omorni.activity.PostedJobsDetailActivity;
import com.omorni.activity.RequestSummaryActivity;
import com.omorni.activity.RequestSummaryPushActivity;
import com.omorni.activity.SellerCheckoutActivity;
import com.omorni.activity.ShowRating;
import com.omorni.activity.ViewRefundActivity;
import com.omorni.model.NotificationResponse;

import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.stripe.android.model.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by V on 3/1/2017.
 */

public class NotificatioAdapter extends RecyclerView.Adapter<NotificatioAdapter.MyViewHolder> implements Filterable {
    private Context context;
    private SavePref savePref;
    private ArrayList<NotificationResponse> arrayList = new ArrayList<NotificationResponse>();
    private ArrayList<NotificationResponse> searchList = new ArrayList<NotificationResponse>();
    private NotificationFilter mFilter;

    public NotificatioAdapter(Context context, ArrayList<NotificationResponse> arrayList) {
        this.context = context;
        this.savePref = new SavePref(context);
        this.arrayList = arrayList;
        this.searchList = arrayList;
    }

    @Override
    public NotificatioAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row, parent, false);
        return new NotificatioAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificatioAdapter.MyViewHolder holder, int position) {
        if (savePref.getUserType().equals("1"))
            holder.notify_time.setTextColor(context.getResources().getColor(R.color.tab_bg_color));
        else
            holder.notify_time.setTextColor(context.getResources().getColor(R.color.green_text_ntify));

        final NotificationResponse notificationResponse = arrayList.get(position);
        holder.notify_msg.setText(notificationResponse.getNottification_message());

        String created = Utils.convertTimeStampDateTime(Long.parseLong(notificationResponse.getNottification_time()));

        holder.notify_time.setText(created);

        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (notificationResponse.getNotification_type().equals("0"))    // if notification contains only text message to show then open dialog
                    openDialog(notificationResponse.getNottification_message());
                else if (notificationResponse.getNotification_type().equals("1"))   // if seller get notification to accept or reject job
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("3"))    //  if seller aceepeted your request then push accepted by buyer
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("6"))   // if seller provide his quotation
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("8"))   // if seller checkout then buyer will recieve push
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("9"))   // if seller cancel his quotation
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("11"))   // if buyer or seller give rating
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("13"))   // if cancel request is aprooved by admin or amount of cancel request is refund by admin
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
                else if (notificationResponse.getNotification_type().equals("14"))   // if refund request is aprooved by admin or amount of refund request is refund by admin
                    getNotificationData(notificationResponse.getId(), notificationResponse.getNotification_type());
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
            mFilter = new NotificatioAdapter.NotificationFilter();
        }
        return mFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView notify_msg, notify_time;
        public RelativeLayout parent_layout;

        public MyViewHolder(View view) {
            super(view);

            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            notify_msg = (TextView) view.findViewById(R.id.notify_msg);
            notify_time = (TextView) view.findViewById(R.id.notify_time);
        }
    }

    private class NotificationFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final ArrayList<NotificationResponse> list = searchList;
            int count = list.size();
            final ArrayList<NotificationResponse> nlist = new ArrayList<NotificationResponse>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getNottification_message();
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
            arrayList = (ArrayList<NotificationResponse>) results.values;
            notifyDataSetChanged();
        }

    }

    private void openDialog(String message) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.show_message_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        final TextView notify_message = (TextView) dialog.findViewById(R.id.notify_message);
        final TextView ok = (TextView) dialog.findViewById(R.id.ok);

        if (savePref.getUserType().equals("1"))
            ok.setBackground(ContextCompat.getDrawable(context, R.drawable.sort_bgd_seller_drawable));
        else
            ok.setBackground(ContextCompat.getDrawable(context, R.drawable.sort_bg_drawable));

        notify_message.setText(message);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void getNotificationData(String notify_id, final String notification_type) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.NOTIFY_ID, notify_id);
        formBuilder.add(AllOmorniParameters.NOTIFY_TYPE, notification_type);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.NOTIFICATION_DETAIL, formBody) {
            @Override
            public void getValueParse(String result) {
                Log.e("data", "here " + result);
                try {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");

                            if (notification_type.equals("3")) {
                                if (jsonObjectBody.getString("status").equals("0")) {
                                    openDialog(context.getResources().getString(R.string.wait_untill) + " " + jsonObjectBody.getString("first_name") + " " + jsonObjectBody.getString("first_name") + " " + context.getResources().getString(R.string.accept_your_job));
                                } else if (jsonObjectBody.getString("status").equals("1")) {       // if order status is not paid
                                    Intent intent = new Intent(context, RequestSummaryPushActivity.class);
                                    intent.putExtra("start_date", jsonObjectBody.getString("start_date"));
                                    intent.putExtra("start_time", jsonObjectBody.getString("start_time"));
                                    intent.putExtra("is_scheduled", jsonObjectBody.getString("is_scheduled"));
                                    intent.putExtra("req_id", jsonObjectBody.getString("id"));
                                    intent.putExtra("req_type", jsonObjectBody.getString("request_type"));
                                    intent.putExtra("processing_fee", jsonObjectBody.getString("processing_fee"));
                                    intent.putExtra("seller_image", jsonObjectBody.getString("seller_image"));
                                    intent.putExtra("package_name", jsonObjectBody.getString("package_name"));
                                    intent.putExtra("total_rating_user", jsonObjectBody.getString("total_rating_user"));
                                    intent.putExtra("avgrating", jsonObjectBody.getString("avgrating"));
                                    intent.putExtra("total_rating_user", jsonObjectBody.getString("total_rating_user"));
                                    intent.putExtra("category", jsonObjectBody.getString("category"));
                                    intent.putExtra("extra_hours", jsonObjectBody.getString("extra_hours"));
                                    intent.putExtra("main_hours", jsonObjectBody.getString("main_hours"));
                                    intent.putExtra("additional_charges", jsonObjectBody.getString("additional_charges"));
                                    intent.putExtra("seller_id", jsonObjectBody.getString("seller_id"));
                                    intent.putExtra("normal_charges", jsonObjectBody.getString("normal_charges"));
                                    intent.putExtra("vat_tax", jsonObjectBody.getString("vat_tax"));
                                    JSONArray jsonArray = new JSONArray(jsonObjectBody.getString("language"));
                                    String language1 = "";
                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                            language1 += jsonObject1.getString("language") + ",";
                                        }
                                        language1 = language1.substring(0, language1.length() - 1);
                                    } else {
                                        language1 = language1;
                                    }

                                    intent.putExtra("language", language1);
                                    intent.putExtra("job_description", jsonObjectBody.getString("job_description"));
                                    intent.putExtra("seller_name", jsonObjectBody.getString("first_name") + " " + jsonObjectBody.getString("last_name"));

                                    if (jsonObjectBody.getString("is_scheduled").equals("0"))
                                        intent.putExtra("service_now", true);
                                    else
                                        intent.putExtra("service_now", false);

                                    context.startActivity(intent);
                                } else if (jsonObjectBody.getString("status").equals("2")) {
                                    openDialog(jsonObjectBody.getString("first_name") + " " + context.getResources().getString(R.string.already_rejected_job));

                                } else if (jsonObjectBody.getString("status").equals("3") || jsonObjectBody.getString("status").equals("4") || jsonObjectBody.getString("status").equals("5") || jsonObjectBody.getString("status").equals("6") || jsonObjectBody.getString("status").equals("7")) {
                                    Intent intent = new Intent(context, OrderSummeryActivity.class);
                                    intent.putExtra("from_my_order", true);
                                    intent.putExtra("req_id", jsonObjectBody.getString("id"));
                                    context.startActivity(intent);
                                } else if (jsonObjectBody.getString("status").equals("8")) {
                                    openDialog(context.getResources().getString(R.string.you_cancel_job));
                                }
                            } else if (notification_type.equals("6")) {
                                Intent intent = new Intent(context, PostedJobsDetailActivity.class);
                                intent.putExtra("job_id", jsonObjectBody.getString("id"));
                                intent.putExtra("job_status", jsonObjectBody.getString("job_status"));
                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else if (notification_type.equals("8")) {
                                Intent intent = new Intent(context, SellerCheckoutActivity.class);
                                intent.putExtra("req_id", jsonObjectBody.getString("id"));
                                intent.putExtra("seller_name", jsonObjectBody.getString("first_name") + " " + jsonObjectBody.getString("last_name"));
                                intent.putExtra("seller_id", jsonObjectBody.getString("seller_id"));
                                intent.putExtra("created_date", jsonObjectBody.getString("created_date"));
                                intent.putExtra("checkin_time", jsonObjectBody.getString("checkin_time"));
                                intent.putExtra("start_date", jsonObjectBody.getString("start_date"));
                                intent.putExtra("is_scheduled", jsonObjectBody.getString("is_scheduled"));
                                intent.putExtra("checkout_time", jsonObjectBody.getString("checkout_time"));
                                intent.putExtra("order_id", jsonObjectBody.getString("order_id"));
                                intent.putExtra("extra_hours", jsonObjectBody.optString("extra_hours"));
                                intent.putExtra("normal_charges", jsonObjectBody.getString("normal_charges"));
                                intent.putExtra("package_name", jsonObjectBody.getString("package_name"));
                                intent.putExtra("additional_charges", jsonObjectBody.getString("additional_charges"));
                                intent.putExtra("main_hours", jsonObjectBody.getString("main_hours"));
                                intent.putExtra("request_type",  jsonObjectBody.getString("request_type"));
                                intent.putExtra("post_title",  jsonObjectBody.getString("post_title"));

                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

                            } else if (notification_type.equals("9")) {
                                Intent intent = new Intent(context, PostedJobsDetailActivity.class);
                                intent.putExtra("job_id", jsonObjectBody.getString("id"));
                                intent.putExtra("job_status", jsonObjectBody.getString("job_status"));
                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else if (notification_type.equals("11")) {
                                Intent intent = new Intent(context, ShowRating.class);
                                intent.putExtra("created_date", jsonObjectBody.getString("created_date"));
                                intent.putExtra("start_date", jsonObjectBody.getString("start_date"));
                                intent.putExtra("order_id", jsonObjectBody.getString("order_id"));
                                intent.putExtra("rating", jsonObjectBody.getString("rating"));
                                intent.putExtra("comment", jsonObjectBody.getString("comment"));
                                intent.putExtra("first_name", jsonObjectBody.getString("first_name"));
                                intent.putExtra("last_name", jsonObjectBody.getString("last_name"));
                                intent.putExtra("user_image", jsonObjectBody.getString("user_image"));
                                intent.putExtra("is_scheduled", jsonObjectBody.getString("is_scheduled"));
                                intent.putExtra("review_date", jsonObjectBody.getString("review_date"));
                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else if (notification_type.equals("13")) {
                                Intent intent = new Intent(context, ViewRefundActivity.class);
                                intent.putExtra("type", "3");
                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else if (notification_type.equals("14")) {
                                Intent intent = new Intent(context, ViewRefundActivity.class);
                                intent.putExtra("type", "2");
                                context.startActivity(intent);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            }

                        } else {
                            if (status.getString("auth_token").equals("0")) {
                                savePref.clearPreferences();
                                Intent in = new Intent(context, LoginActivity.class);
                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(in);
                                ((MainActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            } else {
                                if (notification_type.equals("3")) {    // if time to pay for order has been exceeded
                                    openDialog(context.getResources().getString(R.string.time_exceeded));
                                } else if (notification_type.equals("6") || notification_type.equals("8") || notification_type.equals("9")
                                        || notification_type.equals("11") || notification_type.equals("13") || notification_type.equals("14")) {
                                    openDialog(status.getString("message"));
                                }
                            }
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(context, context.getResources().getString(R.string.internet_error));
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }
}
