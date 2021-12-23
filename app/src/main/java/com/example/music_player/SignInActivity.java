package com.example.music_player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class SignInActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText email;
    EditText password;
    EditText confirmPassword;
    TextView forgotPassword;
    TextView errorSignIn;
    TextView createNewAccount;
    Button login;
    Button testMode;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Music_Player_NoActionBar2);
        setContentView(R.layout.sign_in_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Sign In");
        setSupportActionBar(toolbar);

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        email = findViewById(R.id.Email);
        password = findViewById(R.id.Password);
        confirmPassword = findViewById(R.id.ConfirmPassword);
        forgotPassword = findViewById(R.id.ForgotPassword);
        login = findViewById(R.id.LogIn);
        errorSignIn = findViewById(R.id.ErrorSignin);
        createNewAccount = findViewById(R.id.CreateNewAccount);
        testMode = findViewById(R.id.TestMode);

        confirmPassword.setVisibility(View.GONE);
        errorSignIn.setVisibility(View.GONE);
        forgotPassword.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(login.getText().equals("Sign In")){
                    try{
                        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                getPlayInstance().editor.putString("email", email.getText().toString());
                                getPlayInstance().editor.putString("password", password.getText().toString());
                                getPlayInstance().editor.commit();
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errorSignIn.setText("Error Signing In");
                                errorSignIn.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    catch(Exception ex){
                        errorSignIn.setText("Error Signing In");
                        errorSignIn.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    if(password.getText().toString().equals(confirmPassword.getText().toString())){
                        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                getPlayInstance().editor.putString("email", email.getText().toString());
                                getPlayInstance().editor.putString("password", password.getText().toString());
                                getPlayInstance().editor.commit();
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errorSignIn.setText("Error Creating Account");
                                errorSignIn.setVisibility(View.GONE);
                            }
                        });
                    }
                    else{
                        errorSignIn.setText("Password Must Match");
                        errorSignIn.setVisibility(View.GONE);
                    }
                }
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmPassword.setVisibility(View.VISIBLE);
                errorSignIn.setVisibility(View.GONE);
                forgotPassword.setVisibility(View.GONE);
                createNewAccount.setVisibility(View.GONE);
                login.setText("Create Account");
            }
        });

        testMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlayInstance().editor.putString("email", "rosswarrenalexander@gmail.com");
                getPlayInstance().editor.putString("password", "992472153072");
                getPlayInstance().editor.commit();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
