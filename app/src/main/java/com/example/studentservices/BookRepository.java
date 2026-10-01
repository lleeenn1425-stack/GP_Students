package com.example.studentservices;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/** قراءة وكتابة مجموعة books في Firestore. */
final class BookRepository {

    interface Callback<T> {
        void onResult(@Nullable T result, @Nullable Exception error);
    }

    private static final String COLLECTION = "books";

    private BookRepository() {
    }

    static void loadAll(@NonNull Context context, @NonNull Callback<List<Book>> callback) {
        final Context app = context.getApplicationContext();
        FirebaseFirestore.getInstance().collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Book> books = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        books.add(fromDoc(app, doc));
                    }
                    callback.onResult(books, null);
                })
                .addOnFailureListener(e -> callback.onResult(null, e));
    }

    static void loadById(@NonNull Context context, @NonNull String id,
                         @NonNull Callback<Book> callback) {
        final Context app = context.getApplicationContext();
        FirebaseFirestore.getInstance().collection(COLLECTION).document(id)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(
                        doc.exists() ? fromDoc(app, doc) : null, null))
                .addOnFailureListener(e -> callback.onResult(null, e));
    }

    @NonNull
    private static Book fromDoc(@NonNull Context context, @NonNull DocumentSnapshot d) {
        Book.Builder builder = new Book.Builder(
                d.getId(),
                str(d, "title"),
                str(d, "author"),
                res(context, str(d, "coverColor"), "color", R.color.cover_navy),
                res(context, str(d, "coverIcon"), "drawable", R.drawable.ic_cover_network))
                .description(str(d, "description"))
                .details(str(d, "category"), num(d, "publishedYear").intValue(),
                        str(d, "language"), str(d, "condition"))
                .price(num(d, "price").doubleValue())
                .rating(num(d, "rating").floatValue(), num(d, "ratingCount").intValue());
        String image = str(d, "coverImage");
        if (!image.isEmpty()) {
            builder.coverImage(res(context, image, "drawable", 0));
        }
        return builder.build();
    }

    @NonNull
    private static String str(@NonNull DocumentSnapshot d, @NonNull String field) {
        String v = d.getString(field);
        return v == null ? "" : v;
    }

    @NonNull
    private static Number num(@NonNull DocumentSnapshot d, @NonNull String field) {
        Number v = d.getDouble(field);
        return v == null ? 0 : v;
    }

    private static int res(@NonNull Context context, @NonNull String name,
                           @NonNull String type, int fallback) {
        if (name.isEmpty()) {
            return fallback;
        }
        int id = context.getResources().getIdentifier(name, type, context.getPackageName());
        return id == 0 ? fallback : id;
    }
}
