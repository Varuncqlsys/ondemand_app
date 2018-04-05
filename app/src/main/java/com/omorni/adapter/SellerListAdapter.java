package com.omorni.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.omorni.R;
import com.omorni.activity.SelectedPackageActivity;
import com.omorni.activity.SellerDetailActivity;
import com.omorni.activity.SellerListActivity;
import com.omorni.model.SellerData;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by V on 2/15/2017.
 */

public class SellerListAdapter extends RecyclerView.Adapter<SellerListAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<SellerData> arrayList;
    ArrayList<SellerData> searchChatUserArrayList;
    //private SellerListFilter mFilter;
    private boolean service_now;
    private String date, time, start_time, end_time;
    SavePref savepref;

    public SellerListAdapter(ArrayList<SellerData> arrayList, Context context, boolean service_now) {
        this.context = context;
        this.arrayList = arrayList;
        //this.searchChatUserArrayList = arrayList;
        this.service_now = service_now;
        savepref = new SavePref(context);
        this.searchChatUserArrayList = new ArrayList<SellerData>();
        this.searchChatUserArrayList.addAll(arrayList);
    }

    public SellerListAdapter(ArrayList<SellerData> arrayList, Context context, boolean service_now, String date, String time, String start_time, String end_time) {
        this.context = context;
        this.arrayList = arrayList;
        this.searchChatUserArrayList = new ArrayList<SellerData>();
        this.searchChatUserArrayList.addAll(arrayList);
        this.service_now = service_now;
        this.date = date;
        this.time = time;
        this.start_time = start_time;
        this.end_time = end_time;
        savepref = new SavePref(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            if (savepref.getUserType().equals("1")) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_list_row_seller_rtl, parent, false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_list_row_rtl, parent, false);
            }
        } else {
            if (savepref.getUserType().equals("1")) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_list_row_seller, parent, false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_list_row, parent, false);
            }
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final SellerData sellerData = arrayList.get(position);
        holder.username.setText(sellerData.getFirst_name() + " " + sellerData.getLast_name());
        holder.number_users.setText("(" + sellerData.getTotal_rating_user() + ")");
        holder.user_description.setText(sellerData.getService_title());

        if (sellerData.getAllPackages().size() > 0) {
            holder.basic_value.setText(sellerData.getAllPackages().get(0).getNormal_charges());
            holder.standard_value.setText(sellerData.getAllPackages().get(1).getNormal_charges());
            holder.premium_value.setText(sellerData.getAllPackages().get(2).getNormal_charges());

//            holder.text_basic.setText(sellerData.getAllPackages().get(0).getPackage_name());
//            holder.text_standard.setText(sellerData.getAllPackages().get(1).getPackage_name());
//            holder.text_premium.setText(sellerData.getAllPackages().get(2).getPackage_name());

            holder.text_basic.setText(context.getResources().getString(R.string.basic));
            holder.text_standard.setText(context.getResources().getString(R.string.standard));
            holder.text_premium.setText(context.getResources().getString(R.string.premium));
        }
        /*holder.text_basic.setText(sellerData.getAllPackages().get(0).getPackage_name());
        holder.text_standard.setText(sellerData.getAllPackages().get(1).getPackage_name());
        holder.text_premium.setText(sellerData.getAllPackages().get(2).getPackage_name());*/


        if (savepref.getUserType().equals("1")) {
            Glide.with(context).load(sellerData.getUser_image()).placeholder(R.drawable.user_placeholder_seller).override(120, 120).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);
        } else {
            Glide.with(context).load(sellerData.getUser_image()).placeholder(R.drawable.user_placeholder).override(120, 120).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.user_image);
        }

        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SellerDetailActivity.class);
                intent.putExtra("seller_id", sellerData.getSellerId());
                intent.putExtra(ParameterClass.SELLER_CATEGORY, sellerData.getSeller_category());
                intent.putExtra("omorni_procesing_fee", sellerData.getOmorni_processing_fee());
                intent.putExtra("vat_tax", sellerData.getVat_tax());
                intent.putExtra("req_lat", sellerData.getReq_lat());
                intent.putExtra("req_lng", sellerData.getReq_lng());
                intent.putExtra("favourite", sellerData.getFavourite());


                if (service_now)
                    intent.putExtra("service_now", service_now);
                else {
                    intent.putExtra("service_now", service_now);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);

                    intent.putExtra("start_time", start_time);
                    intent.putExtra("end_time", end_time);
                }
                context.startActivity(intent);
                ((SellerListActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
        holder.rating.setRating(Float.parseFloat(arrayList.get(position).getRating()));

        if (savepref.getUserType().equals("1")) {
            if (arrayList.get(position).getFavourite().equals("0"))
                holder.favourite.setImageResource(R.drawable.un_fav_seller);
            else
                holder.favourite.setImageResource(R.drawable.favorites_seller);
        } else {
            if (arrayList.get(position).getFavourite().equals("0"))
                holder.favourite.setImageResource(R.drawable.unfavourite);
            else
                holder.favourite.setImageResource(R.drawable.fav);
        }


        holder.favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sellerData.getFavourite().equals("1"))
                    favUnfavApi(sellerData.getSellerId(), position, "0");
                else
                    favUnfavApi(sellerData.getSellerId(), position, "1");
            }
        });

        holder.basic_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sellerData.getAllPackages().size() > 0) {
                    Intent intent = new Intent(context, SelectedPackageActivity.class);
                    intent.putExtra("selected_package", searchChatUserArrayList.get(position));
                    intent.putExtra("package", "50");
                    intent.putExtra("omorni_procesing_fee", sellerData.getOmorni_processing_fee());
                    intent.putExtra("from_sellerdetail", false);
                    intent.putExtra("seller_id", sellerData.getSellerId());
                    if (service_now)
                        intent.putExtra("service_now", service_now);
                    else {
                        intent.putExtra("service_now", service_now);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);

                        intent.putExtra("start_time", start_time);
                        intent.putExtra("end_time", end_time);
                    }
                    context.startActivity(intent);
                    ((SellerListActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context,sellerData.getFirst_name() + " " +  context.getResources().getString(R.string.no_basic));
                }
            }
        });

        holder.standard_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sellerData.getAllPackages().size() > 1) {
                    Intent intent = new Intent(context, SelectedPackageActivity.class);
                    intent.putExtra("selected_package", searchChatUserArrayList.get(position));
                    intent.putExtra("package", "80");
                    intent.putExtra("from_sellerdetail", false);
                    intent.putExtra("seller_id", sellerData.getSellerId());
                    intent.putExtra("omorni_procesing_fee", sellerData.getOmorni_processing_fee());
                    if (service_now)
                        intent.putExtra("service_now", service_now);
                    else {
                        intent.putExtra("service_now", service_now);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);

                        intent.putExtra("start_time", start_time);
                        intent.putExtra("end_time", end_time);
                    }
                    context.startActivity(intent);
                    ((SellerListActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context, sellerData.getFirst_name() + " " + context.getResources().getString(R.string.no_standard));
                }
            }
        });

        holder.premium_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sellerData.getAllPackages().size() > 2) {
                    Intent intent = new Intent(context, SelectedPackageActivity.class);
                    intent.putExtra("selected_package", searchChatUserArrayList.get(position));
                    intent.putExtra("package", "120");
                    intent.putExtra("from_sellerdetail", false);
                    intent.putExtra("seller_id", sellerData.getSellerId());
                    intent.putExtra("omorni_procesing_fee", sellerData.getOmorni_processing_fee());
                    if (service_now)
                        intent.putExtra("service_now", service_now);
                    else {
                        intent.putExtra("service_now", service_now);
                        intent.putExtra("date", date);
                        intent.putExtra("time", time);

                        intent.putExtra("start_time", start_time);
                        intent.putExtra("end_time", end_time);
                    }
                    context.startActivity(intent);
                    ((SellerListActivity) context).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } else {
                    Utils.showToast(context, sellerData.getFirst_name() + " " + context.getResources().getString(R.string.no_premium));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView user_image;
        public TextView username, number_users, user_description, standard_plan, premium_plan, basic_value, text_basic, standard_value, text_standard, premium_value, text_premium;
        public RatingBar rating;
        private ImageView favourite;
        public RelativeLayout parent_layout;
        private LinearLayout basic_layout, standard_layout, premium_layout;

        public MyViewHolder(View view) {
            super(view);

            parent_layout = (RelativeLayout) view.findViewById(R.id.parent_layout);

            basic_layout = (LinearLayout) view.findViewById(R.id.basic_layout);
            standard_layout = (LinearLayout) view.findViewById(R.id.standard_layout);
            premium_layout = (LinearLayout) view.findViewById(R.id.premium_layout);

            username = (TextView) view.findViewById(R.id.username);
            number_users = (TextView) view.findViewById(R.id.number_users);
            user_description = (TextView) view.findViewById(R.id.user_description);
            favourite = (ImageView) view.findViewById(R.id.favourite);
            standard_plan = (TextView) view.findViewById(R.id.standard_plan);
            premium_plan = (TextView) view.findViewById(R.id.premium_plan);
            user_image = (CircleImageView) view.findViewById(R.id.user_image);
            rating = (RatingBar) view.findViewById(R.id.rating);
            premium_value = (TextView) view.findViewById(R.id.premium_value);
            text_premium = (TextView) view.findViewById(R.id.text_premium);
            basic_value = (TextView) view.findViewById(R.id.basic_value);
            text_basic = (TextView) view.findViewById(R.id.text_basic);
            standard_value = (TextView) view.findViewById(R.id.standard_value);
            text_standard = (TextView) view.findViewById(R.id.text_standard);

        }
    }

   /* @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SellerListAdapter.SellerListFilter();
        }
        return mFilter;
    }
*/


    private void favUnfavApi(String seller_id, final int position, String fav) {
        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savepref.getUserId());
        formBuilder.add(AllOmorniParameters.FAVOURITE_ID, seller_id);
        formBuilder.add(AllOmorniParameters.TYPE, fav);
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savepref.getAUthToken());
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.FAV_UNFAV, formBody) {
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
                            likeUnlike(position);
                        } else {

                            Utils.showToast(context, status.getString("message"));

                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(context, context.getString(R.string.internet_error));
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    public void likeUnlike(int position) {
        if (arrayList.get(position).getFavourite().equals("1")) {
            arrayList.get(position).setFavourite("0");
        } else {
            arrayList.get(position).setFavourite("1");
        }
        notifyDataSetChanged();
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase();
        arrayList.clear();
        if (charText.length() == 0) {
            arrayList.addAll(searchChatUserArrayList);
        } else {
            for (SellerData wp : searchChatUserArrayList) {
                String value = wp.getFirst_name() + " " + wp.getLast_name() + " " + wp.getJob_description() + " " + wp.getLanguage();
                if (value.toLowerCase().contains(charText.toLowerCase())) {
                    arrayList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


}
