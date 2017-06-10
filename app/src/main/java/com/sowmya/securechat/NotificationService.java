package com.sowmya.securechat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Created by sowmya on 5/29/17.
 */

public class NotificationService extends Service {

    private static final Firebase firebase = new Firebase("https://securechat-96a99.firebaseio.com/");
    private static final String TAG = "NotificationService";
    private static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyyMMddmmss");
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String TEXT = "text";
    private ChildEventListener childlistener;
    private String user_id;
    private int notifyid = 1;
    private String mobile_no;
    Boolean bool = false;
    Hashtable<String,Boolean> table=null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"*IN Notification Service*********");

        SharedPreferences sharedPreferences = getSharedPreferences("SecureChat",0);
        mobile_no = sharedPreferences.getString("mobile_no","");
        user_id = sharedPreferences.getString("user_id","");


        Vector<String> db = new SQLLiteDataSource(getApplicationContext()).getAllConversationIds();

        table = new Hashtable<>();

        for(String x: db){
            table.put(x,false);
        }

        restoreListener();

        firebase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG,"***************on Data change******addListenerForSingleValueEvent*******");
                Log.e(TAG,"********************on * "+dataSnapshot.getKey()+ "* *");
                bool = true;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(bool) {
                    Log.e(TAG, "***************on Data change******addChildEventListener*******");
                    Log.e(TAG, "********************convo id * " + dataSnapshot.getKey() + "* *");
                    String convoid = dataSnapshot.getKey();
                    if(convoid.contains(user_id)){
                        Date date=null;
                        Message message=null;
                        long count=dataSnapshot.getChildrenCount();

                        for(DataSnapshot d:dataSnapshot.getChildren()){
                            Log.e(TAG,"********key++++++++"+d.getKey());
                            HashMap<String,String> msg=(HashMap)d.getValue();
                            try {
                                date = simpleDateFormat.parse(d.getKey());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.e(TAG,"*******sender*"+msg.get(SENDER)+"--------------");
                            Log.e(TAG,"*******receiver*"+msg.get(RECEIVER)+"--------------");
                            Log.e(TAG,"*******text*"+Encryption.decrypt(msg.get(TEXT))+"--------------");
                            message = new Message(msg.get(SENDER),msg.get(RECEIVER),Encryption.decrypt(msg.get(TEXT)),date);

                        }
                        //adding into Local database
                        new SQLLiteDataSource(getApplicationContext()).addMessageToSQLLite(convoid, message);


                        if(message.getReceiver().equals(mobile_no)) {
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setSmallIcon(R.drawable.secure);
                            mBuilder.setContentTitle(message.getSender());
                            mBuilder.setContentText(message.getMsg_text());
                            mBuilder.setPriority(Notification.PRIORITY_MAX);
                            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            mBuilder.setSound(uri);

                            PendingIntent contentIntent =
                                    PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), ChatList.class), 0);
                            mBuilder.setContentIntent(contentIntent);

                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            // notificationID allows you to update the notification later on.
                            mNotificationManager.notify(notifyid, mBuilder.build());

                            table.put(convoid, false);
                            restoreListener();
                        }
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });




    }

    private void restoreListener(){

        for(String key: table.keySet()){
            firebase.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e(TAG,"***************on Data change*************");
                    Log.e(TAG,"********************on * "+dataSnapshot.getKey()+ "* *");
                    table.put(key,true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            firebase.child(key).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(table.get(key)) {
                        Log.e(TAG, "*************" + dataSnapshot.getKey() + "*******");
                        if(key.contains(user_id)){
                            Log.e(TAG,"contains Key");
                            HashMap<String,String> msg = (HashMap)dataSnapshot.getValue();
                            Message message = new Message();
                            message.setSender(msg.get(SENDER));
                            message.setReceiver(msg.get(RECEIVER));
                            message.setMsg_text(Encryption.decrypt(msg.get(TEXT)));

                            if(message.getReceiver().equals(mobile_no)) {
                                Log.e(TAG,"************in receiver***********");

                                Log.e(TAG,"***********return*****************"+isNotificationVisible());

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                                mBuilder.setSmallIcon(R.drawable.user_icon);
                                mBuilder.setContentTitle(getContactName(message.getSender()));
                                mBuilder.setContentText(message.getMsg_text());
                                mBuilder.setPriority(Notification.PRIORITY_MAX);
                                Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                mBuilder.setSound(uri);

                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                // notificationID allows you to update the notification later on.
                                mNotificationManager.notify(notifyid, mBuilder.build());
                            }
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }


    }

    public String getContactName(String number){

        Uri lookupuri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
        String[] mPh = {ContactsContract.PhoneLookup._ID,ContactsContract.PhoneLookup.NUMBER,ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = getApplicationContext().getContentResolver().query(lookupuri,mPh,null,null,null);
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


    private boolean isNotificationVisible(){
        Log.e(TAG,"*********in notification Visible***********");
        Intent notificationIntent = new Intent(getApplicationContext(), NotificationService.class);
        PendingIntent test = PendingIntent.getActivity(getApplicationContext(), notifyid, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebase.removeEventListener(childlistener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
