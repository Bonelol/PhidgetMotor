package com.max.phidgetmotor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Bone on 13-5-20.
 */
public class IpActivity extends Activity {
    private EditText ipAddress;
    private Button btnLogin;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        ipAddress = (EditText) findViewById(R.id.txtIp);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IpActivity.this,ClientActivity.class);
                intent.putExtra("IP",ipAddress.getText().toString());
                startActivity(intent);
            }
        });
    }
}