package com.sowmya.securechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class UploadAndView extends AppCompatActivity {

    private static final int PICK_IMAGE = 1 ;
    private static final String FIREBASE_STORAGE_DIRECTORY ="\"Profile_pic\"";
    private static String USER_ID, MOBILE;
    private ImageView imageView;
    private TextView mobile_text;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_and_view);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences("SecureChat",0);
        USER_ID = sharedPreferences.getString("user_id","");
        MOBILE = sharedPreferences.getString("mobile_no","");

        //firebase storage instance
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //imageview
        imageView = (ImageView) findViewById(R.id.upload);

        //TextView
        mobile_text = (TextView) findViewById(R.id.phonenumber);

        //prograss dialog object
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Downloading.....");
        progressDialog.show();

        //SharedPreferences sharedPreferences=getSharedPreferences()

        loadProfilePic();
        mobile_text.setText(MOBILE);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method will load the image from fire base and displays on the screen of user
     */
    private void loadProfilePic() {
        try {
            mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(USER_ID+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressDialog.dismiss();
                    showInImageView(task.getResult());

                }
            });

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Exception in geting pic from firebase",Toast.LENGTH_SHORT).show();
            Log.e("Execpt",e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            progressDialog.setMessage("Uploading......");
            progressDialog.show();
            Uri uri = data.getData();




            //cpath into Firebae storage
            // File should be stored using userid.jpg format/ To get user iduse shred prefereces and store the file

            //String user_id = "SC000003.jpg";
            //uri=uri.withAppendedPath(uri,user_id);
            //Log.e("Upload",uri.getLastPathSegment());




            StorageReference storageReference=mStorageRef.child(FIREBASE_STORAGE_DIRECTORY).child(USER_ID+".jpg");

            //On Upload Success
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();

                    //Showing uploaded image to User
                    @SuppressWarnings("VisibleForTests") Uri download=taskSnapshot.getDownloadUrl();

                    showInImageView(download);
                    Toast.makeText(getApplicationContext(),"Upload finshed",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void showInImageView(Uri imageUri)
    {
        Picasso.with(UploadAndView.this).load(imageUri).fit().centerCrop().transform(new CircleTransform()).into(imageView);
    }


}

class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}

