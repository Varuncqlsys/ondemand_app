package com.omorni.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.EditProfileActivity;
import com.omorni.activity.MainActivity;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellerSecondFormFragment extends Fragment implements View.OnClickListener {
    private View view;
    private TextView save, basic_price, standard_price, premium_price;
    private EditText basic_description, basic_additional_price, standard_description, standard_additional_price, premium_description, premium_additional_price;
    private SavePref savePref;
    private TextView see_example;
    //    private String video_url = "https://www.youtube.com/watch?v=EknEIzswvC0&list=PLS1QulWo1RIbb1cYyzZpLFCKvdYV_yJ-E";
    private String video_url = "";
    String keywords = "";
    private String[] basic_price_array;
    private String[] standard_price_array;
    private String[] premium_price_array;
    private int basic_start_price, basic_end_price, standard_start_price, standard_end_price, premium_start_price, premium_end_price;
    private int incremental_value;
    private String keywordArray[];
    private CheckBox checkBox;
    private String is_spare_part = "0";
    ScrollView scroll_view;

    public SellerSecondFormFragment() {
        // Required empty public constructor
    }

    private int j = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (Utils.isConfigRtl(getActivity()))
            view = inflater.inflate(R.layout.fragment_seller_second_form_rtl, container, false);
        else
            view = inflater.inflate(R.layout.fragment_seller_second_form, container, false);

        savePref = new SavePref(getActivity());
        initialize();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSeller2Data();
    }

    private void initialize() {
        save = (TextView) view.findViewById(R.id.save);

        if (savePref.getUserType().equals("1"))
            save.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.place_order_seller_drawable));
        else
            save.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.place_order_drawable));

        basic_price = (TextView) view.findViewById(R.id.basic_price);
        standard_price = (TextView) view.findViewById(R.id.standard_price);
        premium_price = (TextView) view.findViewById(R.id.premium_price);
        see_example = (TextView) view.findViewById(R.id.see_example);

        basic_description = (EditText) view.findViewById(R.id.basic_description);
        basic_additional_price = (EditText) view.findViewById(R.id.basic_additional_price);

        standard_description = (EditText) view.findViewById(R.id.standard_description);
        standard_additional_price = (EditText) view.findViewById(R.id.standard_additional_price);
        scroll_view = (ScrollView) view.findViewById(R.id.scroll_view);
        premium_description = (EditText) view.findViewById(R.id.premium_description);
        premium_additional_price = (EditText) view.findViewById(R.id.premium_additional_price);
        checkBox = (CheckBox) view.findViewById(R.id.checkbox);



        basic_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.basic_description) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        standard_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.standard_description) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        premium_description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.premium_description) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });


        save.setOnClickListener(this);
        basic_price.setOnClickListener(this);
        standard_price.setOnClickListener(this);
        premium_price.setOnClickListener(this);
        see_example.setOnClickListener(this);

        if (savePref.getUserType().equals("1")) {
            checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_change_seller));
        } else {
            checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.checkbox_change));
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    is_spare_part = "1";
                } else {
                    is_spare_part = "0";
                }
            }
        });
        if (savePref.getUserType().equals("1")) {
            save.setBackground(getResources().getDrawable(R.drawable.savecontinueseller));
        } else {
            save.setBackground(getResources().getDrawable(R.drawable.savecontinue));
        }

        MainActivity.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


    }


    private void getSeller2Data() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(ParameterClass.USER_ID, savePref.getUserId());

        RequestBody requestBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.GET_SELLER2_SCREEN, requestBody) {
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
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            JSONArray jsonArrayData = jsonObjectBody.getJSONArray("data");
                            JSONArray jsonArrayKeyword = jsonObjectBody.getJSONArray("keywords");
                            JSONObject jsonObjectSettings = jsonObjectBody.getJSONObject("settings");

                            for (int i = 0; i < jsonArrayData.length(); i++) {
                                JSONObject jsonObject1 = jsonArrayData.getJSONObject(i);
                                if (i == 0) {
                                    basic_description.setText(jsonObject1.getString("package_description"));
                                    basic_price.setText(jsonObject1.getString("normal_charges"));
                                    basic_additional_price.setText(jsonObject1.getString("additional_charges"));
                                } else if (i == 1) {
                                    standard_description.setText(jsonObject1.getString("package_description"));
                                    standard_price.setText(jsonObject1.getString("normal_charges"));
                                    standard_additional_price.setText(jsonObject1.getString("additional_charges"));
                                } else if (i == 2) {
                                    premium_description.setText(jsonObject1.getString("package_description"));
                                    premium_price.setText(jsonObject1.getString("normal_charges"));
                                    premium_additional_price.setText(jsonObject1.getString("additional_charges"));
                                }
                            }
                            incremental_value = jsonObjectSettings.getInt("increment_value");
                            video_url = jsonObjectSettings.getString("video_url");
                            is_spare_part = jsonObjectSettings.getString("spare_parts");

                            if (is_spare_part.equals("0")) {
                                checkBox.setChecked(false);
                            } else {
                                checkBox.setChecked(true);
                            }

                            basic_start_price = jsonObjectSettings.getInt("basic_pakage_start");
                            basic_end_price = jsonObjectSettings.getInt("basic_pakage_end");

                            standard_start_price = jsonObjectSettings.getInt("standard_pakage_start");
                            standard_end_price = jsonObjectSettings.getInt("standard_pakage_end");

                            premium_start_price = jsonObjectSettings.getInt("premium_pakage_start");
                            premium_end_price = jsonObjectSettings.getInt("premium_pakage_end");

                            int number_basic_loop = (basic_end_price - basic_start_price) / incremental_value;
                            basic_price_array = new String[number_basic_loop + 1];
                            basic_price_array[0] = String.valueOf(basic_start_price);

                            for (int i = 1; i <= number_basic_loop; i++) {
                                basic_price_array[i] = String.valueOf(Integer.parseInt(basic_price_array[i - 1]) + incremental_value);
                            }

                            int number_standard_loop = (standard_end_price - standard_start_price) / incremental_value;
                            standard_price_array = new String[number_standard_loop + 1];
                            standard_price_array[0] = String.valueOf(standard_start_price);
                            for (int i = 1; i <= number_standard_loop; i++) {
                                standard_price_array[i] = String.valueOf(Integer.parseInt(standard_price_array[i - 1]) + incremental_value);
                            }


                            int number_premium_loop = (premium_end_price - premium_start_price) / incremental_value;
                            premium_price_array = new String[number_premium_loop + 1];
                            premium_price_array[0] = String.valueOf(premium_start_price);
                            for (int i = 1; i <= number_premium_loop; i++) {
                                premium_price_array[i] = String.valueOf(Integer.parseInt(premium_price_array[i - 1]) + incremental_value);
                            }

                            keywordArray = new String[jsonArrayKeyword.length()];
                            for (int i = 0; i < jsonArrayKeyword.length(); i++) {
                                JSONObject jsonObject1 = jsonArrayKeyword.getJSONObject(i);

                                keywordArray[i] = jsonObject1.getString("keyword");
                                if (i == 0)
                                    keywords = keywordArray[i];
                                else
                                    keywords = keywords + "," + keywordArray[i];

                            }
                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
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


    @Override
    public void onResume() {
        super.onResume();
        if (SellerFirstFormFragment.is_update_service) {
            MainActivity.toolbar_title.setText(getResources().getString(R.string.update_services));
        } else {
            MainActivity.toolbar_title.setText(getResources().getString(R.string.become_seller));
        }
        MainActivity.filter.setVisibility(View.VISIBLE);
        MainActivity.filter.setText("");
        MainActivity.filter.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back_btn, 0, 0, 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if (basic_description.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.basic_package_descri_error), getActivity());
                } else if (basic_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.basic_price_error), getActivity());
                } else if (basic_additional_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.additional_basic_package), getActivity());
                } else if (standard_description.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.standard_package_description), getActivity());
                } else if (standard_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.standard_price_error), getActivity());
                } else if (standard_additional_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.additional_standard_package), getActivity());
                } else if (premium_description.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.premium_package_descr), getActivity());
                } else if (premium_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.premium_price_error), getActivity());
                } else if (premium_additional_price.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.additional_premium_package), getActivity());
                } else if (Integer.parseInt(standard_price.getText().toString()) <= Integer.parseInt(basic_price.getText().toString())) {
                    Utils.showToast(getActivity(), getResources().getString(R.string.standard_greater_basic));
                } else if (Integer.parseInt(premium_price.getText().toString()) <= Integer.parseInt(standard_price.getText().toString())) {
                    Utils.showToast(getActivity(), getResources().getString(R.string.premium_greater_standard));
                } else {
                    boolean flag = true;
                    for (int i = 0; i < keywordArray.length; i++) {
                        if (basic_description.getText().toString().contains(keywordArray[i])) {
                            Utils.showToast(getActivity(), getResources().getString(R.string.description_character) + keywords);
                            flag = false;
                            break;
                        } else if (standard_description.getText().toString().contains(keywordArray[i])) {
                            Utils.showToast(getActivity(), getResources().getString(R.string.description_character) + keywords);
                            flag = false;
                            break;
                        } else if (premium_description.getText().toString().contains(keywordArray[i])) {
                            Utils.showToast(getActivity(), getResources().getString(R.string.description_character) + keywords);
                            flag = false;
                            break;
                        }
                    }
                    if (flag)
                        uploadData();
                }

                break;
            case R.id.basic_price:
                openBasicPriceDialog();
                break;
            case R.id.standard_price:
                openStandardPriceDialog();
                break;
            case R.id.premium_price:
                openPremiumPriceDialog();
                break;
            case R.id.see_example:
                openVideoLink();
                break;
        }
    }


    private void openVideoLink() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(video_url)));
    }


    private void openBasicPriceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.select_basic_price));
        builder.setItems(basic_price_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                basic_price.setText(basic_price_array[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openStandardPriceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.select_standard_price));
        builder.setItems(standard_price_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                standard_price.setText(standard_price_array[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openPremiumPriceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.select_premium_price));
        builder.setItems(premium_price_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                premium_price.setText(premium_price_array[position]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void uploadData() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());
        formBuilder.add(ParameterClass.USER_ID, savePref.getUserId());
        formBuilder.add(ParameterClass.package_1, "1");
        formBuilder.add(ParameterClass.package_1_normal_charges, basic_price.getText().toString());
        formBuilder.add(ParameterClass.package_1_additional_charges, basic_additional_price.getText().toString());
        formBuilder.add(ParameterClass.package_1_description, basic_description.getText().toString());

        formBuilder.add(ParameterClass.package_2, "2");
        formBuilder.add(ParameterClass.package_2_additional_charges, standard_additional_price.getText().toString());
        formBuilder.add(ParameterClass.package_2_normal_charges, standard_price.getText().toString());
        formBuilder.add(ParameterClass.package_2_description, standard_description.getText().toString());

        formBuilder.add(ParameterClass.package_3, "3");
        formBuilder.add(ParameterClass.package_3_additional_charges, premium_additional_price.getText().toString());
        formBuilder.add(ParameterClass.package_3_normal_charges, premium_price.getText().toString());
        formBuilder.add(ParameterClass.package_3_description, premium_description.getText().toString());
//        formBuilder.add(ParameterClass.SPARE_PART, is_spare_part);

        RequestBody requestBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.Become_Seller2, requestBody) {
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
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fm.beginTransaction();
                            fragmentTransaction.replace(R.id.container, new SellerThirdFromFragment());
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
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
