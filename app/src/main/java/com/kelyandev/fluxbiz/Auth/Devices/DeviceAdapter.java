package com.kelyandev.fluxbiz.Auth.Devices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kelyandev.fluxbiz.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<DeviceModel> deviceList;
    private OnDeviceActionListener listener;

    /**
     * Constructor of DeviceAdapter
     * @param deviceList The list containing the devices
     */
    public DeviceAdapter(List<DeviceModel> deviceList) {
        this.deviceList = deviceList;
    }

    /**
     * Create a new instance of DeviceViewHolder when needed
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new instance of DeviceViewHolder
     */
    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new DeviceAdapter.DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.DeviceViewHolder holder, int position) {
        DeviceModel device = deviceList.get(position);

        holder.deviceName.setText(device.getDeviceName());
        holder.deviceLastConnexion.setText(String.format("Dernière connexion: %s", device.getLastLogin()));
        holder.deviceGlobalLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.deviceGlobalLayout);
            popup.inflate(R.menu.device_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.disconnectDevice) {
                    listener.onDisconnectClick(deviceList.get(holder.getAdapterPosition()));
                    return true;
                }
                return false;
            });
            popup.show();
        });

    }

    /**
     * Return the total number of items in the Reply list
     * @return The size of the reply list
     */
    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /**
     * View holder class for replies, holding references to each UI elements
     */
    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceLastConnexion;
        ConstraintLayout deviceGlobalLayout;

        /**
         * Constructor for DeviceViewHolder
         * @param itemview The root view of each item in the recycler view
         */
        public DeviceViewHolder(@NonNull View itemview) {
            super(itemview);
            deviceName = itemview.findViewById(R.id.DeviceId);
            deviceLastConnexion = itemview.findViewById(R.id.DeviceLastConnexion);
            deviceGlobalLayout = itemview.findViewById(R.id.deviceItem);
        }
    }

    /**
     * Interface to access the Disconnect action
     */
    public interface OnDeviceActionListener {
        void onDisconnectClick(DeviceModel device);
    }

    /**
     * Sets the device actions listener
     * @param listener The new listener
     */
    public void setOnDeviceActionListener(OnDeviceActionListener listener) {
        this.listener = listener;
    }

    /**
     * Sets a new devices list
     * @param newDeviceList The new devices list
     */
    public void setDevices(List<DeviceModel> newDeviceList) {
        this.deviceList = newDeviceList;
        notifyDataSetChanged();
    }
}
