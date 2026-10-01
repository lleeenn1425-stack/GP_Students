package com.example.studentservices;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * شاشة إنشاء حساب.
 * حالياً تتحقق من المدخلات محلياً فقط — نقطة استدعاء الـ API معلّمة في {@link #performSignUp}.
 */
public class SignUpActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private boolean submitting;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        bindViews();
        applyWindowInsets();
        setupListeners();
    }

    private void bindViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    private void applyWindowInsets() {
        final View header = findViewById(R.id.header);
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(0, bars.top, 0, 0);
            content.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        clearErrorWhileTyping(etFullName, tilFullName);
        clearErrorWhileTyping(etEmail, tilEmail);
        clearErrorWhileTyping(etPassword, tilPassword);

        findViewById(R.id.btnSignUp).setOnClickListener(v -> attemptSignUp());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignUp();
                return true;
            }
            return false;
        });

        // زر الرجوع في الترويسة ورابط "Login" أسفل الشاشة يسويان نفس الشيء: إغلاق هذه الشاشة والعودة للوقن
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void attemptSignUp() {
        String fullName = textOf(etFullName);
        String email = textOf(etEmail);
        String password = textOf(etPassword);

        // بلا && حتى لا يتوقف التحقق عند أول خطأ — نريد كل الحقول المعطوبة تظهر معاً
        boolean valid = validateFullName(fullName);
        valid = validateEmail(email) && valid;
        valid = validatePassword(password) && valid;

        if (valid) {
            performSignUp(fullName, email, password);
        }
    }

    private boolean validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_name_required));
            return false;
        }
        tilFullName.setError(null);
        return true;
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
     */
    private void performSignUp(String fullName, String email, String password) {
        if (submitting) {
            return;
        }
        setSubmitting(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    String uid = r.getUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("fullName", fullName);
                    user.put("email", email);
                    user.put("createdAt", FieldValue.serverTimestamp());
                    FirebaseFirestore.getInstance().collection("users").document(uid).set(user);
                    Intent home = new Intent(this, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(home);
                })
                .addOnFailureListener(e -> {
                    setSubmitting(false);
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        tilEmail.setError(getString(R.string.error_email_taken));
                    } else {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setSubmitting(boolean value) {
        submitting = value;
        findViewById(R.id.btnSignUp).setEnabled(!value);
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

    private void toast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }
}
