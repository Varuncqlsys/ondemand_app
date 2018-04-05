package com.omorni.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.omorni.BuildConfig;
import com.omorni.R;
import com.omorni.adapter.CountryAdapter;
import com.omorni.parser.AllOmorniApis;
import com.omorni.parser.AllOmorniParameters;
import com.omorni.parser.GetAsync;
import com.omorni.utils.SavePref;
import com.omorni.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileActivity extends BaseActivity implements View.OnClickListener {
    private ImageView cover_pic, camera, user_pic, plus, edit_mobile, edit_email;
    private EditText first_name, last_name, phone, email;
    private TextView update;
    private SavePref savePref;
    private Context context;
    private String pic_path_cover = "", pic_path_user = "";
    private File coverPicFile, userPicFile;
    private MediaType MEDIA_TYPE_USER_PIC = null;
    private MediaType MEDIA_TYPE_COVER_PIC = null;
    ArrayList<JsonObject> countryList;
    //    Spinner country_spinner;
    TextView country_code_text;
    EditText edit_phone;
    private static int REQUEST_CAMERA = 201;
    private static int SELECT_FILE = 200;
    private int image_type = 0;
    File mImageFile;
    private static final int REQUEST_PERMISSIONS = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savePref = new SavePref(EditProfileActivity.this);

        if (Utils.isConfigRtl(EditProfileActivity.this)) {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_edit_profile_seller_rtl);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_edit_profile_rtl);
            }
        } else {
            if (savePref.getUserType().equals("1")) {
                setTheme(R.style.AppThemeSeller);
                setContentView(R.layout.activity_edit_profile_seller);
            }// 2 means buyer
            else {
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_edit_profile);
            }
        }

        initialize();
        setToolbar();
        setData();
    }

    private void initialize() {
        context = EditProfileActivity.this;
        savePref = new SavePref(context);
        cover_pic = (ImageView) findViewById(R.id.cover_pic);
        camera = (ImageView) findViewById(R.id.camera);
        user_pic = (ImageView) findViewById(R.id.user_pic);
        plus = (ImageView) findViewById(R.id.plus);
        edit_email = (ImageView) findViewById(R.id.edit_email);
        edit_mobile = (ImageView) findViewById(R.id.edit_mobile);

        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);


        update = (TextView) findViewById(R.id.update);

        update.setOnClickListener(this);
        plus.setOnClickListener(this);
        camera.setOnClickListener(this);
        edit_mobile.setOnClickListener(this);
        edit_email.setOnClickListener(this);

        if (savePref.getIsSocialLogin().equals("0")) {
            edit_email.setVisibility(View.VISIBLE);
        } else {
            edit_email.setVisibility(View.GONE);
        }

    }


    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title);
        toolbar_title.setText(R.string.update_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);
    }

    private void setData() {
        first_name.setText(savePref.getFirstname());
        last_name.setText(savePref.getLastname());
        phone.setText(savePref.getphone());
        email.setText(savePref.getEmail());
        if (savePref.getUserType().equals("1")) {
            Glide.with(context).load(savePref.getUserImage()).placeholder(R.drawable.user_placeholder_seller).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(user_pic);
        } else {
            Glide.with(context).load(savePref.getUserImage()).placeholder(R.drawable.user_placeholder).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(user_pic);
        }

        Glide.with(context).load(savePref.getCoverImage()).placeholder(R.drawable.loading_image2).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(cover_pic);


        first_name.setSelection(first_name.getText().length());
        last_name.setSelection(last_name.getText().length());
        phone.setSelection(phone.getText().length());
        email.setSelection(email.getText().length());
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.camera:
                image_type = 0;
                selectImage();
                break;

            case R.id.plus:
                image_type = 1;
                selectImage();
                break;

            case R.id.edit_mobile:
                openMobileDialog();
                break;

            case R.id.edit_email:
                openEmailDialog();
                break;

            case R.id.update:
                if (first_name.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.error_firstname), context);
                } else if (last_name.getText().toString().length() == 0) {
                    Utils.showSnackBar(view, getResources().getString(R.string.error_lastname), context);
                } else {
                    updateProfileApi(savePref.getphone());
                }


                break;
        }
    }


    private void selectImage() {
        final CharSequence[] items = getResources().getStringArray(R.array.photo_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    int currentapiVersion = Build.VERSION.SDK_INT;
                    if (currentapiVersion >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
                        }else{
                            cameraIntent();
                        }
                    } else {
                        cameraIntent();
                    }
                } else if (item == 1) {
                    galleryIntent();
                } else if (item == 2) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permission : grantResults) {
            permissionCheck = permissionCheck + permission;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            cameraIntent();
        } else {
            // onPermissionsGranted(requestCode);
        }
    }


    /*private void selectImage() {
        final CharSequence[] items = getResources().getStringArray(R.array.photo_array);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    cameraIntent();
                } else if (item == 1) {
                    galleryIntent();
                } else if (item == 2) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }*/


    private void cameraIntent() {

        try {
            createImageFile(context, "image", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri file_uri = FileProvider.getUriForFile(EditProfileActivity.this, BuildConfig.APPLICATION_ID + ".provider", mImageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CAMERA);

//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void createImageFile(Context context, String name, String extension) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mImageFile = File.createTempFile(
                name,  /* prefix */
                extension,        /* suffix */
                storageDir      /* directory */
        );
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_FILE);
    }

    private void openMobileDialog() {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.dialog_confirm_mobile);
        dialog.setCanceledOnTouchOutside(true);
        edit_phone = (EditText) dialog.findViewById(R.id.edit_phone);
        TextView confirm = (TextView) dialog.findViewById(R.id.confirm);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        country_code_text = (TextView) dialog.findViewById(R.id.country_code_text);
        countryList = Utils.getCountries(context);


        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String CountryId = savePref.getCodeOnly();
        int selected = 0;

        for (int i = 0; i < countryList.size(); i++) {
            String code = countryList.get(i).get("code").getAsString();
            if (CountryId.equalsIgnoreCase(code)) {
                selected = i;
                country_code_text.setText("(" + countryList.get(i).get("iso").getAsString() + ")" + " +" + countryList.get(i).get("code").getAsString());
                break;
            }
        }


        country_code_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCountryListDialog();
            }
        });





        /*country_spinner = (Spinner) dialog.findViewById(R.id.country_spinner);
//        country_spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);



        CountryAdapter dataAdapter = new CountryAdapter(context, countryList);
        country_spinner.setAdapter(dataAdapter);


        country_spinner.setSelection(selected);

        country_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final TextView name = (TextView) view.findViewById(R.id.name);
                final TextView code = (TextView) view.findViewById(R.id.code);
                name.setBackgroundResource(android.R.color.transparent);
                name.setTextColor(Color.BLACK);
                code.setBackgroundResource(android.R.color.transparent);
                code.setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        edit_phone.setText(savePref.getOnlyMobileNumber());
        edit_phone.setSelection(edit_phone.getText().length());
        if (savePref.getUserType().equals("1"))
            confirm.setBackground(getResources().getDrawable(R.drawable.sort_bgd_seller_drawable));
        else
            confirm.setBackground(getResources().getDrawable(R.drawable.sort_bg));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_phone.getText().toString().length() == 0) {
                    Utils.showToast(context, getResources().getString(R.string.error_phone));
                } else {
                    dialog.dismiss();
                    Log.e("phone", "here " + country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + edit_phone.getText().toString());
//                    country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+"),country_code_text.getText().toString().length());
                    updateProfileApi(country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + edit_phone.getText().toString());
                }
//                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openCountryListDialog() {
        Collections.sort(countryList, new Comparator<JsonObject>() {
            @Override
            public int compare(JsonObject jsonObject, JsonObject t1) {
                return jsonObject.get("name").getAsString().compareTo(t1.get("name").getAsString());
            }
        });

        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.dialog_country_spinner);
        dialog.setCanceledOnTouchOutside(true);
        CountryAdapter dataAdapter = new CountryAdapter(context, countryList, country_code_text, dialog);

        ListView listView = (ListView) dialog.findViewById(R.id.listview);
        listView.setAdapter(dataAdapter);

        dialog.show();
    }

    private void openEmailDialog() {
        final Dialog dialog = new Dialog(context, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.dialog_confirm);

        dialog.setCanceledOnTouchOutside(true);

        final EditText edit_email = (EditText) dialog.findViewById(R.id.edit_email);
        TextView confirm = (TextView) dialog.findViewById(R.id.confirm);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        if (savePref.getUserType().equals("1"))
            confirm.setBackground(getResources().getDrawable(R.drawable.sort_bgd_seller_drawable));
        else
            confirm.setBackground(getResources().getDrawable(R.drawable.sort_bg));
        edit_email.setText(email.getText().toString());
        edit_email.setSelection(edit_email.getText().length());
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_email.getText().toString().length() == 0) {
                    Utils.showToast(context, getResources().getString(R.string.error_email));
                } else if (!Utils.isValidEmail(edit_email.getText().toString())) {
                    Utils.showToast(context, getResources().getString(R.string.error_valid_email));
                } else {
                    email.setText(edit_email.getText().toString());
                    updateProfileApi(savePref.getphone());
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class StartCrop extends AsyncTask<String, String, String> {
        WeakReference<EditProfileActivity> weakReference;

        public StartCrop(EditProfileActivity activity) {
            weakReference = new WeakReference<EditProfileActivity>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            startCropActivity(Uri.parse(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
//            progressDialog.setMessage("Start Cropping");
//            progressDialog.show();
        }
    }

    private void startCropActivity(Uri imageUri) {
        if (image_type == 1) {
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.OFF)
                    .setAutoZoomEnabled(true)
                    .setAllowRotation(false)
                    .setFixAspectRatio(false)
                    .start(EditProfileActivity.this);
        } else {
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.OFF)
                    .setAutoZoomEnabled(true)
                    .setAllowRotation(false)
                    .setFixAspectRatio(false)
                    .start(EditProfileActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {

                Uri uri = Uri.fromFile(mImageFile);
                StartCrop start = new StartCrop(EditProfileActivity.this);
                start.execute(uri.toString());


//                pic_path_cover = Utils.getAbsolutePath(EditProfileActivity.this, data.getData());
//                Glide.with(context).load(pic_path_cover).override(600, 600).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(cover_pic);
            } else if (requestCode == SELECT_FILE) {
                Uri uri = data.getData();
                StartCrop start = new StartCrop(EditProfileActivity.this);
                start.execute(uri.toString());
//                pic_path_user = Utils.getAbsolutePath(EditProfileActivity.this, data.getData());
//                Glide.with(context).load(pic_path_user).override(200, 200).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(user_pic);
            } else if (requestCode == com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                com.theartofdev.edmodo.cropper.CropImage.ActivityResult result = com.theartofdev.edmodo.cropper.CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri uri = result.getUri();
                    if (image_type == 1) {
//                        user_pic.setImageURI(uri);
                        pic_path_user = Utils.getAbsolutePath(EditProfileActivity.this, uri);
                        Glide.with(EditProfileActivity.this).load(new File(pic_path_user)) // Uri of the picture
                                .into(user_pic);

                    } else if (image_type == 0) {
                        pic_path_cover = Utils.getAbsolutePath(EditProfileActivity.this, uri);
                        Glide.with(EditProfileActivity.this)
                                .load(new File(pic_path_cover)) // Uri of the picture
                                .into(cover_pic);

                    }

                } else if (resultCode == com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                }
            }
        }

    }

    private void openDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle(getString(R.string.app_name));
        myAlertDialog.setCancelable(false);
        myAlertDialog.setMessage(getString(R.string.new_email));
        myAlertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Utils.setStopScheduler(context);
                        savePref.clearPreferences();
                        Intent in = new Intent(context, LoginActivity.class);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(in);
                        finish();
                        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                    }
                });
        myAlertDialog.show();
    }

    private void updateProfileApi(final String phone) {
        RequestBody requestBody = null;
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.show();
        if (!pic_path_cover.equalsIgnoreCase("")) {
            coverPicFile = new File(pic_path_cover);
            MEDIA_TYPE_COVER_PIC = pic_path_cover.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
        }
        if (!pic_path_user.equalsIgnoreCase("")) {
            userPicFile = new File(pic_path_user);
            MEDIA_TYPE_USER_PIC = pic_path_user.endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
        }

        if (!pic_path_cover.equalsIgnoreCase("") && !pic_path_user.equalsIgnoreCase("")) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.COVER_PHOTO, "cover.png", RequestBody.create(MEDIA_TYPE_COVER_PIC, coverPicFile))
                    .addFormDataPart(AllOmorniParameters.USER_IMAGE, "user.png", RequestBody.create(MEDIA_TYPE_USER_PIC, userPicFile))
                    .addFormDataPart(AllOmorniParameters.USER_ID, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken())
                    .addFormDataPart(AllOmorniParameters.mobile, phone)
                    .addFormDataPart(AllOmorniParameters.EMAIL, email.getText().toString())
                    .addFormDataPart(AllOmorniParameters.FIRST_NAME, first_name.getText().toString())
                    .addFormDataPart(AllOmorniParameters.LAST_NAME, last_name.getText().toString())
                    .build();
        } else if (!pic_path_cover.equalsIgnoreCase("")) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.COVER_PHOTO, "cover.png", RequestBody.create(MEDIA_TYPE_COVER_PIC, coverPicFile))
                    .addFormDataPart(AllOmorniParameters.USER_ID, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken())
                    .addFormDataPart(AllOmorniParameters.mobile, phone)
                    .addFormDataPart(AllOmorniParameters.EMAIL, email.getText().toString())
                    .addFormDataPart(AllOmorniParameters.FIRST_NAME, first_name.getText().toString())
                    .addFormDataPart(AllOmorniParameters.LAST_NAME, last_name.getText().toString())
                    .build();
        } else if (!pic_path_user.equalsIgnoreCase("")) {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.USER_IMAGE, "user.png", RequestBody.create(MEDIA_TYPE_USER_PIC, userPicFile))
                    .addFormDataPart(AllOmorniParameters.USER_ID, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken())
                    .addFormDataPart(AllOmorniParameters.mobile, phone)
                    .addFormDataPart(AllOmorniParameters.EMAIL, email.getText().toString())
                    .addFormDataPart(AllOmorniParameters.FIRST_NAME, first_name.getText().toString())
                    .addFormDataPart(AllOmorniParameters.LAST_NAME, last_name.getText().toString())
                    .build();
        } else {
            requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(AllOmorniParameters.USER_ID, savePref.getUserId())
                    .addFormDataPart(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken())
                    .addFormDataPart(AllOmorniParameters.mobile, phone)
                    .addFormDataPart(AllOmorniParameters.EMAIL, email.getText().toString())
                    .addFormDataPart(AllOmorniParameters.FIRST_NAME, first_name.getText().toString())
                    .addFormDataPart(AllOmorniParameters.LAST_NAME, last_name.getText().toString())
                    .build();
        }

        GetAsync mAsync = new GetAsync(this, AllOmorniApis.UPDATE_PROFILE_URL, requestBody) {
            @Override
            public void getValueParse(String result) {
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("body");
                            if (status.getString("is_email").equals("1")) {
                                logoutApi(progressDialog);
                            } else if (status.getString("is_mobile").equals("1")) {
                                Utils.showToast(context, getResources().getString(R.string.otp_sent_mobile));
                                progressDialog.dismiss();
                                savePref.setUserImage(jsonObject1.getString("user_image"));
                                savePref.setCoverImage(jsonObject1.getString("cover_photo"));
                                savePref.setFirstname(jsonObject1.getString("first_name"));
                                savePref.setLastname(jsonObject1.getString("last_name"));
                                Intent intent = new Intent(context, OtpActivityUpdateMobile.class);
                                intent.putExtra("mobile", country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()) + edit_phone.getText().toString());
                                intent.putExtra("mobile_code", country_code_text.getText().toString().substring(country_code_text.getText().toString().indexOf("+") + 1, country_code_text.getText().toString().length()));
                                intent.putExtra("mobile_no", edit_phone.getText().toString());
                                startActivity(intent);
                                finish();
                            } else {
                                progressDialog.dismiss();
                                savePref.setUserImage(jsonObject1.getString("user_image"));
                                savePref.setCoverImage(jsonObject1.getString("cover_photo"));
                                savePref.setFirstname(jsonObject1.getString("first_name"));
                                savePref.setLastname(jsonObject1.getString("last_name"));
                                Utils.showToast(context, status.getString("message"));
                                finish();
                            }

                        } else {
                            progressDialog.dismiss();
                            String is_varify = status.optString("auth_token");
                            if (is_varify.equalsIgnoreCase("0")) {
//                                Utils.setStopScheduler(context);
                                savePref.clearPreferences();
                                Intent in = new Intent(context, LoginActivity.class);
                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(in);
                                finish();
                                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                            }
                            Utils.showToast(context, status.getString("message"));
                        }
                    } catch (JSONException ex) {
                        progressDialog.dismiss();
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        progressDialog.dismiss();
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

    private void logoutApi(final ProgressDialog progressDialog) {

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(AllOmorniParameters.USER_ID, savePref.getUserId());
        formBuilder.add(AllOmorniParameters.AUTH_TOKEN, savePref.getAUthToken());

        RequestBody formBody = formBuilder.build();
        GetAsync mAsync = new GetAsync(context, AllOmorniApis.LOGOUT_URL, formBody) {
            @Override
            public void getValueParse(String result) {
                progressDialog.dismiss();
                if (result != null && !result.equalsIgnoreCase("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject status = jsonObject.getJSONObject("status");
                        if (status.getString("code").equalsIgnoreCase("1")) {
                            openDialog();
                        } else {
                            Utils.checkAuthToken(EditProfileActivity.this, status.getString("auth_token"), status.getString("message"), savePref);
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
