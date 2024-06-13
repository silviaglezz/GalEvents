package com.sga.galevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
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
        //private TextView tvEventGenre;
        //private TextView tvEventSubGenre;
        private ImageView ivEventLogo;
        private TextView tvVenueCity;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventStart = itemView.findViewById(R.id.tvEventStart);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            //tvEventGenre = itemView.findViewById(R.id.tvEventGenre);
            //tvEventSubGenre = itemView.findViewById(R.id.tvEventSubGenre);
            ivEventLogo = itemView.findViewById(R.id.ivEventLogo);
            tvVenueCity = itemView.findViewById(R.id.tvVenueCity);

        }

        public void bind(Event event) {
            tvEventName.setText(event.getName());
            tvEventStart.setText(event.getDates().getStart().getLocalDate());
            tvEventCategory.setText(event.getClassifications().get(0).getSegment().getName());
            //tvEventGenre.setText(event.getClassifications().get(0).getGenre().getName());
            //tvEventSubGenre.setText(event.getClassifications().get(0).getSubgenre().getName());
            tvVenueCity.setText(event.getEmbedded().getVenues().get(0).getCity().getName());

            Picasso.get()
                    .load(event.getImages().get(0).getUrl())
                    .into(ivEventLogo);
        }
    }
}
