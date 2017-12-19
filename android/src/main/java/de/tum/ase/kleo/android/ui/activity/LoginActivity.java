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
import de.tum.ase.kleo.android.util.NetworkUtils;
import de.tum.ase.kleo.android.util.UiUtils;
import de.tum.ase.kleo.android.util.Validator;
import de.tum.ase.kleo.android.client.Principal;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LoginActivity extends Activity {

    // TODO: Use ProgressBar instead or some 3th party library
    private ProgressDialog loggingInDialog;
    private BackendClient backendClient;

    private EditText emailView;
    private EditText passwordView;
    private Disposable disposable;

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
            disposable = backendClient.authenticate(email, password)
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
        final String helloToastMsg = getString(R.string.hello_toast, p.name());
        Toast.makeText(getApplicationContext(), helloToastMsg, Toast.LENGTH_LONG).show();

        proceed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loggingInDialog != null && loggingInDialog.isShowing()) {
            loggingInDialog.cancel();
        }
        NetworkUtils.unsubscribe(disposable);
    }
}
