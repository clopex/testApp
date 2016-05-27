package com.morestudio.littledot.doctor.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.morestudio.littledot.doctor.R;
import com.morestudio.littledot.doctor.api.SipManager;

public class ResultActivity extends Activity {

    private Button m_btnSetting;
    private Button m_btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
