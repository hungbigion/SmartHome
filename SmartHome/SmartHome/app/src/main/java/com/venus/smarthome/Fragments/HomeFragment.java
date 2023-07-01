package com.venus.smarthome.Fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venus.smarthome.Adapter.DeviceAdapter;
import com.venus.smarthome.ConfigActivity;
import com.venus.smarthome.Modal.Device;
import com.venus.smarthome.Modal.User;
import com.venus.smarthome.R;
import com.venus.smarthome.Utils.OpenWeatherIcons;
import com.venus.smarthome.Utils.UnitConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.interaapps.localweather.LocalWeather;
import de.interaapps.localweather.Weather;
import de.interaapps.localweather.utils.Lang;
import de.interaapps.localweather.utils.LocationFailedEnum;
import de.interaapps.localweather.utils.Units;

public class HomeFragment extends Fragment {
    private Handler handler;
    private Runnable runnable;
    private boolean isShowDialog = false;
    private final int TIME_EXPIRE = 10;
    private FirebaseAuth auth;
    private DatabaseReference dbRef, refUser, refDevice;
    private String uid;
    private ImageView imgWifiStatus, imgWeather;
    private TextView textView, txtCity, txtTemp, txtWindSpeed, txtDescription;
    private ExtendedFloatingActionButton extended_fab;
    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private List<Device> deviceList;
    private LocalWeather localWeather;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewDevice);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));

        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(getContext(), deviceList);
        recyclerView.setAdapter(deviceAdapter);

        auth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(uid);
        refDevice = refUser.child("devices");

        imgWifiStatus = view.findViewById(R.id.imgWifiStatus);
        imgWeather = view.findViewById(R.id.imgWeather);
        textView = view.findViewById(R.id.textView);
        txtCity = view.findViewById(R.id.txtCity);
        txtTemp = view.findViewById(R.id.txtTemp);
        txtWindSpeed = view.findViewById(R.id.txtWindSpeed);
        txtDescription = view.findViewById(R.id.txtDescription);
        extended_fab = view.findViewById(R.id.extended_fab);

        localWeather = new LocalWeather(requireActivity(), "eac69daf73e74ec894eb7c307685393c");
        localWeather.setUseCurrentLocation(true);
        localWeather.setUpdateCurrentLocation(true);
        localWeather.lang = Lang.VIETNAMESE;
        localWeather.unit = Units.METRIC;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                compareTime();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    textView.setText("Chào " + user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        refDevice.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                if (device != null) {
                    deviceList.add(device);
                    int position = deviceList.size() - 1;
                    deviceAdapter.notifyItemInserted(position);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Device device = snapshot.getValue(Device.class);
                if (device == null || deviceList == null || deviceList.isEmpty())
                    return;
                for (int i = 0; i < deviceList.size(); i++) {
                    if (Objects.equals(device.getId(), deviceList.get(i).getId())) {
                        deviceList.set(i, device);
                        deviceAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Device device = snapshot.getValue(Device.class);
                if (device == null || deviceList == null || deviceList.isEmpty())
                    return;
                for (int i = 0; i < deviceList.size(); i++) {
                    if (Objects.equals(device.getId(), deviceList.get(i).getId())) {
                        deviceList.remove(deviceList.get(i));
                        deviceAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        localWeather.setWeatherCallback(new LocalWeather.WeatherCallback() {
            @Override
            public void onSuccess(@NonNull Weather weather) {
                new OpenWeatherIcons(getContext(), weather.getIcons()[0], imgWeather);
                String unit = UnitConverter.getTemperatureUnit(localWeather.unit);
                String descriptions = weather.getDescriptions()[0];
                String capitalizedDescription = descriptions.substring(0, 1).toUpperCase() + descriptions.substring(1);
                txtCity.setText(weather.getCountry());
                txtDescription.setText(capitalizedDescription);
                txtTemp.setText(UnitConverter.formatTemperature(weather.getMaxTemperature()) + unit);
                txtWindSpeed.setText("Gió: " + UnitConverter.formatWindSpeed(weather.getWindSpeed()) + "km/h");
            }

            @Override
            public void onFailure(Throwable exception) {
                Log.e("Weather fetching", exception.getMessage());
            }
        });

        localWeather.fetchCurrentLocation(new LocalWeather.CurrentLocationCallback() {
            @Override
            public void onSuccess(@NonNull Location location) {
                localWeather.fetchCurrentWeatherByLocation(location);
            }

            @Override
            public void onFailure(@NonNull LocationFailedEnum failed) {
                Log.e("Location fetching", failed.toString());
            }
        });

        extended_fab.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new AddDeviceFragment()).commit());
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    private void startTimer() {
        handler.postDelayed(runnable, 5000);
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    private void compareTime() {
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        long firebaseTime = user.getTime();
                        long currentTime = System.currentTimeMillis() / 1000;
                        long timeDifference = currentTime - firebaseTime;
                        if ("new account".equals(user.getStatus()) && !isShowDialog) {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                            builder.setTitle("Thông báo")
                                    .setMessage("Bạn sẽ không thể điều khiển thiết bị từ xa được nếu chưa thiết lập dữ liệu giữa ứng dụng và phần cứng Smart Home!.\n" +
                                            "Thiết lập ngay?")
                                    .setPositiveButton("Thiết lập", (dialog, which) -> {
                                        isShowDialog = false;
                                        startActivity(new Intent(getActivity(), ConfigActivity.class));
                                    })
                                    .setOnCancelListener(dialog -> isShowDialog = false)
                                    .show();
                            isShowDialog = true;
                            imgWifiStatus.setBackgroundResource(R.drawable.ic_wifi_off);
                            TypedValue typedValue = new TypedValue();
                            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorError, typedValue, true);
                            imgWifiStatus.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), typedValue.resourceId));
                        } else if (timeDifference >= TIME_EXPIRE && !isShowDialog) {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                            builder.setTitle("Mất kết nối")
                                    .setMessage("Thiết bị của bạn đã bị mất kết nối.\n" +
                                            "Hãy thiết lập lại để tiếp tục sử dụng.")
                                    .setPositiveButton("Thiết lập", (dialog, which) -> {
                                        isShowDialog = false;
                                        startActivity(new Intent(getActivity(), ConfigActivity.class));
                                    })
                                    .setOnCancelListener(dialog -> isShowDialog = false)
                                    .show();
                            isShowDialog = true;
                            imgWifiStatus.setBackgroundResource(R.drawable.ic_wifi_off);
                            TypedValue typedValue = new TypedValue();
                            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorError, typedValue, true);
                            imgWifiStatus.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), typedValue.resourceId));
                        }
                        if (timeDifference < TIME_EXPIRE){
                            imgWifiStatus.setBackgroundResource(R.drawable.ic_wifi);
                            TypedValue typedValue = new TypedValue();
                            requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
                            imgWifiStatus.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), typedValue.resourceId));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
