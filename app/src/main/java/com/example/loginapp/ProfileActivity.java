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

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private AccessToken accessToken;

    final static String FACEBOOK_TAG = "GET_DATA_FROM_FACEBOOK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //to configure the Auth.GOOGLE_SIGN_IN_API
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //for interacting with the Google Sign In API
        gsc = GoogleSignIn.getClient(this,gso);

        //for identifying authenticated users
        accessToken = AccessToken.getCurrentAccessToken();

        // we get the data from account that signed in
        getDataFromGoogleAccount();
        getDataFromFacebookAccount();

        //for sign out from our profile and return to login activity
        binding.profileSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }


    private void getDataFromGoogleAccount() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account!=null) { //that means >> there ara account signed in

            //get data
            String googleName = account.getDisplayName();
            String googleEmail = account.getEmail();
            String googlePicture = String.valueOf(account.getPhotoUrl());

            //set data
            binding.profileName.setText(googleName);
            binding.profileEmail.setText(googleEmail);

            //picasso to load our picture
            Picasso.get()
                    .load(googlePicture)
                    .placeholder(R.drawable.ic_baseline_person_24_gray)
                    .into(binding.profilePicture);
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
                            //get data
                            String f_email = object.getString("email");
                            String fullName = object.getString("name");
                            String url = object.getJSONObject("picture").getJSONObject("data").getString("url");

                            //set Date
                            binding.profileEmail.setText(f_email);
                            binding.profileName.setText(fullName);
                            Picasso.get()
                                    .load(url)
                                    .placeholder(R.drawable.ic_baseline_person_24_gray)
                                    .into(binding.profilePicture);
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

    private void signOut() {
        //to sign out, we have to get the client /google//
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //go back to Main activity
                navigateToMainActivity();
            }
        });

        //Facebook
        LoginManager.getInstance().logOut();
        navigateToMainActivity();
    }

    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}