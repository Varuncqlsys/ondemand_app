package com.omorni.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.model.CardListResponse;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.stripe.android.model.Card;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class UpdateCardActivity extends BaseActivity {
    private EditText card_number, card_holder_name, card_expiry_month, card_expiry_year, card_verification_code;
    private TextView add_card;
    private Context context;
    private SavePref savePref;
    private CardListResponse cardListResponse;
    private String[] yearArray;
    CharSequence[] items = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    int current_year, future_year = 40;
    ImageView image_card_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = UpdateCardActivity.this;
        savePref = new SavePref(context);
        if (savePref.getUserType().equals("1")) {
            setTheme(R.style.AppThemeSeller);
            setContentView(R.layout.update_card_activity_seller);
        }// 2 means buyer
        else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.update_card_activity);
        }

        initialize();
        setToolbar();
        setData();
    }

    private void initialize() {
        current_year = Calendar.getInstance().get(Calendar.YEAR);
        yearArray = new String[future_year];
        for (int i = 0; i < future_year; i++) {
            yearArray[i] = String.valueOf(current_year + i);
        }

        cardListResponse = getIntent().getParcelableExtra("selected_card");

        card_number = (EditText) findViewById(R.id.card_number);
        card_holder_name = (EditText) findViewById(R.id.card_holder_name);
        card_expiry_month = (EditText) findViewById(R.id.card_expiry_month);
        card_expiry_year = (EditText) findViewById(R.id.card_expiry_year);
        card_verification_code = (EditText) findViewById(R.id.card_verification_code);
        add_card = (TextView) findViewById(R.id.add_card);
        image_card_type = (ImageView) findViewById(R.id.image_card_type);
        card_expiry_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMonth();
            }
        });

        card_expiry_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openYear();
            }
        });
        add_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (card_number.getText().toString().isEmpty()) {
                    Utils.showToast(context, getString(R.string.error_cardno));
                } else if (card_number.getText().toString().length() < 16) {
                    Utils.showToast(context, getString(R.string.error_cardlength));
                } else if (card_expiry_month.getText().toString().isEmpty()) {
                    Utils.showToast(context, getString(R.string.error_expirymnth));
                } else if (card_expiry_year.getText().toString().isEmpty()) {
                    Utils.showToast(context, getString(R.string.error_expiryyear));
                } else if (card_holder_name.getText().toString().isEmpty()) {
                    Utils.showToast(context, getString(R.string.error_name));
                } else if (!new Card(card_number.getText().toString(), Integer.parseInt(card_expiry_month.getText().toString()), Integer.parseInt(card_expiry_year.getText().toString()), null).validateCard()) {
                    Utils.showToast(context, getString(R.string.error_cardlength));
                }
                /*else if (card_verification_code.getText().toString().isEmpty()) {
                    Utils.showToast(context, getString(R.string.error_cvv));
                }*/
                else {
                    updateCardApi();
                }
            }
        });

        card_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 16) {
                    image_card_type.setImageBitmap(Utils.returnCardType(s.toString(), UpdateCardActivity.this));
                } else {
                    image_card_type.setImageBitmap(null);
                }
            }
        });
    }

    private void openMonth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.expiry_month));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                card_expiry_month.setText(items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void openYear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.expiry_year));
        builder.setItems(yearArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                card_expiry_year.setText(yearArray[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        toolbar_title.setText(getResources().getString(R.string.update_card));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
    }

    private void setData() {
        card_number.setText(cardListResponse.getCard_number());
        card_holder_name.setText(cardListResponse.getCard_name());
        card_expiry_month.setText(cardListResponse.getExpiry_month());
        card_expiry_year.setText(cardListResponse.getExpiry_year());
        card_verification_code.setText("123");
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

    private void updateCardApi() {
        String card_type  = new Card(card_number.getText().toString(), Integer.parseInt(card_expiry_month.getText().toString()), Integer.parseInt(card_expiry_year.getText().toString()), null).getType();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.CARD_ID, cardListResponse.getId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(AllOmorniParameters.CARD_NAME, card_holder_name.getText().toString());
        formBuilder.add(AllOmorniParameters.CARD_NUMBER, card_number.getText().toString());
        formBuilder.add(AllOmorniParameters.EXPIRY_MONTH_ADD, card_expiry_month.getText().toString());
        formBuilder.add(AllOmorniParameters.EXPIRY_YEAR_ADD, card_expiry_year.getText().toString());
        formBuilder.add(AllOmorniParameters.CARD_TYPE, card_type);

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.UPDATE_CARD_INFO_URL, formBody) {
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
                            finish();
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
