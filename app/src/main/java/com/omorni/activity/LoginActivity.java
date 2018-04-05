package com.omorni.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.omorni.R;
import com.omorni.adapter.ViewPagerAdapter;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.service.UpdateLocationService;
import com.omorni.utils.ConnectivityReceiver;
import com.omorni.utils.ContextWrapper;
import com.omorni.utils.GPSTracker;
import com.omorni.utils.LocaleHelper;
import com.omorni.utils.ParameterClass;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class LoginActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Context context;
    private EditText username_edittext, password_edittext;
    private LinearLayout main_layout;
    private SavePref savePref, savePref_token;
    boolean flag = false;
    private CallbackManager callbackManager;
    String social_id = "", first_name = "", last_name = "", fb_email = "", fb_image = "", social_type = "";
    private static final int RC_SIGN_IN = 007;
    private GoogleSignInOptions gso;
    private ViewPager viewPager;
    CirclePageIndicator circleIndicator;
    private Integer[] imagesArray = {R.drawable.loading_image1, R.drawable.loading_image2, R.drawable.loading_image3};
    ViewPagerAdapter adapter;
    double latitude = 0.0, longitude = 0.0;
    private final static int PERMISSION_CODE_LOCATION = 100;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClientSignIN;
    GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    GoogleSignInResult result1;
    private LinearLayout bottom_layout;
    private TextView dont;
    private SparseIntArray mErrorString;
    private static final int REQUEST_PERMISSIONS = 20;
    private ImageView change_language_toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isConfigRtl(LoginActivity.this)) {
            setContentView(R.layout.activity_login_rtl);
        } else {
            setContentView(R.layout.activity_login);
        }


        //Initializing FacebookSdk api client
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing google api client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClientSignIN = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        initialize();


        mErrorString = new SparseIntArray();

        int currentapiVersion = Build.VERSION.SDK_INT;
        // if current version is M or greater than M
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            String[] array = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            requestAppPermissions(array, R.string.permission, REQUEST_PERMISSIONS);
        } else {
            onPermissionsGranted(REQUEST_PERMISSIONS);
        }

//        setGoogleApiClient();
    }



    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (!isGpsOn())
            showGPSDisabledAlertToUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mGoogleApiClient.disconnect();
//        mGoogleApiClientSignIN.disconnect();
//        if (googleApiClient != null) {
//            googleApiClient.disconnect();
//        }
    }


    /*@Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mGoogleApiClientSignIN.connect();
//        if (googleApiClient != null) {
//            googleApiClient.connect();
//        }
    }*/

    private void initialize() {
        context = LoginActivity.this;
        printKeyHash(LoginActivity.this);
        savePref = new SavePref(context);
        savePref_token = new SavePref();
        Button signin_button = (Button) findViewById(R.id.signin_button);
        final TextView forgot_password = (TextView) findViewById(R.id.forgot_password);
        final TextView signup = (TextView) findViewById(R.id.signup);
//        final TextView dont = (TextView) findViewById(R.id.dont);

        username_edittext = (EditText) findViewById(R.id.username_edittext);
        password_edittext = (EditText) findViewById(R.id.password_edittext);
        change_language_toggle = (ImageView) findViewById(R.id.change_language_toggle);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);
//        bottom_layout = (LinearLayout) findViewById(R.id.bottom_layout);


        signup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    signup.setTextColor(ContextCompat.getColor(context, R.color.transparent_black));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    signup.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                    Intent intent = new Intent(context, SignupActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }

                return true;
            }
        });

        adapter = new ViewPagerAdapter(context, imagesArray);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
//        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setAdapter(new ViewPagerAdapter(LoginActivity.this, imagesArray));
        circleIndicator = (CirclePageIndicator) findViewById(R.id.circleIndicator);
        circleIndicator.setFillColor(getResources().getColor(R.color.colorBlack));
        circleIndicator.setViewPager(viewPager);

        ImageButton facebookButton = (ImageButton) findViewById(R.id.facebook_login);
        ImageButton googleButton = (ImageButton) findViewById(R.id.google_login);

        signin_button.setOnClickListener(this);
        forgot_password.setOnClickListener(this);
