package com.example.signatext.ml;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.Log;
import android.widget.TextView;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.PoseLandmark;

public class HandRecognitionProcessor {
    private static final String TAG = "HandRecognition";
    private final PoseDetector poseDetector;
    private final TextView textView;

    public HandRecognitionProcessor(TextView textView) {
        this.textView = textView;
        AccuratePoseDetectorOptions options =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);
    }

    @SuppressLint("UnsafeOptInUsageError")
    public void processImage(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    String detectedLetter = detectarLetra(pose);
                    textView.post(() -> textView.setText(detectedLetter));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error en el reconocimiento", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String detectarLetra(Pose pose) {
        // Usamos landmarks de la mano derecha.
        PoseLandmark wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark thumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark index = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);

        if (wrist == null || thumb == null || index == null) {
            Log.e(TAG, "Faltan landmarks");
            return "Mano no detectada";
        }

        float likelihoodThreshold = 0.5f;
        if (wrist.getInFrameLikelihood() < likelihoodThreshold ||
                thumb.getInFrameLikelihood() < likelihoodThreshold ||
                index.getInFrameLikelihood() < likelihoodThreshold) {
            Log.e(TAG, "Baja probabilidad en los landmarks");
            return "Mano no detectada";
        }

        PointF wristPos = wrist.getPosition();
        PointF thumbPos = thumb.getPosition();
        PointF indexPos = index.getPosition();


        if (distance(thumbPos, wristPos) > 40 && distance(indexPos, wristPos) > 40) {
            float dThumbIndex = distance(thumbPos, indexPos);
            if (dThumbIndex >= 20 && dThumbIndex < 40) {

                PointF mid = new PointF((thumbPos.x + indexPos.x) / 2, (thumbPos.y + indexPos.y) / 2);

                float threshold = 20f;
                float diffY = mid.y - wristPos.y;
                if (diffY > threshold) {
                    return "A";  // Apertura hacia abajo.
                } else if (diffY < -threshold) {
                    return "U";  // Apertura hacia arriba.
                } else {
                    return "C";  // Apertura neutra.
                }
            }
        }
        return "No reconocido";
    }

    private float distance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}