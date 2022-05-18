package ua.martishyn.app.data.entities;

import ua.martishyn.app.data.entities.enums.ComfortClass;

import java.io.Serializable;

public class Wagon implements Entity {
    private int id;
    private int routeId;
    private ComfortClass comfortClass;
    private int numOfSeats;
    private int priceForSeat;

    public Wagon() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public ComfortClass getComfortClass() {
        return comfortClass;
    }

    public void setComfortClass(ComfortClass comfortClass) {
        this.comfortClass = comfortClass;
    }

    public int getNumOfSeats() {
        return numOfSeats;
    }

    public void setNumOfSeats(int numOfSeats) {
        this.numOfSeats = numOfSeats;
    }

    public int getPriceForSeat() {
        return priceForSeat;
    }

    public void setPriceForSeat(int priceForSeat) {
        this.priceForSeat = priceForSeat;
    }
}
