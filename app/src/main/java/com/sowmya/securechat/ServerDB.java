package com.sowmya.securechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sowmya on 5/6/17.
 */

public class ServerDB {

    private final static String REG_URL = "http://13.58.44.250/signup";
    private final static String LOG_URL = "http://13.58.44.250/login";
    private final static String GET_USERS_URL = "http://13.58.44.250/getallusers";
    private final static String GET_ID = "http://13.58.44.250/getIdFromMobile";
    private final static String GET_PHONE = "http://13.58.44.250/getMobileFromId";

    private final static String TAG = "SERVER DB CLASS";

    public static void REG_USER(String Name, String Password, String Email, String Phone, Context ctx) {

        class RegisterUser extends AsyncTask<String, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (s.equalsIgnoreCase("true")){
                    Log.e(TAG,"********on post execute**********");
                    Toast.makeText(ctx.getApplicationContext(),"Registration Successful",Toast.LENGTH_LONG).show();
                    /*Intent intent = new Intent(ctx.getApplicationContext(),RegistrationImageUpload.class);
                    intent.putExtra("mobile_no",Phone);
                    ctx.startActivity(intent);*/
                }
                else{
                    Toast.makeText(ctx.getApplicationContext(),"Registration Unsuccessful",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                URL url;
                String response = "";
                try {

                    url = new URL(REG_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type","application/text");

                    Log.e(TAG,"******IN background Task***********");

                    //creating a json String
                    String user = "{\"name\":\""+Name+"\",\"mobile_no\":\""+Phone+"\",\"email_id\":\""+Email+"\",\"password\":\""+Password +"\"}";

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(user);
                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    Log.e(TAG,"*******http response "+responseCode+"   **********");

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.e(TAG,"*********in Db***********");
                        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        response = br.readLine();
                    }
                    else {
                        response="0";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;
            }
        }
        RegisterUser ru = new RegisterUser();
        ru.execute();
    }

    public static void LOG_USER(String Phone,String Password, Context ctx){

        class LoginUser extends AsyncTask<String, Void, String> {

            private static final String FIREBASE_STORAGE_DIRECTORY ="gs://securechat-96a99.appspot.com";
            private StorageReference mStorageRef;

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                JSONObject json = null;
                try {

                    json = new JSONObject(s);
                    if(json.has("Login Sucessfull")){


                        String user_id = json.getString("Login Sucessfull");
                        //Creating Shared Preference for the user after successful login
                        SharedPreferences sharedPreferences = ctx.getSharedPreferences("SecureChat",0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("mobile_no",Phone);
                        editor.putString("user_id",json.getString("Login Sucessfull"));
                        editor.putString("Database","false");
                        editor.commit();

                        Uri uri = Uri.parse("android.resource://com.sowmya.securechat/drawable/user_icon");

                        FirebaseStorage storage =  FirebaseStorage.getInstance("gs://securechat-96a99.appspot.com");
                        mStorageRef = storage.getReference();
                        mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(user_id+".jpg");

                        mStorageRef.putFile(uri);
                        //Toast to display successfull login
                        Toast.makeText(ctx.getApplicationContext(),"Login Successful",Toast.LENGTH_LONG).show();

                        //Intent to move from login activity to chat List Activity
                        Intent intent = new Intent(ctx.getApplicationContext(),ChatList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }else{
                        //Toast to display Unsuccessful login
                        Toast.makeText(ctx.getApplicationContext(),"Login Unsuccessful.....TryAgain!!!",Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                String response="",x;
                URL url;
                String json_data = "{\"mobile_no\":\""+Phone+"\",\"password\":\""+Password+"\"}";
                Log.e(TAG,"************json***********"+json_data+"*********8");
                try {
                    url = new URL(LOG_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type","application/text");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(json_data);
                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {

                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while((x = br.readLine()) != null) {
                            response += x;
                        }

                    } else {
                        response = "{Error Signing In:0}";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;

            }
        }

        LoginUser loginUser = new LoginUser();
        loginUser.execute();

    }

    public static void GET_ALL_USERS(Context ctx, ListView list){

        Vector<String> vt = new Vector<>();

        class GET_ALL extends AsyncTask<String, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    String uname;
                    JSONObject jsonObject = new JSONObject(s);
                    String string = jsonObject.getString("users");
                    JSONArray arr = new JSONArray(string);
                    for(int i=0;i<arr.length();i++){
                        JSONObject obj = arr.getJSONObject(i);
                        String mobile=obj.getString("mobile_no");
                        String name = obj.getString("name");
                        String user_id = obj.getString("user_id");
                        if ((uname = contactExists(ctx,mobile))!=null){
                            vt.add(uname+"\n"+mobile +"\n" +user_id);
                        }
                        ListAdapter lta = new CustomList_contacts(ctx,vt);
                        list.setAdapter(lta);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params) {

                URL url;
                String response = "";
                String x;
                try {
                    url = new URL(GET_USERS_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    int responseCode=conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while((x = br.readLine()) != null){
                            response += x;
                        }
                    }
                    else {
                        response="Error Registering";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;

            }

            public String contactExists(Context c, String number){
                Uri lookupuri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
                String[] mPh = {ContactsContract.PhoneLookup._ID,ContactsContract.PhoneLookup.NUMBER,ContactsContract.PhoneLookup.DISPLAY_NAME};
                Cursor cur = c.getContentResolver().query(lookupuri,mPh,null,null,null);
                try{
                    if(cur.moveToFirst()){
                        return cur.getString(2);
                    }
                }finally {
                    if(cur !=null){
                        cur.close();
                    }
                }
                return null;
            }

        }
        GET_ALL ru = new GET_ALL();
        ru.execute();

    }

    public static String GET_PHONE(String id){
        String phone="";
        class GetPhone extends AsyncTask<String,String,String >{

            @Override
            protected String doInBackground(String... params) {

                URL url;
                String response="",x;
                try {
                    url = new URL(GET_PHONE);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type","application/text");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String json_data = "{\"user_id\":\""+id+"\"}";
                    writer.write(json_data);
                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    Log.e(TAG,"********responsecode**************+"+responseCode+"**********");
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        response = br.readLine();
                        Log.e(TAG,"*********"+response+"************response object***");

                    } else {
                        response = "Error getting ID";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;

            }

        }
        GetPhone getphone = new GetPhone();
        try {
            phone = getphone.execute().get();
            Log.e(TAG,"**********response form background****"+id+"********");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return phone;
    }

    public static String GET_USERID(String Phone, Context ctx) {

        String id="";
        class GetUserId extends AsyncTask<String, String, String> {


           /* @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e(TAG,"******s*********"+s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String string = jsonObject.getString("users");
                    id =string;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
*/
            @Override
            protected String doInBackground(String... params) {

                URL url;
                String response="",x;
                try {
                    url = new URL(GET_ID);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type","application/text");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String json_data = "{\"mobile_no\":\""+Phone+"\"}";
                    writer.write(json_data);
                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    Log.e(TAG,"********responsecode**************+"+responseCode+"**********");
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        response = br.readLine();
                        Log.e(TAG,"*********"+response+"************response object***");

                    } else {
                        response = "Error getting ID";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return response;

            }
        }
        GetUserId getUserId = new GetUserId();
        try {
            id = getUserId.execute().get();
            Log.e(TAG,"**********response form background****"+id+"********");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return id;
    }

}