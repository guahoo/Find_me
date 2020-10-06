package com.find_me;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.find_guahoo_findme.R;
import com.vistrav.ask.Ask;

import static android.Manifest.permission.SEND_SMS;
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
    public static TextView xTextView, yTextView, nameTextView, numberTextView;
    String name;
    ImageView lampView;
    Runnable lampLiting;
    int i=0;




    public static void setLatitude(String latitude) {
        MainActivity.latitude = latitude;
    }

    public static void setLongitude(String longitude) {
        MainActivity.longitude = longitude;
    }

    static String latitude, longitude;
    protected ImageButton xCopingImageButton, chooseContactButton;
    ImageButton smsSendButton;

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    private String phoneNo;
    private String message;
    GeoLocationFinder geoLocationFinder;
    boolean isPressed=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();



        smsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    final Handler handler=new Handler();
                    lampLiting = new Runnable() {
                        @Override
                        public void run() {
                            if(i == 20){ // just remove call backs
                                handler.removeCallbacks(this);

                            } else { // post again
                                if (isPressed) {
                                    i++;
                                    light_lamp(true);
                                    isPressed=false;
                                }else {
                                    light_lamp(false);
                                    isPressed=true;
                                }
                                handler.postDelayed(this, 1000);
                            }
                        }

                    };

                    lampLiting.run();





                getGeoPosition();
            }
        });

        chooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Number_holder_Activity.class);
                startActivity(intent);
//                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
//                startActivityForResult(intent, REQUEST_CODE_ASK_PERMISSIONS);

            }

        });
        xCopingImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(xTextView.getText().toString()+", "+yTextView.getText().toString());

            }
        });
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        try {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String contactId = c.getString(c.getColumnIndex(Contacts._ID));
                String hasNumber = c.getString(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER));
                String num;
                if (Integer.valueOf(hasNumber) == 1) {
                    Cursor numbers = getContentResolver().query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (numbers != null && numbers.moveToNext()) {

                        num = numbers.getString(numbers.getColumnIndex(Phone.NUMBER));
                        name = numbers.getString(numbers.getColumnIndex(Phone.DISPLAY_NAME));
                        setPhoneNo(num);
                        savePhoneNumber(num,name);
                        numberTextView.setText(num);
                        nameTextView.setText(name);
                        Toast.makeText(MainActivity.this, NUMBER_ADD + num, Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (NullPointerException npe) {
          nameTextView.setText("Контакт не выбран");
          numberTextView.setText("Номер не выбран");
        }
    }

    protected void startActivityTurnOnLocation() {

        Toast.makeText(this, TURONLOCATION, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS );
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST);
    }




    private void init() {
        sharedPreferences = this.getSharedPreferences(PREFERENCES,MODE_PRIVATE);
        xTextView = findViewById(R.id.latitude_view);
        yTextView = findViewById(R.id.longitude_view);
        nameTextView = findViewById(R.id.name_view);
        numberTextView = findViewById(R.id.numbertextview);

        xCopingImageButton = findViewById(R.id.button_copy);
        smsSendButton = findViewById(R.id.button_sos);
        chooseContactButton = findViewById(R.id.button_phone_book);
        lampView = findViewById(R.id.lampView);

        phoneNo = sharedPreferences.getString(PHONE_NUMBER, null);
        String contactName = sharedPreferences.getString("name", null);

        yTextView.setText("Долгота не определена");
        xTextView.setText("Широта не определена");


        try {
            numberTextView.setText(phoneNo);
            nameTextView.setText(contactName);
        }catch (NullPointerException npe){
            nameTextView.setText("Контакт не выбран");
            numberTextView.setText("Номер не выбран");

        }

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
//        phoneNo = ("89991131808");


        try {

            if (ContextCompat.checkSelfPermission(this, SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        SEND_SMS)) {
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
                        new String[]{SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);


            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this,"Неверный номер!", Toast.LENGTH_LONG).show();
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


    private void savePhoneNumber(String number,String name){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone",number);
        editor.putString("name",name);
        editor.apply();

    }

    private void light_lamp (boolean buttonsosisPressed){
        if (buttonsosisPressed) {
            lampView.setImageResource(R.drawable.lamp_light);
        } else {
            lampView.setImageResource(R.drawable.lamp);
        }
    }


}

