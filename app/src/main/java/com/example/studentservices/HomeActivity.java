package com.example.studentservices;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * الشاشة الرئيسية (Home).
 * الكتب من Firestore، والبحث يفلتر "أحدث الكتب" محلياً،
 * قسم Books و See all يفتحان {@link BookListActivity}، والضغط على كتاب يفتح
 * {@link BookDetailsActivity}؛ بقية الأقسام تعرض "Coming soon" لحين بناء شاشاتها.
 * نقطة ربط الـ API معلّمة في {@link #loadLatestBooks}.
 */
public class HomeActivity extends AppCompatActivity {

    /** القائمة الكاملة كما وصلت من المصدر؛ البحث يعرض جزءاً منها ولا يعدّل عليها. */
    private final List<Book> latestBooks = new ArrayList<>();

    private EditText etSearch;
    private TextView tvEmpty;
    private BookAdapter bookAdapter;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bindViews();
        applyWindowInsets();
        setupCategories();
        setupBooksList();
        setupListeners();
        loadLatestBooks();
    }

    private void bindViews() {
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void applyWindowInsets() {
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            content.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setupCategories() {
        bindCategory(R.id.cardBooks, R.drawable.ic_book_open, R.color.purple_primary,
                R.string.books, R.string.books_desc, v -> openBooks());

        bindCategory(R.id.cardResources, R.drawable.ic_document, R.color.accent_blue,
                R.string.resources, R.string.resources_desc, v -> openResources());

        bindCategory(R.id.cardAdvertising, R.drawable.ic_notifications, R.color.accent_orange,
                R.string.advertising, R.string.advertising_desc, v -> openAdvertising());
    }

    /** يعبّي كرت قسم (من item_home_category) بالأيقونة واللون والنصوص. */
    private void bindCategory(@IdRes int cardId, @DrawableRes int icon, @ColorRes int accent,
                              @StringRes int title, @StringRes int description,
                              View.OnClickListener onClick) {
        View card = findViewById(cardId);
        ColorStateList tint = ColorStateList.valueOf(ContextCompat.getColor(this, accent));

        ImageView ivIcon = card.findViewById(R.id.ivCategoryIcon);
        ivIcon.setImageResource(icon);
        ImageViewCompat.setImageTintList(ivIcon, tint);

        ImageView ivArrow = card.findViewById(R.id.ivCategoryArrow);
        ImageViewCompat.setImageTintList(ivArrow, tint);

        TextView tvTitle = card.findViewById(R.id.tvCategoryTitle);
        tvTitle.setText(title);

        TextView tvDesc = card.findViewById(R.id.tvCategoryDesc);
        tvDesc.setText(description);

        card.setOnClickListener(onClick);
    }

    private void setupBooksList() {
        RecyclerView rvLatestBooks = findViewById(R.id.rvLatestBooks);
        bookAdapter = new BookAdapter(this::openBookDetails);
        rvLatestBooks.setAdapter(bookAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent login = new Intent(this, LoginActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        });
        findViewById(R.id.btnNotifications).setOnClickListener(v -> openNotifications());
        findViewById(R.id.tvSeeAll).setOnClickListener(v -> openBooks());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                showBooks(s.toString());
            }
        });

        // الفلترة تحصل أثناء الكتابة، فزر البحث في الكيبورد يكفيه إخفاء الكيبورد
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    /**
     * نقطة الوصل مع الباك-إند لاحقاً: جلب أحدث الكتب من الـ API مع مؤشر تحميل.
     * الكتب من Firestore عبر {@link BookRepository}.
     */
    private void loadLatestBooks() {
        BookRepository.loadAll(this, (books, error) -> {
            if (books == null) {
                Toast.makeText(this, R.string.msg_load_failed, Toast.LENGTH_LONG).show();
                return;
            }
            latestBooks.clear();
            latestBooks.addAll(books.subList(0, Math.min(books.size(), 6)));
            showBooks(textOf(etSearch));
        });
    }

    /** يعرض الكتب المطابقة للبحث (أو كلها إذا كان الحقل فارغاً). */
    private void showBooks(@NonNull String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<Book> result = new ArrayList<>();
        for (Book book : latestBooks) {
            if (q.isEmpty() || book.matches(q)) {
                result.add(book);
            }
        }
        bookAdapter.submitList(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ===== مخارج الشاشة — كل وحدة تتحول لـ startActivity عند بناء شاشتها =====

    private void openBooks() {
        startActivity(new Intent(this, BookListActivity.class));
    }

    private void openResources() {
        comingSoon();
    }

    private void openAdvertising() {
        comingSoon();
    }

    private void openNotifications() {
        comingSoon();
    }

    private void openBookDetails(@NonNull Book book) {
        startActivity(BookDetailsActivity.newIntent(this, book));
    }

    private void comingSoon() {
        Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
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
        return text == null ? "" : text.toString();
    }
}
