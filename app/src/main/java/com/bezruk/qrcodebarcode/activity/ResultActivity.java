package com.bezruk.qrcodebarcode.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;
import com.bezruk.qrcodebarcode.utility.AdManager;
import com.bezruk.qrcodebarcode.utility.AppUtils;
import com.bezruk.qrcodebarcode.utility.CodeGenerator;
import com.bezruk.qrcodebarcode.utility.ResultOfTypeAndValue;
import com.google.android.gms.ads.AdView;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ResultActivity extends AppCompatActivity {

    private Activity mActivity;
    private Context mContext;

    private TextView result, actionText, scannedCodeType, tileOfResult;
    private ImageView actionIcon, resultIcon;

    //private LinearLayout buttonCopy, buttonSearch, buttonShare, buttonAction;
    private ImageView codeColorBtn, codeSaveBtn, codeShareBtn, resultCopyBtn,
            resultShareBtn, resultActionBtn1, resultActionBtn2, resultActionBtn3, resultActionBtn4;
    ImageView resultQrCodeImage;
    ArrayList<String> arrayList, colorList;
    String lastResult;
    int colorResult;
    int resultForFragment = 0;
    int positionForHistoryFragment = 0;
    ResultOfTypeAndValue resultValues;
    ColorPicker cp1;
    Button btnColor;
    private Bitmap output;
    private int action_btn1, action_btn2, action_btn3, action_btn4;
    boolean shouldShare = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVars();
        initViews();
        initFunctionality();
        initListeners();
    }

    private void initVars() {
        mActivity = ResultActivity.this;
        mContext = mActivity.getApplicationContext();

        //full ads load
        AdManager.getInstance(mContext).loadFullScreenAd(mActivity,getString(R.string.interstitial_ad_unit_id));
    }

    private void initViews() {
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        result = (TextView) findViewById(R.id.result);
        //actionText = (TextView) findViewById(R.id.actionText);
        scannedCodeType = (TextView) findViewById(R.id.scanned_result_type_of_code);
        tileOfResult = (TextView) findViewById(R.id.scanned_result_tile);
        actionIcon = (ImageView) findViewById(R.id.actionIcon);
        resultIcon = (ImageView) findViewById(R.id.resultIcon);

       /* buttonCopy = (LinearLayout) findViewById(R.id.buttonCopy);
        buttonSearch = (LinearLayout) findViewById(R.id.buttonSearch);
        buttonShare = (LinearLayout) findViewById(R.id.buttonShare);
        buttonAction = (LinearLayout) findViewById(R.id.buttonAction);*/

        codeColorBtn = (ImageView) findViewById(R.id.color_of_result_qrcode_btn);
        codeSaveBtn = (ImageView) findViewById(R.id.save_of_result_qrcode_btn);
        codeShareBtn = (ImageView) findViewById(R.id.share_of_result_qrcode_btn);

        resultCopyBtn = (ImageView) findViewById(R.id.copy_result_btn);
        resultShareBtn = (ImageView) findViewById(R.id.share_result_btn);
        resultActionBtn1 = (ImageView) findViewById(R.id.action1_result_btn);
        resultActionBtn2 = (ImageView) findViewById(R.id.action2_result_btn);
        resultActionBtn3 = (ImageView) findViewById(R.id.action3_result_btn);
        resultActionBtn4 = (ImageView) findViewById(R.id.action4_result_btn);

        resultQrCodeImage = (ImageView) findViewById(R.id.result_qr_code_img);

        cp1 = new ColorPicker(mActivity, 0, 0, 0);
       // btnColor = cp1.findViewById(R.id.okColorButton);
    }

    private void initFunctionality() {

        if (getSupportActionBar()!=null){

            getSupportActionBar().setTitle(getString(R.string.result));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        Bundle b = getIntent().getExtras();

        if (b != null) {
            resultForFragment = b.getInt("key");
            positionForHistoryFragment = b.getInt("position");
        }

        arrayList = AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED);
        if (resultForFragment == Constants.HISTORY_SCAN_FRAGMENT) {
            //setResult
            arrayList = AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED);
            Collections.reverse(arrayList);
            lastResult = arrayList.get(positionForHistoryFragment);
            //setColor
            colorList = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED);
            Collections.reverse(colorList);
            colorResult = Integer.parseInt(colorList.get(positionForHistoryFragment));
        } else if (resultForFragment == Constants.HISTORY_GENERATE_FRAGMENT) {
            //setResult
            arrayList = AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_CREATED);
            Collections.reverse(arrayList);
            lastResult = arrayList.get(positionForHistoryFragment);
            //setColor
            colorList = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_CREATED);
            Collections.reverse(colorList);
            colorResult = Integer.parseInt(colorList.get(positionForHistoryFragment));
        } else if (resultForFragment == Constants.SCAN_FRAGMENT) {
            //setResult
            arrayList = AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED);
            lastResult = arrayList.get(arrayList.size() - 1);
            //setColor
            colorList = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED);
            Collections.reverse(colorList);
            if (!colorList.isEmpty() && !colorList.get(colorList.size() - 1).isEmpty()) {
                colorResult = Integer.parseInt(colorList.get(colorList.size() - 1));
            } else {
                colorResult=Integer.parseInt(Integer.toString(Color.BLACK));
                // Handle the case when the colorList is empty or the last element is an empty string
            }
        }



        // TODO Sample banner Ad implementation
        AdManager.getInstance(mContext).showBannerAd((AdView) findViewById(R.id.adViewResult));

        resultValues = AppUtils.getResourceType(lastResult);
        String finalResult = resultValues.getValue();
        CodeGenerator.setBLACK(colorResult);
        //int type = AppUtils.getResourceType(lastResult);
        if (resultValues.getType() == Constants.TYPE_TEXT) {
            //buttonAction.setVisibility(View.GONE);
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_text);
            action_btn1 = Constants.SEARCH_IN_WEB;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_search_web_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_WEB) {
            //actionIcon.setImageResource(R.drawable.ic_web);
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_url);
            resultIcon.setImageResource(R.drawable.ic_web);
            //actionText.setText(getString(R.string.action_visit));
            action_btn1 = Constants.GO_URL;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_web_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_YOUTUBE) {
            //actionIcon.setImageResource(R.drawable.ic_video);
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_youtube);
            resultIcon.setImageResource(R.drawable.ic_video);
            //actionText.setText(getString(R.string.action_youtube));
            action_btn1 = Constants.GO_URL;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_web_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_PHONE) {
            // actionIcon.setImageResource(R.drawable.ic_call);
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_phone);
            resultIcon.setImageResource(R.drawable.ic_call);
            //actionText.setText(getString(R.string.action_call));
            action_btn1 = Constants.TO_CALL;
            action_btn2 = Constants.ADD_CONTACT;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_call_white);
            resultActionBtn2.setVisibility(View.VISIBLE);
            resultActionBtn2.setImageResource(R.drawable.ic_contact_white);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_EMAIL) {
            //actionIcon.setImageResource(R.drawable.ic_email);
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_email);
            resultIcon.setImageResource(R.drawable.ic_email);
            //actionText.setText(getString(R.string.action_email));
            action_btn1 = Constants.SEND_EMAIL;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_email_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_BARCODE) {
            scannedCodeType.setText(R.string.scanned_type_barcode);
            tileOfResult.setText(R.string.result_barcode);
            resultIcon.setImageResource(R.drawable.ic_barcode);
            //buttonAction.setVisibility(View.GONE);
            action_btn1 = Constants.SEARCH_IN_WEB;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_search_web_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_WIFI) {
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_wifi);
            resultIcon.setImageResource(R.drawable.ic_wifi);
            // buttonAction.setVisibility(View.GONE);
            action_btn1 = Constants.WIFI_CONNECT;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_wifi_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_SMS) {
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_sms);
            resultIcon.setImageResource(R.drawable.ic_sms);
            //buttonAction.setVisibility(View.GONE);
            action_btn1 = Constants.ADD_CONTACT;
            action_btn2 = Constants.SEND_SMS;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_contact_white);
            resultActionBtn2.setVisibility(View.VISIBLE);
            resultActionBtn2.setImageResource(R.drawable.ic_sms_white);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        } else if (resultValues.getType() == Constants.TYPE_VCARD) {
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_vcard);
            resultIcon.setImageResource(R.drawable.ic_contact);
            // buttonAction.setVisibility(View.GONE);
            resultActionBtn1.setVisibility(View.GONE);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
            String name = "", org = "", title = "", tel = "", url = "", email = "", adr = "", note = "";
            Matcher m;
            if (lastResult.contains("BEGIN:VCARD") || lastResult.contains("begin:vcard")) {
                m = Pattern.compile("TEL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
                while (m.find()) {
                    tel = tel + "\n" + m.group(1);
                    action_btn1 = Constants.TO_CALL;
                    action_btn2 = Constants.ADD_CONTACT;
                    resultActionBtn1.setVisibility(View.VISIBLE);
                    resultActionBtn1.setImageResource(R.drawable.ic_call_white);
                    resultActionBtn2.setVisibility(View.VISIBLE);
                    resultActionBtn2.setImageResource(R.drawable.ic_contact_white);
                }
                Log.d("TEEEE", tel);
                m = Pattern.compile("EMAIL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
                while (m.find()) {
                    email = m.group(1);
                    if (email != null && !email.equals("")) {
                        action_btn4 = Constants.SEND_EMAIL;
                        resultActionBtn4.setVisibility(View.VISIBLE);
                        resultActionBtn4.setImageResource(R.drawable.ic_email_white);
                    }
                }
            } else {
                m = Pattern.compile("TEL:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
                while (m.find()) {
                    tel = m.group(1);
                    if (tel != null) {
                        tel = tel.substring(0, tel.indexOf(";"));
                        if (!tel.equals("")) {
                            action_btn1 = Constants.TO_CALL;
                            action_btn2 = Constants.ADD_CONTACT;
                            resultActionBtn1.setVisibility(View.VISIBLE);
                            resultActionBtn1.setImageResource(R.drawable.ic_call_white);
                            resultActionBtn2.setVisibility(View.VISIBLE);
                            resultActionBtn2.setImageResource(R.drawable.ic_contact_white);
                        }
                    }

                }
                m = Pattern.compile("EMAIL:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
                while (m.find()) {
                    email = m.group(1);
                    if (email != null) {
                        email = email.substring(0, email.indexOf(";"));
                        if (!email.equals("")) {
                            action_btn4 = Constants.SEND_EMAIL;
                            resultActionBtn4.setVisibility(View.VISIBLE);
                            resultActionBtn4.setImageResource(R.drawable.ic_email_white);
                        }
                    }


                }
            }
            m = Pattern.compile("URL:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
            while (m.find()) {
                url = m.group(1);
                if (lastResult.contains("MECARD") || lastResult.contains("mecard")) {
                    if (url != null) {
                        url = url.substring(0, url.indexOf(";"));
                    }
                }
                if (url != null && !url.equals("")) {
                    action_btn3 = Constants.GO_URL;
                    resultActionBtn3.setVisibility(View.VISIBLE);
                    resultActionBtn3.setImageResource(R.drawable.ic_web_white);
                }
            }


           /* m = Pattern.compile("TEL:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
            while (m.find()) {
                tel = m.group(1);
                if (lastResult.contains("MECARD") || lastResult.contains("mecard")) {
                    tel = tel.substring(0, tel.indexOf(";"));
                }
                if (!tel.equals("")) {
                    action_btn1 = Constants.TO_CALL;
                    action_btn2 = Constants.ADD_CONTACT;
                    resultActionBtn1.setVisibility(View.VISIBLE);
                    resultActionBtn1.setImageResource(R.drawable.ic_call_white);
                    resultActionBtn2.setVisibility(View.VISIBLE);
                    resultActionBtn2.setImageResource(R.drawable.ic_contact_white);
                }
            }
            m = Pattern.compile("EMAIL:(.*)", Pattern.CASE_INSENSITIVE).matcher(lastResult);
            while (m.find()) {
                email = m.group(1);
                if (lastResult.contains("MECARD") || lastResult.contains("mecard")) {
                    email = email.substring(0, email.indexOf(";"));
                }
                if (!email.equals("")) {
                    action_btn4 = Constants.SEND_EMAIL;
                    resultActionBtn4.setVisibility(View.VISIBLE);
                    resultActionBtn4.setImageResource(R.drawable.ic_email_white);
                }
            }*/

        } else if (resultValues.getType() == Constants.TYPE_GEO) {
            scannedCodeType.setText(R.string.scanned_type_qrcode);
            tileOfResult.setText(R.string.result_geo);
            resultIcon.setImageResource(R.drawable.ic_location);
            //buttonAction.setVisibility(View.GONE);
            action_btn1 = Constants.SEARCH_IN_WEB;
            resultActionBtn1.setVisibility(View.VISIBLE);
            resultActionBtn1.setImageResource(R.drawable.ic_search_web_white);
            resultActionBtn2.setVisibility(View.GONE);
            resultActionBtn3.setVisibility(View.GONE);
            resultActionBtn4.setVisibility(View.GONE);
        }

        if (lastResult.contains("barcode:")) {
            lastResult = lastResult.replace("barcode:", "");
        }
        generateCode(lastResult);//for qr/barcode image
        result.setText(finalResult);
        result.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initListeners() {

        codeColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cp1.show();
                } catch (Exception e) {
                    AppUtils.showToast(mContext, "An unexpected error has occurred");
                }

            }
        });

        cp1.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                CodeGenerator.setBLACK(cp1.getColor());
                String inputeForColor;
                if (lastResult.contains("barcode:")) {
                    inputeForColor = lastResult.replace("barcode:", "");
                } else {
                    inputeForColor = lastResult;
                }
                generateCode(lastResult);
                //saving color
                if (resultForFragment == Constants.HISTORY_SCAN_FRAGMENT) {
                    ArrayList<String> previousColor = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED);
                    Collections.reverse(previousColor);
                    previousColor.set(positionForHistoryFragment, Integer.toString(cp1.getColor()));
                    Collections.reverse(previousColor);
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_SCANNED, previousColor);

                } else if (resultForFragment == Constants.HISTORY_GENERATE_FRAGMENT) {
                    ArrayList<String> previousColor = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_CREATED);
                    Collections.reverse(previousColor);
                    previousColor.set(positionForHistoryFragment, Integer.toString(cp1.getColor()));
                    Collections.reverse(previousColor);
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_CREATED, previousColor);

                } else if (resultForFragment == Constants.SCAN_FRAGMENT) {
                    ArrayList<String> previousColor = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED);
                    Collections.reverse(previousColor);
                    previousColor.set((colorList.size() - 1), Integer.toString(cp1.getColor()));
                    Collections.reverse(previousColor);
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_SCANNED, previousColor);
                }
                // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                cp1.dismiss();
            }
        });

        codeSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShare = false;
                saveAndShare(shouldShare, lastResult, output);
            }
        });

        codeShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShare = true;
                saveAndShare(shouldShare, lastResult, output);
            }
        });

        resultCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.copyToClipboard(mContext, result.getText().toString());
            }
        });

        resultShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.share(mActivity, result.getText().toString());
            }
        });

        resultActionBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ResultOfTypeAndValue resultValues = AppUtils.getResourceType(lastResult);
                AppUtils.executeAction(mActivity, result.getText().toString(), lastResult, resultValues.getType(), action_btn1);
            }
        });

        resultActionBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ResultOfTypeAndValue resultValues = AppUtils.getResourceType(lastResult);
                AppUtils.executeAction(mActivity, result.getText().toString(), lastResult, resultValues.getType(), action_btn2);
            }
        });

        resultActionBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ResultOfTypeAndValue resultValues = AppUtils.getResourceType(lastResult);
                AppUtils.executeAction(mActivity, result.getText().toString(), lastResult, resultValues.getType(), action_btn3);
            }
        });

        resultActionBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ResultOfTypeAndValue resultValues = AppUtils.getResourceType(lastResult);
                AppUtils.executeAction(mActivity, result.getText().toString(), lastResult, resultValues.getType(), action_btn4);
            }
        });


        /*buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.copyToClipboard(mContext, result.getText().toString());
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.searchInWeb(mActivity, result.getText().toString());
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.share(mActivity, result.getText().toString());
            }
        });

        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultOfTypeAndValue resultValues = AppUtils.getResourceType(lastResult);
                AppUtils.executeAction(mActivity, result.getText().toString(), resultValues.getType());
            }
        });*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void generateCode(final String input) {
        CodeGenerator codeGenerator = new CodeGenerator();
        if (resultValues.getType() == Constants.TYPE_BARCODE) {
            codeGenerator.generateBarFor(input);
        } else {
            codeGenerator.generateQRFor(input);
        }
        codeGenerator.setResultListener(new CodeGenerator.ResultListener() {
            @Override
            public void onResult(Bitmap bitmap) {
                //((BitmapDrawable)outputBitmap.getDrawable()).getBitmap().recycle();
                output = bitmap;
                resultQrCodeImage.setImageBitmap(bitmap);
            }
        });
        codeGenerator.execute();
    }

    private void saveAndShare(final boolean shouldShare, final String input, Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (shouldShare) {
                AppUtils.shareImage(mActivity, bitmap);
            } else {
                AppUtils.saveImage(mActivity, input, bitmap);
            }
        }else{
            if (checkWritePermission()) {
                if (shouldShare) {
                    AppUtils.shareImage(mActivity, bitmap);
                } else {
                    AppUtils.saveImage(mActivity, input, bitmap);
                }
            }
        }

    }

    private boolean checkWritePermission() {
        if ((ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.PERMISSION_REQ);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == Constants.PERMISSION_REQ) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        saveAndShare(shouldShare, lastResult, output);
                    } else {
                        AppUtils.showToast(mContext, getString(R.string.permission_not_granted));
                    }
                }
            }
        }
    }
}

