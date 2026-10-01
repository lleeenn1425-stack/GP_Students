package com.example.studentservices;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * شاشة تفاصيل الكتاب (Book Details).
 * تستلم id الكتاب عبر {@link #newIntent}؛ الكتاب يُجلب من Firestore.
 * حفظ الكتاب (Save Book) وزر Contact Owner مؤقتان لحين ربط الباك والمحادثات.
 */
public class BookDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_BOOK_ID = "com.example.studentservices.extra.BOOK_ID";
    private static final String STATE_SAVED = "state_saved";

    private ImageView ivSave;
    private TextView tvSave;

    /** حالة "محفوظ" — بالذاكرة فقط في وضع التصميم؛ تُحفظ في الباك لاحقاً. */
    private boolean saved;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull Book book) {
        return new Intent(context, BookDetailsActivity.class)
                .putExtra(EXTRA_BOOK_ID, book.getId());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent login = new Intent(this, LoginActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
            finish();
            return;
        }

        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        if (bookId == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);

        saved = savedInstanceState != null && savedInstanceState.getBoolean(STATE_SAVED);

        bindViews();
        applyWindowInsets();
        setupListeners();
        renderSaved();

        BookRepository.loadById(this, bookId, (book, error) -> {
            if (book == null) {
                Toast.makeText(this, R.string.msg_load_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            bindBook(book);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVED, saved);
    }

    private void bindViews() {
        ivSave = findViewById(R.id.ivSave);
        tvSave = findViewById(R.id.tvSave);
    }

    private void applyWindowInsets() {
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            content.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindBook(@NonNull Book book) {
        bindCover(book);

        setText(R.id.tvDetailsTitle, book.getTitle());
        setText(R.id.tvDetailsAuthor, book.getAuthor());
        setText(R.id.tvDescription, book.getDescription());

        // الأرقام تُنسّق بـ Locale.US حتى ما تتحول لأرقام عربية على أجهزة اللغة العربية
        String rating = String.format(Locale.US, "%.1f", book.getRating());
        setText(R.id.tvDetailsRating, getString(
                R.string.rating_format, rating, String.valueOf(book.getRatingCount())));

        // Book Information
        setText(R.id.tvCategory, book.getCategory());
        setText(R.id.tvPublished, String.valueOf(book.getPublishedYear()));
        setText(R.id.tvLanguage, book.getLanguage());
        setText(R.id.tvCondition, book.getCondition());
        setText(R.id.tvPrice, formatPrice(book.getPrice()));
    }

    /**
     * الغلاف: صورة الكتاب إن وجدت، وإلا الغلاف المؤقت (لون + عنوان + أيقونة).
     * عند ربط الباك تُحمّل الصورة من الرابط في ivBigCoverImage.
     */
    private void bindCover(@NonNull Book book) {
        MaterialCardView cardCover = findViewById(R.id.cardBigCover);
        cardCover.setCardBackgroundColor(ContextCompat.getColor(this, book.getCoverColor()));

        ImageView ivImage = findViewById(R.id.ivBigCoverImage);
        TextView tvTitle = findViewById(R.id.tvBigCoverTitle);
        ImageView ivIcon = findViewById(R.id.ivBigCoverIcon);

        if (book.getCoverImage() != 0) {
            ivImage.setImageResource(book.getCoverImage());
            ivImage.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            ivIcon.setVisibility(View.GONE);
        } else {
            ivImage.setVisibility(View.GONE);
            tvTitle.setText(book.getTitle());
            tvTitle.setVisibility(View.VISIBLE);
            ivIcon.setImageResource(book.getCoverIcon());
            ivIcon.setVisibility(View.VISIBLE);
        }
    }

    private void setText(@IdRes int viewId, @NonNull String text) {
        ((TextView) findViewById(viewId)).setText(text);
    }

    @NonNull
    private String formatPrice(double price) {
        String amount = price == Math.floor(price)
                ? String.valueOf((long) price)
                : String.format(Locale.US, "%.2f", price);
        return getString(R.string.price_format, amount);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> toggleSaved());
        findViewById(R.id.btnContactOwner).setOnClickListener(v -> contactOwner());
    }

    private void toggleSaved() {
        // عند ربط الباك: استدعاء API الحفظ/إلغاء الحفظ ثم تحديث الحالة
        saved = !saved;
        renderSaved();
    }

    private void renderSaved() {
        ivSave.setImageResource(saved ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
        tvSave.setText(saved ? R.string.saved_book : R.string.save_book);
    }

    /** يتحول لفتح محادثة مع صاحب الكتاب عند بناء وحدة الرسائل. */
    private void contactOwner() {
        Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
    }
}
