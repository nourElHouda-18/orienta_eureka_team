package com.nourbouhafara.riasecapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvProfileSummary;
    private TextView tvLastResult;
    private MaterialButton btnViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppStorage.isLoggedIn(this)) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvProfileSummary = findViewById(R.id.tvProfileSummary);
        tvLastResult = findViewById(R.id.tvLastResult);
        btnViewResult = findViewById(R.id.btnViewResult);

        findViewById(R.id.btnStartRiasec).setOnClickListener(v ->
                startActivity(new Intent(this, RiasecActivity.class))
        );

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        btnViewResult.setOnClickListener(v ->
                startActivity(new Intent(this, RiasecResultsActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AppStorage.isLoggedIn(this)) {
            openLogin();
            return;
        }
        refreshDashboard();
    }

    private void refreshDashboard() {
        String firstName = AppStorage.getFirstName(this);
        String lastName = AppStorage.getLastName(this);
        String email = AppStorage.getEmail(this);

        tvGreeting.setText("Hi, " + firstName + " 👋");
        tvProfileSummary.setText(firstName + " " + lastName + "\n" + email);

        if (AppStorage.hasResult(this)) {
            String code = AppStorage.getHollandCode(this);
            tvLastResult.setText("Last Holland code: " + code);
            btnViewResult.setVisibility(View.VISIBLE);
        } else {
            tvLastResult.setText("Complete the 48-item test to see your RIASEC profile and Top-3 domains.");
            btnViewResult.setVisibility(View.GONE);
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("Your profile and RIASEC result stay saved on this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    AppStorage.logout(this);
                    openLogin();
                })
                .show();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
