package com.sowmya.securechat;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Vector;

/**
 * Created by sowmya on 6/1/17.
 */

public class CustomList_Chats extends ArrayAdapter<String> {

    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    private StorageReference mStorageRef;

    public CustomList_Chats(Context context, Vector chats) {
        super(context,R.layout.cust_row,chats);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.cust_row,parent,false);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        ImageView image = (ImageView) view.findViewById(R.id.user_icon);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView phone = (TextView) view.findViewById(R.id.phone);

        String data = getItem(position);
        String[] user = data.split("\n");

        mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(user[1]+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().transform(new CircleTransform()).into(image);
            }
        });
        String user_name = getContactName(user[0]);
        Log.e("Custom_CHAT","+++++++"+user_name);
        if(user_name!=null)
            name.setText(user_name);
        else
            name.setText(user[0]);

        phone.setText(user[0]); //phone

        return view;
    }

    public String getContactName(String number){

        Uri lookupuri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
        String[] mPh = {ContactsContract.PhoneLookup._ID,ContactsContract.PhoneLookup.NUMBER,ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = getContext().getContentResolver().query(lookupuri,mPh,null,null,null);
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
