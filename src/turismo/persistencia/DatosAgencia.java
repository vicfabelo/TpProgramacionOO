package turismo.persistencia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import turismo.clases.Cliente;
import turismo.clases.Empleado;
import turismo.clases.Proveedor;
import turismo.clases.Reserva;
import turismo.clases.Salida;
import turismo.clases.Tour;
import turismo.clases.Vehiculo;

/**
 * Todo lo que la agencia guarda entre ejecuciones. Se serializa completo a un archivo.
 * Las reservas no se guardan aparte: viven dentro de cada salida.
 */
public class DatosAgencia implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Tour> tours = new ArrayList<>();
    private final List<Salida> salidas = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Vehiculo> vehiculos = new ArrayList<>();
    private final List<Proveedor> proveedores = new ArrayList<>();
    private int ultimoNumeroReserva;
    private int ultimoNumeroSalida;

    public void agregarTour(Tour tour) {
        tours.add(tour);
    }

    public void agregarSalida(Salida salida) {
        salidas.add(salida);
        Collections.sort(salidas); // orden natural: por fecha de inicio
    }

    public void agregarCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    public void agregarEmpleado(Empleado empleado) {
        empleados.add(empleado);
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        vehiculos.add(vehiculo);
    }

    public void agregarProveedor(Proveedor proveedor) {
        proveedores.add(proveedor);
    }

    public int getProximoNumeroReserva() {
        return ultimoNumeroReserva + 1;
    }

    /** Se llama recién cuando la reserva se creó bien, así no quedan números salteados. */
    public void confirmarNumeroReserva() {
        ultimoNumeroReserva++;
    }

    public String getProximoCodigoSalida() {
        return String.format("SAL-%03d", ultimoNumeroSalida + 1);
    }

    /** Igual que con las reservas: el código se consume recién cuando la salida se creó bien. */
    public void confirmarCodigoSalida() {
        ultimoNumeroSalida++;
    }

    public String siguienteCodigoSalida() {
        String codigo = getProximoCodigoSalida();
        confirmarCodigoSalida();
        return codigo;
    }

    /** Todas las reservas de todas las salidas. */
    public List<Reserva> getReservas() {
        List<Reserva> todas = new ArrayList<>();
        for (Salida salida : salidas) {
            todas.addAll(salida.getReservas());
        }
        return todas;
    }

    /** Empleados de un tipo concreto (ej: getEmpleados(Guia.class)). */
    public <T extends Empleado> List<T> getEmpleados(Class<T> tipo) {
        List<T> resultado = new ArrayList<>();
        for (Empleado empleado : empleados) {
            if (tipo.isInstance(empleado)) {
                resultado.add(tipo.cast(empleado));
            }
        }
        return resultado;
    }

    public List<Tour> getTours() {
        return Collections.unmodifiableList(tours);
    }

    public List<Salida> getSalidas() {
        return Collections.unmodifiableList(salidas);
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Empleado> getEmpleados() {
        return Collections.unmodifiableList(empleados);
    }

    public List<Vehiculo> getVehiculos() {
        return Collections.unmodifiableList(vehiculos);
    }

    public List<Proveedor> getProveedores() {
        return Collections.unmodifiableList(proveedores);
    }
}