package com.example.loginapp;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import com.example.loginapp.databinding.ActivityMainBinding;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private CallbackManager callbackManager;

    final static String FACEBOOK_TAG = "FACEBOOK_LOGIN";
    final static int GOOGLE_REQUEST_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /**
         *   To check whether the user has logged in before or not,
         *   if yes the app will run directly on the profile activity
         *   otherwise it will start with the activity logged in
         */
        checkCurrentUser();

        /**
         * for manage the callbacks into the FacebookSdk
         */
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        /**
                         * login successfully, so go to user profile
                         */
                        navigateToProfileActivity();
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(FACEBOOK_TAG, "onError: "+exception.getMessage());
                    }
                });

        binding.facebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile"));
            }
        });

        /**
         * to configure the Auth.GOOGLE_SIGN_IN_API
         */
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /**
         * for interacting with the Google Sign In API
         */
        gsc = GoogleSignIn.getClient(this,gso);

        binding.googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    private void checkCurrentUser() {
        /**
         * for identifying authenticated users
         */
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        /**
         * for get the last singed in account from the Class that holds the basic account information of the signed in Google user
         */
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null || (accessToken != null && !accessToken.isExpired())) {
            navigateToProfileActivity();
        }
    }

    private void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,GOOGLE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                /**
                 * successfully logged in so go to profile activity
                 */
                task.getResult(ApiException.class);
                navigateToProfileActivity();
            } catch (ApiException e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
        finish();
    }
}