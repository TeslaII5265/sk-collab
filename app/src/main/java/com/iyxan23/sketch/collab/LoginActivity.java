package com.iyxan23.sketch.collab;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private boolean isRegister = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Used to send user to MainActivity
        Intent mainActivityIntent = new Intent(this, MainActivity.class);

        // The Login Button
        Button loginButton = findViewById(R.id.login_button);

        // Switching login and register texts
        TextView loginText = findViewById(R.id.login_text);
        TextView registerText = findViewById(R.id.register_text);

        // Check if user has logged in or not
        if (auth.getCurrentUser() != null) {
            // User has logged in!
            // Send him to MainActivity
            startActivity(mainActivityIntent);

            // Finish the activity so the user cannot go back
            // to this activity using the back button
            finish();
        }

        // onClicks ================================================================================
        loginText.setOnClickListener(v -> {
            isRegister = false;

            // updateForm to remove the username text field
            updateForm();
        });

        registerText.setOnClickListener(v -> {
            isRegister = true;

            // updateForm to display the username text field
            updateForm();
        });

        EditText emailEditText    = findViewById(R.id.email_login_text);
        EditText passwordEditText = findViewById(R.id.password_login_text);
        EditText usernameEditText = findViewById(R.id.username_login_text);

        usernameEditText.setVisibility(View.GONE);

        TextView errorText = findViewById(R.id.errorText_login);

        // Focus on Email EditText
        emailEditText.requestFocus();

        loginButton.setOnClickListener(v -> {
            // Check if the inputs are filled
            String res = checkFields();

            if (res == null) {
                // Great, we can login / register now
                if (isRegister) {

                    // Register user's account
                    auth.createUserWithEmailAndPassword(
                            emailEditText.getText().toString().trim(),
                            passwordEditText.getText().toString()
                    ).addOnSuccessListener(s -> {
                        // Get the firebase database
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference usersRef = database.getReference("/userdata/");

                        // Build the user's data
                        HashMap<String, Object> userdata = new HashMap<String, Object>();
                        userdata.put("name", usernameEditText.getText().toString());
                        userdata.put("uid", s.getUser().getUid());

                        // Add the user in the database
                        usersRef.setValue(userdata);

                        // Done! Redirect user to MainActivity
                        startActivity(mainActivityIntent);

                        // Finish the activity so the user cannot go back
                        // to this activity using the back button
                        finish();

                    }).addOnFailureListener(f -> {
                        // Something went wrong..
                        errorText.setText(f.getMessage());
                    });
                } else {
                    // Login
                    auth.signInWithEmailAndPassword(
                            emailEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    ).addOnSuccessListener(s -> {
                        // Success! redirect user to the MainActivity
                        startActivity(mainActivityIntent);
                        finishActivity(0);

                    }).addOnFailureListener(f -> {
                        // Something went wrong!
                        // Show a snackbar
                        errorText.setText(f.getMessage());
                    });
                }
            } else {
                // Something doesn't seem right
                errorText.setText(res);
            }
        });
    }

    // Used to update the form, when the user clicked on "Login" or "Register", Display or Remove a
    // view
    private void updateForm() {
        // Switching login and register texts
        TextView loginText = findViewById(R.id.login_text);
        TextView registerText = findViewById(R.id.register_text);

        // Other stuff
        ConstraintLayout rootLogin = findViewById(R.id.root_login);
        EditText usernameEditText = findViewById(R.id.username_login_text);

        Button loginButton = findViewById(R.id.login_button);

        if (isRegister) {
            // Change the color and sizes of the "tabs"
            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            registerText.setTextColor(0xFFFFFF);

            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            loginText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLogin);
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.username_login, ConstraintSet.BOTTOM);
            constraintSet.applyTo(rootLogin);

            // Add / Display the username edit text, because we're registering
            usernameEditText.setVisibility(View.VISIBLE);

            // Change the text on the login button
            loginButton.setText("Register");

        } else {
            // Change the color and sizes of the "tabs"
            loginText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            loginText.setTextColor(0xFFFFFF);

            registerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            registerText.setTextColor(0x747474);

            // Email EditText animation
            TransitionManager.beginDelayedTransition(rootLogin);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLogin);
            constraintSet.connect(R.id.email_login, ConstraintSet.TOP, R.id.textView3, ConstraintSet.BOTTOM);
            constraintSet.applyTo(rootLogin);

            // Remove the username edit text, because we're logging in
            usernameEditText.setVisibility(View.GONE);

            // Change the text on the login button
            loginButton.setText("Login");
        }
    }

    // Used to check fields, like if email is empty, password empty, etc.
    private String checkFields() {
        String email = ((EditText) findViewById(R.id.email_login_text)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.password_login_text)).getText().toString();
        String username = ((EditText) findViewById(R.id.username_login_text)).getText().toString().trim();

        String res = null;

        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String usernameRegex = "[a-zA-Z0-9._-]+";

        // EMAIL_EMPTY
        if (email.equals(""))
            res = "Email cannot be empty";

        // PASSWORD_EMPTY
        if (password.equals(""))
            res = "Password cannot be empty";

        // USERNAME_EMPTY
        if (username.equals("") && !isRegister)
            res = "Username cannot be empty";

        // PASSWORD_LESS_THAN_6
        if (password.length() < 6)
            res = "Password cannot be less than 6 characters";

        // EMAIL_INVALID
        if (!email.matches(emailRegex))
            res = "Email is invalid";

        // USERNAME_INVALID
        if (!username.matches(usernameRegex))
            res = "Username can only contain A-Z a-z 0-9 -_.";

        return res;
    }
}