package com.nourbouhafara.riasecapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Small local-only storage layer used by the hackathon prototype.
 *
 * Authentication is intentionally offline: one local account is stored on the device.
 * The password itself is never stored; only a SHA-256 digest is persisted.
 */
public final class AppStorage {

    private static final String PREFS = "riasec_app_prefs";

    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_ACCOUNT_EXISTS = "account_exists";
    private static final String KEY_LOGGED_IN = "logged_in";

    private AppStorage() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean registerUser(
            Context context,
            String firstName,
            String lastName,
            String email,
            String password
    ) {
        if (firstName == null || lastName == null || email == null || password == null) {
            return false;
        }

        String cleanFirst = firstName.trim();
        String cleanLast = lastName.trim();
        String cleanEmail = normalizeEmail(email);

        if (cleanFirst.isEmpty() || cleanLast.isEmpty() || cleanEmail.isEmpty() || password.isEmpty()) {
            return false;
        }

        prefs(context).edit()
                .putString(KEY_FIRST_NAME, cleanFirst)
                .putString(KEY_LAST_NAME, cleanLast)
                .putString(KEY_EMAIL, cleanEmail)
                .putString(KEY_PASSWORD_HASH, hashPassword(password))
                .putBoolean(KEY_ACCOUNT_EXISTS, true)
                .putBoolean(KEY_LOGGED_IN, true)
                // A newly registered local account should not inherit another user's result.
                .remove("scores")
                .remove("holland_code")
                .remove("probabilities")
                .remove("top_indices")
                .remove("has_result")
                .apply();
        return true;
    }

    public static boolean hasAccount(Context context) {
        return prefs(context).getBoolean(KEY_ACCOUNT_EXISTS, false)
                && !getEmail(context).isEmpty()
                && !prefs(context).getString(KEY_PASSWORD_HASH, "").isEmpty();
    }

    public static boolean login(Context context, String email, String password) {
        if (!hasAccount(context) || email == null || password == null) {
            return false;
        }

        String savedEmail = getEmail(context);
        String savedHash = prefs(context).getString(KEY_PASSWORD_HASH, "");

        boolean valid = savedEmail.equals(normalizeEmail(email))
                && savedHash.equals(hashPassword(password));

        if (valid) {
            prefs(context).edit().putBoolean(KEY_LOGGED_IN, true).apply();
        }
        return valid;
    }

    public static boolean isLoggedIn(Context context) {
        return hasAccount(context) && prefs(context).getBoolean(KEY_LOGGED_IN, false);
    }

    public static void logout(Context context) {
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public static void saveProfile(Context context, String firstName, String lastName, String email) {
        prefs(context).edit()
                .putString(KEY_FIRST_NAME, firstName.trim())
                .putString(KEY_LAST_NAME, lastName.trim())
                .putString(KEY_EMAIL, normalizeEmail(email))
                .apply();
    }

    public static String getFirstName(Context context) {
        return prefs(context).getString(KEY_FIRST_NAME, "");
    }

    public static String getLastName(Context context) {
        return prefs(context).getString(KEY_LAST_NAME, "");
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, "");
    }

    public static boolean hasProfile(Context context) {
        return !getFirstName(context).isEmpty()
                && !getLastName(context).isEmpty()
                && !getEmail(context).isEmpty();
    }

    public static void saveResult(
            Context context,
            float[] scores,
            String hollandCode,
            float[] probabilities,
            int[] topIndices
    ) {
        prefs(context).edit()
                .putString("scores", join(scores))
                .putString("holland_code", hollandCode)
                .putString("probabilities", join(probabilities))
                .putString("top_indices", join(topIndices))
                .putBoolean("has_result", true)
                .apply();
    }

    public static boolean hasResult(Context context) {
        return prefs(context).getBoolean("has_result", false);
    }

    public static float[] getScores(Context context) {
        return parseFloats(prefs(context).getString("scores", ""), 6);
    }

    public static String getHollandCode(Context context) {
        return prefs(context).getString("holland_code", "");
    }

    public static float[] getProbabilities(Context context) {
        return parseFloats(
                prefs(context).getString("probabilities", ""),
                RiasecOnnxModelHelper.DOMAINS.length
        );
    }

    public static int[] getTopIndices(Context context) {
        return parseInts(prefs(context).getString("top_indices", ""), 3);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(encoded.length * 2);
            for (byte b : encoded) {
                builder.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 is required by every Android runtime. Re-throw if a broken runtime ever lacks it.
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String join(float[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(values[i]);
        }
        return builder.toString();
    }

    private static String join(int[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(values[i]);
        }
        return builder.toString();
    }

    private static float[] parseFloats(String value, int expectedLength) {
        if (value == null || value.isEmpty()) return null;
        String[] parts = value.split(",");
        if (parts.length != expectedLength) return null;

        try {
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i]);
            }
            return result;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int[] parseInts(String value, int expectedLength) {
        if (value == null || value.isEmpty()) return null;
        String[] parts = value.split(",");
        if (parts.length != expectedLength) return null;

        try {
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i]);
            }
            return result;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
