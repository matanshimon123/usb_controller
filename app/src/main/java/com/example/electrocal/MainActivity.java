package com.example.electrocal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button pi_att_button,T_att_button;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pi_att_button = findViewById(R.id.button1);
        pi_att_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });

        T_att_button = findViewById(R.id.button2);
        T_att_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openActivity3();}
        });

        T_att_button = findViewById(R.id.button3);
        T_att_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openActivity4();}
        });


    }


    public void openActivity2()
    {
        Intent intent = new Intent(this, T_Att_Activity.class);
        startActivity(intent);
    }

    public void openActivity3()
    {
        Intent intent = new Intent(this, Pi_Att_Activity.class);
        startActivity(intent);
    }

    public void openActivity4()
    {
        Intent intent = new Intent(this, USBActivity.class);
        startActivity(intent);
    }






}

