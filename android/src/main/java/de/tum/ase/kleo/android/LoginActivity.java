package de.tum.ase.kleo.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.ase.kleo.android.client.AuthenticationException;
import de.tum.ase.kleo.android.client.BackendClient;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LoginActivity extends Activity {

    private ProgressDialog logginInDialog;
    // TODO: configure based on build variant
    private final BackendClient backendClient = new BackendClient("http://192.168.0.11:8080/api/", "kleo-client");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logginInDialog = new ProgressDialog(LoginActivity.this); // this = YourActivity
        logginInDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        logginInDialog.setMessage(getString(R.string.logging_in));
        logginInDialog.setIndeterminate(true);
        logginInDialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.loginSubmit).setOnClickListener(new LoginSubmitListener());
    }

    public class LoginSubmitListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            final String email = ((EditText) findViewById(R.id.loginEmail)).getText().toString();
            final String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();

            if (isBlank(email)) {
                Toast.makeText(getApplicationContext(), R.string.empty_email, Toast.LENGTH_LONG).show();
                return;
            } else if (isBlank(password)) {
                Toast.makeText(getApplicationContext(), R.string.empty_password, Toast.LENGTH_LONG).show();
                return;
            }

            new Thread(() -> {
                try {
                    LoginActivity.this.runOnUiThread(() -> logginInDialog.show());
                    backendClient.authenticate(email, password);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } catch (AuthenticationException e) {
                    LoginActivity.this.runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    LoginActivity.this.runOnUiThread(() -> logginInDialog.dismiss());
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (logginInDialog != null && logginInDialog.isShowing()) {
            logginInDialog.cancel();
        }
    }
}
