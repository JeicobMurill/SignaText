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

        // Se usa el modo preciso de detección de poses
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

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        poseDetector.process(image)
                .addOnSuccessListener(pose -> {
                    String letraDetectada = detectarLetra(pose);
                    textView.post(() -> textView.setText(letraDetectada));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error en el reconocimiento de la mano", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private String detectarLetra(Pose pose) {
        PoseLandmark wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark thumbTip = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark index = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
        PoseLandmark pinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY); // Se usa para verificar alineación

        if (thumbTip == null || index == null || pinky == null) {
            Log.e(TAG, "No se detectaron todos los landmarks");
            return "Mano no detectada";
        }

        PointF thumbTipPos = thumbTip.getPosition();
        PointF indexPos = index.getPosition();
        PointF pinkyPos = pinky.getPosition();
        PointF wristPos = wrist != null ? wrist.getPosition() : new PointF(0, 0);

        if (isFistClosed(thumbTipPos, indexPos, wristPos)) {
            return "A"; // Puño cerrado
        } else if (isBShape(indexPos, pinkyPos, thumbTipPos, wristPos)) {
            return "B"; // Letra B
        } else if (isCShape(indexPos, thumbTipPos)) {
            return "C"; // Letra C
        }

        return "No reconocido";
    }

    private boolean isFistClosed(PointF thumb, PointF index, PointF wrist) {
        return distance(thumb, wrist) < 50 && distance(index, wrist) < 50;
    }

    private boolean isBShape(PointF index, PointF pinky, PointF thumb, PointF wrist) {
        // La "B" se detecta si:
        // - El índice y el meñique están alineados horizontalmente
        // - El pulgar está más abajo que la muñeca (doblado)
        return Math.abs(index.y - pinky.y) < 30 && thumb.y > wrist.y;
    }

    private boolean isCShape(PointF index, PointF thumb) {
        return distance(index, thumb) < 50;
    }

    private float distance(PointF p1, PointF p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}