package com.sga.galevents.io.response;

import com.sga.galevents.model.Event;

import java.util.List;

public class TicketMasterResponse {
    private Embedded _embedded;

    public Embedded getEmbedded() {
        return _embedded;
    }

    public static class Embedded {
        private List<Event> events;

        public List<Event> getEventos() {
            return events;
        }
    }


}
