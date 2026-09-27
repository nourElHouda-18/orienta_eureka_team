package com.nourbouhafara.riasecapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFirstName = findViewById(R.id.tilRegisterFirstName);
        tilLastName = findViewById(R.id.tilRegisterLastName);
        tilEmail = findViewById(R.id.tilRegisterEmail);
        tilPassword = findViewById(R.id.tilRegisterPassword);
        tilConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);

        etFirstName = findViewById(R.id.etRegisterFirstName);
        etLastName = findViewById(R.id.etRegisterLastName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void register() {
        String firstName = textOf(etFirstName);
        String lastName = textOf(etLastName);
        String email = textOf(etEmail);
        String password = textOf(etPassword);
        String confirm = textOf(etConfirmPassword);

        clearErrors();
        boolean valid = true;

        if (firstName.isEmpty()) {
            tilFirstName.setError("Enter your first name");
            valid = false;
        }
        if (lastName.isEmpty()) {
            tilLastName.setError("Enter your last name");
            valid = false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Use at least 6 characters");
            valid = false;
        }
        if (!password.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }
        if (!valid) return;

        if (!AppStorage.registerUser(this, firstName, lastName, email, password)) {
            tilEmail.setError("Could not create the local account");
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
