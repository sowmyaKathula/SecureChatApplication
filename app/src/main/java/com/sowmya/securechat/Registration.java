package com.sowmya.securechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Registration extends AppCompatActivity {

    private TextView textView;
    private String password, email, mobile, name;
    private EditText _name, _mobile,_password, _email;
    private Boolean name_valid=false, mobile_valid=false, pass_valid=false, email_valid=false;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String namePattern="[a-zA-Z]{1,50}";
    String phonePattern="[0-9]{1,10}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Button sign_up = (Button) findViewById(R.id.btn_signup);
        textView = (TextView) findViewById(R.id.link_signin);

        _name = (EditText) findViewById(R.id.input_name);
        _mobile = (EditText) findViewById(R.id.input_phone);
        _password = (EditText) findViewById(R.id.input_password);
        _email = (EditText) findViewById(R.id.input_email);

        _name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                name = s.toString();
                if (name!=null && name.matches(namePattern)) {
                    _name.setError(null);
                    name_valid = true;

                } else {
                    _name.setError("only alphabates");
                    name_valid = false;

                }
            }
        });

        _email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString();
                if (!email.isEmpty() && email.matches(emailPattern)) {
                    _email.setError(null);
                    email_valid=true;
                } else {
                    _email.setError("enter a valid email address");
                    email_valid = false;

                }

            }
        });

        _mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mobile = s.toString();
                if (!mobile.isEmpty() && mobile.matches(phonePattern)) {
                    _mobile.setError(null);
                    mobile_valid = true;

                } else {
                    _mobile.setError("enter a valid mobile number");
                    mobile_valid = false;

                }
            }
        });

        _password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString();
                if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                    _password.setError("between 4 and 10 alphanumeric characters");
                    pass_valid = false;
                } else {
                    _password.setError(null);
                    pass_valid = true;
                }
            }
        });

        sign_up.setOnClickListener(e->{

            Log.e("Registration","*********on click***********");

            password = _password.getText().toString();
            email = _email.getText().toString();
            mobile = _mobile.getText().toString();
            name = _name.getText().toString();

            if(validate()) {
                ServerDB.REG_USER(name, password, email.toLowerCase(), mobile, this);
                //redirects to image upload activity
                loadtoImageUploadActivity();
            }





        });

        /*textView.setOnClickListener(e->{
            Intent intent = new Intent(this,Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });*/

    }

    private void loadtoImageUploadActivity() {
        Intent i=new Intent(getApplicationContext(),RegistrationImageUpload.class);

        //gets user id from server
        String user_id=ServerDB.GET_USERID(mobile,getApplicationContext());

        i.putExtra("user_id",user_id);
        startActivity(i);
        finish();

    }

    private boolean validate() {
        if(!name_valid)
            _name.requestFocus();
        else if(!mobile_valid)
            _mobile.requestFocus();
        else if(!email_valid)
            _email.requestFocus();
        else if(!pass_valid)
            _password.requestFocus();
        if(name_valid && email_valid && mobile_valid && pass_valid)
            return true;
        return false;
    }


}
