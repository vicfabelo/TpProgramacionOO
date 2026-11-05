package turismo.clases;

import java.io.Serializable;

/** Tercero que presta un servicio dentro de una actividad (hotel, restaurante, transportista). */
public class Proveedor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String cuit;
    private final String contacto;
    private final String servicio;

    public Proveedor(String nombre, String cuit, String contacto, String servicio) {
        if (nombre == null || nombre.isBlank() || cuit == null || cuit.isBlank()) {
            throw new IllegalArgumentException("El nombre y el CUIT del proveedor son obligatorios");
        }
        this.nombre = nombre.trim();
        this.cuit = cuit.trim();
        this.contacto = contacto == null ? "" : contacto.trim();
        this.servicio = servicio == null ? "" : servicio.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public String getCuit() {
        return cuit;
    }

    public String getContacto() {
        return contacto;
    }

    public String getServicio() {
        return servicio;
    }

    @Override
    public String toString() {
        return nombre + " (" + servicio + ")";
    }
}