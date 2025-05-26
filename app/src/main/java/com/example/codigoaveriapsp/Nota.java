package com.example.codigoaveriapsp;

public class Nota {
    private String id;
    private String usuarioId;
    private String codigoAveria;
    private String contenido;
    private long timestamp;

    public Nota() {
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCodigoAveria() {
        return codigoAveria;
    }

    public void setCodigoAveria(String codigoAveria) {
        this.codigoAveria = codigoAveria;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}