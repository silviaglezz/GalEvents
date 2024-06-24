package com.sga.galevents.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sga.galevents.model.Event;
import com.sga.galevents.R;
import com.sga.galevents.io.SimilarEventAdapter;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventDetailsDialogFragment extends DialogFragment implements SimilarEventAdapter.OnEventClickListener {
    private static final String ARG_EVENT = "Evento";
    private RecyclerView rvSimilarEvents;
    private SimilarEventAdapter similarEventAdapter;
    private List<Event> similarEvents;
    private FirebaseFirestore fStore;
    private String currentId;
    private String selectedCategory;
    private String selectedGenre;


    public static EventDetailsDialogFragment newInstance(Event event) {
        EventDetailsDialogFragment fragment = new EventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_event_details, container, false);

        TextView tvEventId = view.findViewById(R.id.tvEventId);
        TextView tvEventName = view.findViewById(R.id.tvEventName);
        TextView tvEventStart = view.findViewById(R.id.tvEventStart);
        TextView tvEventStartTime = view.findViewById(R.id.tvEventStartTime);
        TextView tvVenueCity = view.findViewById(R.id.tvVenueCity);
        TextView tvVenueName = view.findViewById(R.id.tvVenueName);
        TextView tvEventCategory = view.findViewById(R.id.tvEventCategory);
        TextView tvEventGenre = view.findViewById(R.id.tvEventGenre);
        TextView tvEventSubGenre = view.findViewById(R.id.tvEventSubGenre);

        Button btnClose = view.findViewById(R.id.btnClose);
        ImageButton btnAddCalendar = view.findViewById(R.id.btnAddCalendar);
        ImageButton btnAddFavorite = view.findViewById(R.id.btnAddFavorite);

        Event event = null;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
            if(event != null){
                tvEventId.setText(event.getId());
                tvEventName.setText(event.getName());
                tvEventStart.setText(event.getDates().getStart().getLocalDate());
                tvEventStartTime.setText(event.getDates().getStart().getLocalTime());
                tvVenueCity.setText(event.getEmbedded().getVenues().get(0).getCity().getName());
                tvVenueName.setText(event.getEmbedded().getVenues().get(0).getName());
                tvEventCategory.setText(event.getClassifications().get(0).getSegment().getName());
                tvEventGenre.setText(event.getClassifications().get(0).getGenre().getName());
                tvEventSubGenre.setText(event.getClassifications().get(0).getSubGenre().getName());
            }
        }

        btnClose.setOnClickListener(v -> dismiss());

        fStore = FirebaseFirestore.getInstance();

        rvSimilarEvents = view.findViewById(R.id.rvSimilarEvents);
        rvSimilarEvents.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));

        similarEvents = new ArrayList<>();
        similarEventAdapter = new SimilarEventAdapter(similarEvents);
        similarEventAdapter.setOnEventClickListener(this);
        rvSimilarEvents.setAdapter(similarEventAdapter);

        currentId = tvEventId.getText().toString();
        selectedCategory = tvEventCategory.getText().toString();
        selectedGenre = tvEventGenre.getText().toString();

        getSimilarEvents(selectedCategory, selectedGenre, currentId);

        if (event != null) {
            checkFavoriteStatus(event, btnAddFavorite);
        }
        btnAddCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Añadir al calendario", Toast.LENGTH_SHORT).show();
            }
        });

        //Variable para poder usar dentro del boton de añadir a favoritos
        final Event finalEvent = event;
        btnAddFavorite.setOnClickListener(v -> {
            if (finalEvent != null) {
               changeFavoriteStatus(finalEvent, btnAddFavorite);
            }
        });

        return view;
    }

    private void getSimilarEvents(String selectedCategory, String selectedGenre, String currentId){
        List<Event> events = new ArrayList<>();

        similarEvents.clear();

        Log.d("getSimilarEvents", "Genero: " + selectedGenre);
        Log.d("getSimilarEvents", "Id actual: " + currentId);

        fStore.collection("events")
                .whereEqualTo("category", selectedCategory)
                .whereEqualTo("genre", selectedGenre)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // Lista para mantener todas las tareas de obtener venue
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                            //Lista para guardar todos los documentos del evento
                            List<QueryDocumentSnapshot> eventDocuments = new ArrayList<>();

                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                String eventId = document.getId();


                                Log.d("getSimilarEvents", "Comparando eventId: " + eventId + " con currentId: " + currentId);

                                //Si los id son iguales, no se agrega a la lista porque pasa a la siguiente iteracion
                                if (eventId.equals(currentId)) {
                                    Log.d("getSimilarEvents", "Skipping current event: " + eventId);
                                    continue;
                                }
                                Log.d("getSimilarEvents", "Incluyendo evento: " + eventId);

                                eventDocuments.add(document);
                            }

                            //Barajar la lista para aleatorizar
                            Collections.shuffle(eventDocuments);

                            //Seleccionar los 3 primeros documentos de la lista barajada
                            for (int i = 0; i < Math.min(3, eventDocuments.size()); i++){
                                QueryDocumentSnapshot document = eventDocuments.get(i);

                                String eventId = document.getId();
                                String eventName = document.getString("name");
                                String eventStart = document.getString("start");
                                String eventStartTime = document.getString("startTime");
                                String eventVenueRef = document.getString("venueRef");
                                String eventCategory = document.getString("category");
                                String eventGenre = document.getString("genre");
                                String eventSubGenre = document.getString("subgenre");
                                String eventLogo = document.getString("logo");
                                Boolean eventFavorite = document.getBoolean("favorite");

                                // Crear una tarea para obtener la información del venue
                                Task<DocumentSnapshot> venueTask = fStore.collection("venues").document(eventVenueRef).get();

                                //Obtener la informacion del venue
                                venueTask.addOnSuccessListener(venueDocument -> {
                                    if (venueDocument.exists()){
                                        String venueName = venueDocument.getString("name");
                                        String venueAddress = venueDocument.getString("address");
                                        String venueCity = venueDocument.getString("city");

                                        // Crear un objeto Event y agregarlo a la lista
                                        Event event = new Event(eventId, eventName, eventStart, eventStartTime, eventCategory, eventGenre, eventSubGenre, eventLogo, eventFavorite, venueName, venueAddress, venueCity);
                                        events.add(event);

                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e("getSimilarEvents", "Error al obtener el venue: " + e);
                                });
                                //Agregar la tarea a la lista de tareas
                                tasks.add(venueTask);
                            }
                            //Cuando todas las tareas de obtener venues se completen
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                                similarEvents.clear();
                                similarEvents.addAll(events);
                                similarEventAdapter.notifyDataSetChanged();

                                Log.d("getSimilarEvents", "Todos los venues obtenidos");
                            });
                        }else{
                            Log.e("getSimilarEvents", "La consulta no obtuvo resultados.");
                        }
                    }else{
                        Log.e("getSimilarEvents", "Error al obtener los eventos: ", task.getException());
                    }
                });
    }

    //Método que se llama cuando se hace click en un evento
    public void onEventClick(Event event) {
        // Mostrar el diálogo con los detalles del evento
        EventDetailsDialogFragment dialogFragment = EventDetailsDialogFragment.newInstance(event);
        dialogFragment.show(getParentFragmentManager(), "SimilarEventDetailsFragment");
    }

    public void checkFavoriteStatus(Event event, ImageButton btnAddFavorite) {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        DocumentReference eventRef = fStore.collection("events").document(event.getId());

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                Boolean isFavorite = documentSnapshot.getBoolean("favorite");
                if (isFavorite != null && isFavorite){
                    btnAddFavorite.setImageResource(R.drawable.ic_favorite_color);
                }else{
                    btnAddFavorite.setImageResource(R.drawable.ic_favorite_border);
                }
            }
        }).addOnFailureListener(e -> Log.e("checkFavoriteStatus", "Error al obtener el estado del evento " + e));
    }

    public void changeFavoriteStatus(Event event, ImageButton btnAddFavorite) {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        DocumentReference eventRef = fStore.collection("events").document(event.getId());

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isFavorite = documentSnapshot.getBoolean("favorite");
                if (isFavorite != null && isFavorite) {
                    eventRef.update("favorite", false)
                            .addOnSuccessListener(aVoid -> btnAddFavorite.setImageResource(R.drawable.ic_favorite_border))
                            .addOnFailureListener(e -> Log.e("changeFavoriteStatus", "Error al actualizar el estado de favorito: " + e));
                } else {
                    eventRef.update("favorite", true)
                            .addOnSuccessListener(aVoid -> btnAddFavorite.setImageResource(R.drawable.ic_favorite_color))
                            .addOnFailureListener(e -> Log.e("changeFavoriteStatus", "Error al actualizar el estado de favorito: " + e));
                }
            }
        }).addOnFailureListener(e -> Log.e("changeFavoriteStatus", "Error al obtener el evento: " + e));
    }
}
