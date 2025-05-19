package com.example.codigoaveriapsp;

import java.io.Serializable;

public class CodigoAveria implements Serializable
{
    private String codigo;
    private String descripcion;
    private String marca;
    private String modelo;
    private String solucion;
    // Timestamp para registrar cuando se accedió/guardó el código
    private long timestamp;

    // Constructor vacío (necesario para Firebase)
    public CodigoAveria() {}

    public CodigoAveria(String codigo, String descripcion, String marca,String modelo, String solucion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.modelo = modelo;
        this.solucion = solucion;
        this.timestamp = System.currentTimeMillis(); // Asignar timestamp actual por defecto

    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getMarca() {
        return marca;
    }
    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSolucion() {
        return solucion;
    }

    public void setSolucion(String solucion) {
        this.solucion = solucion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
