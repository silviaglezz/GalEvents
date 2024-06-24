package com.sga.galevents.io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sga.galevents.R;
import com.sga.galevents.model.Event;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener{
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, listener, events);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEventName;
        private TextView tvEventStart;
        private TextView tvEventCategory;
        private ImageView ivEventLogo;
        private TextView tvVenueCity;

        public EventViewHolder(@NonNull View itemView, OnEventClickListener listener, List<Event> events) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventStart = itemView.findViewById(R.id.tvEventStart);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            ivEventLogo = itemView.findViewById(R.id.ivEventLogo);
            tvVenueCity = itemView.findViewById(R.id.tvVenueCity);

            itemView.setOnClickListener(v -> {
                if (listener != null){
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        listener.onEventClick(events.get(position));
                    }
                }
            });
        }

        public void bind(Event event) {
            tvEventName.setText(event.getName());
            String formattedDate = convertDateFormat(event.getDates().getStart().getLocalDate());
            tvEventStart.setText(formattedDate);
            tvEventCategory.setText(event.getClassifications().get(0).getSegment().getName());
            tvVenueCity.setText(event.getEmbedded().getVenues().get(0).getCity().getName());

            Picasso.get()
                    .load(event.getImages().get(0).getUrl())
                    .into(ivEventLogo);
        }

        public String convertDateFormat(String dateStr) {
            DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            try {
                LocalDate date = LocalDate.parse(dateStr, originalFormat);
                return date.format(targetFormat);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                return dateStr; // Si hay un error, devolver la fecha original
            }
        }
    }
}
