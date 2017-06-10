package com.sowmya.securechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class ChatScreen extends AppCompatActivity implements MessageDataSource.MessagesCallbacks{

    private ArrayList<Message> mMessages;
    private MessagesAdapter mAdapter;
    private String sender, user1_id;
    private String mRecipient, user2_name,user2_id;
    private ListView mListView;
    private Date mLastMessageDate = new Date();
    private String mConvoId;
    private MessageDataSource.MessagesListener mListener;
    private SQLLiteDataSource sqlLiteDataSource;
    private TextView name_view;
    private StorageReference mStorageRef;
    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    //private String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView imageView = (ImageView) findViewById(R.id.profile_image);
        setSupportActionBar(toolbar);

        name_view = (TextView) findViewById(R.id.name);

        sqlLiteDataSource=new SQLLiteDataSource(getApplicationContext());
        SharedPreferences sharedPreferences = getSharedPreferences("SecureChat", Context.MODE_PRIVATE);
        sender = sharedPreferences.getString("mobile_no","");
        user1_id = sharedPreferences.getString("user_id","");


        MessageDataSource.user=sender;
        mRecipient = getIntent().getStringExtra("mobile_no2");
        mListView = (ListView)findViewById(R.id.message_list);
        mMessages = new ArrayList<>();
        mAdapter = new MessagesAdapter(mMessages);
        mListView.setAdapter(mAdapter);


        user2_name = getIntent().getStringExtra("user2");
        user2_id = getIntent().getStringExtra("id2");

        Log.e("CHATACtivity","******sender***************"+sender);
        Log.e("CHATACtivity","******receiver***************"+mRecipient);
        Log.e("CHATACtivity","******receiver name***************"+user2_id);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(user2_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ChatScreen.this).load(uri).fit().centerCrop().transform(new CircleTransform()).into(imageView);
            }
        });
        name_view.setText(user2_name);
        name_view.setOnClickListener(e->{
            Intent intent = new Intent(this,Profile.class);
            intent.putExtra("name",user2_name);
            intent.putExtra("mobile",mRecipient);
            intent.putExtra("id",user2_id);
            startActivity(intent);
        });

        if(sender.charAt(9) > mRecipient.charAt(9))
            mConvoId = user1_id+"*"+user2_id;
        else
            mConvoId = user2_id+"*"+user1_id;

        loadMessageFromSQLLite(mConvoId);

        Log.e("CHT","********************************id*****************"+mConvoId+"***");
        Log.e("CHT","*************Before on click ****************88888");
        FloatingActionButton send = (FloatingActionButton) findViewById(R.id.sendButton);
        send.setOnClickListener(e->{
            EditText new_message = (EditText)findViewById(R.id.new_message);
            String str=new_message.getText().toString();
            if(str!=null && str.length()!=0){
                new_message.setText("");
                Message message = new Message();
                message.setMsg_text(str);
                message.setSender(sender);
                message.setReceiver(mRecipient);
                message.setDate(new Date());

                Log.e("CHT",sender+" *********************************");
                Log.e("CHT",message.getSender()+"___________________________________________");
                MessageDataSource.saveMessage(message,mConvoId);

                //Storing the new Message in SQLLite helper
                sqlLiteDataSource.addMessageToSQLLite(mConvoId,message);
            }

        });

        mListener = MessageDataSource.addMessagesListener(mConvoId, this);

    }


    private void loadMessageFromSQLLite(String mConvoId) {
        Vector<Message> message=sqlLiteDataSource.getMessageFromConversationID(mConvoId);

        //showMesage(message.toString());
        if(message!=null) {

            for(Message m:message) {
                mMessages.add(m);
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    private void showMesage(String message)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage(message);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed();
                            return true;
            case R.id.locate: Intent i= new Intent(this,ShowLocationMap.class);
                                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.putExtra("user_id",user2_id);
                                i.putExtra("name",user2_name);
                                startActivity(i);
                                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scroll()
    {
        mListView.post(new Runnable() {
            @Override
            public void run() {

                mListView.setSelection(mAdapter.getCount()-1);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onMessageAdded(Message message) {
        mMessages.add(message);
        mAdapter.notifyDataSetChanged();
        scroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageDataSource.stop(mListener);
    }


    private class MessagesAdapter extends ArrayAdapter<Message> {
        MessagesAdapter(ArrayList<Message> messages){
            super(ChatScreen.this, R.layout.message_item, R.id.message, messages);
            Log.e("CHT","IN Message Adapter"+ messages.isEmpty());
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.e("CHT", " IN getView +******" + position + " *****position***");
            convertView = super.getView(position, convertView, parent);

                Message message = getItem(position);
                Log.e("message:- ",message.toString());
                TextView nameView = (TextView) convertView.findViewById(R.id.message);
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                nameView.setText(message.getMsg_text());

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nameView.getLayoutParams();

                int sdk = Build.VERSION.SDK_INT;
            Log.e("CHT","**************"+message.getMsg_text());
            if(message != null) {
                if (message.getSender().equals(sender)) {
                    if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                        nameView.setBackground(getDrawable(R.drawable.bubble_right_green));
                    } else {
                        nameView.setBackgroundDrawable(getDrawable(R.drawable.bubble_right_green));
                    }
                    layoutParams.gravity = Gravity.RIGHT;
                } else if(message.getSender().equals(mRecipient)) {
                    if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                        nameView.setBackground(getDrawable(R.drawable.bubble_left_gray));
                    } else {
                        nameView.setBackgroundDrawable(getDrawable(R.drawable.bubble_left_gray));
                    }
                    layoutParams.gravity = Gravity.LEFT;
                }
            }

                nameView.setLayoutParams(layoutParams);
            return convertView;
        }
    }

}
