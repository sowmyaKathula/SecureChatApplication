package com.sowmya.securechat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.sowmya.securechat.R.attr.colorPrimary;
import static com.sowmya.securechat.R.id.imageView;

public class Profile extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    private StorageReference mStorageRef;
    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    private String name, mobile, id;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.profile_id);

        name = getIntent().getStringExtra("name");
        mobile = getIntent().getStringExtra("mobile");
        id = getIntent().getStringExtra("id");

        TextView textView1 = (TextView) findViewById(R.id.user_name);
        textView1.setText(name);
        TextView textView2 = (TextView) findViewById(R.id.phone_no);
        textView2.setText(mobile);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        dynamicToolbarColor();

        toolbarTextAppernce();

    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void dynamicToolbarColor() {

        Uri uri1;
        mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Picasso.with(Profile.this).load(uri).fit().centerCrop().into(imageView);
                    InputStream image_stream = getContentResolver().openInputStream(uri);
                     Bitmap bitmap = BitmapFactory.decodeStream(image_stream);
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
                            collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimary));
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //Picasso.with(Profile.this).load(uri).fit().centerCrop().transform(new CircleTransform()).into(imageView);
            }
        });
        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                uri);*/
        /*Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(R.attr.colorPrimary));
                collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(R.attr.colorPrimaryDark));
            }
        });*/
    }


    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }

}
