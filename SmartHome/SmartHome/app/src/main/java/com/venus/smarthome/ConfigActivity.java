package com.venus.smarthome;

import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.venus.smarthome.Modal.User;
import com.venus.smarthome.Utils.LoadingDialog;

import java.util.Objects;

public class ConfigActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference dbRef, refUser, refDevice;
    String uid;
    LoadingDialog loadingDialog;
    ImageView imgButtonBack;
    WebView webView;
    TextView txtLoading, txtInfo, txtConfig;
    Button btnComplete;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        auth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        dbRef =  FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(uid);
        refDevice = refUser.child("devices");
        loadingDialog = new LoadingDialog(this);
        imgButtonBack = findViewById(R.id.imgButtonBack);
        txtConfig = findViewById(R.id.txtConfig);
        txtInfo = findViewById(R.id.txtInfo);
        txtLoading = findViewById(R.id.txtLoading);
        btnComplete = findViewById(R.id.btnComplete);
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url = "http://192.168.4.1/wifi?uid=" + uid;

        imgButtonBack.setOnClickListener(view -> finish());
        btnComplete.setOnClickListener(view -> {
            startActivity(new Intent(ConfigActivity.this, MainActivity.class));
            finish();
        });

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                long firebaseTime = user.getTime();
                long currentTime = System.currentTimeMillis() / 1000;
                long timeDifference = currentTime - firebaseTime;
                if (user.getStatus().equals("connected") && timeDifference < 5) {
                    isConnected = true;
                    Toast.makeText(ConfigActivity.this, "Thiết lập thành công!", Toast.LENGTH_LONG).show();
                    txtLoading.setVisibility(View.VISIBLE);
                    txtLoading.setText("Thiết lập hoàn tất!");
                    btnComplete.setVisibility(View.VISIBLE);
                } else
                    refUser.child("status").setValue("connecting");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            txtLoading.setText("Vui lòng bật Wi-Fi");
            Toast.makeText(getApplicationContext(), "Vui lòng bật Wi-Fi", Toast.LENGTH_SHORT).show();
            return;
        }

        txtLoading.setVisibility(View.VISIBLE);

        autoConnectWifi(url);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100 && webView.getUrl().equals("http://192.168.4.1/wifisave") && isWifiSmartHome())
                    webView.loadUrl(url);
            }
        });

    }

    private void autoConnectWifi(String url) {
        if (isConnected)
            return;
        WifiUtils.withContext(getApplicationContext())
                .connectWith("Smart Home", "88888888")
                .setTimeout(20000)
                .onConnectionResult(new ConnectionSuccessListener() {
                    @Override
                    public void success() {
                        txtLoading.setVisibility(GONE);
                        webView.setVisibility(View.VISIBLE);
                        webView.loadUrl(url);
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                                webView.setVisibility(GONE);
                                Toast.makeText(ConfigActivity.this, "Vẫn đang nỗ lực thiết lập!", Toast.LENGTH_LONG).show();
                            }
                        });
                        if (webView.getUrl().contains("http://192.168.4.1/wifi?uid=") && !isWifiSmartHome())
                            Toast.makeText(ConfigActivity.this, "Kết nối tới Wifi Smart Home thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failed(@NonNull ConnectionErrorCode errorCode) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            if (isConnected)
                                return;
                            autoConnectWifi(url);
                            txtLoading.setText("Đang kết nối đến Wifi Smart Home. Hãy đảm bảo Wifi Smart Home được bật. Nếu kết nối quá lâu hãy kết nối tới Wifi bằng phương pháp thủ công!");
                            Toast.makeText(ConfigActivity.this, "Vẫn đang nỗ lực kết nối tới Wifi Smart Home!", Toast.LENGTH_SHORT).show();
                        }, 500);
                    }
                })
                .start();
    }

    private boolean isWifiSmartHome() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID().equals("\"Smart Home\"");
    }

}