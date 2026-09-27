package com.nourbouhafara.riasecapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class ChatAssistantActivity extends AppCompatActivity {

    private LinearLayout messagesContainer;
    private ScrollView messagesScroll;
    private EditText input;
    private MaterialButton sendButton;
    private TextView modeLabel;
    private View typingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_assistant);

        messagesContainer = findViewById(R.id.chatMessages);
        messagesScroll = findViewById(R.id.chatScroll);
        input = findViewById(R.id.etAssistantMessage);
        sendButton = findViewById(R.id.btnSendAssistant);
        modeLabel = findViewById(R.id.tvAssistantMode);

        findViewById(R.id.btnBackAssistant).setOnClickListener(v -> finish());

        MaterialButton q1 = findViewById(R.id.btnQuestionRiasec);
        MaterialButton q2 = findViewById(R.id.btnQuestionWhy);
        MaterialButton q3 = findViewById(R.id.btnQuestionFields);

        q1.setOnClickListener(v -> sendQuestion(q1.getText().toString()));
        q2.setOnClickListener(v -> sendQuestion(q2.getText().toString()));
        q3.setOnClickListener(v -> sendQuestion(q3.getText().toString()));

        sendButton.setOnClickListener(v -> {
            String question = input.getText().toString().trim();
            if (question.isEmpty()) return;
            input.setText("");
            hideKeyboard();
            sendQuestion(question);
        });

        modeLabel.setText(LlmClient.isConfigured()
                ? "AI mode · " + BuildConfig.OPENROUTER_MODEL
                : "Offline guide · add an OpenRouter key for free-form chat");

        addAssistantMessage("Ask me about RIASEC, your Holland Code, why the model ranked your domains, or examples of fields you can explore.");
    }

    private void sendQuestion(String question) {
        if (TextUtils.isEmpty(question)) return;

        addUserMessage(question);
        setBusy(true);

        String profile = LocalCareerAssistant.buildProfileContext(this);

        if (!LlmClient.isConfigured()) {
            addAssistantMessage(LocalCareerAssistant.answer(this, question));
            setBusy(false);
            return;
        }

        LlmClient.ask(profile, question, new LlmClient.Callback() {
            @Override
            public void onSuccess(String answer) {
                runOnUiThread(() -> {
                    addAssistantMessage(answer);
                    setBusy(false);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    // If the network/API is unavailable, still keep the demo useful.
                    addAssistantMessage(LocalCareerAssistant.answer(ChatAssistantActivity.this, question));
                    setBusy(false);
                    if (!"LLM_NOT_CONFIGURED".equals(message)) {
                        Toast.makeText(ChatAssistantActivity.this,
                                "AI service unavailable; showing the offline explanation.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void addUserMessage(String text) {
        addBubble(text, true);
    }

    private void addAssistantMessage(String text) {
        addBubble(text, false);
    }

    private void addBubble(String text, boolean user) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(15f);
        bubble.setTextColor(ContextCompat.getColor(this, R.color.orienta_text_primary));
        bubble.setLineSpacing(0f, 1.08f);
        bubble.setPadding(dp(16), dp(12), dp(16), dp(12));
        bubble.setBackgroundResource(user ? R.drawable.bg_chat_user : R.drawable.bg_chat_assistant);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = user ? Gravity.END : Gravity.START;
        params.setMargins(user ? dp(48) : 0, dp(8), user ? 0 : dp(48), 0);
        bubble.setLayoutParams(params);

        messagesContainer.addView(bubble);
        scrollToBottom();
    }

    private void setBusy(boolean busy) {
        sendButton.setEnabled(!busy);
        input.setEnabled(!busy);

        if (busy) {
            if (typingView == null) {
                TextView typing = new TextView(this);
                typing.setText("Thinking…");
                typing.setTextSize(13f);
                typing.setTextColor(ContextCompat.getColor(this, R.color.orienta_text_secondary));
                typing.setPadding(dp(12), dp(8), dp(12), dp(8));
                typing.setBackgroundResource(R.drawable.bg_chat_assistant);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, dp(8), dp(64), 0);
                typing.setLayoutParams(params);
                typingView = typing;
                messagesContainer.addView(typingView);
            }
        } else if (typingView != null) {
            messagesContainer.removeView(typingView);
            typingView = null;
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        messagesScroll.post(() -> messagesScroll.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
