package fr.wcs.firebasechat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequest, mDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsReqDatabase, mFriendDatabase;
    private FirebaseUser mCurrentUser;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendsReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = findViewById(R.id.imageView_profile);
        mProfileName = findViewById(R.id.text_profil_name);
        mProfileStatus = findViewById(R.id.tv_current_status);
        mProfileFriendsCount = findViewById(R.id.tv_total_friends);
        mProfileSendRequest = findViewById(R.id.btn_friend_request);
        mDeclineBtn = findViewById(R.id.btn_decline_request);
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mCurrentState = "not_friends";

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).into(mProfileImage);

                //-------------------- FRIENDS LIST / REQUEST----------------

                mFriendsReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent
                        (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(userId)){
                            String requestType = dataSnapshot.child(userId).child("request_type")
                                    .getValue().toString();
                            if(requestType.equals("received")){

                                mCurrentState = "req_received";
                                mProfileSendRequest.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (requestType.equals("sent")){

                                mCurrentState ="req_sent";
                                mProfileSendRequest.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                        } else {

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){

                                        mCurrentState = "friends";
                                        mProfileSendRequest.setText("Unfriend this person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //req friends database
        mProfileSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Friend req btn disparait apres avoir cliquer dessus
                mProfileSendRequest.setEnabled(false);


                // ------ NOT FRIEND REQUEST STATE

                if(mCurrentState.equals("not_friends")){

                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(userId).child("request_type")
                            .setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                mFriendsReqDatabase.child(userId).child(mCurrentUser.getUid())
                                        .child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProfileSendRequest.setEnabled(true);
                                        mCurrentState = "req_sent";
                                        mProfileSendRequest.setText("Cancel Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                        Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                // ------ CANCEL REQUEST STATE ------------

                if (mCurrentState.equals("req_sent")){

                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendsReqDatabase.child(userId).child(mCurrentUser.getUid())
                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mProfileSendRequest.setEnabled(true);
                                            mCurrentState = "not_friends";
                                            mProfileSendRequest.setText("Send Friend Request");

                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);
                                        }
                                    });
                                }
                            });

                }


                // ------------------ REQ RECEIVED STATE-------------

                if(mCurrentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId).setValue(currentDate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(userId).child(mCurrentUser.getUid()).setValue
                                    (currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendsReqDatabase.child(userId).child(mCurrentUser.getUid())
                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mProfileSendRequest.setEnabled(true);
                                                            mCurrentState = "friends";
                                                            mProfileSendRequest.setText("Unfriend this Person");

                                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                                            mDeclineBtn.setEnabled(false);
                                                        }
                                                    });
                                                }
                                            });


                                }
                            });
                        }
                    });

                }

                //--------------------UNFRIEND STATE
                if (mCurrentState.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendRequest.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendRequest.setText("Send friend request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });
                            // mFriendsReqDatabase.child(mCurrentUser.getUid()).child(userId).child("request_type")
                }
            }
        });

    }
}
