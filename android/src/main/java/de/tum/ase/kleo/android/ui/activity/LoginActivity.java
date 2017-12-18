package de.tum.ase.kleo.android.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.ase.kleo.android.KleoApplication;
import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.android.client.AuthenticationException;
import de.tum.ase.kleo.android.client.BackendClient;
import de.tum.ase.kleo.android.util.UiUtils;
import de.tum.ase.kleo.android.util.Validator;

public class LoginActivity extends Activity {

    // TODO: Use ProgressBar instead or some 3th party library
    private ProgressDialog loggingInDialog;
    private BackendClient backendClient;

    private EditText emailView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.backendClient = ((KleoApplication) getApplication()).backendClient();

        if (backendClient.isAuthenticated()) {
            proceed();
            return;
        }

        emailView = findViewById(R.id.loginEmail);
        passwordView = findViewById(R.id.loginPassword);
        loggingInDialog = new ProgressDialog(LoginActivity.this);
        loggingInDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loggingInDialog.setMessage(getString(R.string.logging_in));
        loggingInDialog.setIndeterminate(true);
        loggingInDialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.loginSubmit).setOnClickListener(v ->
                LoginActivity.this.authenticate());
    }

    private void authenticate() {
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        String emailValidationResult = Validator.validateEmail(email, getString(R.string.empty_email), getString(R.string.incorrect_email));
        String passwordValidationResult = Validator.validatePassword(password, getString(R.string.empty_password), getString(R.string.incorrect_password));

        UiUtils.hideKeyboard(LoginActivity.this, findViewById(R.id.loginSubmit));

        if (emailValidationResult != null) {
            emailView.setError(emailValidationResult);
        } else if (passwordValidationResult != null) {
            passwordView.setError(passwordValidationResult);
        } else {
            // TODO: 1. Do not use threads. Instead you can use (at least) async tasks!!
            // TODO: 2. This is potential memory leak, when user rotates a device during of the network request it will leak a activity. Thread should be interrupted!!
            new Thread(() -> {
                try {
                    LoginActivity.this.runOnUiThread(() -> loggingInDialog.show());
                    backendClient.authenticate(email, password);
                    proceed();
                } catch (AuthenticationException e) {
                    LoginActivity.this.runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show());
                } finally {
                    LoginActivity.this.runOnUiThread(() -> loggingInDialog.dismiss());
                }
            }).start();
        }
    }

    private void proceed() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loggingInDialog != null && loggingInDialog.isShowing()) {
            loggingInDialog.cancel();
        }
    }
}
