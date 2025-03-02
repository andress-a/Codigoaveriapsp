package com.example.codigoaveriapsp;

public class CodigoAveria
{
    private String codigo;
    private String descripcion;
    private String solucion;

    // Constructor vacío (necesario para Firebase)
    public CodigoAveria() {}

    public CodigoAveria(String codigo, String descripcion, String solucion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.solucion = solucion;
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

    public String getSolucion() {
        return solucion;
    }

    public void setSolucion(String solucion) {
        this.solucion = solucion;
    }
}
