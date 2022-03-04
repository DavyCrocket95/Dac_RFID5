package com.example.dac_rfid5;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // Méthode de gestion du clic sur le bouton
    public void startSignUpActivity(View view) {
        Log.i(TAG, "startSignUpActivity: ");
        signUpActivity();
    }

    private final ActivityResultLauncher<Intent> signLauncher =
            registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                        @Override
                        public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                            onSignResult(result);
                        }
                    }
            );

    private void onSignResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // On est connecté
            // Récupérer le nom de l'utilsateur connecté
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        } else {
            // Pas connecté et pourquoi ? ...
            Log.i(TAG, "onSignResult: Pas dde connection");
        }
    }

    private void signUpActivity() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_background)
                //.setTheme(R.style.LoginTheme)
                //.setTosAndPrivacyPolicyUrls("https://google.fr", "https://samuelvialle.com")
                .setIsSmartLockEnabled(true)
                .build();

        signLauncher.launch(signInIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignInActivity.this,
                    MainActivity.class));
        }
    }

}