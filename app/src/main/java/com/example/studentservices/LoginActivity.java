package com.example.studentservices;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * شاشة تسجيل الدخول.
 * حالياً تتحقق من المدخلات محلياً فقط — نقطة استدعاء الـ API معلّمة في {@link #performLogin}.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private boolean loggingIn;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        bindViews();
        applyWindowInsets();
        setupListeners();
    }

    private void bindViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    /**
     * منذ Android 15 (targetSdk 35) يُفرض العرض من حافة إلى حافة، فلا بد من معالجة الـ insets يدوياً.
     * الترويسة تأخذ إزاحة شريط الحالة كـ padding — والموجة خلفية لها، فترسم خلف الـ padding ولا تنزل.
     */
    private void applyWindowInsets() {
        final View header = findViewById(R.id.header);
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(0, bars.top, 0, 0);
            content.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        clearErrorWhileTyping(etEmail, tilEmail);
        clearErrorWhileTyping(etPassword, tilPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());

        // زر "تم" في لوحة المفاتيح يشغّل تسجيل الدخول مباشرة
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(
                v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

        findViewById(R.id.tvSignUp).setOnClickListener(
                v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void attemptLogin() {
        String email = textOf(etEmail);
        String password = textOf(etPassword);

        boolean valid = validateEmail(email);
        // لا نستخدم && حتى لا يُختصر التقييم وتظهر رسالة خطأ واحدة فقط
        valid = validatePassword(password) && valid;

        if (valid) {
            performLogin(email, password);
        }
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return false;
        }
        tilEmail.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError(getString(R.string.error_password_short));
            return false;
        }
        tilPassword.setError(null);
        return true;
    }

    /**
     * نقطة الوصل مع الباك-إند لاحقاً: هنا يُستدعى الـ API ويُعرض مؤشر تحميل.
     * حالياً (تصميم فقط): بعد نجاح التحقق المحلي ننتقل للهوم مباشرة.
     * عند الربط: أضف finish() بعد الانتقال حتى لا يرجع زر الرجوع لشاشة الدخول.
     */
    private void performLogin(String email, String password) {
        if (loggingIn) {
            return;
        }
        loggingIn = true;
        findViewById(R.id.btnLogin).setEnabled(false);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    loggingIn = false;
                    findViewById(R.id.btnLogin).setEnabled(true);
                    Toast.makeText(this, getString(R.string.msg_login_failed),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void clearErrorWhileTyping(TextInputEditText field, TextInputLayout layout) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @NonNull
    private String textOf(TextView view) {
        CharSequence text = view.getText();
        return text == null ? "" : text.toString().trim();
    }
}
