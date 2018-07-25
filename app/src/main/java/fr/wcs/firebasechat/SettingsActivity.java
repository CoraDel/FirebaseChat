package fr.wcs.firebasechat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabaase;
    private Uri mSelectedImage = null;
    private FirebaseUser mCurrentUser;
    private ImageView mImageUser;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;
    private Uri mPhotoUri;
    private Toolbar mToolbar;

    private StorageReference mImageStorage;


    private static int GALLERY_SELECT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mImageUser = (ImageView) findViewById(R.id.img_settings);
        mName = (TextView) findViewById(R.id.text_display_name);
        mStatus = (TextView) findViewById(R.id.text_status_settings);
        mStatusBtn = (Button) findViewById(R.id.btn_status);
        mImageBtn = (Button) findViewById(R.id.btn_change_img);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.settings_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String current_uid = mCurrentUser.getUid();

        // Envoie dans la firebase
        mUserDatabaase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mUserDatabaase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Toast.makeText(SettingsActivity.this, dataSnapshot.toString(), Toast.LENGTH_SHORT).show();
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")){
                    Picasso.get().load(image).into(mImageUser);
                }
                else {
                    Picasso.get().load(R.drawable.defaultimageuser).into(mImageUser);
                }


                /*
                Glide.with(SettingsActivity.this)
                        .load(image)
                        .into(mImageUser);
                        */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //PutExtra de la valeur du status vers StatusActivity
                String status_value = mStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_SELECT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_SELECT && resultCode == RESULT_OK){
            mSelectedImage = data.getData();
            mPhotoUri = mSelectedImage;

            final String current_user_id = mCurrentUser.getUid();


            StorageReference filepath = mImageStorage.child("profil_image").child(current_user_id).child(mSelectedImage.getLastPathSegment());
            filepath.putFile(mPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String url = downloadUrl.toString();

                    mUserDatabaase.child("image").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Update complete", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

            });

        }
        else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
