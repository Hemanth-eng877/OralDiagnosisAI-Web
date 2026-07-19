package com.oraldiagnosis.ai;

import android.app.Application;
import com.oraldiagnosis.ai.firebase.FirebaseManager;

public class OralDiagnosisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseManager.init(this);
    }
}
