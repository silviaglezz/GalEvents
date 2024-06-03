package com.sga.galevents.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sga.galevents.Event;
import com.sga.galevents.EventAdapter;
import com.sga.galevents.R;
import com.sga.galevents.io.TicketMasterApiAdapter;
import com.sga.galevents.io.response.TicketMasterResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity{
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> events;
    private FirebaseFirestore fStore;
    private String apiKey;
    private ExecutorService executorService;
    private Handler mainThreadHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvEvents = findViewById(R.id.rvEvents);
        events = new ArrayList<>();
        fStore = FirebaseFirestore.getInstance();
        apiKey = "A2qjk4CKGbBiLT6Oc7YUL8LdrImtdkGI";

        adapter = new EventAdapter(events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        executorService = Executors.newFixedThreadPool(4);
        mainThreadHandler = new Handler(Looper.getMainLooper());

        //getEvents();
        searchEventsInCities();
    }

    //Funcion para buscar eventos en las distintas ciudades gallegas
    private void searchEventsInCities() {
        String[] cities = {"Vigo", "Pontevedra", "A Coruña", "Santiago de Compostela", "Ourense", "Lugo"};
        String countryCode = "ES";//Spain
        String radius = "60";
        String unit = "km";
        String locale = "*";
        int maxRetries = 5;

        for (String city: cities){
            fetchEvents(city, radius, unit, locale, countryCode, 0, maxRetries);

        }
    }

    //Funcion para recoger los eventos de cada ciudad
    private void fetchEvents(String city, String radius, String unit, String locale, String countryCode, int retryCount, int maxRetries){
        Call<TicketMasterResponse> call = TicketMasterApiAdapter.getApiService().getEvents(apiKey, radius, unit, locale, city, countryCode);
        call.enqueue(new Callback<TicketMasterResponse>() {
            @Override
            public void onResponse(Call<TicketMasterResponse> call, Response<TicketMasterResponse> response) {
                if (response.isSuccessful()){
                    TicketMasterResponse ticketMasterResponse = response.body();
                    if (ticketMasterResponse != null && ticketMasterResponse.getEmbedded() != null){
                        List<Event> eventList = ticketMasterResponse.getEmbedded().getEventos();
                        if (eventList != null && !eventList.isEmpty()) {
                            Log.d("onResponseSearch", "Numero de eventos: " + eventList.size() + ", en la city: " +city);
                            try {
                                Log.d("onResponseSearch", "Antes de llamar a extractEventsData en " + city);
                                extractEventsData(ticketMasterResponse);
                                Log.d("onResponseSearch", "Después de llamar a extractEventsData en " + city);
                            } catch (Exception e) {
                                Log.e("onResponseSearch", "Error al llamar a extractEventsData", e);
                            }
                        } else {
                            Log.e("onResponseSearch", "La lista de eventos es nula");
                        }
                    }else{
                        Log.e("onResponseSearch", "La respuesta del servidor está vacía o no contiene eventos" + ", ciudad: " + city);
                    }
                }else if (response.code() == 429){
                    if(retryCount < maxRetries){
                        long backoffTime = (long) Math.pow(2, retryCount) * 1000; //Exponencial backoff
                        Log.e("onResponseSearch", "Too Many Requests. Reintentando en " + backoffTime + " ms para la ciudad: " + city);

                        new Handler(Looper.getMainLooper()).postDelayed(()->{
                            executorService.execute(()-> fetchEvents(city, radius, unit, locale, countryCode, retryCount + 1, maxRetries));
                        }, backoffTime);

                    }else{
                        Log.e("onResponseSearch", "Too Many Requests. Máximo número de reintentos alcanzado para la ciudad: " + city);
                    }
                }else{
                    Log.e("onResponseSearch", "Respuesta no exitosa para la ciudad: " + city + ", Mensaje: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TicketMasterResponse> call, Throwable t) {
                Log.e("onFailure", "Error al recuperar datos de la API: " + t);
            }
        });
    }

    //Extraer los datos de la respuesta de TicketMaster
    private void extractEventsData(TicketMasterResponse ticketMasterResponse){
        Log.d("extractEventsData", "Entramos en extractEventsData");

        executorService.execute(() -> {
            try {
                Log.d("extractEventsData", "Procesando eventos en hilo separado");
                List<Event> eventList = new ArrayList<>();
                if (ticketMasterResponse.getEmbedded() != null && ticketMasterResponse.getEmbedded().getEventos() != null){
                    for (Event embeddedEvent : ticketMasterResponse.getEmbedded().getEventos()){
                        Log.d("extractEventsData", "Procesando evento: " + embeddedEvent.getName());

                        // Extraer datos del evento
                        String id = embeddedEvent.getId();
                        String name = embeddedEvent.getName();
                        String start = embeddedEvent.getDates().getStart().getUtc();

                        String category = "";
                        String genre = "";
                        String subgenre = "";
                        if (embeddedEvent.getClassifications() != null && !embeddedEvent.getClassifications().isEmpty()) {
                            Event.Classifications classification = embeddedEvent.getClassifications().get(0);
                            category = classification.getSegment() != null ? classification.getSegment().getName() : "";
                            genre = classification.getGenre() != null ? classification.getGenre().getName() : "";
                            subgenre = classification.getSubGenre() != null ? classification.getSubGenre().getName() : "";
                        }

                        String logo = "";
                        if (embeddedEvent.getImages() != null && !embeddedEvent.getImages().isEmpty()) {
                            logo = embeddedEvent.getImages().get(0).getUrl();
                        }

                        String venueName = "";
                        String venueAddress = "";
                        String venueCity = "";
                        if (embeddedEvent.getEmbedded() != null && embeddedEvent.getEmbedded().getVenues() != null && !embeddedEvent.getEmbedded().getVenues().isEmpty()) {
                            Event.Venues venue = embeddedEvent.getEmbedded().getVenues().get(0);
                            venueName = venue.getName();
                            venueAddress = venue.getAddress() != null ? venue.getAddress().getLine1() : "";
                            venueCity = venue.getCity() != null ? venue.getCity().getName() : "";
                        }
                        // Crear un nuevo objeto Event y añadirlo a la lista
                        Event event = new Event(id, name, start, category, genre, subgenre, logo, venueName, venueAddress, venueCity);
                        eventList.add(event);
                    }
                }
                Log.d("extractEventsData", "Número de eventos procesados: " + eventList.size());
                mainThreadHandler.post(() -> {
                    Log.d("extractEventsData", "Posteando al hilo principal para guardar eventos");
                    saveEvents(eventList);
                });
            }catch (Exception e){
                Log.e("extractEventsData", "Error al procesar eventos: " + e.getMessage());
            }

        });
    }

    //Funcion para guardar los eventos en la base de datos
    private void saveEvents(List<Event> eventList){
        Log.d("saveEvents", "Entramos en saveEvents");

        for (Event event: eventList){
            //Verificar si ya existe un evento con el mismo id
            fStore.collection("events")
                    .whereEqualTo("id", event.getId())
                    .get()
                    .addOnSuccessListener(task ->{
                        if (task.isEmpty()){
                            //Mapa para guardar los datos del venue en la base de datos
                            Map<String, Object> venueMap = new HashMap<>();
                            venueMap.put("name", event.getEmbedded().getVenues().get(0).getName());
                            venueMap.put("address", event.getEmbedded().getVenues().get(0).getAddress().getLine1());
                            venueMap.put("city", event.getEmbedded().getVenues().get(0).getCity().getName());

                            //Guardar el mapa en la coleccion de venues
                            fStore.collection("venues").add(venueMap)
                                    .addOnSuccessListener(documentReference -> {
                                        //Obtener el id del documento
                                        String venueId = documentReference.getId();

                                        //Mapa para guardar los datos del evento
                                        Map<String, Object> eventMap = new HashMap<>();
                                        eventMap.put("id", event.getId());
                                        eventMap.put("name", event.getName());
                                        eventMap.put("start", event.getDates().getStart().getUtc());
                                        eventMap.put("category", event.getClassifications().get(0).getSegment().getName());
                                        eventMap.put("genre", event.getClassifications().get(0).getGenre().getName());
                                        eventMap.put("subgenre", event.getClassifications().get(0).getSubGenre().getName());
                                        eventMap.put("logo", event.getImages().get(0).getUrl());
                                        eventMap.put("venueRef", venueId);

                                        //Guardar el mapa en la coleccion de eventos
                                        fStore.collection("events").add(eventMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("saveEvents", "Evento guardado con éxito: " + event.getName());
                                                    getEvents();
                                                })
                                                .addOnFailureListener(e -> Log.e("saveEvents", "Error al guardar el evento: " + event.getName(), e));
                                    })
                                    .addOnFailureListener(e -> Log.e("saveEvents", "Error al guardar el venue: " + event.getEmbedded().getVenues().get(0).getName(), e));
                        }else{
                            Log.d("saveEvents", "El evento con nombre " + event.getName() + " ya existe.");
                        }
                    });
        }
    }

    //Funcion para coger 5 eventos de la base de datos y mostrarlos por pantalla
    private void getEvents() {
        fStore.collection("events")
                .limit(5)//limitar a los 5 primeros eventos
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                String eventId = document.getId();
                                String eventName = document.getString("name");
                                String eventStart = document.getString("start");
                                String eventVenueRef = document.getString("venueRef");
                                String eventCategory = document.getString("category");
                                String eventGenre = document.getString("genre");
                                String eventSubGenre = document.getString("subgenre");
                                String eventLogo = document.getString("logo");

                                //Obtener la informacion del venue
                                fStore.collection("venues").document(eventVenueRef)
                                        .get()
                                        .addOnSuccessListener(venueDocument -> {
                                           if (venueDocument.exists()){
                                                   String venueName = venueDocument.getString("name");
                                                   String venueAddress = venueDocument.getString("address");
                                                   String venueCity = venueDocument.getString("city");

                                                   // Crear un objeto Event y agregarlo a la lista
                                                   Event event = new Event(eventId, eventName, eventStart, eventCategory, eventGenre, eventSubGenre, eventLogo, venueName, venueAddress, venueCity);
                                                   events.add(event);

                                                   // Notificar al adaptador que los datos han cambiado
                                                   adapter.notifyDataSetChanged();
                                           }
                                        }).addOnFailureListener(e -> Log.e("getEvents", "Error al obtener el venue: " + e));
                            }
                        }else{
                            Log.e("getEvents", "La consulta no obtuvo resultados.");
                        }
                    }else{
                        Log.e("getEvents", "Error al obtener los eventos: ", task.getException());
                    }
                });
    }
}
