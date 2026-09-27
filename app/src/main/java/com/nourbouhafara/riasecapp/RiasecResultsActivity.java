package com.nourbouhafara.riasecapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class RiasecResultsActivity extends AppCompatActivity {

    public static final String EXTRA_RIASEC_SCORES = "riasec_scores";
    public static final String EXTRA_HOLLAND_CODE = "holland_code";
    public static final String EXTRA_DOMAIN_PROBABILITIES = "domain_probabilities";
    public static final String EXTRA_TOP_DOMAIN_INDICES = "top_domain_indices";

    private float[] scores;
    private String hollandCode;
    private float[] probabilities;
    private int[] topIndices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riasec_results);

        readData();
        if (!hasValidData()) {
            Toast.makeText(this, "No RIASEC result is available yet.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViewById(R.id.btnBackResults).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btnRetake).setOnClickListener(v -> {
            startActivity(new Intent(this, RiasecActivity.class));
            finish();
        });
        findViewById(R.id.btnAskAssistant).setOnClickListener(v ->
                startActivity(new Intent(this, ChatAssistantActivity.class))
        );

        bindResults();
    }

    private void readData() {
        scores = getIntent().getFloatArrayExtra(EXTRA_RIASEC_SCORES);
        hollandCode = getIntent().getStringExtra(EXTRA_HOLLAND_CODE);
        probabilities = getIntent().getFloatArrayExtra(EXTRA_DOMAIN_PROBABILITIES);
        topIndices = getIntent().getIntArrayExtra(EXTRA_TOP_DOMAIN_INDICES);

        if (scores == null) scores = AppStorage.getScores(this);
        if (hollandCode == null || hollandCode.isEmpty()) hollandCode = AppStorage.getHollandCode(this);
        if (probabilities == null) probabilities = AppStorage.getProbabilities(this);
        if (topIndices == null) topIndices = AppStorage.getTopIndices(this);
    }

    private boolean hasValidData() {
        return scores != null
                && scores.length == 6
                && hollandCode != null
                && hollandCode.length() >= 3
                && probabilities != null
                && probabilities.length == RiasecOnnxModelHelper.DOMAINS.length
                && topIndices != null
                && topIndices.length >= 3;
    }

    private void bindResults() {
        ((TextView) findViewById(R.id.tvHollandCode)).setText(hollandCode);
        ((TextView) findViewById(R.id.tvProfileDescription)).setText(buildProfileDescription());

        bindScore(R.id.tvScoreR, R.id.progressScoreR, scores[0]);
        bindScore(R.id.tvScoreI, R.id.progressScoreI, scores[1]);
        bindScore(R.id.tvScoreA, R.id.progressScoreA, scores[2]);
        bindScore(R.id.tvScoreS, R.id.progressScoreS, scores[3]);
        bindScore(R.id.tvScoreE, R.id.progressScoreE, scores[4]);
        bindScore(R.id.tvScoreC, R.id.progressScoreC, scores[5]);

        bindDomain(0, R.id.tvDomain1, R.id.tvDomainProbability1, R.id.progressDomain1, R.id.tvDomainDescription1);
        bindDomain(1, R.id.tvDomain2, R.id.tvDomainProbability2, R.id.progressDomain2, R.id.tvDomainDescription2);
        bindDomain(2, R.id.tvDomain3, R.id.tvDomainProbability3, R.id.progressDomain3, R.id.tvDomainDescription3);
    }

    private void bindScore(int textId, int progressId, float score) {
        ((TextView) findViewById(textId)).setText(String.format(Locale.US, "%.2f", score));
        ProgressBar progress = findViewById(progressId);
        progress.setMax(500);
        progress.setProgress(Math.round(score * 100f));
    }

    private void bindDomain(int rank, int titleId, int probabilityId, int progressId, int descriptionId) {
        int classIndex = topIndices[rank];
        String domain = RiasecOnnxModelHelper.DOMAINS[classIndex];
        float probability = probabilities[classIndex];

        ((TextView) findViewById(titleId)).setText(domain);
        ((TextView) findViewById(probabilityId)).setText(String.format(Locale.US, "%.1f%%", probability * 100f));
        ((TextView) findViewById(descriptionId)).setText(getDomainDescription(domain));

        ProgressBar progress = findViewById(progressId);
        progress.setMax(1000);
        progress.setProgress(Math.round(probability * 1000f));
    }

    private String buildProfileDescription() {
        return dimensionName(hollandCode.charAt(0))
                + " • "
                + dimensionName(hollandCode.charAt(1))
                + " • "
                + dimensionName(hollandCode.charAt(2));
    }

    private String dimensionName(char code) {
        switch (code) {
            case 'R': return "Realistic";
            case 'I': return "Investigative";
            case 'A': return "Artistic";
            case 'S': return "Social";
            case 'E': return "Enterprising";
            case 'C': return "Conventional";
            default: return "";
        }
    }

    private String getDomainDescription(String domain) {
        switch (domain) {
            case "STEM":
                return "Science, technology, engineering, computing and analytical fields.";
            case "Health":
                return "Health sciences, healthcare and wellbeing.";
            case "Business & Management":
                return "Business, economics, entrepreneurship and management.";
            case "Social & Public Sciences":
                return "Psychology, law, society and public affairs.";
            case "Humanities & Communication":
                return "Languages, humanities, media and communication.";
            case "Arts & Design":
                return "Creative expression, visual communication and design.";
            case "Education":
                return "Teaching, learning and educational development.";
            default:
                return "A domain that may be worth exploring based on your interests.";
        }
    }
}
