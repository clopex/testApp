package com.morestudio.littledot.doctor.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.morestudio.littledot.doctor.R;
import com.morestudio.littledot.doctor.api.SipManager;
import com.morestudio.littledot.doctor.service.SipService;

public class ResultActivity extends Activity {

    SipHome sipHome = new SipHome();
    private final String serviceName = "com.morestudio.littledot.doctor.service.SipService";

    private Button m_btnSetting;
    private Button m_btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //Intent intent = new Intent(ResultActivity.this, PingDocService.class);
        //ResultActivity.this.startService(intent);


        m_btnSetting = (Button)findViewById(R.id.btnSetting);
        m_btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(prefsIntent);
            }
        });
        m_btnLogout = (Button)findViewById(R.id.btnLogout);
        m_btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent serviceIntent = new Intent(ResultActivity.this, SipService.class);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                //serviceIntent.setPackage(ResultActivity.this.getPackageName());
                //serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ResultActivity.this, ResultActivity.class));

                stopService(serviceIntent);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                Log.i("Servis: ", serviceName + isRun());

                /*Intent serviceIntent = new Intent(SipManager.ACTION_DEFER_OUTGOING_UNREGISTER);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(ResultActivity.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ResultActivity.this, ResultActivity.class));
                stopService(serviceIntent);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);*/

                //Intent intent = new Intent(SipManager.ACTION_DEFER_OUTGOING_UNREGISTER);
                //intent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(ResultActivity.this, ResultActivity.class));
                //stopService(intent);
                //sendBroadcast(intent);
                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(intent);
                //finish();
            }
        });
    }

    private boolean isRun() {
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        //Intent intent = new Intent(ResultActivity.this, PingDocService.class);
        //ResultActivity.this.stopService(intent);
        super.onDestroy();
    }
}
