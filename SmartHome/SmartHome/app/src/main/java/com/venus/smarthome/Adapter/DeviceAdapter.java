package com.venus.smarthome.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.venus.smarthome.Fragments.EditDeviceFragment;
import com.venus.smarthome.Modal.Device;
import com.venus.smarthome.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private FirebaseAuth auth;
    private DatabaseReference refDevice;
    private String uid;
    private Context context;
    private List<Device> deviceList;
    private Map<String, Integer> seekbarValues;

    public DeviceAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        refDevice = FirebaseDatabase.getInstance().getReference("users").child(uid).child("devices");
        seekbarValues = new HashMap<>();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {

        if (position < 0 || position >= deviceList.size())
            return;

        Device device = deviceList.get(position);

        holder.cardView.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.txtDeviceName);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_device, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_edit) {
                    showEditDeviceFragment(device);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    showDeleteConfirmation(device);
                    return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });

        holder.txtDeviceName.setText(device.getName());

        boolean isDeviceOn = device.getStatus() != 0;
        if (device.getId().equals("servo"))
            holder.txtStatus.setText(isDeviceOn ? "MỞ" : "ĐÓNG");
        else
            holder.txtStatus.setText(isDeviceOn ? "BẬT" : "TẮT");
        holder.txtStatus.setTextColor(isDeviceOn ? Color.parseColor("#006E2C") : Color.parseColor("#BA1A1A"));
        holder.switchDevice.setChecked(isDeviceOn);
        holder.sliderDevice.setEnabled(isDeviceOn);

        holder.sliderDevice.setTag(device.getId());

        if (device.getType().equals("adjust")) {
            holder.sliderDevice.setVisibility(View.VISIBLE);
            holder.sliderDevice.setValue(device.getSpeed());
            if (device.getSpeed() > 0)
                holder.txtStatus.setText(device.getSpeed() + "%");

            Integer seekbarValue = seekbarValues.get(device.getId());
            if (seekbarValue != null) {
                holder.sliderDevice.setValue(seekbarValue);
                holder.txtStatus.setText(seekbarValue + "%");
            }

            holder.sliderDevice.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    int newSpeed = Math.round(holder.sliderDevice.getValue());
                    String deviceId = (String) holder.sliderDevice.getTag();
                    refDevice.child(deviceId).child("speed").setValue(newSpeed);
                    refDevice.child(deviceId).child("status").setValue(newSpeed);
                }
            });
        } else {
            holder.sliderDevice.setVisibility(View.GONE);
        }

        holder.switchDevice.setOnClickListener(view -> {
            if (device.getStatus() == 0) {
                if (device.getType().equals("adjust")) {
                    if (device.getSpeed() == 0) {
                        refDevice.child(device.getId()).child("status").setValue(100);
                        refDevice.child(device.getId()).child("speed").setValue(100);
                    } else
                        refDevice.child(device.getId()).child("status").setValue(device.getSpeed());
                } else
                    refDevice.child(device.getId()).child("status").setValue(1);
            } else {
                if (device.getType().equals("adjust"))
                    refDevice.child(device.getId()).child("speed").setValue(device.getStatus());
                refDevice.child(device.getId()).child("status").setValue(0);
            }
        });
    }

    private void showEditDeviceFragment(Device device) {
        EditDeviceFragment fragment = EditDeviceFragment.newInstance(device);
        AppCompatActivity activity = (AppCompatActivity) context;
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    private void showDeleteConfirmation(Device device) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Bạn có muốn xóa thiết bị không?")
                .setMessage("Thiết bị sẽ bị xóa vĩnh viễn và không thể khôi phục lại được. Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    refDevice.child(device.getId()).removeValue();
                    Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Không", (dialog, which) -> {})
                .show();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView txtDeviceName, txtStatus;
        MaterialSwitch switchDevice;
        Slider sliderDevice;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            switchDevice = itemView.findViewById(R.id.switchDevice);
            sliderDevice = itemView.findViewById(R.id.sliderDevice);
        }
    }
}