//        signup.setOnClickListener(this);
        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        //registerCallback from facebook
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                final ProgressDialog progressDialog = Utils.initializeProgress(context);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getResources().getString(R.string.loading));
                progressDialog.show();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                // Application code
                                try {
                                    social_id = object.getString("id");
//                                    String phone = object.getString("phone");
                                    first_name = object.getString("first_name");
                                    last_name = object.getString("last_name");

                                    fb_email = object.getString("email");
                                    if (fb_email.equals("")) {
                                        fb_email = object.getString("id") + "@gmail.com";
                                    }
                                    fb_image = "https://graph.facebook.com/" + social_id + "/picture?type=large";
                                    socialLoginApi(progressDialog);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,first_name,last_name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                LoginManager.getInstance().logOut();
//                Toast.makeText(LoginActivity.this, "On facebook cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                LoginManager.getInstance().logOut();

            }
        });
        if (savePref_token.getString(LoginActivity.this, "app_language", "en").equals("en")) {
            change_language_toggle.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.eng_on));
        } else {
            change_language_toggle.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.arabic_on));
        }

        change_language_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savePref_token.getString(LoginActivity.this, "app_language", "ar").equals("en")) {
                    change_language_toggle.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.arabic_on));

                    ContextWrapper.wrap(LoginActivity.this, "ar");
//                    LocaleHelper.setLocale(LoginActivity.this, "ar");
                } else {
                    change_language_toggle.setImageDrawable(ContextCompat.getDrawable(LoginActivity.this, R.drawable.eng_on));
                    ContextWrapper.wrap(LoginActivity.this, "en");
//                    LocaleHelper.setLocale(LoginActivity.this, "en");
                }
                finish();
                startActivity(getIntent());
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin_button:
                String email_value = username_edittext.getText().toString().trim();
                if (email_value.isEmpty()) {
                    Utils.showSnackBar(main_layout, getString(R.string.error_email), context);
                } else if (password_edittext.getText().toString().isEmpty()) {
                    Utils.showSnackBar(main_layout, getString(R.string.error_password), context);
                } else if (!Utils.isValidEmail(email_value)) {
                    Utils.showSnackBar(main_layout, getString(R.string.error_valid_email), context);
                } else {
                    getCurentLocation(email_value);
                }
                break;

            case R.id.forgot_password:
                Intent intent1 = new Intent(context, ForgotPasswordActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;

            case R.id.signup:

                break;

            case R.id.facebook_login:
                if (ConnectivityReceiver.isConnected()) {
                    flag = true;
                    //getInstance from facebook

                    social_type = "0";
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email", "user_birthday",
                            "user_friends", "user_photos"));
                } else {
                    openDialog();
                }
                break;

            case R.id.google_login:
                if (ConnectivityReceiver.isConnected()) {
                    social_type = "1";
                    //Creating an intent
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClientSignIN);

                    //Starting intent for result
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                } else {
                    openDialog();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If signin with facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //If signin with google plus
        if (requestCode == RC_SIGN_IN) {
            ProgressDialog progressDialog = Utils.initializeProgress(context);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.show();

            result1 = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result1, progressDialog);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mGoogleApiClientSignIN.connect();
    }

    //After the signing we are calling this function
    private void handleSignInResult(GoogleSignInResult result, final ProgressDialog progressDialog) {
        //If the login succeed
        if (result.isSuccess()) {
            social_type = "1";

            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();
            social_id = acct.getId();

            first_name = acct.getGivenName();

            if (first_name != null && !first_name.equals("null")) {
                last_name = acct.getFamilyName();
                fb_email = acct.getEmail();
                Uri url = acct.getPhotoUrl();
                if (url != null) {
                    fb_image = acct.getPhotoUrl().toString();
                } else {
                    fb_image = "";
                }
                String username = acct.getDisplayName();
                socialLoginApi(progressDialog);
            } else {
                social_type = "1";
                Auth.GoogleSignInApi.signOut(mGoogleApiClientSignIN);
                mGoogleApiClientSignIN.disconnect();
                mGoogleApiClientSignIN.connect();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClientSignIN);
                                startActivityForResult(signInIntent, RC_SIGN_IN);
                            }
                        });

                    }
                }, 6000);

            }

        } else {
            progressDialog.dismiss();
            Auth.GoogleSignInApi.signOut(mGoogleApiClientSignIN);
            mGoogleApiClientSignIN.disconnect();
            mGoogleApiClientSignIN.connect();
            Toast.makeText(this, "" + getResources().getString(R.string.cancelled_google), Toast.LENGTH_LONG).show();
        }
    }

    /****
     * Internet_Check Dialog
     ****/
    private void openDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(Utils.Internet_Check);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /****
     * SocialLoginApi
     ****/
    private void socialLoginApi(final ProgressDialog progress) {
        String app_language = "";
        if (SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.EMAIL, fb_email);
        formBuilder.add(AllOmorniParameters.FIRST_NAME, first_name);
        formBuilder.add(AllOmorniParameters.LAST_NAME, last_name);
        formBuilder.add(AllOmorniParameters.SOCIAL_ID, social_id);
        formBuilder.add(AllOmorniParameters.SOCIAL_TYPE, social_type);
        formBuilder.add(AllOmorniParameters.USER_IMAGE, fb_image);
        formBuilder.add(AllOmorniParameters.LATTITUDE, savePref.getLat());
        formBuilder.add(AllOmorniParameters.LONGITUDE, savePref.getLong());
        formBuilder.add(AllOmorniParameters.DEVICE_TYPE, "0");
        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, savePref.getString(context, "token", ""));
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.SOCIAL_LOGIN_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                progress.dismiss();
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClientSignIN);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            savePref.setUserId(jsonObjectBody.getString("id"));
                            savePref.setFirstname(jsonObjectBody.getString("first_name"));
                            savePref.setLastname(jsonObjectBody.getString("last_name"));
                            savePref.setEmail(jsonObjectBody.getString("email"));
                            savePref.setphone(jsonObjectBody.getString("mobile"));
                            savePref.setLat(jsonObjectBody.getString("latitude"));
                            savePref.setLong(jsonObjectBody.getString("longitude"));
                            savePref.setUserImage(jsonObjectBody.getString("user_image"));
                            savePref.setCoverImage(jsonObjectBody.getString("cover_photo"));
                            savePref.setAuthToken(jsonObjectBody.getString("auth_token"));
                            savePref.setOnlyMobileNumber(jsonObjectBody.getString("mobile_no"));
                            savePref.setCodeOnly(jsonObjectBody.getString("mobile_code"));
                            savePref.setPassword(jsonObjectBody.getString("password"));
                            savePref.setIsSocialLogin("1");
                            if (jsonObjectBody.getString("is_approved").equals("1")) {
                                savePref.setUserType("1");
                            } else {
                                savePref.setUserType("2");
                            }

                            savePref.setSellerToggle("0");
                            ContextWrapper.wrap(context, SavePref.getString(context, "app_language", "ar"));
