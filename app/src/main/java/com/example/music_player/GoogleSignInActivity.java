package com.example.music_player;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class GoogleSignInActivity extends AppCompatActivity {
    Toolbar toolbar;

    ImageView GoogleImage;
    TextView GoogleName;
    TextView GoogleEmail;
    TextView GoogleID;
    SignInButton GoogleSignInButton;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.google_sign_in_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        GoogleImage = findViewById(R.id.GoogleImage);
        GoogleName = findViewById(R.id.GoogleName);
        GoogleEmail = findViewById(R.id.GoogleEmailAddress);
        GoogleSignInButton = findViewById(R.id.GoogleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        getPlayInstance().account = GoogleSignIn.getLastSignedInAccount(this);
        if(getPlayInstance().account != null) {
            setAccountInfo();
        }

        GoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 100);
            }
        });
    }

    void setAccountInfo(){
        Glide.with(this).load(getPlayInstance().account.getPhotoUrl()).into(GoogleImage);
        GoogleName.setText(getPlayInstance().account.getDisplayName());
        GoogleEmail.setText(getPlayInstance().account.getEmail());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                getPlayInstance().account = task.getResult(ApiException.class);
                setAccountInfo();
            }
            catch(Exception ex){

            }
        }
    }
}