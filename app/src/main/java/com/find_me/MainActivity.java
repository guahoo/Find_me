package com.find_me;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vistrav.ask.Ask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.provider.Telephony.Carriers.NAME;
import static com.find_me.GeoLocationFinder.TURONLOCATION;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final String SEND = "Сообщение отправлено";
    private static final String SMS_FAILED = "Сообщение не отправлено, попробуйте позже";
    private static final String COPYCOORDS = "Координаты скопированы";
    private static final String FINDME = "Найди меня! Широта: N %s, Долгота: E %s";
    private static final String PREFERENCES="PREFERENCES";
    private static final String PHONE_NUMBER = "phone" ;
    private static final String NUMBER_ADD = "Добавлен номер: ";
    private static final String CHOOSEPHONENUMBER = "Пожалуйста, выберите номер получателя";
    SharedPreferences sharedPreferences;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    static final int LOCATION_SETTINGS_REQUEST = 1;
    public static TextView xTextView, yTextView;



    public static void setLatitude(String latitude) {
        MainActivity.latitude = latitude;
    }

    public static void setLongitude(String longitude) {
        MainActivity.longitude = longitude;
    }

    static String latitude, longitude;
    protected ImageButton xCopingImageButton, chooseContactButton;
    Button smsSendButton;

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    private String phoneNo;
    private String message;
    GeoLocationFinder geoLocationFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        smsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGeoPosition();
            }
        });

        chooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_ASK_PERMISSIONS);

            }

        });
        xCopingImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(xTextView.getText().toString());
                copy(yTextView.getText().toString());
            }
        });
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                        if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(Contacts._ID));
                        String hasNumber = c.getString(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getContentResolver().query(Phone.CONTENT_URI, null,
                                    Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(Phone.NUMBER));
                                setPhoneNo(num);
                                savePhoneNumber(num);
                                Toast.makeText(MainActivity.this, NUMBER_ADD + num, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

    protected void startActivityTurnOnLocation() {

        Toast.makeText(this, TURONLOCATION, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS );
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST);
    }




    private void init() {
        sharedPreferences = this.getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        xTextView = findViewById(R.id.xtextView);
        yTextView = findViewById(R.id.ytextView);

        xCopingImageButton = findViewById(R.id.copyXButton);
        smsSendButton = findViewById(R.id.sendSmsButton);
        chooseContactButton = findViewById(R.id.choseContactButton);

        Ask.on(this)
                .forPermissions(Manifest.permission.READ_CONTACTS)
                .go();






    }

    void copy(String coord) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(COPYCOORDS, coord);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(),COPYCOORDS, Toast.LENGTH_SHORT).show();
    }

    public void sendSms(String x, String y) {

        message = String.format(FINDME, x, y);
        phoneNo = sharedPreferences.getString(PHONE_NUMBER,null);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);
                Toast.makeText(getApplicationContext(), SEND,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        SMS_FAILED, Toast.LENGTH_LONG).show();
            }


        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                try {
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                } catch (Exception npe) {
                    Toast.makeText(getApplicationContext(),
                            CHOOSEPHONENUMBER, Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(), SEND, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        SMS_FAILED, Toast.LENGTH_LONG).show();
                return;
            }
        }


    }

    void getGeoPosition() {
        geoLocationFinder = new GeoLocationFinder(MainActivity.this);
        geoLocationFinder.getLastLocation();
    }


    private void savePhoneNumber(String number){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone",number);
        editor.apply();

    }


}

