package com.bezruk.qrcodebarcode.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;
import com.bezruk.qrcodebarcode.utility.AdManager;
import com.bezruk.qrcodebarcode.utility.AppUtils;
import com.bezruk.qrcodebarcode.utility.CodeGenerator;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GenerateFragment extends Fragment {

    private Activity mActivity;
    private Context mContext;

    private EditText inputText, inputText2, inputText3, inputText4;
    private ImageView outputBitmap;
    RelativeLayout inputLayout, inputLayout2, inputLayout3, inputLayout4, inputLayout5;
    private ImageView text_btn, contact_btn, url_btn, email_btn, wifi_btn, barcode_btn, sms_btn, contact_choose_btn;
    private TextView generate_btn;
    private ImageView save, share, color;

    private static final int TYPE_QR = 0, TYPE_BAR = 1;
    private static int TYPE = TYPE_QR;
    static final int PICK_CONTACT = 1;

    private static final int TEXT = 0, CONTACT = 1, URL = 2, EMAIL = 3, WIFI = 4, BARCODE = 5, SMS = 6;
    private static int CURRENT_BTN = TEXT;
    private int generate_action = 0;

    private Bitmap output;
    private String inputStr;
    private Spinner wifiType;
    ColorPicker cp1;
    Button btnColor;
    String input;
    boolean shouldShare = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_generate, container, false);


        initView(rootView);
        initFunctionality();
        initListener();

        return rootView;
    }

    private void initVar() {
        mActivity = getActivity();
        if (mActivity != null) {
            mContext = mActivity.getApplicationContext();
        }

        //full ads load
    }

    private void initView(View rootView) {
        inputText = (EditText) rootView.findViewById(R.id.inputText);
        inputText2 = (EditText) rootView.findViewById(R.id.inputText2);
        inputText3 = (EditText) rootView.findViewById(R.id.inputText3);
        inputText4 = (EditText) rootView.findViewById(R.id.inputText4);
        inputLayout = (RelativeLayout) rootView.findViewById(R.id.inputLayout);
        inputLayout2 = (RelativeLayout) rootView.findViewById(R.id.inputLayout2);
        inputLayout3 = (RelativeLayout) rootView.findViewById(R.id.inputLayout3);
        inputLayout4 = (RelativeLayout) rootView.findViewById(R.id.inputLayout4);
        inputLayout5 = (RelativeLayout) rootView.findViewById(R.id.inputLayout5);
        outputBitmap = (ImageView) rootView.findViewById(R.id.outputBitmap);
        contact_choose_btn = (ImageButton) rootView.findViewById(R.id.contact_choose_btn);
        text_btn = (ImageView) rootView.findViewById(R.id.text_btn);
        contact_btn = (ImageView) rootView.findViewById(R.id.contact_btn);
        url_btn = (ImageView) rootView.findViewById(R.id.url_btn);
        email_btn = (ImageView) rootView.findViewById(R.id.email_btn);
        wifi_btn = (ImageView) rootView.findViewById(R.id.wifi_btn);
        barcode_btn = (ImageView) rootView.findViewById(R.id.barcode_btn);
        sms_btn = (ImageView) rootView.findViewById(R.id.sms_btn);
        save = (ImageView) rootView.findViewById(R.id.save);
        share = (ImageView) rootView.findViewById(R.id.share);
        color = (ImageView) rootView.findViewById(R.id.color);
        generate_btn = (TextView) rootView.findViewById(R.id.generate_btn);
        wifiType = (Spinner) rootView.findViewById(R.id.wifi_type);
        cp1 = new ColorPicker(mActivity, 0, 0, 0);
      //  btnColor = cp1.findViewById(R.id.okColorButton);

        save.setVisibility(View.GONE);
        share.setVisibility(View.GONE);
        color.setVisibility(View.GONE);
        contact_choose_btn.setVisibility(View.INVISIBLE);

    }

    private void initFunctionality() {


    }

    private void initListener() {

        generate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (generate_action == 1) {
                    CodeGenerator.setBLACK(Color.BLACK);
                    input = inputText.getText().toString();
                    switch (CURRENT_BTN) {
                        case TEXT:
                            generateCode(input);
                            break;
                        case CONTACT:
                            input = "BEGIN:VCARD\n" +
                                    "VERSION:3.0\n" +
                                    "N:" + inputText.getText().toString() + "\n" +
                                    "TEL:" + inputText2.getText().toString() + "\n" +
                                    "EMAIL:" + inputText3.getText().toString() + "\n" +
                                    "ADR:" + inputText4.getText().toString() + "\n" +
                                    "END:VCARD";
                            generateCode(input);
                            break;
                        case URL:
                            if (input.contains("https://") || input.contains("http://")) {
                                generateCode(input);
                            } else {
                                input = "http://" + input;
                                generateCode(input);
                            }
                            break;
                        case EMAIL:
                            input = "MATMSG:TO:" + inputText.getText().toString() +
                                    ";SUB:" + inputText2.getText().toString() +
                                    ";BODY:" + inputText3.getText().toString() + ";;";
                            generateCode(input);
                            break;

                        case WIFI:
                            String type;
                            if (wifiType.getSelectedItemPosition() == 0) type = "WPA";
                            else if (wifiType.getSelectedItemPosition() == 1) type = "WEP";
                            else type = "";

                            if (type.equals("")) {
                                input = "WIFI:S:" + inputText.getText().toString() +
                                        ";P:" + inputText2.getText().toString() + ";;";
                                generateCode(input);
                            } else {
                                input = "WIFI:S:" + inputText.getText().toString() +
                                        ";T:" + type +
                                        ";P:" + inputText2.getText().toString() + ";;";
                                generateCode(input);
                            }
                            break;
                        case BARCODE:
                            generateCode(input);
                            input = "barcode:" + input;
                            break;
                        case SMS:
                            input = "SMSTO:" + inputText.getText().toString() +
                                    ":" + inputText2.getText().toString();
                            generateCode(input);
                            break;
                    }

                    Log.d("GENNN", input);
                    //Saving results
                    ArrayList<String> previousResult = AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_CREATED);
                    previousResult.add(input);
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_CREATED, previousResult);

                    //save date of generation
                    String currentDate;
                    if (Locale.getDefault().equals(Locale.US)) {
                        currentDate = new SimpleDateFormat("MM.dd.yyyy HH:mm").format(Calendar.getInstance().getTime());
                    } else {
                        currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(Calendar.getInstance().getTime());
                    }
                    ArrayList<String> previousDate = AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_CREATED);
                    previousDate.add(currentDate);
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_CREATED, previousDate);

                    //saving color (standart black)
                    ArrayList<String> previousColor = AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_CREATED);
                    previousColor.add(Integer.toString(Color.BLACK));
                    AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_CREATED, previousColor);
                }

            }
        });

        wifiType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        inputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    generate_btn.setBackgroundResource(R.color.colorPrimary);
                    generate_action = 1;
                } else {
                    generate_btn.setBackgroundResource(R.color.grey);
                    generate_action = 0;
                    save.setVisibility(View.GONE);
                    share.setVisibility(View.GONE);
                    color.setVisibility(View.GONE);
                }

                /*if (s.length() != 0) {
                    generateCode(s.toString());
                } else {
                    save.hide();
                    share.hide();
                    if (TYPE == TYPE_QR) {
                        outputBitmap.setImageResource(R.drawable.ic_qr_placeholder);
                    } else {
                        outputBitmap.setImageResource(R.drawable.ic_bar_placeholder);
                    }
                }*/
            }
        });

        text_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = TEXT;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                ;
                contact_choose_btn.setVisibility(View.INVISIBLE);
                inputText.setHint(R.string.type_here_text);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight()*3);
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 0, 30, 0);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 0, 30, 0);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);


                outputBitmap.setImageResource(R.drawable.qr_bg1);
                text_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });

        contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = CONTACT;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.VISIBLE);
                inputText.setHint(R.string.type_here_name);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT);
                inputText2.setHint(R.string.type_here_phone);
                inputText2.setInputType(InputType.TYPE_CLASS_PHONE);
                inputText3.setHint(R.string.type_here_address);
                inputText3.setInputType(InputType.TYPE_CLASS_TEXT);
                inputText4.setHint(R.string.type_here_email);
                inputText4.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 10, 30, 10);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 10, 30, 10);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 10, 30, 10);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg7);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });

        url_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = URL;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.INVISIBLE);
                inputText.setHint(R.string.type_here_url);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 0, 30, 0);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 0, 30, 0);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg3);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });
        email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = EMAIL;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.INVISIBLE);
                inputText.setHint(R.string.type_here_email);
                inputText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                inputText2.setHint(R.string.type_here_email_subject);
                inputText2.setInputType(InputType.TYPE_CLASS_TEXT);
                inputText3.setHint(R.string.type_here_text);
                inputText3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 10, 30, 10);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight() * 3);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 10, 30, 10);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg4);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });
        wifi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = WIFI;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.INVISIBLE);
                inputText.setHint(R.string.type_here_wifi_ssid);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT);
                inputText2.setHint(R.string.type_here_wifi_password);
                inputText2.setInputType(InputType.TYPE_CLASS_TEXT);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 10, 30, 10);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 0, 30, 0);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 10, 30, 10);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg5);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });
        barcode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = BARCODE;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.INVISIBLE);
                inputText.setHint(R.string.type_here_barcode);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 0, 30, 0);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 0, 30, 0);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg6);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.grey));
            }
        });
        sms_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CURRENT_BTN = SMS;
                inputText.setText("");
                inputText2.setText("");
                inputText3.setText("");
                inputText4.setText("");
                save.setVisibility(View.GONE);
                share.setVisibility(View.GONE);
                color.setVisibility(View.GONE);
                contact_choose_btn.setVisibility(View.VISIBLE);
                inputText.setHint(R.string.type_here_sms_phone_number);
                inputText.setInputType(InputType.TYPE_CLASS_PHONE);
                inputText2.setHint(R.string.type_here_sms_text);
                inputText2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                RelativeLayout.LayoutParams params;
                /*params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight());
                params.setMargins(30, 20, 30, 10);
                inputLayout.setLayoutParams(params);*/
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, inputLayout.getHeight() * 3);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout);
                params.setMargins(30, 10, 30, 10);
                inputLayout2.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout2);
                params.setMargins(30, 0, 30, 0);
                inputLayout3.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout3);
                params.setMargins(30, 0, 30, 0);
                inputLayout4.setLayoutParams(params);
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.BELOW, R.id.inputLayout4);
                params.setMargins(30, 0, 30, 0);
                inputLayout5.setLayoutParams(params);

                outputBitmap.setImageResource(R.drawable.qr_bg7);
                text_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                contact_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                url_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                email_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                wifi_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                barcode_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                sms_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        contact_choose_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkReadContactsPermission();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShare = false;

                saveAndShare(shouldShare, inputStr, output);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldShare = true;
                saveAndShare(shouldShare, inputStr, output);
            }
        });


        color.setOnClickListener(new View.OnClickListener() {
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
                if (input.contains("barcode:")) {
                    inputeForColor = input.replace("barcode:", "");
                } else {
                    inputeForColor = input;
                }
                generateCode(inputeForColor);
                // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                cp1.dismiss();
            }
        });

    }

    private void generateCode(final String input) {
        CodeGenerator codeGenerator = new CodeGenerator();
        if (CURRENT_BTN == BARCODE) {
            codeGenerator.generateBarFor(input);
        } else {
            codeGenerator.generateQRFor(input);
        }
        codeGenerator.setResultListener(new CodeGenerator.ResultListener() {
            @Override
            public void onResult(Bitmap bitmap) {
                //((BitmapDrawable)outputBitmap.getDrawable()).getBitmap().recycle();
                output = bitmap;
                inputStr = input;
                outputBitmap.setImageBitmap(bitmap);

                if (!save.isShown()) {
                    save.setVisibility(View.VISIBLE);
                    share.setVisibility(View.VISIBLE);
                    color.setVisibility(View.VISIBLE);
                }
            }
        });
        codeGenerator.execute();
    }


    //for select of contact
    @SuppressLint("Range")
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri contactData = data.getData();
                    Cursor c = null;
                    if (contactData != null) {
                        c = mActivity.getContentResolver().query(contactData, null, null, null, null);
                        if (c != null && c.moveToFirst()) {


                            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                            if (hasPhone.equalsIgnoreCase("1")) {
                                Cursor phones = mActivity.getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                        null, null);
                                if (phones != null) {
                                    phones.moveToFirst();
                                    String cNumber = phones.getString(phones.getColumnIndex("data1"));
                                    //AppUtils.showToast(mContext, cNumber);

                                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                                    if (CURRENT_BTN == CONTACT) {
                                        inputText.setText(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                                        inputText2.setText(cNumber);
                                    } else if (CURRENT_BTN == SMS) {
                                        inputText.setText(cNumber);
                                    }
                                }
                                if (phones != null) {
                                    phones.close();
                                }

                            }

                        }
                        if (c != null) {
                            c.close();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }



            }
        }
    }


    private void saveAndShare(final boolean shouldShare, final String input, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (shouldShare) {
                AppUtils.shareImage(mActivity, bitmap);
            } else {
                AppUtils.saveImage(mActivity, input, bitmap);
            }
        }else {
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

    private boolean checkReadContactsPermission() {
        if ((ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_CONTACTS},
                    Constants.PERMISSION_REQ);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == Constants.PERMISSION_REQ) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        saveAndShare(shouldShare, inputStr, output);
                    } else {
                        AppUtils.showToast(mContext, getString(R.string.permission_not_granted));
                    }
                } else if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT);
                    } else {
                        AppUtils.showToast(mContext, getString(R.string.permission_not_granted));
                    }
                }
            }
        }
    }

}
