package com.smart.campusmarket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/**
 * Sign up / log in with email and password. A verification link must be opened before the
 * account can use the app, which is what makes "verified student" mean something.
 */
public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private boolean signUpMode = false;

    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText editEmail, editPassword;
    private TextView textMessage;
    private MaterialButton btnSubmit, btnForgot, btnResend;
    private MaterialButtonToggleGroup toggle;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.authMain), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        textMessage = findViewById(R.id.textAuthMessage);
        btnSubmit = findViewById(R.id.btnAuthSubmit);
        btnForgot = findViewById(R.id.btnForgot);
        btnResend = findViewById(R.id.btnResend);
        toggle = findViewById(R.id.toggleAuth);
        progress = findViewById(R.id.progressAuth);

        toggle.check(R.id.btnModeLogin);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) setMode(checkedId == R.id.btnModeSignup);
        });

        btnSubmit.setOnClickListener(v -> submit());
        btnForgot.setOnClickListener(v -> sendReset());
        btnResend.setOnClickListener(v -> resendVerification());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.isEmailVerified()) goToMain();   // already signed in
    }

    // ------------------------------------------------------------------ modes

    private void setMode(boolean signUp) {
        signUpMode = signUp;
        btnSubmit.setText(signUp ? R.string.auth_signup : R.string.auth_login);
        btnForgot.setVisibility(signUp ? View.GONE : View.VISIBLE);
        btnResend.setVisibility(View.GONE);
        textMessage.setVisibility(View.GONE);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!busy);
        btnForgot.setEnabled(!busy);
        btnResend.setEnabled(!busy);
    }

    private void showMessage(String message) {
        textMessage.setText(message);
        textMessage.setVisibility(View.VISIBLE);
    }

    // ----------------------------------------------------------------- actions

    private void submit() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        textMessage.setVisibility(View.GONE);

        String email = readEmail();
        String password = editPassword.getText() == null ? "" : editPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.auth_error_email));
            return;
        }
        if (signUpMode && !isAllowedEmail(email)) {
            layoutEmail.setError(getString(R.string.auth_error_domain, AppConfig.STUDENT_EMAIL_DOMAIN));
            return;
        }
        if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.auth_error_password));
            return;
        }

        setBusy(true);
        if (signUpMode) signUp(email, password);
        else logIn(email, password);
    }

    private void signUp(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setBusy(false);
                showMessage(friendlyError(task.getException()));
                return;
            }
            FirebaseUser user = task.getResult().getUser();
            if (user == null) {
                setBusy(false);
                return;
            }
            user.sendEmailVerification().addOnCompleteListener(sent -> {
                auth.signOut();   // they must verify, then log in
                setBusy(false);
                toggle.check(R.id.btnModeLogin);
                showMessage(getString(R.string.auth_verify_sent, email));
            });
        });
    }

    private void logIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                setBusy(false);
                showMessage(friendlyError(task.getException()));
                return;
            }
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                setBusy(false);
                return;
            }
            // Refresh so a just-verified email is recognised, then refresh the token so
            // Firestore rules see email_verified = true.
            user.reload().addOnCompleteListener(reloaded -> {
                FirebaseUser fresh = auth.getCurrentUser();
                if (fresh != null && fresh.isEmailVerified()) {
                    fresh.getIdToken(true).addOnCompleteListener(t -> goToMain());
                } else {
                    setBusy(false);
                    showMessage(getString(R.string.auth_not_verified, email));
                    btnResend.setVisibility(View.VISIBLE);   // stays signed in so we can resend
                }
            });
        });
    }

    private void resendVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        setBusy(true);
        user.sendEmailVerification().addOnCompleteListener(task -> {
            setBusy(false);
            showMessage(task.isSuccessful()
                    ? getString(R.string.auth_resent, user.getEmail())
                    : friendlyError(task.getException()));
        });
    }

    private void sendReset() {
        String email = readEmail();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.auth_error_email));
            return;
        }
        setBusy(true);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            setBusy(false);
            showMessage(getString(R.string.auth_reset_sent, email));   // same message either way
        });
    }

    // ---------------------------------------------------------------- helpers

    private String readEmail() {
        return editEmail.getText() == null
                ? "" : editEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedEmail(String email) {
        String domain = AppConfig.STUDENT_EMAIL_DOMAIN;
        if (domain == null || domain.isEmpty()) return true;
        String host = email.substring(email.indexOf('@') + 1);
        return host.equals(domain) || host.endsWith("." + domain);
    }

    private String friendlyError(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException
                || e instanceof FirebaseAuthInvalidUserException) {
            return getString(R.string.auth_error_credentials);
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return getString(R.string.auth_error_exists);
        }
        if (e instanceof FirebaseNetworkException) {
            return getString(R.string.auth_error_network);
        }
        return getString(R.string.auth_error_generic, e == null ? "" : e.getMessage());
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}