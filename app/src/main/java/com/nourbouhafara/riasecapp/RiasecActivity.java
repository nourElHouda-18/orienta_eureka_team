package com.nourbouhafara.riasecapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiasecActivity extends AppCompatActivity {

    private final int[] answers = new int[48];
    private int currentIndex = 0;
    private boolean restoringSelection = false;

    private TextView tvQuestionProgress;
    private TextView tvProgressPercent;
    private TextView tvQuestionText;
    private ProgressBar progressQuestions;
    private ProgressBar progressLoading;
    private RadioGroup radioGroup;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private RadioButton radio4;
    private RadioButton radio5;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<RiasecQuestion> questions = Arrays.asList(
            new RiasecQuestion("R1", "Test the quality of parts before shipment"),
            new RiasecQuestion("R2", "Lay brick or tile"),
            new RiasecQuestion("R3", "Work on an offshore oil-drilling rig"),
            new RiasecQuestion("R4", "Assemble electronic parts"),
            new RiasecQuestion("R5", "Operate a grinding machine in a factory"),
            new RiasecQuestion("R6", "Fix a broken faucet"),
            new RiasecQuestion("R7", "Assemble products in a factory"),
            new RiasecQuestion("R8", "Install flooring in houses"),

            new RiasecQuestion("I1", "Study the structure of the human body"),
            new RiasecQuestion("I2", "Study animal behavior"),
            new RiasecQuestion("I3", "Do research on plants or animals"),
            new RiasecQuestion("I4", "Develop a new medical treatment or procedure"),
            new RiasecQuestion("I5", "Conduct biological research"),
            new RiasecQuestion("I6", "Study whales and other types of marine life"),
            new RiasecQuestion("I7", "Work in a biology lab"),
            new RiasecQuestion("I8", "Make a map of the bottom of an ocean"),

            new RiasecQuestion("A1", "Conduct a musical choir"),
            new RiasecQuestion("A2", "Direct a play"),
            new RiasecQuestion("A3", "Design artwork for magazines"),
            new RiasecQuestion("A4", "Write a song"),
            new RiasecQuestion("A5", "Write books or plays"),
            new RiasecQuestion("A6", "Play a musical instrument"),
            new RiasecQuestion("A7", "Perform stunts for a movie or television show"),
            new RiasecQuestion("A8", "Design sets for plays"),

            new RiasecQuestion("S1", "Give career guidance to people"),
            new RiasecQuestion("S2", "Do volunteer work at a non-profit organization"),
            new RiasecQuestion("S3", "Help people who have problems with drugs or alcohol"),
            new RiasecQuestion("S4", "Teach an individual an exercise routine"),
            new RiasecQuestion("S5", "Help people with family-related problems"),
            new RiasecQuestion("S6", "Supervise the activities of children at a camp"),
            new RiasecQuestion("S7", "Teach children how to read"),
            new RiasecQuestion("S8", "Help elderly people with their daily activities"),

            new RiasecQuestion("E1", "Sell restaurant franchises to individuals"),
            new RiasecQuestion("E2", "Sell merchandise at a department store"),
            new RiasecQuestion("E3", "Manage the operations of a hotel"),
            new RiasecQuestion("E4", "Operate a beauty salon or barber shop"),
            new RiasecQuestion("E5", "Manage a department within a large company"),
            new RiasecQuestion("E6", "Manage a clothing store"),
            new RiasecQuestion("E7", "Sell houses"),
            new RiasecQuestion("E8", "Run a toy store"),

            new RiasecQuestion("C1", "Generate the monthly payroll checks for an office"),
            new RiasecQuestion("C2", "Inventory supplies using a hand-held computer"),
            new RiasecQuestion("C3", "Use a computer program to generate customer bills"),
            new RiasecQuestion("C4", "Maintain employee records"),
            new RiasecQuestion("C5", "Compute and record statistical and other numerical data"),
            new RiasecQuestion("C6", "Operate a calculator"),
            new RiasecQuestion("C7", "Handle customers' bank transactions"),
            new RiasecQuestion("C8", "Keep shipping and receiving records")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riasec);

        AppCompatImageButton btnBack = findViewById(R.id.btnBackRiasec);
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        progressQuestions = findViewById(R.id.progressQuestions);
        progressLoading = findViewById(R.id.progressLoading);
        radioGroup = findViewById(R.id.radioGroupScore);
        radio1 = findViewById(R.id.radioScore1);
        radio2 = findViewById(R.id.radioScore2);
        radio3 = findViewById(R.id.radioScore3);
        radio4 = findViewById(R.id.radioScore4);
        radio5 = findViewById(R.id.radioScore5);
        btnPrevious = findViewById(R.id.btnPreviousQuestion);
        btnNext = findViewById(R.id.btnNextQuestion);

        btnBack.setOnClickListener(v -> finish());
        btnPrevious.setOnClickListener(v -> previousQuestion());
        btnNext.setOnClickListener(v -> nextQuestion());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (restoringSelection) return;
            int score = scoreForCheckedId(checkedId);
            if (score > 0) {
                answers[currentIndex] = score;
                btnNext.setEnabled(true);
            }
        });

        showQuestion();
    }

    private void showQuestion() {
        RiasecQuestion question = questions.get(currentIndex);
        int questionNumber = currentIndex + 1;
        int percent = Math.round(questionNumber * 100f / questions.size());

        tvQuestionProgress.setText(String.format(Locale.US, "Question %d of %d", questionNumber, questions.size()));
        tvProgressPercent.setText(String.format(Locale.US, "%d%%", percent));
        tvQuestionText.setText(question.text);
        progressQuestions.setProgress(percent);
        btnPrevious.setVisibility(currentIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(currentIndex == questions.size() - 1 ? "See my results" : "Next");

        restoringSelection = true;
        radioGroup.clearCheck();
        int saved = answers[currentIndex];
        if (saved > 0) {
            radioGroup.check(idForScore(saved));
        }
        restoringSelection = false;
        btnNext.setEnabled(saved > 0);
    }

    private void previousQuestion() {
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    private void nextQuestion() {
        if (answers[currentIndex] == 0) {
            Toast.makeText(this, "Choose an answer first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            runModel();
        }
    }

    private void runModel() {
        btnNext.setEnabled(false);
        btnPrevious.setEnabled(false);
        progressLoading.setVisibility(View.VISIBLE);

        final float[] scores = calculateScores();
        final float[] features = buildFeatures(scores);
        final String hollandCode = buildHollandCode(scores);

        executor.execute(() -> {
            try (RiasecOnnxModelHelper helper = new RiasecOnnxModelHelper(getApplicationContext())) {
                RiasecOnnxModelHelper.PredictionResult prediction = helper.predict(features);
                float[] probabilities = prediction.getProbabilities();
                int[] topIndices = topThree(probabilities);

                AppStorage.saveResult(
                        getApplicationContext(),
                        scores,
                        hollandCode,
                        probabilities,
                        topIndices
                );

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, RiasecResultsActivity.class);
                    intent.putExtra(RiasecResultsActivity.EXTRA_RIASEC_SCORES, scores);
                    intent.putExtra(RiasecResultsActivity.EXTRA_HOLLAND_CODE, hollandCode);
                    intent.putExtra(RiasecResultsActivity.EXTRA_DOMAIN_PROBABILITIES, probabilities);
                    intent.putExtra(RiasecResultsActivity.EXTRA_TOP_DOMAIN_INDICES, topIndices);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception ex) {
                runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    btnNext.setEnabled(true);
                    btnPrevious.setEnabled(true);
                    Toast.makeText(
                            this,
                            "Could not run the RIASEC model: " + ex.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private float[] calculateScores() {
        float[] scores = new float[6];
        for (int dimension = 0; dimension < 6; dimension++) {
            int start = dimension * 8;
            float sum = 0f;
            for (int i = start; i < start + 8; i++) {
                sum += answers[i];
            }
            scores[dimension] = sum / 8f;
        }
        return scores;
    }

    private float[] buildFeatures(float[] scores) {
        float[] features = new float[54];
        for (int i = 0; i < 48; i++) {
            features[i] = answers[i];
        }
        System.arraycopy(scores, 0, features, 48, 6);
        return features;
    }

    private String buildHollandCode(float[] scores) {
        Character[] letters = {'R', 'I', 'A', 'S', 'E', 'C'};
        Integer[] indices = {0, 1, 2, 3, 4, 5};

        Arrays.sort(indices, Comparator.comparingDouble((Integer i) -> scores[i]).reversed());
        return "" + letters[indices[0]] + letters[indices[1]] + letters[indices[2]];
    }

    private int[] topThree(float[] probabilities) {
        Integer[] indices = new Integer[probabilities.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble((Integer i) -> probabilities[i]).reversed());
        return new int[]{indices[0], indices[1], indices[2]};
    }

    private int scoreForCheckedId(int checkedId) {
        if (checkedId == R.id.radioScore1) return 1;
        if (checkedId == R.id.radioScore2) return 2;
        if (checkedId == R.id.radioScore3) return 3;
        if (checkedId == R.id.radioScore4) return 4;
        if (checkedId == R.id.radioScore5) return 5;
        return 0;
    }

    private int idForScore(int score) {
        switch (score) {
            case 1: return R.id.radioScore1;
            case 2: return R.id.radioScore2;
            case 3: return R.id.radioScore3;
            case 4: return R.id.radioScore4;
            case 5: return R.id.radioScore5;
            default: return View.NO_ID;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private static final class RiasecQuestion {
        final String code;
        final String text;

        RiasecQuestion(String code, String text) {
            this.code = code;
            this.text = text;
        }
    }
}
