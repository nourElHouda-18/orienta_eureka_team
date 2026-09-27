# RIASEC ONNX probability-output bug fix

Fixed the Android inference error:

`Could not run the RIASEC model: Could not read class probabilities from the ONNX model output.`

## Cause

CatBoost's ONNX classifier export returns `probabilities` as `seq(map(int64, float))`.
ONNX Runtime Java exposes that as an `OnnxSequence` containing `OnnxMap` values, not as a plain Java `List<Map<...>>`.

The previous helper called `getValue()` first and then checked whether the sequence element was a Java `Map`, so it never recognized the probability output on Android.

## Fix

`RiasecOnnxModelHelper.java` now:

- checks the returned `OnnxValue` type;
- unwraps `OnnxSequence`;
- unwraps the first `OnnxMap`;
- converts class keys 0..6 to the seven domain probabilities;
- still supports direct tensor and direct map outputs for robustness.

No model weights, questions, feature order, or domain mapping were changed.
