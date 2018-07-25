package fr.wcs.firebasechat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mStatus;
    private Button mSaveBtn;
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(current_uid);



        //AppBar
        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mStatus = (EditText) findViewById(R.id.edText_new_status);
        mSaveBtn = (Button) findViewById(R.id.button_update_btn);


        //recup du putExtra
        String status_value = getIntent().getStringExtra("status_value");
        mStatus.setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status = mStatus.getText().toString();

                //Envoie Firebase plus addOnCompleteListener, changement de texte dans Firebase
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(StatusActivity.this, "Udpate Complete", Toast.LENGTH_SHORT).show();
                            Intent IntentSets = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(IntentSets);

                        } else {
                            Toast.makeText(StatusActivity.this, "Update Fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
