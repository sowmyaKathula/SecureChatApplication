package com.sowmya.securechat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;

/**
 * Chat list screen
 * Flow..
 * On invoking on create method, startsGPSService
 * Gets user id from shared preferences
 * Creates an object from SQLiteDataSource class, and invokes getAllMessages
 *
 */
public class ChatList extends AppCompatActivity {

    private FloatingActionButton show_contacts;
    private String user_id;
    private String mobile_no;
    private SQLLiteDataSource sqlLiteDataSource;
    private ListAdapter listAdapter;
    private static final Firebase firebase = new Firebase("https://securechat-96a99.firebaseio.com/");
    private static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyyMMddmmss");
    private Boolean data = false;
    private Vector v;
    private ListView list;
    private String choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //starting the Intent Service of Gps to access the location
        startGPSService();
        startNotificationService();


        //adding chat activity to the layout and setting the title for the activity
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Chats");

        //getting the information of user with the help of shared preferences
        SharedPreferences sharedPreferences=getSharedPreferences("SecureChat", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id","");
        mobile_no = sharedPreferences.getString("mobile_no","");
        choice = sharedPreferences.getString("Database","");
        Log.e("chat list:- ",user_id);

        //Creating an instance for accessing the database...tables are created if doesn't exist
        //getting the conversations into the database... if the app is re-installed.
        sqlLiteDataSource=new SQLLiteDataSource(getApplicationContext());


        //loading all the chats from the db to the listview and creating listeners to the list view
        //on clicking on the list view directed to chatScreen Activity
        list=(ListView)findViewById(R.id.list_chat);

        if(choice.equals("false")) {
            Log.e("In contacts","In false");
            loadConversationID();
            sharedPreferences.edit().putString("Database","true").commit();

        }else{
            Log.e("In contacts","In true");
            v=loadAllChats();
            assignListeners();
        }

        show_contacts = (FloatingActionButton) findViewById(R.id.show_contacts);
        show_contacts.setOnClickListener(e->{
            //Thread for splashScreen
            Thread th = new Thread(){
                @Override
                public void run() {
                    try{
                        sleep(3000);
                        Intent it = new Intent(getApplicationContext(), ContactList.class);
                        startActivity(it);
                    }catch (Exception e){
                        Log.e("Thread Splash Screen",e.getMessage());
                    }
                }
            };
            th.start();
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.profile: Intent i=new Intent(this,UploadAndView.class);
                                startActivity(i);
                                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startNotificationService(){
        Intent intent = new Intent(getApplicationContext(),NotificationService.class);
        startService(intent);
    }

    private void startGPSService(){
        Intent intent = new Intent(getApplicationContext(),GPS_Service.class);
        startService(intent);
    }

    private void loadConversationID(){

        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("CHAT_ACT","on value");
                data = true;
                firebase.removeEventListener(this);
                v=loadAllChats();
                assignListeners();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                if(key.contains(user_id)) {
                    com.firebase.client.Query query = dataSnapshot.getRef().limitToLast(1);
                    Log.e("CHAT_ACT", "*****conversation********" + key);
                    sqlLiteDataSource.addConversationToSQLLite(key);
                }
                if(data == true)
                    firebase.removeEventListener(this);

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

    private Vector loadAllChats() {
        Log.e("Chat List:- ","Load contacs Called");
        Vector result = sqlLiteDataSource.getAllConversationIds();
        Vector<String> v = new Vector<>();
        Iterator<String> i = result.iterator();
        while(i.hasNext()){
            String x = i.next();
            Log.e("CHAT_ACT","----------------"+x+"_------------------");
            if(x.indexOf(user_id)==0){
                String phone = ServerDB.GET_PHONE(x.substring(11));
                Log.e("CHAT_ACT","***88values********" +x.substring(11)+"*888phone***"+phone);
                v.add(phone +"\n" +x.substring(11));

            }
            else if(x.indexOf(user_id)==11){
                String phone = ServerDB.GET_PHONE(x.substring(0,10));
                Log.e("CHAT_ACT","***88values********" +x.substring(0,10)+"*888phone***"+phone);
                v.add(phone +"\n" +x.substring(0,10));
            }
        }
        return v;
    }

    private void assignListeners(){

        Log.e("CHAT_ACT","_______________in assign listener++++++++++");
        ListAdapter lta = new CustomList_Chats(this,v);
        list.setAdapter(lta);

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
                String user_id = ServerDB.GET_USERID(phone,ChatList.this);
                Log.e("Contact List","************"+user_id+"***********");

                Intent intent = new Intent(ChatList.this,ChatScreen.class);
                intent.putExtra("mobile_no2",phone);
                intent.putExtra("user2",name);
                intent.putExtra("id2",user_id);
                startActivity(intent);
            }
        });
    }


}
