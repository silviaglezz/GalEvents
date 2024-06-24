package com.sga.galevents.controller;

import com.sga.galevents.model.Event;
import com.sga.galevents.model.HomeModel;

import java.util.List;

public class HomeController {
    private HomeModel homeModel;

    public HomeController(){
        homeModel = new HomeModel();
    }

    public void searchEventsInCities(String[] cities, String countryCode, String radius, String unit, String locale, int maxRetries, SearchEventsCallback callback){
        homeModel.searchEventsInCities(cities, countryCode, radius, unit, locale, maxRetries, new HomeModel.FetchEventsCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                callback.onSuccess(eventList);
            }

            @Override
            public void onRetry(String city, int retryCount, int maxRetries) {
                callback.onRetry(city, retryCount, maxRetries);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void getEvents(GetEventsCallback callback){
        homeModel.getEvents(new HomeModel.GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                callback.onSuccess(eventList);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void deletePastEvents(DeleteEventsCallback callback) {
        homeModel.deletePastEvents(new HomeModel.DeleteEventsCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(String errorMessage, Throwable throwable) {
                callback.onFailure(errorMessage, throwable);
            }
        });
    }

    // Definición de callback para manejar la búsqueda de eventos en ciudades
    public interface SearchEventsCallback {
        void onSuccess(List<Event> eventList);
        void onRetry(String city, int retryCount, int maxRetries);
        void onFailure(String errorMessage);
    }

    // Definición de callback para manejar eventos obtenidos
    public interface GetEventsCallback {
        void onSuccess(List<Event> eventList);
        void onFailure(String errorMessage);
    }

    // Definición de callback para manejar eliminación de eventos
    public interface DeleteEventsCallback {
        void onSuccess();
        void onFailure(String errorMessage, Throwable throwable);
    }
}