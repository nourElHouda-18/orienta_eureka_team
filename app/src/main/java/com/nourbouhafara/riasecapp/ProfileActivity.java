package com.nourbouhafara.riasecapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilEmail;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmail);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);

        etFirstName.setText(AppStorage.getFirstName(this));
        etLastName.setText(AppStorage.getLastName(this));
        etEmail.setText(AppStorage.getEmail(this));

        findViewById(R.id.btnBackProfile).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String firstName = textOf(etFirstName);
        String lastName = textOf(etLastName);
        String email = textOf(etEmail);

        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);

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

        if (!valid) return;

        AppStorage.saveProfile(this, firstName, lastName, email);
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
