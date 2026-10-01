package com.example.studentservices;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * موديل الكتاب.
 * لون الغلاف وأيقونته مؤقتان لوضع التصميم — عند ربط الباك يُضاف حقل coverUrl لصورة الغلاف
 * (أو قائمة صور imageUrls لشاشة التفاصيل).
 * {@code coverImage} صورة الغلاف الكبيرة في شاشة التفاصيل (اختيارية، 0 = ما فيه صورة فيُعرض
 * الغلاف المؤقت).
 * الحقول الأساسية تُمرر في الـ Builder، وحقول التفاصيل اختيارية.
 */
public class Book {

    private final String id;
    private final String title;
    private final String author;
    @ColorRes
    private final int coverColor;
    @DrawableRes
    private final int coverIcon;
    /** صورة الغلاف في شاشة التفاصيل؛ 0 = ما فيه (نستخدم لون + أيقونة الغلاف المؤقتين). */
    @DrawableRes
    private final int coverImage;

    // ===== تفاصيل الكتاب (شاشة Book Details) =====
    private final String description;
    private final String category;
    private final int publishedYear;
    private final String language;
    private final String condition;
    /** السعر بالريال السعودي. */
    private final double price;
    private final float rating;
    private final int ratingCount;

    private Book(@NonNull Builder b) {
        this.id = b.id;
        this.title = b.title;
        this.author = b.author;
        this.coverColor = b.coverColor;
        this.coverIcon = b.coverIcon;
        this.coverImage = b.coverImage;
        this.description = b.description;
        this.category = b.category;
        this.publishedYear = b.publishedYear;
        this.language = b.language;
        this.condition = b.condition;
        this.price = b.price;
        this.rating = b.rating;
        this.ratingCount = b.ratingCount;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getAuthor() {
        return author;
    }

    @ColorRes
    public int getCoverColor() {
        return coverColor;
    }

    @DrawableRes
    public int getCoverIcon() {
        return coverIcon;
    }

    /** صورة الغلاف الكبيرة لشاشة التفاصيل، أو 0 إذا ما فيه. */
    @DrawableRes
    public int getCoverImage() {
        return coverImage;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    @NonNull
    public String getLanguage() {
        return language;
    }

    @NonNull
    public String getCondition() {
        return condition;
    }

    public double getPrice() {
        return price;
    }

    public float getRating() {
        return rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    /** بحث بالعنوان أو المؤلف. {@code lowerQuery} لازم يكون بأحرف صغيرة مسبقاً. */
    public boolean matches(@NonNull String lowerQuery) {
        return title.toLowerCase(Locale.ROOT).contains(lowerQuery)
                || author.toLowerCase(Locale.ROOT).contains(lowerQuery);
    }

    /**
     * مقارنة المحتوى — يستخدمها DiffUtil في {@link BookAdapter} لتحديث العناصر المتغيرة فقط.
     * تقارن الحقول اللي تظهر في القائمة فقط؛ حقول التفاصيل ما تنعرض هناك.
     */
    public boolean sameContentAs(@NonNull Book other) {
        return title.equals(other.title)
                && author.equals(other.author)
                && coverColor == other.coverColor
                && coverIcon == other.coverIcon;
    }

    public static final class Builder {

        private final String id;
        private final String title;
        private final String author;
        private final int coverColor;
        private final int coverIcon;

        private int coverImage;
        private String description = "";
        private String category = "";
        private int publishedYear;
        private String language = "";
        private String condition = "";
        private double price;
        private float rating;
        private int ratingCount;

        public Builder(@NonNull String id, @NonNull String title, @NonNull String author,
                       @ColorRes int coverColor, @DrawableRes int coverIcon) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.coverColor = coverColor;
            this.coverIcon = coverIcon;
        }

        @NonNull
        public Builder coverImage(@DrawableRes int coverImage) {
            this.coverImage = coverImage;
            return this;
        }

        @NonNull
        public Builder description(@NonNull String description) {
            this.description = description;
            return this;
        }

        @NonNull
        public Builder details(@NonNull String category, int publishedYear,
                               @NonNull String language, @NonNull String condition) {
            this.category = category;
            this.publishedYear = publishedYear;
            this.language = language;
            this.condition = condition;
            return this;
        }

        @NonNull
        public Builder price(double price) {
            this.price = price;
            return this;
        }

        @NonNull
        public Builder rating(float rating, int ratingCount) {
            this.rating = rating;
            this.ratingCount = ratingCount;
            return this;
        }

        @NonNull
        public Book build() {
            return new Book(this);
        }
    }
}
