package com.sga.galevents.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sga.galevents.Event;
import com.sga.galevents.EventAdapter;
import com.sga.galevents.R;
import com.sga.galevents.controller.HomeController;
import com.sga.galevents.io.TicketMasterApiAdapter;
import com.sga.galevents.io.response.TicketMasterResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity{
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<Event> events;
    private HomeController controller;
    private FirebaseFirestore fStore;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fStore = FirebaseFirestore.getInstance();

        rvEvents = findViewById(R.id.rvEvents);
        events = new ArrayList<>();

        adapter = new EventAdapter(events);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        //Inicializar HomeController
        controller = new HomeController();

        //Verificar si hay eventos en la base de datos
        fStore.collection("events").get().addOnCompleteListener(task ->{
            if (task.isSuccessful() && task.getResult().isEmpty()){
                //Si no hay eventos, buscar eventos en las ciudades y guardarlos
                searchEventsInCities();
            }else{
                //Si hay eventos, mostrarlos por pantalla
                getEvents();
            }
        });
        deletePastEvents();
    }

    // Método para buscar eventos en ciudades
    private void searchEventsInCities() {
        String[] cities = {"Vigo", "Pontevedra", "A Coruña", "Santiago de Compostela", "Ourense", "Lugo"};
        String countryCode = "ES";//Spain
        String radius = "60";
        String unit = "km";
        String locale = "*";
        int maxRetries = 5;

        // Llamar al método correspondiente del controlador
        controller.searchEventsInCities(cities, countryCode, radius, unit, locale, maxRetries, new HomeController.SearchEventsCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                // Actualizar la lista de eventos y notificar al adaptador
                events.clear();
                events.addAll(eventList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onRetry(String city, int retryCount, int maxRetries) {
                // Manejar reintento de búsqueda si es necesario
                Log.d("HomeActivity", "Reintentando buscar eventos en " + city + ", intento " + retryCount + "/" + maxRetries);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Manejar fallo en la búsqueda de eventos
                Log.e("HomeActivity", "Error al buscar eventos: " + errorMessage);
            }
        });
    }

    // Método para obtener eventos desde la base de datos
    private void getEvents() {
        controller.getEvents(new HomeController.GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                // Actualizar la lista de eventos y notificar al adaptador
                events.clear();
                events.addAll(eventList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Manejar fallo al obtener eventos
                Log.e("HomeActivity", "Error al obtener eventos: " + errorMessage);
            }
        });
    }

    // Método para eliminar eventos pasados
    private void deletePastEvents() {
        controller.deletePastEvents(new HomeController.DeleteEventsCallback() {
            @Override
            public void onSuccess() {
                // Manejar éxito en la eliminación de eventos pasados
                Log.d("HomeActivity", "Eventos pasados eliminados con éxito");
            }

            @Override
            public void onFailure(String errorMessage, Throwable throwable) {
                // Manejar fallo en la eliminación de eventos pasados
                Log.e("HomeActivity", "Error al eliminar eventos pasados: " + errorMessage, throwable);
            }
        });
    }
}
