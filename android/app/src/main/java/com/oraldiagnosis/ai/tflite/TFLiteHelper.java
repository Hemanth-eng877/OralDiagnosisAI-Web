package com.oraldiagnosis.ai.tflite;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class TFLiteHelper {
    private Interpreter interpreter;

    public TFLiteHelper(Context context) throws IOException {
        MappedByteBuffer modelFile;
        try {
            modelFile = FileUtil.loadMappedFile(context, "oral_model.tflite");
        } catch (IOException e) {
            modelFile = FileUtil.loadMappedFile(context, "oral.tflite");
        }
        Interpreter.Options options = new Interpreter.Options();
        interpreter = new Interpreter(modelFile, options);
    }

    public PredictionResult predict(Bitmap bitmap) {
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();

        TensorImage tensorImage = new TensorImage(org.tensorflow.lite.DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, 2}, org.tensorflow.lite.DataType.FLOAT32);
        interpreter.run(tensorImage.getBuffer(), probabilityBuffer.getBuffer().rewind());

        float[] results = probabilityBuffer.getFloatArray();
        int classIndex = results[0] > results[1] ? 0 : 1;
        float confidence = results[classIndex] * 100.0f;
        String label = classIndex == 0 ? "Normal" : "OSCC";

        return new PredictionResult(label, confidence);
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class PredictionResult {
        public String label;
        public float confidence;

        public PredictionResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }
}
