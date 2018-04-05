package com.omorni.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.adapter.CardListAdapter;
import com.omorni.model.CardListResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.RecyclerViewClickListener;
import com.omorni.utils.RecyclerViewTouchListener;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class CardListActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private CardListAdapter cardListAdapter;
    private ArrayList<CardListResponse> arrayList;
    private Context context;
    private SavePref savePref;
    private TextView add_card, select_card;
    String type = "";
    String card_number = "";
    public int selected_position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(this);

        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.activity_card_list_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_card_list);
        }

        type = getIntent().getStringExtra("type");
        initialize();
        setToolbar();


    }

    @Override
    protected void onResume() {
        super.onResume();
        getCardListApi();
    }

    private void initialize() {
        select_card = (TextView) findViewById(R.id.select_card);
        if (type.equalsIgnoreCase("payment")) {
            card_number = getIntent().getStringExtra("card_number");
        } else {
            select_card.setVisibility(View.GONE);
        }
        context = CardListActivity.this;
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        add_card = (TextView) findViewById(R.id.add_card);
        arrayList = new ArrayList<CardListResponse>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        cardListAdapter = new CardListAdapter(context, arrayList, card_number, type);
        recyclerView.setAdapter(cardListAdapter);

        add_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddCardActivity.class);
                startActivity(intent);
            }
        });

       /* recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), recyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (type.equalsIgnoreCase("payment")) {
                    if (cardListAdapter != null) {
                        selected_position = position;
                        cardListAdapter.setSelection(arrayList.get(position).getCard_number());
                    }
                } else {
                    Intent intent = new Intent(context, UpdateCardActivity.class);
                    intent.putExtra("selected_card", arrayList.get(position));
                    context.startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/

        select_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.size() > 0 && selected_position != -1) {
                    returnIntent();
                } else {
                    Utils.showToast(CardListActivity.this, getResources().getString(R.string.no_card_selected));
                }
            }
        });
    }


    private void returnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", arrayList.get(selected_position));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        if (type.equalsIgnoreCase("payment")) {
            toolbar_title.setText(R.string.select_card);
        } else {
            toolbar_title.setText(R.string.update_payment);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getCardListApi() {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.GET_CARDS, formBody) {
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
                            if (arrayList.size() > 0)
                                arrayList.clear();
                            JSONArray jsonArray = jsonObject.getJSONArray("body");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                CardListResponse cardListResponse = new CardListResponse();
                                cardListResponse.setId(jsonObject1.getString("id"));
                                cardListResponse.setUser_id(jsonObject1.getString("user_id"));
                                cardListResponse.setCard_name(jsonObject1.getString("card_name"));
                                cardListResponse.setCard_number(jsonObject1.getString("card_number"));
                                cardListResponse.setExpiry_month(jsonObject1.getString("expiry_month"));
                                cardListResponse.setExpiry_year(jsonObject1.getString("expiry_year"));
                                cardListResponse.setStatus(jsonObject1.getString("status"));

                                arrayList.add(cardListResponse);
                            }
                            cardListAdapter.notifyDataSetChanged();

                        } else {

                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(context, getResources().getString(R.string.internet_error));
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

}
