package com.sowmya.securechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by sowmya on 5/12/17.
 */

public class MessageDataSource {

    public static String user;


    private static final Firebase firebase = new Firebase("https://securechat-96a99.firebaseio.com/");
    private static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyyMMddmmss");
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String TEXT = "text";

    public static void saveMessage(Message message,String mConvoid){
        Log.e("VHT","****************IN save Messages**********************8888");
        Date date = message.getDate();
        String key = simpleDateFormat.format(date);
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(TEXT,Encryption.encrypt(message.getMsg_text()));
        hashMap.put(SENDER,message.getSender());
        hashMap.put(RECEIVER,message.getReceiver());
        firebase.child(mConvoid).child(key).setValue(hashMap);

    }


    public static MessagesListener addMessagesListener(String convoId, final MessagesCallbacks callbacks){
        Log.e("VHT","************IN add Message Listener*************************");
        MessagesListener listener = new MessagesListener(callbacks);
        firebase.child(convoId).addChildEventListener(listener);
        return listener;

    }


    public static void stop(MessagesListener listener){

        firebase.removeEventListener(listener);
    }

    public static class MessagesListener implements ChildEventListener {
        private MessagesCallbacks callbacks;
        MessagesListener(MessagesCallbacks callbacks){
            this.callbacks = callbacks;
        }
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.e("VHT0","****************************on child Added*******************");
            //Log.e("VHT","*********************======"+fb+"+++++++++++");
            HashMap<String,String> msg = (HashMap)dataSnapshot.getValue();
            Message message = new Message();
            message.setSender(msg.get(SENDER));
            message.setReceiver(msg.get(RECEIVER));
            message.setMsg_text(Encryption.decrypt(msg.get(TEXT)));
            try {
                message.setDate(simpleDateFormat.parse(dataSnapshot.getKey()));
            }catch (Exception e){
                Log.d("swdefsrdtf", "Couldn't parse date"+e);
            }
            if(callbacks != null){
                callbacks.onMessageAdded(message);
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
    }


    public interface MessagesCallbacks{
        public void onMessageAdded(Message message);
    }
}
