package com.omorni.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.CardListActivity;
import com.omorni.activity.UpdateCardActivity;
import com.omorni.model.CardListResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.omorni.utils.Utils.maskCardNumber;

/**
 * Created by V on 4/4/2017.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<CardListResponse> arrayList;
    String card_number_selected = "";
    String type = "";
    SavePref savePref;

    public CardListAdapter(Context context, ArrayList<CardListResponse> arrayList, String selection, String type) {
        this.context = context;
        this.arrayList = arrayList;
        this.card_number_selected = selection;
        this.type = type;
        savePref = new SavePref(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (Utils.isConfigRtl(context)) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_row_rtl, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_row, parent, false);
        }
        return new CardListAdapter.MyViewHolder(itemView);

    }

    public void setSelection(String card_number_selected) {
        this.card_number_selected = card_number_selected;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final CardListResponse cardListResponse = arrayList.get(position);
        holder.bank_name.setText(cardListResponse.getCard_name());
        holder.card_number.setText(maskCardNumber(cardListResponse.getCard_number(), "xxxx-xxxx-xxxx-####"));
        //holder.card_number.setText(cardListResponse.getCard_number());
        holder.card_image.setImageBitmap(Utils.returnCardType(cardListResponse.getCard_number(), context));

        if (savePref.getUserType().equals("1")) {
            holder.radio_button.setBackground(context.getResources().getDrawable(R.drawable.radio_selector_seller));
        } else {
            holder.radio_button.setBackground(context.getResources().getDrawable(R.drawable.radio_selector_buyer));
        }

        if (card_number_selected.equalsIgnoreCase(cardListResponse.getCard_number())) {
            holder.radio_button.setChecked(true);
        } else {
            holder.radio_button.setChecked(false);
        }

        if (type.equalsIgnoreCase("payment")) {
            holder.radio_button.setVisibility(View.VISIBLE);
            holder.arrow.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        } else {
            holder.radio_button.setVisibility(View.GONE);
            holder.arrow.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equalsIgnoreCase("payment")) {
                    ((CardListActivity) context).selected_position = position;
                    setSelection(arrayList.get(position).getCard_number());

                } else {
                    Intent intent = new Intent(context, UpdateCardActivity.class);
                    intent.putExtra("selected_card", arrayList.get(position));
                    context.startActivity(intent);
                }
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView card_image, arrow, delete;
        public TextView bank_name, card_number;
        public RelativeLayout parent;
        public RadioButton radio_button;

        public MyViewHolder(View view) {
            super(view);
            card_image = (ImageView) view.findViewById(R.id.card_image);
            bank_name = (TextView) view.findViewById(R.id.bank_name);
            card_number = (TextView) view.findViewById(R.id.card_number);
            parent = (RelativeLayout) view.findViewById(R.id.parent);
            radio_button = (RadioButton) view.findViewById(R.id.radio_button);
            arrow = (ImageView) view.findViewById(R.id.arrow);
            delete = (ImageView) view.findViewById(R.id.delete);
        }
    }


    public void showDeleteDialog(final int pos) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.want_delete_card))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteCardApi(pos);
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


    private void deleteCardApi(final int pos) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.CARD_ID, arrayList.get(pos).getId());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.DELETE_CARD, formBody) {
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
                            arrayList.remove(pos);
                            notifyDataSetChanged();
                        } else {
                            Utils.showToast(context, status.getString("message"));
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
