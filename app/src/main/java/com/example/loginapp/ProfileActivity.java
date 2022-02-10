package com.example.loginapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import com.example.loginapp.databinding.ActivityProfileBinding;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    //View binding/that interacts with views.
    private ActivityProfileBinding binding;
    private String accountName, accountEmail, accountPicture;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private AccessToken accessToken;

    final static String FACEBOOK_TAG = "GET_DATA_FROM_FACEBOOK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        /**
         * for identifying authenticated users
         */
        accessToken = AccessToken.getCurrentAccessToken();

        /**
         *get the data from account that signed in
         */
        getDataFromGoogleAccount();
        getDataFromFacebookAccount();

        binding.profileSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private void getDataFromGoogleAccount() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account!=null) {

            /**
             * get data
             */
            accountName = account.getDisplayName();
            accountEmail = account.getEmail();
            accountPicture = String.valueOf(account.getPhotoUrl());

            /**
             * set data
             */
            setDateIntoFields();
        }
    }

    private void getDataFromFacebookAccount() {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            /**
                             * get data
                             */
                            accountEmail = object.getString("email");
                            accountName = object.getString("name");
                            accountPicture = object.getJSONObject("picture").getJSONObject("data").getString("url");

                            /**
                             * set data
                             */
                            setDateIntoFields();
                        } catch (JSONException e) {
                            Toast.makeText(ProfileActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(FACEBOOK_TAG, "onCompleted: Error ( "+e.getMessage()+") ");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setDateIntoFields() {
        binding.profileName.setText(accountName);
        binding.profileEmail.setText(accountEmail);
        Picasso.get()
                .load(accountPicture)
                .placeholder(R.drawable.ic_baseline_person_24_gray)
                .into(binding.profilePicture);
    }

    private void signOut() {
        /**
         * to sign out, we have to get the client /google
         */
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //go back to Main activity
                navigateToMainActivity();
            }
        });

        /**
         * Facebook
         */
        LoginManager.getInstance().logOut();
    }

    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}