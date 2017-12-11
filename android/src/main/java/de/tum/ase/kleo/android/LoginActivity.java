package de.tum.ase.kleo.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.ase.kleo.android.client.AuthenticationException;
import de.tum.ase.kleo.android.client.BackendClient;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LoginActivity extends Activity {

    private ProgressDialog loggingInDialog;
    private BackendClient backendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.backendClient = ((KleoApplication) getApplication()).backendClient();

        loggingInDialog = new ProgressDialog(LoginActivity.this);
        loggingInDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loggingInDialog.setMessage(getString(R.string.logging_in));
        loggingInDialog.setIndeterminate(true);
        loggingInDialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.loginSubmit).setOnClickListener(v -> LoginActivity.this.authenticate());
    }

    private void authenticate() {
        final String email = ((EditText) findViewById(R.id.loginEmail)).getText().toString();
        final String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();

        if (isBlank(email)) {
            Toast.makeText(getApplicationContext(), R.string.empty_email, Toast.LENGTH_LONG).show();
        } else if (isBlank(password)) {
            Toast.makeText(getApplicationContext(), R.string.empty_password, Toast.LENGTH_LONG).show();
        } else {
            new Thread(() -> {
                try {
                    LoginActivity.this.runOnUiThread(() -> loggingInDialog.show());
                    backendClient.authenticate(email, password);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } catch (AuthenticationException e) {
                    LoginActivity.this.runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    LoginActivity.this.runOnUiThread(() -> loggingInDialog.dismiss());
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loggingInDialog.isShowing()) {
            loggingInDialog.cancel();
        }
    }
}
