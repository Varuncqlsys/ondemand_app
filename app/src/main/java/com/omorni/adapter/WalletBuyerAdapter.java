package com.omorni.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.activity.MainActivity;
import com.omorni.activity.MyWalletBuyerActivity;
import com.omorni.model.MyWalletBalanceResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by V on 4/4/2017.
 */

public class WalletBuyerAdapter extends RecyclerView.Adapter<WalletBuyerAdapter.MyViewHolder> {
    private Context context;
    ArrayList<MyWalletBalanceResponse> arrayList;
    SavePref savePref;
    Date current_date;
    String refund_option = "0";

    public WalletBuyerAdapter(Context context, ArrayList<MyWalletBalanceResponse> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        savePref = new SavePref(context);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        String formattedDate = formatter.format(c.getTime());
        try {
            current_date = (Date) formatter.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(Utils.isConfigRtl(context)){
            if (savePref.getUserType().equals("1"))
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_seller_list_row_rtl, parent, false);
            else
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_buyer_list_row_rtl, parent, false);
        }else{
            if (savePref.getUserType().equals("1"))
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_seller_list_row, parent, false);
            else
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_buyer_list_row, parent, false);
        }

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final MyWalletBalanceResponse myWalletBalanceResponse = arrayList.get(position);
        holder.username.setText(myWalletBalanceResponse.getFirst_name() + " " + myWalletBalanceResponse.getLast_name());
        holder.rating.setRating(Float.parseFloat(myWalletBalanceResponse.getRating()));
        holder.price.setText(myWalletBalanceResponse.getTotal_amounts() + " " + context.getResources().getString(R.string.sar));

        if (savePref.getUserType().equals("1")) {
            Glide.with(context).load(myWalletBalanceResponse.getUser_image())
                    .override(150, 150).centerCrop()
                    .placeholder(R.drawable.user_placeholder_seller)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);
        } else {
            Glide.with(context).load(myWalletBalanceResponse.getUser_image())
                    .override(150, 150).centerCrop()
                    .placeholder(R.drawable.user_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);
        }

        if (myWalletBalanceResponse.getIs_scheduled().equals("0")) {
            String start_date = Utils.convertTimeStampDate(Long.parseLong(myWalletBalanceResponse.getCreated_date()));
            holder.service_type.setText(context.getResources().getString(R.string.request_service));
            holder.date.setText(start_date);
            holder.time.setText(Utils.convertTimeStampTime(Long.parseLong(myWalletBalanceResponse.getCreated_date())));
        } else {
            holder.service_type.setText(context.getResources().getString(R.string.schedule_service));
            holder.date.setText(myWalletBalanceResponse.getStart_date());
            holder.time.setText(myWalletBalanceResponse.getStart_time());
        }

        if (myWalletBalanceResponse.getStatus().equals("0")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.pending));
        } else if (myWalletBalanceResponse.getStatus().equals("1")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.not_paid));
        } else if (myWalletBalanceResponse.getStatus().equals("2")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.decline));
        } else if (myWalletBalanceResponse.getStatus().equals("3")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.progress));
        } else if (myWalletBalanceResponse.getStatus().equals("4")) {
//            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.complete));
            if (myWalletBalanceResponse.getIs_sent().equals("1")) {
                disableRefundCancel(holder);
            } else {
                checkDateValidation(holder, myWalletBalanceResponse);
            }
            holder.cancel.setVisibility(View.GONE);
            holder.cancel_disable.setVisibility(View.VISIBLE);
        } else if (myWalletBalanceResponse.getStatus().equals("5")) {

           /* if (myWalletBalanceResponse.getWithdraw_status().equals("0")) {
                holder.cancel.setVisibility(View.VISIBLE);
                holder.cancel_disable.setVisibility(View.GONE);
            } else {
                holder.cancel.setVisibility(View.GONE);
                holder.cancel_disable.setVisibility(View.VISIBLE);
            }*/
            holder.status.setText(context.getResources().getString(R.string.paid));
            // if buyer already made request for refund or cancel job
            if (myWalletBalanceResponse.getIs_sent().equals("1")) {
                disableRefundCancel(holder);
            } else {
                checkDateValidation(holder, myWalletBalanceResponse);
            }
        } else if (myWalletBalanceResponse.getStatus().equals("6")) {
            /*if (myWalletBalanceResponse.getWithdraw_status().equals("0")) {
                holder.cancel.setVisibility(View.VISIBLE);
                holder.cancel_disable.setVisibility(View.GONE);
            } else {
                holder.cancel.setVisibility(View.GONE);
                holder.cancel_disable.setVisibility(View.VISIBLE);
            }*/
            holder.status.setText(context.getResources().getString(R.string.en_route_status));
            // if buyer already made request for refund or cancel job
            if (myWalletBalanceResponse.getIs_sent().equals("1")) {
                disableRefundCancel(holder);
            } else {
                checkDateValidation(holder, myWalletBalanceResponse);
            }

        } else if (myWalletBalanceResponse.getStatus().equals("7")) {
//            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.finished));
            if (myWalletBalanceResponse.getIs_sent().equals("1")) {
                disableRefundCancel(holder);
            } else {
                checkDateValidation(holder, myWalletBalanceResponse);
            }
            holder.cancel.setVisibility(View.GONE);
            holder.cancel_disable.setVisibility(View.VISIBLE);
        } else if (myWalletBalanceResponse.getStatus().equals("8")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.canceled));
        } else if (myWalletBalanceResponse.getStatus().equals("9")) {
            disableRefundCancel(holder);
            holder.status.setText(context.getResources().getString(R.string.refunded));
        } else if (myWalletBalanceResponse.getStatus().equals("10")) {
            holder.refund.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.VISIBLE);
            holder.refund_disable.setVisibility(View.VISIBLE);
            holder.cancel_disable.setVisibility(View.GONE);

            holder.status.setText(context.getResources().getString(R.string.seller_canceled));
        } else if (myWalletBalanceResponse.getStatus().equals("11")) {
            holder.refund.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.GONE);
            holder.refund_disable.setVisibility(View.VISIBLE);
            holder.cancel_disable.setVisibility(View.VISIBLE);
            holder.status.setText(context.getResources().getString(R.string.on_hold));
        } else if (myWalletBalanceResponse.getStatus().equals("12")) {
            holder.refund.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.GONE);
            holder.refund_disable.setVisibility(View.VISIBLE);
            holder.cancel_disable.setVisibility(View.VISIBLE);

            holder.status.setText(context.getResources().getString(R.string.refund_initiated));
        }


        holder.refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyWalletBuyerActivity.is_bank_account_added)
                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                else
                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));

