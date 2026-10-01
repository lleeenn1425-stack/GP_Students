package com.example.studentservices;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * شاشة الكتب (Books): كل الكتب مع بحث بالعنوان أو المؤلف.
 * وضع التصميم: الكتب تُجلب من Firestore عبر {@link BookRepository}.
 * زر الفلتر و Add book يعرضان "Coming soon" لحين بناء شاشاتهما.
 */
public class BookListActivity extends AppCompatActivity {

    /** القائمة الكاملة كما وصلت من المصدر؛ البحث يعرض جزءاً منها ولا يعدّل عليها. */
    private final List<Book> allBooks = new ArrayList<>();

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
        setContentView(R.layout.activity_book_list);

        bindViews();
        applyWindowInsets();
        setupBooksList();
        setupListeners();
        loadBooks();
    }

    private void bindViews() {
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void applyWindowInsets() {
        final View content = findViewById(R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bookListRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            content.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setupBooksList() {
        RecyclerView rvBooks = findViewById(R.id.rvBooks);
        bookAdapter = new BookAdapter(this::openBookDetails);
        rvBooks.setAdapter(bookAdapter);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFilter).setOnClickListener(v -> comingSoon());
        findViewById(R.id.btnAddBook).setOnClickListener(v -> comingSoon());

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
     * نقطة الوصل مع الباك-إند لاحقاً: جلب كل الكتب من الـ API مع مؤشر تحميل (وترقيم صفحات).
     * الكتب من Firestore عبر {@link BookRepository}.
     */
    private void loadBooks() {
        BookRepository.loadAll(this, (books, error) -> {
            if (books == null) {
                Toast.makeText(this, R.string.msg_load_failed, Toast.LENGTH_LONG).show();
                return;
            }
            allBooks.clear();
            allBooks.addAll(books);
            showBooks(textOf(etSearch));
        });
    }

    /** يعرض الكتب المطابقة للبحث (أو كلها إذا كان الحقل فارغاً). */
    private void showBooks(@NonNull String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        List<Book> result = new ArrayList<>();
        for (Book book : allBooks) {
            if (q.isEmpty() || book.matches(q)) {
                result.add(book);
            }
        }
        bookAdapter.submitList(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
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
