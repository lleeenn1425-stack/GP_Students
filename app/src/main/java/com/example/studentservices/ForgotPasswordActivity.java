package com.example.studentservices;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

/**
 * شاشة استعادة كلمة المرور — حالتان في نفس الشاشة:
 * 1) نموذج إدخال الإيميل  2) تأكيد "تم الإرسال".
 * وضع التصميم: بلا أي تحقق، أي ضغطة على زر الإرسال تفتح حالة التأكيد.
 * عند ربط الباك يُضاف التحقق واستدعاء الـ API في {@link #performSendResetLink}.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private View layoutForm;
    private View layoutSuccess;
    private TextView tvSentEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        bindViews();
        applyWindowInsets();
        setupListeners();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        layoutForm = findViewById(R.id.layoutForm);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        tvSentEmail = findViewById(R.id.tvSentEmail);
    }

    private void applyWindowInsets() {
        final View header = findViewById(R.id.header);
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(0, bars.top, 0, 0);
            content.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnSend).setOnClickListener(v -> performSendResetLink(textOf(etEmail)));

        etEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSendResetLink(textOf(etEmail));
                return true;
            }
            return false;
        });

        // كل مخارج الشاشة (زر الرجوع، الرابط، زر النجاح) ترجع للوقن عبر إغلاق هذه الشاشة
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
        findViewById(R.id.btnSuccessLogin).setOnClickListener(v -> finish());

        findViewById(R.id.tvResend).setOnClickListener(
                v -> Toast.makeText(this, R.string.msg_resent, Toast.LENGTH_SHORT).show());
    }

    /**
     * نقطة الوصل مع الباك-إند لاحقاً: التحقق من الإيميل + استدعاء الـ API،
     * ولا يُنتقل لحالة النجاح إلا عند نجاح الطلب.
     * حالياً (تصميم فقط): ننتقل مباشرة، ونعرض ما كُتب أو إيميلاً تجريبياً إن كان الحقل فارغاً.
     */
    private void performSendResetLink(String email) {
        hideKeyboard();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.error_email_required, Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> {
                    tvSentEmail.setText(email);
                    layoutForm.setVisibility(View.GONE);
                    layoutSuccess.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            WindowCompat.getInsetsController(getWindow(), focus)
                    .hide(WindowInsetsCompat.Type.ime());
            focus.clearFocus();
        }
    }

    @NonNull
    private String textOf(TextView view) {
        CharSequence text = view.getText();
        return text == null ? "" : text.toString().trim();
    }
}
