package com.sowmya.securechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Login extends AppCompatActivity {

    private EditText mobile_no,password;
    private TextView textView;
    private String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobile_no = (EditText)findViewById(R.id.input_phone);
        password = (EditText)findViewById(R.id.input_password);

        mobile_no.setText(getIntent().getStringExtra("mobile_no"));

        Button signin = (Button)findViewById(R.id.btn_signin);

        textView = (TextView) findViewById(R.id.link_signup);

        signin.setOnClickListener(e->{

            Log.e(TAG,"**********Sign in Clicked ************");
            //Checking the validations of User from Server Side
            ServerDB.LOG_USER(mobile_no.getText().toString(),password.getText().toString(),this);

        });

        textView.setOnClickListener(e->{
            Intent intent = new Intent(this,Registration.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();
        });
    }
}
