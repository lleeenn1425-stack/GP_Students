package com.example.studentservices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

/**
 * أدابتر قائمة الكتب. ListAdapter + DiffUtil حتى تتحدث القائمة بسلاسة عند البحث،
 * ونفس الأدابتر يصلح لاحقاً لشاشة "كل الكتب".
 */
public class BookAdapter extends ListAdapter<Book, BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(@NonNull Book book);
    }

    private static final DiffUtil.ItemCallback<Book> DIFF = new DiffUtil.ItemCallback<Book>() {
        @Override
        public boolean areItemsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            return oldItem.sameContentAs(newItem);
        }
    };

    private final OnBookClickListener listener;

    public BookAdapter(@NonNull OnBookClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardCover;
        private final TextView tvCoverTitle;
        private final ImageView ivCoverIcon;
        private final TextView tvBookTitle;
        private final TextView tvBookAuthor;
        private final View btnView;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCover = itemView.findViewById(R.id.cardCover);
            tvCoverTitle = itemView.findViewById(R.id.tvCoverTitle);
            ivCoverIcon = itemView.findViewById(R.id.ivCoverIcon);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            btnView = itemView.findViewById(R.id.btnView);
        }

        void bind(@NonNull Book book, @NonNull OnBookClickListener listener) {
            cardCover.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), book.getCoverColor()));
            tvCoverTitle.setText(book.getTitle());
            ivCoverIcon.setImageResource(book.getCoverIcon());
            tvBookTitle.setText(book.getTitle());
            tvBookAuthor.setText(book.getAuthor());

            // الضغط على الكرت كله أو زر View يفتح نفس التفاصيل
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            btnView.setOnClickListener(v -> listener.onBookClick(book));
        }
    }
}
