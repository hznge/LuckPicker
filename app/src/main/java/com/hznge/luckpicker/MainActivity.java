package com.hznge.luckpicker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final Intent mAccessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private TextView mServiceStatusView;
    private Button mPickBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mServiceStatusView = (TextView) findViewById(R.id.text_accessible);

        mPickBtn = (Button) findViewById(R.id.pickMoney_MainActivity);
        mPickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mAccessibleIntent);
            }
        });
        updateServiceStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void updateServiceStatus() {
        boolean isServiceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info :
                accessibilityServices) {
            if (info.getId().equals(getPackageName() + ".services.RedPacketService")) {
                isServiceEnabled = true;
            }
        }

        mServiceStatusView.setText(isServiceEnabled ? "Open" : "Close");
    }


}
