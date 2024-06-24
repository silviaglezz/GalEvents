package com.sga.galevents.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {
    private String id;
    private String name;
    private Dates dates;
    private List<Classifications> classifications;
    private List<Images> images;
    private _Embedded _embedded;
    private Boolean favorite;

    // Constructor para inicializar un Event con los detalles necesarios
    public Event(String id, String name, Dates dates, List<Classifications> classifications, List<Images> images, _Embedded _embedded) {
        this.id = id;
        this.name = name;
        this.dates = dates;
        this.classifications = classifications;
        this.images = images;
        this._embedded = _embedded;
    }

    // Constructor para inicializar un Event con todos los detalles
    public Event(String id, String name, String start, String startTime, String category, String genre,
                 String subGenre, String logo, Boolean favorite, String venueName, String address, String city) {
        this.id = id;
        this.name = name;
        this.dates = new Dates(new Dates.Start(start, startTime));

        //inicializar la lista y añadir elementos
        this.classifications = new ArrayList<>();
        this.classifications.add(new Classifications(new Classifications.Segment(category), new Classifications.Genre(genre), new Classifications.SubGenre(subGenre)));

        this.images = new ArrayList<>();
        this.images.add(new Images(logo));

        this.favorite = favorite;

        List<Venues> venues = new ArrayList<>();
        venues.add(new Venues(venueName, new Venues.Address(address), new Venues.City(city)));
        this._embedded = new _Embedded(venues);
    }

    // Métodos getter y setter
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dates getDates() {
        return dates;
    }

    public List<Classifications> getClassifications() {
        return classifications;
    }

    public List<Images> getImages() {
        return images;
    }

    public _Embedded getEmbedded() {
        return _embedded;
    }

    public Boolean getFavorite(){
        return favorite;
    }

    public void setFavorite(java.lang.Boolean favorite) {
        this.favorite = favorite;
    }

    public static class Dates {
        private Start start;

        public Dates(Start start) {
            this.start = start;
        }

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }

        public static class Start {
            private String localDate;
            private String localTime;

            public Start(String localDate, String localTime) {
                this.localDate = localDate;
                this.localTime = localTime;
            }

            public String getLocalDate() {
                return localDate;
            }

            public void setLocalDate(String localDate) {
                this.localDate = localDate;
            }

            public String getLocalTime() {
                return localTime;
            }

            public void setLocalTime(String localTime) {
                this.localTime = localTime;
            }
        }
    }

    public static class Classifications {
        private Segment segment;
        private Genre genre;
        private SubGenre subGenre;

        public Classifications(Segment segment, Genre genre, SubGenre subGenre) {
            this.segment = segment;
            this.genre = genre;
            this.subGenre = subGenre;
        }

        public Segment getSegment() {
            return segment;
        }

        public void setSegment(Segment segment) {
            this.segment = segment;
        }

        public Genre getGenre() {
            return genre;
        }

        public void setGenre(Genre genre) {
            this.genre = genre;
        }

        public SubGenre getSubGenre() {
            return subGenre;
        }

        public void setSubGenre(SubGenre subGenre) {
            this.subGenre = subGenre;
        }

        public static class Segment {
            private String name;

            public Segment(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class Genre {
            private String name;

            public Genre(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class SubGenre {
            private String name;

            public SubGenre(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

    public static class Images {
        private String url;

        public Images(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class _Embedded {
        private List<Venues> venues;

        public _Embedded(List<Venues> venues) {
            this.venues = venues;
        }

        public List<Venues> getVenues() {
            return venues;
        }

        public void setVenues(List<Venues> venues) {
            this.venues = venues;
        }
    }

    public static class Venues {
        private String name;
        private Address address;
        private City city;

        public Venues(String name, Address address, City city) {
            this.name = name;
            this.address = address;
            this.city = city;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public City getCity() {
            return city;
        }

        public void setCity(City city) {
            this.city = city;
        }

        public static class Address {
            private String line1;

            public Address(String line1) {
                this.line1 = line1;
            }

            public String getLine1() {
                return line1;
            }

            public void setLine1(String line1) {
                this.line1 = line1;
            }
        }

        public static class City {
            private String name;

            public City(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}