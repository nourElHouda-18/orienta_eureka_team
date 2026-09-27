package com.nourbouhafara.riasecapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppStorage.isLoggedIn(this)) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilLoginEmail);
        tilPassword = findViewById(R.id.tilLoginPassword);
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);

        if (AppStorage.hasAccount(this)) {
            etEmail.setText(AppStorage.getEmail(this));
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvCreateAccount).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }


    private void attemptLogin() {
        String email = textOf(etEmail);
        String password = textOf(etPassword);

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valid = true;
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Enter your password");
            valid = false;
        }
        if (!valid) return;

        if (!AppStorage.hasAccount(this)) {
            tilEmail.setError("No local account yet. Create one first.");
            return;
        }

        if (!AppStorage.login(this, email, password)) {
            tilPassword.setError("Email or password is incorrect");
            return;
        }

        openMain();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
