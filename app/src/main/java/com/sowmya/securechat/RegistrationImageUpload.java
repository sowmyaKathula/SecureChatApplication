package com.sowmya.securechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class RegistrationImageUpload extends AppCompatActivity {

    private ImageView imageView;
    private static final int PICK_IMAGE=1;
    private ProgressDialog progressDialog;
    private StorageReference mStorageRef;
    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    private static String USER_ID ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_image_upload);

        USER_ID=getIntent().getStringExtra("user_id");

        imageView=(ImageView) findViewById(R.id.imageView);

        progressDialog=new ProgressDialog(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button button=(Button)findViewById(R.id.sign_up);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TO-DO
                /*
                 Open the gallery and choose the image whch we have to upload to database
                 */

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode ==  PICK_IMAGE && resultCode == RESULT_OK)
        {
            progressDialog.setMessage("Uploading......");
            progressDialog.show();
            Uri uri = data.getData();

            StorageReference storageReference=mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(USER_ID+".jpg");
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();

                    //Showing uploaded image to User
                    @SuppressWarnings("VisibleForTests") Uri download=taskSnapshot.getDownloadUrl();

                    showInImageeView(download);
                    Toast.makeText(getApplicationContext(),"Upload finshed",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showInImageeView(Uri imageUri)
    {
        Picasso.with(RegistrationImageUpload.this).load(imageUri).fit().centerCrop().into(imageView);
    }
}
