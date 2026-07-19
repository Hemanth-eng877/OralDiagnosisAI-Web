package com.oraldiagnosis.ai;

import org.junit.Test;
import static org.junit.Assert.*;

import com.oraldiagnosis.ai.tflite.TFLiteHelper;

public class PredictionResultUnitTest {
    @Test
    public void predictionResult_NormalCreation() {
        TFLiteHelper.PredictionResult res = new TFLiteHelper.PredictionResult("Normal", 99.1f);
        assertEquals("Normal", res.label);
        assertEquals(99.1f, res.confidence, 0.001f);
    }

    @Test
    public void predictionResult_OsccCreation() {
        TFLiteHelper.PredictionResult res = new TFLiteHelper.PredictionResult("OSCC", 88.4f);
        assertEquals("OSCC", res.label);
        assertEquals(88.4f, res.confidence, 0.001f);
    }
}
