package edu.upc.eetac.dsa.videostore.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marc on 3/01/16.
 * Coleccion de peliculas compradas y alquiladas.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)


public class MoviesUser {
    private List<Buys> bought = new ArrayList<>();
    private List<Rent> rented  = new ArrayList<>();

    public List<Rent> getRented() {
        return rented;
    }

    public void setRented(List<Rent> rented) {
        this.rented = rented;
    }

    public List<Buys> getBought() {
        return bought;
    }

    public void setBought(List<Buys> bought) {
        this.bought = bought;
    }
}
