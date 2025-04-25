package com.kelyandev.fluxbiz.Auth.Devices;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class DeviceManager {

    public static void checkDeviceValidity(String userId, String deviceId, final DeviceCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docId = userId + "::" + deviceId;
        db.collection("devices")
                .document(docId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    callback.onDeviceValid(snapshot.exists());
                    Log.d("UserVerificationProcess", "Session Checked successfully !");
                })
                .addOnFailureListener(e -> callback.onDeviceValid(false));
    }

    public interface DeviceCheckCallback {
        void onDeviceValid(boolean isValid);
    }
}
