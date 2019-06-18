package com.fermin.sumasml;

public class Jugadores {
    private String nombre;
    private int puntuacion;

    public Jugadores() {

    }

    public Jugadores(String nombre, int puntuacion) {
        this.nombre = nombre;
        this.puntuacion = puntuacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }



    @Override
    public String toString() {
        String tab = new String(Character.toChars(0x09));
        return nombre +tab+tab+tab+tab+puntuacion+" pnt";
    }


}
