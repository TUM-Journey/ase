package de.tum.ase.kleo.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import de.tum.ase.kleo.android.R;
import de.tum.ase.kleo.app.client.BackendClient;
import de.tum.ase.kleo.app.client.Principal;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LoginActivity extends Activity {

    private ProgressDialog loggingInDialog;
    private BackendClient backendClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.backendClient = ((KleoApplication) getApplication()).backendClient();

        if (backendClient.isAuthenticated()) {
            proceed();
            return;
        }

        loggingInDialog = new ProgressDialog(LoginActivity.this);
        loggingInDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loggingInDialog.setMessage(getString(R.string.login_popup_process_label));
        loggingInDialog.setIndeterminate(true);
        loggingInDialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.login_submit_btn).setOnClickListener(v -> LoginActivity.this.authenticate());
    }

    private void authenticate() {
        final String email = ((EditText) findViewById(R.id.login_email_input)).getText().toString();
        final String password = ((EditText) findViewById(R.id.login_password_input)).getText().toString();

        if (isBlank(email)) {
            Toast.makeText(getApplicationContext(), R.string.login_warning_empty_email, Toast.LENGTH_LONG).show();
        } else if (isBlank(password)) {
            Toast.makeText(getApplicationContext(), R.string.login_warning_empty_password, Toast.LENGTH_LONG).show();
        } else {
            backendClient.authenticate(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe((d) -> this.showLoggingInDialog())
                    .doFinally(this::hideLoggingInDialog)
                    .subscribe(this::helloAndProceed, this::showError);
        }
    }

    private void showError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showLoggingInDialog() {
        loggingInDialog.show();
    }

    private void hideLoggingInDialog() {
        loggingInDialog.dismiss();
    }

    private void proceed() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void helloAndProceed(Principal p) {
        final String helloToastMsg = getString(R.string.main_hello_toast, p.name());
        Toast.makeText(getApplicationContext(), helloToastMsg, Toast.LENGTH_LONG).show();

        proceed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loggingInDialog != null && loggingInDialog.isShowing()) {
            loggingInDialog.cancel();
        }
    }
}
