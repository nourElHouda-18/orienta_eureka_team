# Validation report

Validation performed before packaging this project:

- XML parsing: all Android manifest/resource XML files parsed successfully.
- Resource reference check: Java `R.id`, `R.layout`, and `R.drawable` references resolve to project resources.
- Manifest activity check: every declared local activity class exists.
- Authentication flow check:
  - Login is the launcher activity.
  - Registration validates first name, last name, email, password length, and confirmation.
  - Registration creates the local profile and logged-in session.
  - MainActivity redirects to Login when no session is active.
  - Logout clears the session but keeps profile/result data.
  - Profile edits update the name/last name/email used by login/profile display.
- Model artifact check:
  - Exact uploaded CBM SHA-256: `63c537b71f69c048dedbfb4158dc373d8a2c3875d68ccd4bcbed19a2b9ffa549`.
  - The CBM inside `model_artifacts/` has the same SHA-256.
  - CatBoost reports 54 feature names and they match `riasec_features.json` exactly.
  - Seven model classes match the seven domain labels.
  - ONNX file under `model_artifacts/` is byte-identical to the Android asset.
  - Java ONNX probability handling uses `OnnxSequence -> OnnxMap`, matching ONNX Runtime's Java API for CatBoost classifier probability output.

A full Gradle `assembleDebug` could not be completed in the packaging environment because the Gradle wrapper attempted to download Gradle 8.11.1 from `services.gradle.org`, and that environment has no external network access. Android Studio with internet access can perform that dependency download/sync normally.
