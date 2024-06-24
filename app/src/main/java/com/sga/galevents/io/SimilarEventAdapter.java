package com.sga.galevents.io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sga.galevents.R;
import com.sga.galevents.model.Event;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class SimilarEventAdapter extends RecyclerView.Adapter<SimilarEventAdapter.SimilarEventViewHolder> {
    private List<Event> events;
    private OnEventClickListener listener;

    public SimilarEventAdapter(List<Event> events) {
        this.events = events;
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SimilarEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_similar_event, parent, false);
        return new SimilarEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimilarEventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class SimilarEventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSimilarEventName;
        private ImageView ivSimilarEventLogo;
        private ImageButton btnAddFavorite;

        public SimilarEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSimilarEventName = itemView.findViewById(R.id.tvSimilarEventName);
            ivSimilarEventLogo = itemView.findViewById(R.id.ivSimilarEventLogo);
            btnAddFavorite = itemView.findViewById(R.id.btnAddFavorite);
        }

        public void bind(Event event, OnEventClickListener listener) {
            tvSimilarEventName.setText(event.getName());

            Picasso.get()
                    .load(event.getImages().get(0).getUrl())
                    .into(ivSimilarEventLogo);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });


        }


    }
}
