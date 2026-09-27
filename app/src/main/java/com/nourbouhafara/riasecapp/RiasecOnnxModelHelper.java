package com.nourbouhafara.riasecapp;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxMap;
import ai.onnxruntime.OnnxSequence;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/** Runs the user's exact final 54-feature CatBoost model, exported from CBM to ONNX. */
public final class RiasecOnnxModelHelper implements AutoCloseable {

    private static final String MODEL_NAME = "riasec_catboost_final.onnx";
    private static final int FEATURE_COUNT = 54;

    public static final String[] DOMAINS = {
            "Arts & Design",
            "Business & Management",
            "Education",
            "Health",
            "Humanities & Communication",
            "STEM",
            "Social & Public Sciences"
    };

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String inputName;

    public RiasecOnnxModelHelper(Context context) throws IOException, OrtException {
        environment = OrtEnvironment.getEnvironment();
        byte[] modelBytes = readAsset(context);
        try (OrtSession.SessionOptions options = new OrtSession.SessionOptions()) {
            session = environment.createSession(modelBytes, options);
        }
        inputName = session.getInputNames().iterator().next();
    }

    public PredictionResult predict(float[] features) throws OrtException {
        if (features == null || features.length != FEATURE_COUNT) {
            throw new IllegalArgumentException("The RIASEC model requires exactly 54 features.");
        }

        long[] shape = {1, FEATURE_COUNT};

        try (
                OnnxTensor tensor = OnnxTensor.createTensor(
                        environment,
                        FloatBuffer.wrap(features),
                        shape
                );
                OrtSession.Result result = session.run(
                        Collections.singletonMap(inputName, tensor)
                )
        ) {
            float[] probabilities = extractProbabilities(result);
            int predictedClass = 0;
            for (int i = 1; i < probabilities.length; i++) {
                if (probabilities[i] > probabilities[predictedClass]) {
                    predictedClass = i;
                }
            }
            return new PredictionResult(predictedClass, probabilities);
        }
    }

    private float[] extractProbabilities(OrtSession.Result result) throws OrtException {
        for (int i = 0; i < result.size(); i++) {
            OnnxValue output = result.get(i);

            // Some classifiers return probabilities directly as a tensor.
            if (output instanceof OnnxTensor) {
                Object value = output.getValue();

                if (value instanceof float[][]) {
                    float[][] matrix = (float[][]) value;
                    if (matrix.length > 0 && matrix[0].length == DOMAINS.length) {
                        return matrix[0].clone();
                    }
                }

                if (value instanceof float[]) {
                    float[] vector = (float[]) value;
                    if (vector.length == DOMAINS.length) {
                        return vector.clone();
                    }
                }
            }

            /*
             * CatBoost's ONNX classifier export returns the probability output as
             * seq(map(int64, float)). In ONNX Runtime Java, OnnxSequence#getValue()
             * returns a List<OnnxValue>; its element is OnnxMap, not java.util.Map.
             */
            if (output instanceof OnnxSequence) {
                List<? extends OnnxValue> sequenceValues = ((OnnxSequence) output).getValue();

                if (!sequenceValues.isEmpty() && sequenceValues.get(0) instanceof OnnxMap) {
                    OnnxMap onnxMap = (OnnxMap) sequenceValues.get(0);
                    Map<?, ?> probabilityMap = onnxMap.getValue();
                    float[] probabilities = probabilitiesFromMap(probabilityMap);
                    if (probabilities != null) {
                        return probabilities;
                    }
                }
            }

            // Handle a direct map output too, for completeness.
            if (output instanceof OnnxMap) {
                Map<?, ?> probabilityMap = ((OnnxMap) output).getValue();
                float[] probabilities = probabilitiesFromMap(probabilityMap);
                if (probabilities != null) {
                    return probabilities;
                }
            }
        }

        throw new IllegalStateException(
                "Could not read class probabilities from the ONNX model output."
        );
    }

    private float[] probabilitiesFromMap(Map<?, ?> probabilityMap) {
        if (probabilityMap == null || probabilityMap.isEmpty()) {
            return null;
        }

        float[] probabilities = new float[DOMAINS.length];
        boolean found = false;

        for (Map.Entry<?, ?> entry : probabilityMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof Number && value instanceof Number) {
                int classIndex = ((Number) key).intValue();
                if (classIndex >= 0 && classIndex < probabilities.length) {
                    probabilities[classIndex] = ((Number) value).floatValue();
                    found = true;
                }
            }
        }

        return found ? probabilities : null;
    }

    private static byte[] readAsset(Context context) throws IOException {
        try (
                InputStream inputStream = context.getAssets().open(MODEL_NAME);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }

    @Override
    public void close() throws OrtException {
        session.close();
    }

    public static final class PredictionResult {
        private final int predictedClass;
        private final float[] probabilities;

        public PredictionResult(int predictedClass, float[] probabilities) {
            this.predictedClass = predictedClass;
            this.probabilities = probabilities.clone();
        }

        public int getPredictedClass() {
            return predictedClass;
        }

        public float[] getProbabilities() {
            return probabilities.clone();
        }
    }
}
