package com.sowmya.securechat;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ContactList extends AppCompatActivity {


    private ListView list;
    Hashtable<String,String> contact_list = new Hashtable<>();
    //Vector<String> contact_list = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Select Contact");

        list = (ListView) findViewById(R.id.list);



            // carry on the normal flow, as the case of  permissions  granted.
            //To start a back ground service to access the location and update in database
            Log.d("Myproject","permissions accepted");

            //read the contacts from the phone and display in the list View
            //getContacts();

            ServerDB.GET_ALL_USERS(this, list);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout v = (LinearLayout) view;

                LinearLayout getChildView = (LinearLayout) v.getChildAt(0);
                LinearLayout getNextChild = (LinearLayout) getChildView.getChildAt(1) ;
                TextView textView = (TextView) getNextChild.getChildAt(0);
                TextView phoneView = (TextView) getNextChild.getChildAt(1);

                //Log.e("CONTACT","*********"+textView.getText().toString()+"******"+phoneview.getText().toString()+"*****");

                String name = textView.getText().toString();

                String phone = phoneView.getText().toString();

                Toast.makeText(getApplicationContext(),"You Clicked on contact "+name+" ph= "+phone,Toast.LENGTH_SHORT ).show();

                //get the id of the contact choosen
                String user_id = ServerDB.GET_USERID(phone,ContactList.this);
                Log.e("Contact List","************"+user_id+"***********");

                Intent intent = new Intent(ContactList.this,ChatScreen.class);
                intent.putExtra("mobile_no2",phone);
                intent.putExtra("user2",name);
                intent.putExtra("id2",user_id);
                startActivity(intent);
                finish();
            }
        });


    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void getContacts(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));


                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (pCur != null && pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact_list.put(SantString(phoneNo),name);
                        //contact_list.add(SantString(phoneNo)+"\n"+name);
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
    }

    public static String SantString(String Phonenumber){
        String temp=Phonenumber.replaceAll("[-+.^: ,()]","");
        if(temp.length()==10){
            return temp;
        }else{
            String x="";
            for (int i = 2; i <temp.length() ; i++) {
                x+=temp.charAt(i);
            }
            return x;
        }
    }

}
