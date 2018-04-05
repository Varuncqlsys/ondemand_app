package com.omorni.fragment;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omorni.R;
import com.omorni.activity.CardListActivity;
import com.omorni.activity.ChangeLanguageActivity;
import com.omorni.activity.ChangePasswordActivity;
import com.omorni.activity.DisclaimerActivity;
import com.omorni.activity.DisclaimerLatestActivity;
import com.omorni.activity.EditProfileActivity;
import com.omorni.activity.LegalActivity;
import com.omorni.activity.LoginActivity;
import com.omorni.activity.MainActivity;

import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.service.UpdateLocationService;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;


public class SettingFragment extends Fragment implements View.OnClickListener {
    private SavePref savePref;
    private TextView username, user_phone, user_email, language_value;
    private TextView version_number;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if (Utils.isConfigRtl(getActivity())) {
            view = inflater.inflate(R.layout.fragment_setting_rtl, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_setting, container, false);
        }

        initialize(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void initialize(View view) {
        savePref = new SavePref(getActivity());

        username = (TextView) view.findViewById(R.id.username);
        user_phone = (TextView) view.findViewById(R.id.user_phone);
        user_email = (TextView) view.findViewById(R.id.user_email);
        language_value = (TextView) view.findViewById(R.id.language_value);
        version_number = (TextView) view.findViewById(R.id.version_number);
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            version_number.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        RelativeLayout changePasswordLayout = (RelativeLayout) view.findViewById(R.id.change_password_layout);
        RelativeLayout languageLayout = (RelativeLayout) view.findViewById(R.id.language_layout);
//        RelativeLayout ratesLayout = (RelativeLayout) view.findViewById(R.id.rates_layout);
        RelativeLayout appRateLayout = (RelativeLayout) view.findViewById(R.id.app_rate_layout);
        RelativeLayout feedbackLayout = (RelativeLayout) view.findViewById(R.id.feedback_layout);
        LinearLayout update_skill_linear = (LinearLayout) view.findViewById(R.id.update_skill_linear);
        RelativeLayout update_profile_layout = (RelativeLayout) view.findViewById(R.id.update_profile_layout);
        RelativeLayout update_payment_layout = (RelativeLayout) view.findViewById(R.id.update_payment_layout);
        RelativeLayout update_skills_layout = (RelativeLayout) view.findViewById(R.id.update_skills_layout);
        RelativeLayout legal_layout = (RelativeLayout) view.findViewById(R.id.legal_layout);
        Button sign_out = (Button) view.findViewById(R.id.sign_out);

        if (savePref.getUserType().equals("1")) {
            sign_out.setBackgroundResource(R.drawable.seeler_bg_seller_drawable);
            update_skill_linear.setVisibility(View.VISIBLE);
        } else {
            sign_out.setBackgroundResource(R.drawable.seeler_bg_drawable);
            update_skill_linear.setVisibility(View.GONE);
        }

        if (SavePref.getString(getActivity(), "app_language", "en").equals("en")) {
            language_value.setText(getResources().getString(R.string.english));
        } else {
            language_value.setText(getResources().getString(R.string.arabic));
        }
        changePasswordLayout.setOnClickListener(this);
        languageLayout.setOnClickListener(this);
//        ratesLayout.setOnClickListener(this);
        appRateLayout.setOnClickListener(this);
        feedbackLayout.setOnClickListener(this);
        update_profile_layout.setOnClickListener(this);
        update_payment_layout.setOnClickListener(this);
        update_skills_layout.setOnClickListener(this);
        legal_layout.setOnClickListener(this);
        sign_out.setOnClickListener(this);
    }

    private void setData() {
        username.setText(savePref.getFirstname() + " " + savePref.getLastname());
        user_phone.setText(savePref.getphone());
        user_email.setText(savePref.getEmail());
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.change_password_layout:
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                getActivity().startActivity(intent);
                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.language_layout:
                Intent intent_language = new Intent(getActivity(), ChangeLanguageActivity.class);
                startActivity(intent_language);
                break;

            case R.id.update_skills_layout:
                SellerFirstFormFragment sellerFirstFormFragment = new SellerFirstFormFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("is_update_service", true);
                sellerFirstFormFragment.setArguments(bundle);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.container, sellerFirstFormFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.update_profile_layout:
                Intent intent1 = new Intent(getActivity(), EditProfileActivity.class);
                getActivity().startActivity(intent1);
                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.update_payment_layout:
                Intent intent2 = new Intent(getActivity(), CardListActivity.class);
                intent2.putExtra("type", "update");
                getActivity().startActivity(intent2);
                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.app_rate_layout:
                openPlayStore();
                break;

            case R.id.feedback_layout:
                sendFeedBack();
                break;

            case R.id.legal_layout:
                Intent intent3 = new Intent(getActivity(), LegalActivity.class);
                getActivity().startActivity(intent3);
                ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.sign_out:
                openDialog();

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.toolbar_title.setText(getResources().getString(R.string.settings));
        MainActivity.toolbar_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        MainActivity.filter.setVisibility(View.GONE);
        setData();
    }

    private void openDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle(getString(R.string.app_name));
        myAlertDialog.setMessage(getString(R.string.want_logout));
        myAlertDialog.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        logoutApi();

                    }
                });
        myAlertDialog.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        myAlertDialog.show();
    }

    private void logoutApi() {
        final ProgressDialog progressDialog = Utils.initializeProgress(getActivity());
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(getActivity(), AllOmorniApis.LOGOUT_URL, formBody) {
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
                            NotificationManager nMgr = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                            nMgr.cancelAll();
                            savePref.clearPreferences();
                            getActivity().stopService(new Intent(getActivity(), UpdateLocationService.class));
                            Intent in = new Intent(getActivity(), LoginActivity.class);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(in);
                            getActivity().finish();
                            ((MainActivity) getActivity()).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Utils.checkAuthToken(getActivity(), status.getString("auth_token"), status.getString("message"), savePref);
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showToast(getActivity(), getResources().getString(R.string.internet_error));
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    private void sendFeedBack() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] recipients = {"support@omorni.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        intent.setType("text/html");
        startActivity(Intent.createChooser(intent, "Send mail"));
    }

    private void openPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }
}
