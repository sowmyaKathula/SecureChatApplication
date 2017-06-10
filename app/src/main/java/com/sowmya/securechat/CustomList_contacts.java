package com.sowmya.securechat;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
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

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by sowmya on 5/16/17.
 */

public class CustomList_contacts extends ArrayAdapter<String> {

    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    private StorageReference mStorageRef;

    public CustomList_contacts(Context context, Vector Contacts) {
        super(context, R.layout.cust_row, Contacts);
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

        mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(user[2]+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().transform(new CircleTransform()).into(image);
            }
        });

        name.setText(user[0]); //userName
        phone.setText(user[1]); //phone

        return view;
    }
}
