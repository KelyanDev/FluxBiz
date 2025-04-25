package com.kelyandev.fluxbiz.Settings.Security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.Auth.Devices.DeviceAdapter;
import com.kelyandev.fluxbiz.Auth.Devices.DeviceModel;
import com.kelyandev.fluxbiz.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ConnectedSessionsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView textViewDisconnect;
    private TextView CurrentDeviceName, CurrentDeviceLastConnexion;
    private LinearLayout CurrentDeviceLayout;
    private RecyclerView recyclerView;
    private String userId, currentDeviceId;
    private FirebaseFirestore db;
    private List<DeviceModel> otherDevices;
    private DeviceAdapter deviceAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedinstance) {
        View view = inflater.inflate(R.layout.fragment_connected_sessions, container, false);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        // Current Session views
        CurrentDeviceName = view.findViewById(R.id.DeviceId);
        CurrentDeviceLastConnexion = view.findViewById(R.id.DeviceLastConnexion);
        CurrentDeviceLayout = view.findViewById(R.id.currentSession);
        // Recycler view
        recyclerView = view.findViewById(R.id.DevicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Disconnect all sessions button
        textViewDisconnect = view.findViewById(R.id.textViewSessionsDeconnexion);

        getAllSessions(currentUser);
        textViewDisconnect.setOnClickListener(v -> {
            disconnectAllSessions(otherDevices);
        });

        return view;
    }

    /**
     * Gets all the devices the user is logged into
     * @param currentUser The current user
     */
    private void getAllSessions(FirebaseUser currentUser) {
        if (currentUser == null) return;

        userId = currentUser.getUid();
        currentDeviceId = getSafeDeviceId(requireContext());

        db.collection("devices")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    otherDevices = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String deviceId = doc.getString("deviceId");
                        String deviceName = doc.getString("deviceName");
                        Timestamp lastLoginTime = doc.getTimestamp("lastActive");
                        String lastLogin = "";

                        if (lastLoginTime != null) {
                            Date date = lastLoginTime.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            lastLogin = sdf.format(date);
                        }

                        if (deviceId.equals(currentDeviceId)) {
                            CurrentDeviceName.setText(deviceName != null ? deviceName : "Appareil inconnu");
                            continue;
                        }

                        otherDevices.add(new DeviceModel(deviceId, deviceName, lastLogin));
                    }

                    deviceAdapter = new DeviceAdapter(otherDevices);
                    deviceAdapter.setOnDeviceActionListener(device -> {
                        int position = otherDevices.indexOf(device);
                        if (position == -1) return;

                        String deviceDocId = userId + "::" + device.getDeviceId();
                        Log.d("DeviceDeco", deviceDocId);


                        db.collection("devices")
                                .document(deviceDocId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Session déconnectée avec succès !", Toast.LENGTH_SHORT).show();
                                    otherDevices.remove(position);
                                    deviceAdapter.notifyItemRemoved(position);
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("Disconnect", "Erreurs lors de la déconnexion");
                                });
                    });

                    recyclerView.setAdapter(deviceAdapter);

                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erreur de chargement des appareils", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Gets the device ID from the preferences; Generates one if none exist
     * @param context The context
     * @return The device's Id
     */
    private String getSafeDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }

    /**
     * Disconnect all of the other sessions the user is currently logged into.
     * @param devicesList The list of devices.
     */
    private void disconnectAllSessions(List<DeviceModel> devicesList) {
        if (currentUser == null || devicesList == null) return;

        for (DeviceModel device : devicesList) {
            String deviceId = device.getDeviceId();

            db.collection("users")
                    .document(userId)
                    .collection("devices")
                    .document(deviceId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Vos autres sessions ont bien été déconnectés !", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Disconnect", "Erreurs lors de la déconnexion");
                    });
        }

        deviceAdapter.setDevices(new ArrayList<>());
    }

}
