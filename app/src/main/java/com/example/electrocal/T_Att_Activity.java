package com.example.electrocal;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class T_Att_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.t_att_layout);
    }


    public void  onBtnClick_T (View view) {
        EditText Zparameter, att;
        int Z, att_Db;
        double R1, R2, R3, k;
        TextView R1_result = findViewById(R.id.textViewR1_T);
        TextView R2_result = findViewById(R.id.textViewR2_T);
        TextView R3_result = findViewById(R.id.textViewR3_T);

        Zparameter = findViewById(R.id.edtTxtZ_T);
        att = findViewById(R.id.edtTxtatt_T);

        String Ztext = Zparameter.getText().toString().trim();
        String attText = att.getText().toString().trim();

        if (!Ztext.isEmpty() && !attText.isEmpty()) {

            Z = Integer.parseInt(Zparameter.getText().toString());
            att_Db = Integer.parseInt(att.getText().toString());

            double temp1 = (att_Db * 0.1);
            k = Math.pow(10, temp1);
            double temp2 = Math.sqrt(k * Z * Z);

            R1 = (Z * (k + 1) - 2 * temp2) / (k - 1);
            R3 = (Z * (k + 1) - 2 * temp2) / (k - 1);
            R2 = (2 * temp2) / (k - 1);

            R1_result.setText("R1: " + String.format("%.4f", R1));
            R3_result.setText("R3: " + String.format("%.4f", R3));
            R2_result.setText("R2: " + String.format("%.4f", R2));

        } else {
            // Show a toast message (upload to git worked)
            Toast.makeText(getApplicationContext(), "Check values", Toast.LENGTH_SHORT).show();
        }


    }


}