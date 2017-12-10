package de.tum.ase.kleo.android;

import android.app.Activity;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.loginSubmit).setOnClickListener(new LoginSubmitListener());
    }

    public class LoginSubmitListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String email = ((EditText) findViewById(R.id.loginEmail)).getText().toString();
            String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();
        }
    }
}
