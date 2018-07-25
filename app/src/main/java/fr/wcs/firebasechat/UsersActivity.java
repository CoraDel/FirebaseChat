package fr.wcs.firebasechat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        // on veut recuperer les données qui appartient a Users


        // Réglage de la Recycler View attention au dependencies à ajouter
        mUsersList= (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        // Une RecyclerView a besoin d'une Model Class ! Attention dans le model les attributs doivent etre ecrit
        //comme dans la Firebase !

    }

    //lire les données Firebase, besoin d'une onStart method
    @Override
    public void onStart() {
        super.onStart();

        // On lui passe la Model class et le ViewHolder, dans les parentheses du UserViewHolder passer la class Model,
        // le layout item ou single user qui sert de model à l'item et la ViewHolder et la référence Firebase
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {
                // set values to viewHolder
                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setImage(getApplicationContext(), users.getImage());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);


    }

    // Une FirebaseRecycleView fonctionne toujours avec un ViewHolder
    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;


        public UsersViewHolder(View itemView) {
            super(itemView);
            //Obligé de créer un constructeur pour la ViewHolder
            mView = itemView;
        }

        // setters a mettre directement dans la meme classe que le ViewHolder et PopulateView
        // dans les setters on attributs les valeurs de nos text de l'item model
        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.tv_user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = (TextView) mView.findViewById(R.id.tv_user_single_status);
            userStatus.setText(status);
        }
        public void setImage(Context ctx, String image){
            ImageView post_image = mView.findViewById(R.id.iv_users_single_img);
            Picasso.get().load(image).into(post_image);
        }
    }
}
