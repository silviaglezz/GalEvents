
package com.sga.galevents.model;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sga.galevents.io.TicketMasterApiAdapter;
import com.sga.galevents.io.response.TicketMasterResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeModel {
    private FirebaseFirestore fStore;
    private ExecutorService executorService;
    private static final String apiKey = "A2qjk4CKGbBiLT6Oc7YUL8LdrImtdkGI";
    private int citiesProcessed;

    public HomeModel(){
        fStore = FirebaseFirestore.getInstance();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Método para convertir el formato de la fecha
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

    //Función para eliminar de la base de datos los eventos pasados
    public void deletePastEvents(DeleteEventsCallback callback) {
        //Fecha actual en el formato adecuado
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String currentDate = LocalDate.now().format(sdf);

        fStore.collection("events")
                .whereLessThan("start", currentDate)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        List<Task<Void>> deleteTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String venueRef = document.getString("venueRef");

                            //Eliminar el evento
                            deleteTasks.add(fStore.collection("events").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("deletePastEvents", "Evento eliminado con éxito: " + document.getId());

                                        //Eliminar el venue del evento
                                        if(venueRef != null && !venueRef.isEmpty()){
                                            deleteVenue(venueRef);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("deletePastEvents", "Error al eliminar el evento: " + document.getId(), e)));
                        }
                        //Completar todas las tareas de eliminación
                        Tasks.whenAll(deleteTasks)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure("Error al eliminar eventos pasados", e));
                    } else{
                        Log.e("deletePastEvents", "Error al obtener los eventos: ", task.getException());
                        callback.onFailure("Error al obtener eventos para eliminar", task.getException());
                    }
                });
    }

    //Funcion para buscar eventos en las distintas ciudades gallegas
    public void searchEventsInCities(String[] cities,  String countryCode, String radius, String unit, String locale, int maxRetries, FetchEventsCallback callback) {
        citiesProcessed = 0;

        for (String city: cities){
            fetchEvents(city, radius, unit, locale, countryCode, 0, maxRetries, callback);
        }
    }

    //Funcion para recoger los eventos de cada ciudad
    public void fetchEvents(String city, String radius, String unit, String locale, String countryCode, int retryCount, int maxRetries, FetchEventsCallback callback){
        Call<TicketMasterResponse> call = TicketMasterApiAdapter.getApiService().getEvents(apiKey, radius, unit, locale, city, countryCode);
        call.enqueue(new Callback<TicketMasterResponse>() {
            @Override
            public void onResponse(Call<TicketMasterResponse> call, Response<TicketMasterResponse> response) {
                if (response.isSuccessful()){
                    TicketMasterResponse ticketMasterResponse = response.body();
                    if (ticketMasterResponse != null && ticketMasterResponse.getEmbedded() != null){
                        List<Event> eventList = ticketMasterResponse.getEmbedded().getEventos();
                        if (eventList != null && !eventList.isEmpty()) {
                            Log.d("onResponseFetchEvents", "Numero de eventos: " + eventList.size() + ", en la city: " +city);
                            extractEventsData(eventList, callback);
                            callback.onSuccess(eventList);
                        } else {
                            Log.e("onResponseFetchEvents", "La lista de eventos es nula");
                            callback.onFailure("La lista de eventos es nula para la ciudad: " + city);
                        }
                    }else{
                        Log.e("onResponseFetchEvents", "La respuesta del servidor está vacía o no contiene eventos" + ", ciudad: " + city);
                        callback.onFailure("La respuesta del servidor está vacía o no contiene eventos para la ciudad: " + city);
                    }
                }else if (response.code() == 429){
                    if(retryCount < maxRetries){
                        long backoffTime = (long) Math.pow(2, retryCount) * 1000; //Exponencial backoff
                        Log.e("onResumeFetchEvents", "Too Many Requests. Reintentando en " + backoffTime + " ms para la ciudad: " + city);
                        callback.onRetry(city, retryCount, maxRetries);

                        new Handler(Looper.getMainLooper()).postDelayed(()->{
                            executorService.execute(()-> fetchEvents(city, radius, unit, locale, countryCode, retryCount + 1, maxRetries, callback));
                        }, backoffTime);

                    }else{
                        Log.e("onResumeFetchEvents", "Too Many Requests. Máximo número de reintentos alcanzado para la ciudad: " + city);
                        callback.onFailure("Too Many Requests. Máximo número de reintentos alcanzado para la ciudad: " + city);
                    }
                }else{
                    Log.e("onResumeFetchEvents", "Respuesta no exitosa para la ciudad: " + city + ", Mensaje: " + response.message());
                    callback.onFailure("Respuesta no exitosa para la ciudad: " + city + ", Mensaje: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TicketMasterResponse> call, Throwable t) {
                Log.e("onFailureFetchEvents", "Error al recuperar datos de la API: " + t);
                callback.onFailure("Error al recuperar datos de la API para la ciudad: " + city + ", Error: " + t.getMessage());
            }
        });
    }

    //Extraer los datos de la respuesta de TicketMaster
    public void extractEventsData(List<Event> eventList, FetchEventsCallback callback){
        List<Event> processedEvents  = new ArrayList<>();

        if (eventList != null && !eventList.isEmpty()){
            for (Event embeddedEvent : eventList){
                Log.d("extractEventsData", "Procesando evento: " + embeddedEvent.getName());

                // Extraer datos del evento
                String id = embeddedEvent.getId();
                String name = embeddedEvent.getName();
                String start = embeddedEvent.getDates().getStart().getLocalDate();
                String startTime = embeddedEvent.getDates().getStart().getLocalTime();

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

                Boolean favorite = false;

                String venueName = "";
                String venueAddress = "";
                String venueCity = "";
                if (embeddedEvent.getEmbedded() != null && !embeddedEvent.getEmbedded().getVenues().isEmpty()) {
                    Event.Venues venue = embeddedEvent.getEmbedded().getVenues().get(0);
                    venueName = venue.getName();
                    venueAddress = venue.getAddress() != null ? venue.getAddress().getLine1() : "";
                    venueCity = venue.getCity() != null ? venue.getCity().getName() : "";
                }

                // Crear un nuevo objeto Event y añadirlo a la lista
                Event event = new Event(id, name, start, startTime, category, genre, subgenre, logo, favorite, venueName, venueAddress, venueCity);
                processedEvents.add(event);
            }
        }
        Log.d("extractEventsData", "Número de eventos procesados: " + processedEvents.size());
        saveEvents(processedEvents, new SaveEventsCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(processedEvents);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    //Funcion para guardar los eventos en la base de datos
    public void saveEvents(List<Event> eventList, SaveEventsCallback callback) {
        List<Task<Void>> tasks = new ArrayList<>();

        for (Event event : eventList) {
            Task<Void> task = saveEventToFireStore(event);
            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(aVoid -> {
                    Log.d("saveEvents", "Todos los eventos procesados.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("saveEvents", "Error al procesar algunos eventos.");
                    callback.onFailure("Error al procesar algunos eventos.");
                });
    }

    private Task<Void> saveEventToFireStore(Event event){
        return fStore.runTransaction(transaction -> {
            // Verificar si el evento ya existe
            DocumentReference eventRef = fStore.collection("events").document(event.getId());
            DocumentSnapshot snapshot = transaction.get(eventRef);

            if (snapshot.exists()) {
                // Evento ya existe, no hacer nada
                Log.d("saveEventToFirestore", "El evento con ID " + event.getId() + " ya existe");
                return null; // Retornar nulo para salir de la transacción sin hacer cambios
            } else {
                // Evento no existe, guardarlo
                Map<String, Object> venueMap = new HashMap<>();
                venueMap.put("name", event.getEmbedded().getVenues().get(0).getName());
                venueMap.put("address", event.getEmbedded().getVenues().get(0).getAddress().getLine1());
                venueMap.put("city", event.getEmbedded().getVenues().get(0).getCity().getName());

                // Guardar el mapa en la colección de venues
                DocumentReference venueRef = fStore.collection("venues").document();
                transaction.set(venueRef, venueMap);

                // Convertir la fecha antes de guardarla
                String formattedStart = convertDateFormat(event.getDates().getStart().getLocalDate());

                // Mapa para guardar los datos del evento
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", event.getId());
                eventMap.put("name", event.getName());
                eventMap.put("start", formattedStart);
                eventMap.put("startTime", event.getDates().getStart().getLocalTime());
                eventMap.put("category", event.getClassifications().get(0).getSegment().getName());
                eventMap.put("genre", event.getClassifications().get(0).getGenre().getName());
                eventMap.put("subgenre", event.getClassifications().get(0).getSubGenre().getName());
                eventMap.put("logo", event.getImages().get(0).getUrl());
                eventMap.put("venueRef", venueRef.getId());
                eventMap.put("favorite", event.getFavorite());

                // Guardar el mapa en la colección de eventos
                DocumentReference eventDocRef = fStore.collection("events").document(event.getId());
                transaction.set(eventDocRef, eventMap);

                return null; // Retornar nulo para salir de la transacción después de guardar el evento
            }
        });
    }

    //Funcion para coger 5 eventos de la base de datos y mostrarlos por pantalla
    public void getEvents(GetEventsCallback callback) {
        List<Event> events = new ArrayList<>();

        fStore.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // Lista para mantener todas las tareas de obtener venue
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
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

                                        if (events.size() == result.size()) {
                                            //Barajar la lista de eventos
                                            Collections.shuffle(events);

                                            //Seleccionar los 5 primeros eventos de la lista barajada
                                            List<Event> randomEvents = events.subList(0, Math.min(5, events.size()));
                                            callback.onSuccess(randomEvents);
                                        }
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e("getEvents", "Error al obtener el venue: " + e);
                                    callback.onFailure("Error al obtener el venue");
                                });
                                //Agregar la tarea a la lista de tareas
                                tasks.add(venueTask);
                            }
                            //Cuando todas las tareas de obtener venues se completen
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                                Log.d("getEvents", "Todos los venues obtenidos");
                            });

                        }else{
                            Log.e("getEvents", "La consulta no obtuvo resultados.");
                            callback.onFailure("La consulta no obtuvo resultados");
                        }
                    }else{
                        Log.e("getEvents", "Error al obtener los eventos: ", task.getException());
                        callback.onFailure("Error al obtener eventos");
                    }

                });
    }

    // Función para eliminar un venue de la base de datos
    private void deleteVenue(String venueRef) {
        fStore.collection("venues").document(venueRef)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("deleteVenue", "Venue eliminado con éxito: " + venueRef))
                .addOnFailureListener(e -> Log.e("deleteVenue", "Error al eliminar el venue: " + venueRef, e));
    }

    // Definición de callbacks para manejar resultados de las operaciones
    public interface FetchEventsCallback {
        void onSuccess(List<Event> eventList);
        void onRetry(String city, int retryCount, int maxRetries);
        void onFailure(String errorMessage);
    }

    public interface SaveEventsCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface DeleteEventsCallback {
        void onSuccess();
        void onFailure(String errorMessage, Throwable throwable);
    }

    public interface GetEventsCallback {
        void onSuccess(List<Event> eventList);
        void onFailure(String errorMessage);
    }
}