//                            LocaleHelper.setLocale(context, SavePref.getString(context, "app_language", "ar"));
                            Log.e("login", "response " + SavePref.getString(context, "app_language", "en") + "  " + Utils.isConfigRtl(context));
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("message", status.getString("message"));
                            intent.putExtra("from_signup", true);
                            context.startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            getLegalData(status.optString("is_varify"), status.optString("user_id"), status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.internet_error), context);
                }
                progress.dismiss();
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }


    public void openTermDialog(final String terms, final String is_varify, final String user_id, final String message) {
        final Dialog dialog = new Dialog(LoginActivity.this, android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.terms);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        WebView webView = (WebView) dialog.findViewById(R.id.webView);
        ImageView back = (ImageView) dialog.findViewById(R.id.back);

        TextView agree = (TextView) dialog.findViewById(R.id.agree);
        TextView disagree = (TextView) dialog.findViewById(R.id.disagree);

        final TextView htmlTextView = (TextView) dialog.findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            htmlTextView.setText(Html.fromHtml(terms, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM));
        else
            htmlTextView.setText(Html.fromHtml(terms));

//        webView.loadDataWithBaseURL(null, terms, "text/html", "utf-8", null);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setDisplayZoomControls(false);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // if current user is not verify then open Confirm otp screen
                if (is_varify.equalsIgnoreCase("0")) {
                    Intent intent = new Intent(context, PhoneNumberActivity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                Utils.showToast(context, message);

            }
        });

        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getLegalData(final String is_varify, final String user_id, final String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(ParameterClass.LANGUAGE_TYPE, SavePref.getString(LoginActivity.this, "app_language", "en"));
        formBuilder.add(ParameterClass.TYPE, "0");
        RequestBody requestBody = formBuilder.build();

        GetAsync mAsync = new GetAsync(context, AllOmorniApis.TERMS_CONDITION, requestBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                Log.e("result", "here " + result);
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObjectBody = jsonObject.getJSONObject("body");
                            String terms = jsonObjectBody.getString("content");
                            openTermDialog(terms, is_varify, user_id, message);
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

    private void loginAPI(String email, String password) {
        String app_language = "";
        if (SavePref.getString(context, "app_language", "en").equals("en"))
            app_language = "0";
        else
            app_language = "1";

        final ProgressDialog progressDialog = Utils.initializeProgress(context);
        progressDialog.show();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.EMAIL, email);
        formBuilder.add(AllOmorniParameters.DEVICE_TYPE, "0");
        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, savePref.getString(context, "token", ""));
//        formBuilder.add(AllOmorniParameters.DEVICE_TOKEN, "abc");
        formBuilder.add(AllOmorniParameters.PASSWORD, password);
        formBuilder.add(AllOmorniParameters.LATTITUDE, savePref.getLat());
        formBuilder.add(AllOmorniParameters.LONGITUDE, savePref.getLong());
        formBuilder.add(AllOmorniParameters.APP_LANG, app_language);
        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.LOGIN_URL, formBody) {
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
                            savePref.setUserId(jsonObjectBody.getString("id"));

                            savePref.setFirstname(jsonObjectBody.getString("first_name"));
                            savePref.setLastname(jsonObjectBody.getString("last_name"));
                            savePref.setEmail(jsonObjectBody.getString("email"));
                            savePref.setphone(jsonObjectBody.getString("mobile"));
                            savePref.setLat(jsonObjectBody.getString("latitude"));
                            savePref.setLong(jsonObjectBody.getString("longitude"));
                            savePref.setUserImage(jsonObjectBody.getString("user_image"));
                            savePref.setCoverImage(jsonObjectBody.getString("cover_photo"));
                            savePref.setSellerToggle("0");
                            savePref.setAuthToken(jsonObjectBody.getString("auth_token"));
                            savePref.setOnlyMobileNumber(jsonObjectBody.getString("mobile_no"));
                            savePref.setCodeOnly(jsonObjectBody.getString("mobile_code"));
                            savePref.setIsSocialLogin("0");
                            savePref.setPassword(jsonObjectBody.getString("password"));
                            if (jsonObjectBody.getString("is_approved").equals("1")) {
                                savePref.setUserType("1");
                            } else {
                                savePref.setUserType("2");
                            }

                            ContextWrapper.wrap(context, SavePref.getString(context, "app_language", "ar"));
//                            LocaleHelper.setLocale(context, SavePref.getString(context, "app_language", "ar"));

                            Log.e("login", "response " + SavePref.getString(context, "app_language", "en") + "  " + Utils.isConfigRtl(context));
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("message", status.getString("message"));
                            intent.putExtra("from_signup", false);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                        } else {
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.showSnackBar(main_layout, getResources().getString(R.string.internet_error), context);
                }
            }

            @Override
            public void retry() {

            }
        };
        mAsync.execute();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            for (android.content.pm.Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    private void getCurentLocation(String email_value) {
        GPSTracker obj = new GPSTracker(context);
        Location location = obj.getLocation();
        if (location != null) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            savePref.setLat(String.valueOf(latitude));
            savePref.setLong(String.valueOf(longitude));
            loginAPI(email_value, password_edittext.getText().toString());
        } else {
            Utils.showToast(LoginActivity.this, getResources().getString(R.string.gps_disable));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClientSignIN.disconnect();
        /*if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }*/
    }

    private boolean isGpsOn() {
        boolean isGpsOn = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGpsOn = false;

        } else {
            isGpsOn = true;
        }
        return isGpsOn;
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(getResources().getString(R.string.gps_disable))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.turn_on),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            //If everything went fine lets get latitude and longitude
            savePref.setLat(String.valueOf(location.getLatitude()));
            savePref.setLong(String.valueOf(location.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    // check requested permissions are on or off
    public void requestAppPermissions(final String[] requestedPermissions, final int stringId, final int requestCode) {
        mErrorString.put(requestCode, stringId);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), stringId, Snackbar.LENGTH_INDEFINITE);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(LoginActivity.this, requestedPermissions, requestCode);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permission : grantResults) {
            permissionCheck = permissionCheck + permission;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode);
        } else {
            // onPermissionsGranted(requestCode);
        }
    }

    // if permissions granted succesfully
    private void onPermissionsGranted(int requestcode) {

    }


}