//                checkRefundValidation(holder, myWalletBalanceResponse, position);
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyWalletBuyerActivity.is_bank_account_added)
                    openCancelDialog("2", myWalletBalanceResponse.getRequest_id(), position);
                else
                    Utils.showToast(context, context.getResources().getString(R.string.add_details_cancel));

            }
        });
    }


    private void disableRefundCancel(MyViewHolder holder) {
        holder.refund.setVisibility(View.GONE);
        holder.cancel.setVisibility(View.GONE);
        holder.refund_disable.setVisibility(View.VISIBLE);
        holder.cancel_disable.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView user_image;
        public TextView username, service_type, status, sales, date, time, refund, cancel, price, refund_disable, cancel_disable;
        public AppCompatRatingBar rating;


        public MyViewHolder(View view) {
            super(view);
            user_image = (CircleImageView) view.findViewById(R.id.user_image);
            username = (TextView) view.findViewById(R.id.username);
            service_type = (TextView) view.findViewById(R.id.service_type);
            status = (TextView) view.findViewById(R.id.status);
            sales = (TextView) view.findViewById(R.id.sales);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            refund = (TextView) view.findViewById(R.id.refund);
            cancel = (TextView) view.findViewById(R.id.cancel);
            price = (TextView) view.findViewById(R.id.price);
            refund_disable = (TextView) view.findViewById(R.id.refund_disable);
            cancel_disable = (TextView) view.findViewById(R.id.cancel_disable);
            rating = (AppCompatRatingBar) view.findViewById(R.id.rating);
        }
    }

    private void openRefundDialog(final String type, final String req_id, final int position) {

        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        if (savePref.getUserType().equals("1")) {
            dialog.setContentView(R.layout.order_refund_dialog_seller);
        } else {
            dialog.setContentView(R.layout.order_refund_dialog);
        }
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        final ImageView first_option = (ImageView) dialog.findViewById(R.id.first_option);
        final ImageView second_option = (ImageView) dialog.findViewById(R.id.second_option);
        final ImageView third_option = (ImageView) dialog.findViewById(R.id.third_option);
        final TextView refund = (TextView) dialog.findViewById(R.id.refund);
        final EditText edit_other = (EditText) dialog.findViewById(R.id.edit_other);
        final ImageView cross = (ImageView) dialog.findViewById(R.id.cross);

        if (savePref.getUserType().equals("1")) {
            first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
            second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
            third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
        } else {
            first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
            second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
            third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
        }

        first_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "1";
                edit_other.setVisibility(View.GONE);
                if (savePref.getUserType().equals("1")) {
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                } else {
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected));
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                }
            }
        });

        second_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "2";
                edit_other.setVisibility(View.GONE);
                if (savePref.getUserType().equals("1")) {
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                } else {
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected));
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                }
            }
        });

        third_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refund_option = "3";
                edit_other.setVisibility(View.VISIBLE);
                if (savePref.getUserType().equals("1")) {
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected_seller));
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select_seller));
                } else {
                    third_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_selected));
                    second_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                    first_option.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.radio_un_select));
                }
            }
        });

        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (refund_option.equals("0")) {
                    Utils.showToast(context, context.getResources().getString(R.string.select_option));
                } else if (refund_option.equals("3")) {
                    if (edit_other.getText().toString().length() == 0) {
                        Utils.showToast(context, context.getResources().getString(R.string.mention_reason));
                    } else {
                        // hit api
                        refundCancelRequestApi(type, req_id, position, edit_other.getText().toString());
                    }
                } else {
                    String message = "";
                    // hit api
                    if (refund_option.equals("1"))
                        message = context.getResources().getString(R.string.seller_not_show);
                    else if (refund_option.equals("2"))
                        message = context.getResources().getString(R.string.not_satisfied);

                    refundCancelRequestApi(type, req_id, position, message);
                    dialog.dismiss();
                }
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }


    private void openCancelDialog(final String type, final String req_id, final int position) {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        if (savePref.getUserType().equals("1")) {
            dialog.setContentView(R.layout.order_cancel_dialog_seller);
        } else {
            dialog.setContentView(R.layout.order_cancel_dialog);
        }
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        TextView confirm = (TextView) dialog.findViewById(R.id.confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refundCancelRequestApi(type, req_id, position, "");
                dialog.dismiss();
            }
        });
    }

    private void checkRefundValidation(MyViewHolder holder, MyWalletBalanceResponse myWalletBalanceResponse, final int position) {
        if (myWalletBalanceResponse.getWithdraw_status().equals("0")) {
            // means request now or scheduled service
            if (myWalletBalanceResponse.getRequest_type().equals("0")) {
                if (myWalletBalanceResponse.getIs_scheduled().equals("0")) {
                    String package_id = "";
                    String start_date = Utils.convertTimeStampDate(Long.parseLong(myWalletBalanceResponse.getCreated_date()));
                    String start_time = Utils.convertTimeStampTime(Long.parseLong(myWalletBalanceResponse.getCreated_date()));


                    start_time = start_time.replace(":", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date job_start_date = null;
                    try {
                        job_start_date = sdf.parse(start_date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    String minutes = String.valueOf(c.get(Calendar.MINUTE));
                    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }
                    if (hour.length() == 1) {
                        hour = "0" + hour;
                    }
                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);
                    // means basic package
                    if (myWalletBalanceResponse.getPackage_id().equals("1")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 1 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }// means standard package
                    else if (myWalletBalanceResponse.getPackage_id().equals("2")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 2 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }


                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));

                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }// means premium package
                    else if (myWalletBalanceResponse.getPackage_id().equals("3")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 3 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }

                }// if scheduled service
                else {
//                String start_date = Utils.convertTimeStampDate(Long.parseLong(myWalletBalanceResponse.getStart_date()));
//                String start_time = Utils.convertTimeStampTime(Long.parseLong(myWalletBalanceResponse.getStart_time()));
                    String start_time = "";
                    start_time = myWalletBalanceResponse.getStart_time().replace(":", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date job_start_date = null;
                    try {
                        job_start_date = sdf.parse(myWalletBalanceResponse.getStart_date());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    String minutes = String.valueOf(c.get(Calendar.MINUTE));
                    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }
                    if (hour.length() == 1) {
                        hour = "0" + hour;
                    }
                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);


                    // if basic package
                    if (myWalletBalanceResponse.getPackage_id().equals("1")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 1 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {

                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            //Convert long to String
                            String dayDifference = Long.toString(differenceDates);

                            Log.e("dayDifference", "date " + dayDifference);
                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));

                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }// if package is standard
                    else if (myWalletBalanceResponse.getPackage_id().equals("2")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 2 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            //Convert long to String
                            String dayDifference = Long.toString(differenceDates);

                            Log.e("dayDifference", "date " + dayDifference);
                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }// if package is premium
                    else if (myWalletBalanceResponse.getPackage_id().equals("3")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 3 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            } else {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            //Convert long to String
                            String dayDifference = Long.toString(differenceDates);

                            Log.e("dayDifference", "date " + dayDifference);
                            if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                            } else {
                                if (MyWalletBuyerActivity.is_bank_account_added)
                                    openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                                else
                                    Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                            }

                        } else if (current_date.compareTo(job_start_date) > 1) {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        } else {
                            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                        }
                    }
                }
            }// means posted job
            else {
                String start_time = "";
                start_time = myWalletBalanceResponse.getStart_time().replace(":", "");
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date job_start_date = null;
                try {
                    job_start_date = sdf.parse(myWalletBalanceResponse.getStart_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);
                String minutes = String.valueOf(c.get(Calendar.MINUTE));
                String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                if (minutes.length() == 1) {
                    minutes = "0" + minutes;
                }
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }
                String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                if (current_date.compareTo(job_start_date) == 0) {
                    // if remaining time is more than 3 hours buyer can refund the job request
                    if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                        if (MyWalletBuyerActivity.is_bank_account_added)
                            openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                        else
                            Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                    } else {
                        Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                    }

                } else if (current_date.compareTo(job_start_date) == 1) {
                    Log.e("current", "date " + current_date);
                    if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                        Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                    } else {
                        if (MyWalletBuyerActivity.is_bank_account_added)
                            openRefundDialog("1", myWalletBalanceResponse.getRequest_id(), position);
                        else
                            Utils.showToast(context, context.getResources().getString(R.string.add_details_refund));
                    }

                } else if (current_date.compareTo(job_start_date) > 1) {
                    Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));

                } else {
                    Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
                }
            }

        } else {
            Utils.showToast(context, context.getResources().getString(R.string.soryy_no_action));
        }
    }

    private void checkDateValidation(MyViewHolder holder, MyWalletBalanceResponse myWalletBalanceResponse) {
        if (myWalletBalanceResponse.getWithdraw_status().equals("0")) {
            // means request now or scheduled service
            if (myWalletBalanceResponse.getRequest_type().equals("0")) {
                if (myWalletBalanceResponse.getIs_scheduled().equals("0")) {

                    String package_id = "";
                    String start_date = Utils.convertTimeStampDate(Long.parseLong(myWalletBalanceResponse.getCreated_date()));
                    String start_time = Utils.convertTimeStampTime(Long.parseLong(myWalletBalanceResponse.getCreated_date()));


                    start_time = start_time.replace(":", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date job_start_date = null;
                    try {
                        job_start_date = sdf.parse(start_date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    String minutes = String.valueOf(c.get(Calendar.MINUTE));
                    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }
                    if (hour.length() == 1) {
                        hour = "0" + hour;
                    }

                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);
                    // means basic package
                    if (myWalletBalanceResponse.getPackage_id().equals("1")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 1 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // if remaining time is less than 5 minute or we can say enable only for 5 min
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) < 6) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {

                                /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                                /*if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 100) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }
                        } /*else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        }*/ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        }
                    }// means standard package
                    else if (myWalletBalanceResponse.getPackage_id().equals("2")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 2 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // if remaining time is less than 5 minute or we can say enable only for 5 min
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) < 6) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {
                                /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                                /*if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 200) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }


                        } /*else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        } */ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        }
                    }// means premium package
                    else if (myWalletBalanceResponse.getPackage_id().equals("3")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 3 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // if remaining time is less than 5 minute or we can say enable only for 5 min
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) < 6) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }
                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {
                                /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                               /* if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 300) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }

                        }/* else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        } */ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        }
                    }

                }// if scheduled service
                else {
//                String start_date = Utils.convertTimeStampDate(Long.parseLong(myWalletBalanceResponse.getStart_date()));
//                String start_time = Utils.convertTimeStampTime(Long.parseLong(myWalletBalanceResponse.getStart_time()));
                    String start_time = "";
                    start_time = myWalletBalanceResponse.getStart_time().replace(":", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date job_start_date = null;
                    try {
                        job_start_date = sdf.parse(myWalletBalanceResponse.getStart_date());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    String minutes = String.valueOf(c.get(Calendar.MINUTE));
                    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }
                    if (hour.length() == 1) {
                        hour = "0" + hour;
                    }
                    String currenttime = String.valueOf(hour) + String.valueOf(minutes);
                    // if basic package
                    if (myWalletBalanceResponse.getPackage_id().equals("1")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 1 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // buyer can cancel job untill 1 hour before start time of job
                            if (Integer.parseInt(start_time) - Integer.parseInt(currenttime) > 0 && Integer.parseInt(start_time) - Integer.parseInt(currenttime) >= 100) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }

                        } else if (current_date.compareTo(job_start_date) == 1) {
                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {

                               /* if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                                /*if (myWalletBalanceResponse.getPackage_id().equals("1")) {
                                    if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 100) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                } else if (myWalletBalanceResponse.getPackage_id().equals("2")) {
                                    if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                } else if (myWalletBalanceResponse.getPackage_id().equals("3")) {
                                    if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/

                                /*if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {

                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 100) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }


                        } /*else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);

                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);

                        }*/ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.VISIBLE);
                            holder.cancel_disable.setVisibility(View.GONE);
                        }
                    }// if package is standard
                    else if (myWalletBalanceResponse.getPackage_id().equals("2")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 2 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // buyer can cancel job untill 1 hour before start time of job
                            if (Integer.parseInt(start_time) - Integer.parseInt(currenttime) > 0 && Integer.parseInt(start_time) - Integer.parseInt(currenttime) >= 100) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }
                        } else if (current_date.compareTo(job_start_date) == 1) {
                            Log.e("current", "date " + current_date);

                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {
                                /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 200) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                               /* if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 200) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }


                        } /*else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);

                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        }*/ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                            holder.cancel.setVisibility(View.VISIBLE);
                            holder.cancel_disable.setVisibility(View.GONE);
                        }
                    }// if package is premium
                    else if (myWalletBalanceResponse.getPackage_id().equals("3")) {
                        if (current_date.compareTo(job_start_date) == 0) {
                            // if remaining time is more than 3 hours buyer can refund the job request
                            if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            } else {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            }

                            // buyer can cancel job untill 1 hour before start time of job
                            if (Integer.parseInt(start_time) - Integer.parseInt(currenttime) > 0 && Integer.parseInt(start_time) - Integer.parseInt(currenttime) >= 100) {
                                holder.cancel.setVisibility(View.VISIBLE);
                                holder.cancel_disable.setVisibility(View.GONE);
                            } else {
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }
                        } else if (current_date.compareTo(job_start_date) == 1) {
                            long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            if (Integer.parseInt(dayDifference) > 1) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);

                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            } else {

                                /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }*/

                                if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    holder.refund.setVisibility(View.VISIBLE);
                                    holder.refund_disable.setVisibility(View.GONE);
                                }

                                /*Log.e("current", "date " + current_date);
                                if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                                    holder.refund.setVisibility(View.GONE);
                                    holder.refund_disable.setVisibility(View.VISIBLE);
                                } else {
                                    if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 300) {
                                        holder.refund.setVisibility(View.GONE);
                                        holder.refund_disable.setVisibility(View.VISIBLE);
                                    } else {
                                        holder.refund.setVisibility(View.VISIBLE);
                                        holder.refund_disable.setVisibility(View.GONE);
                                    }
                                }*/
                                holder.cancel.setVisibility(View.GONE);
                                holder.cancel_disable.setVisibility(View.VISIBLE);
                            }

                        } /*else if (current_date.compareTo(job_start_date) > 1) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);

                            holder.cancel.setVisibility(View.GONE);
                            holder.cancel_disable.setVisibility(View.VISIBLE);
                        } */ else {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);

                            holder.cancel.setVisibility(View.VISIBLE);
                            holder.cancel_disable.setVisibility(View.GONE);
                        }
                    }
                }
            }// means posted job
            else {
                String start_time = "";
                start_time = myWalletBalanceResponse.getStart_time().replace(":", "");
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date job_start_date = null;
                try {
                    job_start_date = sdf.parse(myWalletBalanceResponse.getStart_date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);
                String minutes = String.valueOf(c.get(Calendar.MINUTE));
                String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                if (minutes.length() == 1) {
                    minutes = "0" + minutes;
                }
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }
                String currenttime = String.valueOf(hour) + String.valueOf(minutes);

                if (current_date.compareTo(job_start_date) == 0) {
                    // if remaining time is more than 3 hours buyer can refund the job request
                    if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 0 && Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                        holder.refund.setVisibility(View.VISIBLE);
                        holder.refund_disable.setVisibility(View.GONE);
                    } else {
                        holder.refund.setVisibility(View.GONE);
                        holder.refund_disable.setVisibility(View.VISIBLE);
                    }

                    // buyer can cancel job untill 1 hour before start time of job
                    if (Integer.parseInt(start_time) - Integer.parseInt(currenttime) > 0 && Integer.parseInt(start_time) - Integer.parseInt(currenttime) >= 100) {
                        holder.cancel.setVisibility(View.VISIBLE);
                        holder.cancel_disable.setVisibility(View.GONE);
                    } else {
                        holder.cancel.setVisibility(View.GONE);
                        holder.cancel_disable.setVisibility(View.VISIBLE);
                    }

                } else if (current_date.compareTo(job_start_date) == 1) {
                    Log.e("current", "date " + current_date);

                    long difference = Math.abs(current_date.getTime() - job_start_date.getTime());
                    long differenceDates = difference / (24 * 60 * 60 * 1000);
                    String dayDifference = Long.toString(differenceDates);
                    if (Integer.parseInt(dayDifference) > 1) {
                        holder.refund.setVisibility(View.GONE);
                        holder.refund_disable.setVisibility(View.VISIBLE);

                        holder.cancel.setVisibility(View.GONE);
                        holder.cancel_disable.setVisibility(View.VISIBLE);
                    } else {

                        /*if (Integer.parseInt(currenttime)> Integer.parseInt(start_time) ) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                        } else {
                            holder.refund.setVisibility(View.VISIBLE);
                            holder.refund_disable.setVisibility(View.GONE);
                        }*/

                        if (Integer.parseInt(currenttime) - Integer.parseInt(start_time) > 300) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                        } else {
                            holder.refund.setVisibility(View.VISIBLE);
                            holder.refund_disable.setVisibility(View.GONE);
                        }

                        /*if (Integer.parseInt(currenttime) > Integer.parseInt(start_time)) {
                            holder.refund.setVisibility(View.GONE);
                            holder.refund_disable.setVisibility(View.VISIBLE);
                        } else {
                            if ((Integer.parseInt(currenttime) + 2400) - Integer.parseInt(start_time) < 300) {
                                holder.refund.setVisibility(View.GONE);
                                holder.refund_disable.setVisibility(View.VISIBLE);
                            } else {
                                holder.refund.setVisibility(View.VISIBLE);
                                holder.refund_disable.setVisibility(View.GONE);
                            }
                        }*/
                        holder.cancel.setVisibility(View.GONE);
                        holder.cancel_disable.setVisibility(View.VISIBLE);
                    }


                } /*else if (current_date.compareTo(job_start_date) > 1) {
                    holder.refund.setVisibility(View.GONE);
                    holder.refund_disable.setVisibility(View.VISIBLE);

                    holder.cancel.setVisibility(View.GONE);
                    holder.cancel_disable.setVisibility(View.VISIBLE);

                }*/ else {
                    holder.refund.setVisibility(View.GONE);
                    holder.refund_disable.setVisibility(View.VISIBLE);

                    holder.cancel.setVisibility(View.VISIBLE);
                    holder.cancel_disable.setVisibility(View.GONE);
                }
            }

        } else {
            holder.refund.setVisibility(View.GONE);
            holder.refund_disable.setVisibility(View.VISIBLE);
        }
    }

    private void refundCancelRequestApi(final String type, String req_id, final int position, String refund_reason) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.REQUEST_ID, req_id);
        formBuilder.add(AllOmorniParameters.TYPE, type);
        if (type.equals("1"))
            formBuilder.add(AllOmorniParameters.REQ_MSG, refund_reason);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.REFUND_CANCEL_REQUEST, formBody) {
            @Override
            public void getValueParse(String result) {
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
                            Utils.showToast(context, status.getString("message"));
                            arrayList.get(position).setWithdraw_status("1");
                            arrayList.get(position).setIs_sent("1");
                            if (type.equals("1"))
                                arrayList.get(position).setStatus("9");
                            else
                                arrayList.get(position).setStatus("8");

                            notifyDataSetChanged();
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
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
