package com.find_me;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.find_guahoo_findme.R;

import java.util.ArrayList;

public class Number_holder_Activity extends AppCompatActivity {
    ImageButton[] imageButtons;
    ImageButton button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    TextView numberTextview;
    int clicknumber = 0;
    ArrayList<Character> numberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_holder);
        init();
    }

    private void init() {

        numberTextview = findViewById(R.id.numbertextview);

        imageButtons = new ImageButton[]{
                button0 = findViewById(R.id.number_button_11),
                button1 = findViewById(R.id.number_button_1),
                button2 = findViewById(R.id.number_button_2),
                button3 = findViewById(R.id.number_button_3),
                button4 = findViewById(R.id.number_button_4),
                button5 = findViewById(R.id.number_button_5),
                button6 = findViewById(R.id.number_button_6),
                button7 = findViewById(R.id.number_button_7),
                button8 = findViewById(R.id.number_button_8),
                button9 = findViewById(R.id.number_button_9)
        };

         numberList = new ArrayList<>();

        for (int i = 0; i < imageButtons.length; i++) {
            final int finalI = i;
            imageButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick(finalI);
                }
            });
        }


    }

    private void buttonClick(final int position) {

        if (clicknumber<=10) {
            clicknumber++;


            if (clicknumber==1&position==8){
                numberList.add('+');
                numberList.add('7');
            } else if (clicknumber == 1&position != 8) {
                numberList.add('+');
                numberList.add( String.valueOf(position).charAt(0));
            } else {
                numberList.add( String.valueOf(position).charAt(0));
            }

              if (clicknumber==1){
                numberList.add(' ');
            } else if (clicknumber==4){
                numberList.add(' ');
            } else if (clicknumber==7){
                numberList.add('-');
            } else if (clicknumber==9){
                numberList.add('-');
            }

            StringBuilder stringBuilder = new StringBuilder(numberList.size());
            for(Character ch:numberList){
                stringBuilder.append(ch);
            }
            numberTextview.setText(stringBuilder.toString());
        }
    }


}



