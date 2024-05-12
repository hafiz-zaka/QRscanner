package com.bezruk.qrcodebarcode.utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AppUtils {

    private static long backPressed = 0;
    private final int IDD_RATE_DIALOG = 0;
    AlertDialog.Builder ad;


    public static void tapToExit(Activity activity) {
        if (backPressed + 2500 > System.currentTimeMillis()) {
            activity.finish();
        } else {
            showToast(activity.getApplicationContext(), activity.getResources().getString(R.string.tapAgain));
        }
        backPressed = System.currentTimeMillis();
    }

    public static void showRateDialog(final Activity activity){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        alertDialog.setTitle(activity.getResources().getString(R.string.rate_menu_title));
        alertDialog.setMessage(activity.getResources().getString(R.string.rate_menu_message));
        //alertDialog.setIcon(R.drawable.save);

        alertDialog.setPositiveButton(activity.getResources().getString(R.string.rate_menu_yes_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here
                rateThisApp(activity);
                AppPreference.getInstance(activity.getApplicationContext()).setInteger(PrefKey.RATE_DIALOG_VALUE,1);
            }
        });

        alertDialog.setNegativeButton(activity.getResources().getString(R.string.rate_menu_no_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here
                AppPreference.getInstance(activity.getApplicationContext()).setInteger(PrefKey.RATE_DIALOG_VALUE,1);
                activity.finish();
            }
        });

        alertDialog.setNeutralButton(activity.getResources().getString(R.string.rate_menu_maybe_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed Cancel button. Write Logic Here
                activity.finish();
            }
        });

        alertDialog.show();
    }


    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static void share(Activity activity, String text) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            activity.startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareApp(Activity activity) {
        share(activity, activity.getResources().getString(R.string.share) + " https://play.google.com/store/apps/details?id=" + activity.getPackageName());
    }

    public static void rateThisApp(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vibrateDevice(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    public static void saveImage(Activity activity, String imageName, Bitmap bitmap) {
        String folderName = Constants.SAVE_TO;
        //to immediately display the saved image in the default gallery
        try {
            OutputStream fos;
            String imagePath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imagePath = Environment.DIRECTORY_PICTURES + File.separator + folderName;
                ContentResolver resolver = activity.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, imagePath);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            } else {
                imagePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString() + File.separator + folderName;
                File file = new File(imagePath);
                if (!file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdir();
                }
                File image = new File(imagePath, imageName + ".png");
                fos = new FileOutputStream(image);
            }

            boolean saved = false;
            if (fos != null) {
                saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            }


            if (saved) {
                new SingleMediaScanner(activity.getApplicationContext(), imagePath + File.separator + imageName + ".png");
                Toast.makeText(activity.getApplicationContext(),
                        activity.getString(R.string.saved_to) + " '" + Environment.DIRECTORY_PICTURES + File.separator + folderName + "'",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity.getApplicationContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(activity.getApplicationContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareImage(Activity activity, Bitmap bitmap) {
        try {
            //saving file in cache directory
            File imagesFolder = new File(activity.getCacheDir(), "images");
            if (!imagesFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                imagesFolder.mkdir();
            }
            File file = new File(imagesFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            //for avoid some problem
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            //get file uri from file provider
            Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(),
                    "com.qrcode.app.path.fileprovider", file);

            //share
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(shareIntent, "Share via"));

        } catch (Exception e) {
            Toast.makeText(activity.getApplicationContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
        }
    }


    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        showToast(context, context.getResources().getString(R.string.copied));
    }


    public static void searchInWeb(Activity activity, String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, text);
            activity.startActivity(intent);
        } catch (Exception e) {
            try {
                String escapedQuery = URLEncoder.encode(text, "UTF-8");
                Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent1);
            } catch (Exception e1) {
                Toast.makeText(activity.getApplicationContext(), "No browsers found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void executeAction(Activity activity, String finalReadyResult, String scannedResult, int type, int constant) {
        if (type == Constants.TYPE_WEB || type == Constants.TYPE_YOUTUBE) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalReadyResult)));
        } else if (type == Constants.TYPE_PHONE) {
            if (constant == Constants.TO_CALL) {
                if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                    activity.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", finalReadyResult, null)));
                }
            } else if (constant == Constants.ADD_CONTACT) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, "");
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, finalReadyResult);
                int PICK_CONTACT = 100;
                activity.startActivityForResult(intent, PICK_CONTACT);
            }

        } else if (type == Constants.TYPE_SMS) {
            if (constant == Constants.ADD_CONTACT) {
                try {
                    String number = "", message = "";
                    scannedResult = scannedResult.replace("SMSTO:", "");
                    scannedResult = scannedResult.replace("smsto:", "");
                    number = scannedResult;
                    if (scannedResult.contains(":")) {
                        String[] str = scannedResult.split(":");
                        number = str[0];
                        message = str[1];
                        //scannedResult = number + "\n" + message;
                    }
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, "");
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                    int PICK_CONTACT = 100;
                    activity.startActivityForResult(intent, PICK_CONTACT);
                } catch (Exception e) {
                    Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                }
            } else if (constant == Constants.SEND_SMS) {
                try {
                    String number = "", message = "";
                    scannedResult = scannedResult.replace("SMSTO:", "");
                    scannedResult = scannedResult.replace("smsto:", "");
                    //scannedResult = "rrrrr";
                    number = scannedResult;
                    if (scannedResult.contains(":")) {
                        String[] str = scannedResult.split(":");
                        number = str[0];
                        if (str.length > 1) {
                            for (int i = 1; i < str.length; i++) {
                                if (message.equals("")) {
                                    message = str[i];
                                } else {
                                    message = message + ":" + str[i];
                                }
                            }
                        }
                        //scannedResult = number + "\n" + message;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
                    intent.putExtra("sms_body", message);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (type == Constants.TYPE_EMAIL) {
            if (constant == Constants.SEND_EMAIL) {
                if (scannedResult.contains("mailto:") || scannedResult.contains("MAILTO:")) {
                    try {
                        scannedResult = scannedResult.replace("mailto:", "");
                        scannedResult = scannedResult.replace("MAILTO:", "");

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{scannedResult});
                        //i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
                        //i.putExtra(Intent.EXTRA_TEXT   , "body of email");
                        activity.startActivity(Intent.createChooser(i, "Send Email"));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                } else if (scannedResult.contains("matmsg:") || scannedResult.contains("MATMSG:")) {
                    try {
                        String email = "", sub = "", body = "";
                        Matcher m = Pattern.compile("to:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            email = m.group(1);
                            email = email.substring(0, email.indexOf(";"));
                            Log.d("EMAIL", email);
                        }
                        m = Pattern.compile("sub:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            sub = m.group(1);
                            sub = sub.substring(0, sub.indexOf(";"));
                        }
                        scannedResult = scannedResult.replace("\n", " ");
                        m = Pattern.compile("body:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            body = m.group(1);
                            body = body.substring(0, body.indexOf(";"));
                        }

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        i.putExtra(Intent.EXTRA_SUBJECT, sub);
                        i.putExtra(Intent.EXTRA_TEXT, body);
                        activity.startActivity(Intent.createChooser(i, "Send Email"));
                        //result = email + "\n" + sub + "\n" + body;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (type == Constants.TYPE_TEXT || type == Constants.TYPE_GEO || type == Constants.TYPE_BARCODE) {
            searchInWeb(activity, finalReadyResult);
        } else if (type == Constants.WIFI_CONNECT ) {
            try {

                String ssid = "", typeOfWIFI = "", password = "";
                    Matcher m = Pattern.compile("s:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                    while (m.find()) {
                        ssid = m.group(1);
                        ssid = ssid.substring(0, ssid.indexOf(";"));
                    }
                    m = Pattern.compile("t:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                    while (m.find()) {
                        typeOfWIFI = m.group(1);
                        typeOfWIFI = typeOfWIFI.substring(0, typeOfWIFI.indexOf(";"));
                    }
                    m = Pattern.compile("p:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                    while (m.find()) {
                        password = m.group(1);
                        password = password.substring(0, password.indexOf(";"));
                    }
                    if (typeOfWIFI.equals("")) typeOfWIFI = "nopass";

                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + ssid + "\"";

                    if (typeOfWIFI.equals("WEP")) {
                        conf.wepKeys[0] = "\"" + password + "\"";
                        conf.wepTxKeyIndex = 0;
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    } else if (typeOfWIFI.equals("WPA")) {
                        conf.preSharedKey = "\"" + password + "\"";
                    } else if (typeOfWIFI.equals("nopass")) {
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Wi-Fi protocol error", Toast.LENGTH_SHORT).show();
                    }
                    WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.addNetwork(conf);

                     WifiManager wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                     wifi.setWifiEnabled(true);

                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for( WifiConfiguration i : list ) {
                        if(i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(i.networkId, true);
                            wifiManager.reconnect();
                            Toast.makeText(activity.getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

            } catch (Exception e) {
                Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
            }
        } else if (type == Constants.TYPE_VCARD) {
            String name = "", org = "", title = "", tel = "", url = "", email = "", adr = "", note = "";
            Matcher m;
            try {
                if (constant == Constants.TO_CALL) {
                    if (scannedResult.contains("BEGIN:VCARD") || scannedResult.contains("begin:vcard")) {
                        m = Pattern.compile("TEL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            tel = m.group(1);
                        }
                    } else {
                        m = Pattern.compile("TEL:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            tel = m.group(1);
                            if (scannedResult.contains("MECARD") || scannedResult.contains("mecard")) {
                                tel = tel.substring(0, tel.indexOf(";"));
                            }
                        }
                    }

                    if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                        activity.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null)));
                    }
                } else if (constant == Constants.ADD_CONTACT) {
                    m = Pattern.compile("N:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                    while (m.find()) {
                        name = m.group(1);
                        if (scannedResult.contains("MECARD") || scannedResult.contains("mecard")) {
                            name = name.substring(0, name.indexOf(";"));
                        }
                    }
                    if (scannedResult.contains("BEGIN:VCARD") || scannedResult.contains("begin:vcard")) {
                        m = Pattern.compile("TEL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            tel = m.group(1);
                        }
                    } else {
                        m = Pattern.compile("TEL:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            tel = m.group(1);
                            tel = tel.substring(0, tel.indexOf(";"));
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, tel);
                    int PICK_CONTACT = 100;
                    activity.startActivityForResult(intent, PICK_CONTACT);
                } else if (constant == Constants.GO_URL) {
                    m = Pattern.compile("URL:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                    while (m.find()) {
                        url = m.group(1);
                        if (scannedResult.contains("MECARD") || scannedResult.contains("mecard")) {
                            url = url.substring(0, url.indexOf(";"));
                        }
                    }
                    if (url.contains("http://") || url.contains("https://")) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        url = "https://" + url;
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }

                } else if (constant == Constants.SEND_EMAIL) {
                    if (scannedResult.contains("BEGIN:VCARD") || scannedResult.contains("begin:vcard")) {
                        m = Pattern.compile("EMAIL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            email = m.group(1);
                        }
                    } else {
                        m = Pattern.compile("EMAIL:(.*)", Pattern.CASE_INSENSITIVE).matcher(scannedResult);
                        while (m.find()) {
                            email = m.group(1);
                            email = email.substring(0, email.indexOf(";"));
                        }
                    }

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    activity.startActivity(Intent.createChooser(i, "Send Email"));
                }
            } catch (Exception e) {
                Toast.makeText(activity.getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static ResultOfTypeAndValue getResourceType(String result) {
        if (result != null && (result.contains("https://youtu.be") || result.contains("https://www.youtube.com"))) {
            return new ResultOfTypeAndValue(Constants.TYPE_YOUTUBE, result);
        } else if (result != null && Patterns.WEB_URL.matcher(result).matches()) {
			if (!result.startsWith("http://") && !result.startsWith("https://")) {
                result = "http://" + result;
            }
            return new ResultOfTypeAndValue(Constants.TYPE_WEB, result);
        }  else if (result != null && (result.contains("BEGIN:VCARD") || result.contains("begin:vcard")
                || result.contains("MECARD") || result.contains("mecard"))) {
            String name = "", org = "", title = "", tel = "", tel2 = "", url = "", email = "", adr = "", birthDay = "", note = "";
            try {
                Matcher m = Pattern.compile("N:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    name = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        name = name.substring(0, name.indexOf(";"));
                    }
                }
                m = Pattern.compile("FN:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    name = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        name = name.substring(0, name.indexOf(";"));
                    }
                }
                m = Pattern.compile("ORG:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    org = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        org = org.substring(0, org.indexOf(";"));
                    }
                }
                m = Pattern.compile("TITLE:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    title = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        title = title.substring(0, title.indexOf(";"));
                    }
                }
                m = Pattern.compile("URL:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    url = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        url = url.substring(0, url.indexOf(";"));
                    }
                }
                if (result.contains("BEGIN:VCARD") || result.contains("begin:vcard")) {
                    m = Pattern.compile("TEL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        tel = tel + "\n" + m.group(1);
                    }
                    m = Pattern.compile("EMAIL.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        email = m.group(1);
                    }
                    m = Pattern.compile("ADR.*:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        adr = adr + "\n" + m.group(1);
                    }
                    if (adr.contains(";")) {
                        String[] adrArray = adr.split(";");
                        adr = "";
                        for (String a : adrArray) {
                            adr = adr + a + "\n" ;
                        }
                    }
                }
                else {
                    m = Pattern.compile("TEL:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        tel = m.group(1);
                        tel = tel.substring(0, tel.indexOf(";"));
                    }
                    m = Pattern.compile("EMAIL:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        email = m.group(1);
                        email = email.substring(0, email.indexOf(";"));

                    }
                    m = Pattern.compile("ADR:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        adr = m.group(1);
                        adr = adr.substring(0, adr.indexOf(";"));

                    }
                }

                m = Pattern.compile("BDAY:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    birthDay = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        birthDay = birthDay.substring(0, adr.indexOf(";"));
                    }
                }
                m = Pattern.compile("NOTE:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    note = m.group(1);
                    if (result.contains("MECARD") || result.contains("mecard")) {
                        note = note.substring(0, note.indexOf(";"));
                    }
                }
                result = name + "\n" + org + "\n" + title + "\n" + tel + "\n" + url + "\n" + email + "\n" + adr + "\n" + birthDay + "\n" + note;
                String[] s = result.split("\n");
                result = "";
                ArrayList<String> arr = new ArrayList<>();
                for (String i : s) {
                    if (result.equals("")) {
                        if (!i.equals("")) {
                            result = i;
                        }
                    } else {
                        if (!i.equals("")) {
                            result = result + "\n" + i;
                        }
                    }
                }
            } catch (Exception e) {
                return new ResultOfTypeAndValue(Constants.TYPE_VCARD, result);
            }
            return new ResultOfTypeAndValue(Constants.TYPE_VCARD, result);
        } else if (result != null && ((Patterns.PHONE.matcher(result).matches() ||
                result.contains("tel:") || Patterns.PHONE.matcher(result).matches() ||
                result.contains("TEL:"))) && !result.contains("barcode:")) {
            try {
                result = result.replace("tel:", "");
                result = result.replace("TEL:", "");
            } catch (Exception e) {
                return new ResultOfTypeAndValue(Constants.TYPE_PHONE, result);
            }
            return new ResultOfTypeAndValue(Constants.TYPE_PHONE, result);
        } else if (result != null && (Patterns.EMAIL_ADDRESS.matcher(result).matches() ||
                result.contains("mailto:") || result.contains("MAILTO:") ||
                result.contains("matmsg:") || result.contains("MATMSG:"))) {
            if (result.contains("mailto:") || result.contains("MAILTO:")) {
                try {
                    result = result.replace("mailto:", "");
                    result = result.replace("MAILTO:", "");
                } catch (Exception e) {
                    return new ResultOfTypeAndValue(Constants.TYPE_EMAIL, result);
                }
                return new ResultOfTypeAndValue(Constants.TYPE_EMAIL, result);
            } else if (result.contains("matmsg:") || result.contains("MATMSG:")) {
                try {
                    String email = "", sub = "", body = "";
                    Matcher m = Pattern.compile("to:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        email = m.group(1);
                        email = email.substring(0, email.indexOf(";"));
                        Log.d("EMAIL", email);
                    }
                    m = Pattern.compile("sub:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        sub = m.group(1);
                        sub = sub.substring(0, sub.indexOf(";"));
                    }
                    result = result.replace("\n", " ");
                    m = Pattern.compile("body:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                    while (m.find()) {
                        body = m.group(1);
                        body = body.substring(0, body.indexOf(";"));
                    }
                    result = email + "\n" + sub + "\n" + body;
                } catch (Exception e) {
                    return new ResultOfTypeAndValue(Constants.TYPE_EMAIL, result);
                }
                return new ResultOfTypeAndValue(Constants.TYPE_EMAIL, result);
            } else {
                return new ResultOfTypeAndValue(Constants.TYPE_EMAIL, result);
            }

        } else if (result != null && result.contains("barcode:")) {
            result = result.replace("barcode:", "");
            return new ResultOfTypeAndValue(Constants.TYPE_BARCODE, result);
        } else if (result != null && (result.contains("WIFI:") || result.contains("wifi:"))) {
            try {
                String ssid = "", type = "", password = "";
                Matcher m = Pattern.compile("s:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    ssid = m.group(1);
                    ssid = ssid.substring(0, ssid.indexOf(";"));
                }
                m = Pattern.compile("t:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    type = m.group(1);
                    type = type.substring(0, type.indexOf(";"));
                }
                m = Pattern.compile("p:(.*)", Pattern.CASE_INSENSITIVE).matcher(result);
                while (m.find()) {
                    password = m.group(1);
                    password = password.substring(0, password.indexOf(";"));
                }
                if (type.equals("")) type = "nopass";

                result = ssid + "\n" + type + "\n" + password;
            } catch (Exception e) {
                return new ResultOfTypeAndValue(Constants.TYPE_WIFI, result);
            }
            return new ResultOfTypeAndValue(Constants.TYPE_WIFI, result);
        } else if (result != null && (result.contains("SMSTO:") || result.contains("smsto:"))) {
            try {
                String number = "", message = "";
                result = result.replace("SMSTO:", "");
                result = result.replace("smsto:", "");
                number = result;
                if (result.contains(":")) {
                    String[] str = result.split(":");
                    number = str[0];
                    if (str.length > 1) {
                        for (int i = 1; i < str.length; i++) {
                            if (message.equals("")) {
                                message = str[i];
                            } else {
                                message = message + ":" + str[i];
                            }
                        }
                    }
                    result = number + "\n" + message;
                }
            } catch (Exception e) {
                return new ResultOfTypeAndValue(Constants.TYPE_SMS, result);
            }
            return new ResultOfTypeAndValue(Constants.TYPE_SMS, result);
        } else if (result != null && (result.contains("GEO:") || result.contains("geo:"))) {
            try {
                result = result.replace("GEO:", "");
                result = result.replace("geo:", "");
            } catch (Exception e) {
                return new ResultOfTypeAndValue(Constants.TYPE_GEO, result);
            }
            return new ResultOfTypeAndValue(Constants.TYPE_GEO, result);
        } else {
            return new ResultOfTypeAndValue(Constants.TYPE_TEXT, result);
        }

    }


}


