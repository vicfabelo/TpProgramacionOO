# Plan de pruebas - Agencia Rumbo Sur

Este plan tiene dos partes: las **pruebas unitarias** automáticas (JUnit, carpeta `/test`) y las **pruebas funcionales** de los casos de uso principales, con dato de entrada, resultado esperado y resultado obtenido.

Las pruebas unitarias se ejecutaron por última vez el 09/11/2026 (77 OK). Las pruebas funcionales se ejecutaron el 15/10/2026 sobre los datos de prueba iniciales del sistema (los que se cargan la primera vez que se abre el programa), con la fecha del sistema fija en 15/10/2026.

## 1. Pruebas unitarias (JUnit 4)

Se ejecutan desde IntelliJ (clic derecho sobre la carpeta `test` → *Run 'All Tests'*) o con `mvn test`.

| Clase de prueba | Qué verifica | Tests | Resultado |
|---|---|---|---|
| `PasajeroTest` | Porcentaje de tarifa por edad y tipo (adulto, menor de 12, menor de 3, jubilado), edad calculada a la fecha de la salida, `Comparator` por apellido y nombre. | 8 | OK |
| `TourTest` | Itinerario ordenado (`Comparable`), tarifa por temporada y por vigencia, edad mínima y seguro obligatorio en aventura, cupo de la salida dentro de los límites del tour. | 11 | OK |
| `ReservaTest` | Precio por tipo de pasajero, promociones (más favorable, sin acumular, no aplica a adicionales), seña del 30 %, recargo 7 % por cuotas, fecha límite del saldo, políticas de cancelación y devolución por medio de pago, reemplazo de pasajero, notas de ajuste. | 25 | OK |
| `SalidaTest` | Cupo excedido, confirmación por cupo mínimo, salida completa, control a 7 días, cancelación de la agencia, guía habilitado y sin superposición, capacidad del vehículo, inicio sin guía o sin lista, lista ordenada, liquidación del guía. | 16 | OK |
| `AgenciaControllerTest` | Primera ejecución con datos de prueba, numeración correlativa de reservas y salidas aunque falle una, salidas en el pasado, documento duplicado, política según quién cancela, vencimientos, vehículo superpuesto, lista de pasajeros en archivo, exportación de reportes. Usa un repositorio en memoria y un reloj fijo. | 10 | OK |
| `GeneradorReportesTest` | Ventas por vendedor ordenadas sin anuladas, ingresos netos de devoluciones, reporte de cancelaciones, conversión a texto. | 4 | OK |
| `RepositorioArchivoTest` | Guardar y volver a leer los datos (con las referencias entre objetos), archivo inexistente, archivo dañado. | 3 | OK |
| **Total** | | **77** | **77 OK, 0 fallas** |

## 2. Pruebas funcionales

| ID | Caso de uso | Dato de entrada | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|
| F01 | CU01/CU02 | Tour de aventura "Escalada" con tarifa pero **sin seguro**; se programa una salida. | Rechazo: los tours de aventura deben ofrecer seguro. | "El tour de aventura Escalada debe ofrecer seguro de viaje". | OK |
| F02 | CU02 | Tour NAT-01 (3 días), fecha 14/11/2026, cupo 10. | Salida PROGRAMADA del 14/11 al 16/11 con código correlativo. | SAL-005 PROGRAMADA del 14/11/2026 al 16/11/2026 (el intento fallido de F01 no consumió el código). | OK |
| F03 | CU02 | Tour NAT-01 (cupo 5 a 15) con cupo 20. | Rechazo por cupo fuera de los límites. | "El cupo de la salida debe estar entre 5 y 15 (límites del tour)". | OK |
| F04 | CU03 | Cliente con documento 35111222 (ya existe). | Rechazo por documento repetido. | "Ya existe un cliente con documento 35111222". | OK |
| F05 | CU04/CU05 | 2 adultos en SAL-004 (febrero = temporada alta, $260.000), reservando 110 días antes. | Reserva PENDIENTE con promoción "Reserva anticipada": 2 × 260.000 × 0,85 = $442.000. | Reserva 5 PENDIENTE, promoción "Reserva anticipada", total $442.000 (tarifas $520.000). | OK |
| F06 | CU04 | 10 pasajeros en SAL-002 (cupo 12, 3 ocupados). | `CupoExcedidoExcepcion` indicando los lugares libres. | "Se piden 10 lugares y quedan 9 en la salida SAL-002". | OK |
| F07 | CU04 | Pasajero de 11 años en Rafting (edad mínima 14). | `PasajeroNoValidoExcepcion`. | "Nene, P tiene 11 años y el tour Rafting en el río Atuel exige 14". | OK |
| F08 | CU06 | Seña de la reserva 3 (30 % de $447.000) por transferencia. | Reserva CONFIRMADA; saldo $312.900. | Reserva 3 CONFIRMADA, pagado $134.100, saldo $312.900. | OK |
| F09 | CU06 | Pago de $100.000 con tarjeta en 3 cuotas. | Recargo 7 % ($7.000) cobrado aparte; el saldo baja $100.000. | Recargo $7.000, cobrado $107.000, saldo $212.900. | OK |
| F10 | CU07 | Contratar "Excursión nocturna de estrellas" ($25.000) a un pasajero de la reserva 1. | El total sube $25.000. | Total $525.000 → $550.000. | OK |
| F11 | CU08 | Reserva en SAL-003 (faltan 75 días), pagado $50.000 en efectivo; cancela el cliente. | Devolución 90 % = $45.000 en efectivo. | 90 % = $45.000 por EFECTIVO. | OK |
| F12 | CU08 | Reserva con $40.000 pagados con tarjeta; cancela la agencia. | Devolución 100 % = $40.000. | 100 % = $40.000. | OK |
| F13 | CU09 | Reemplazar a "Ruiz, Elena" en la reserva 1 (faltan 20 días). | Reemplazo aceptado sin cambiar el total. | Reemplazo aceptado, total sin cambios $550.000. | OK |
| F14 | CU10 | Asignar a Diego Fernández (cultural y naturaleza) a la salida de rafting (aventura). | `GuiaNoDisponibleExcepcion`. | "Fernández, Diego no está habilitado para tours de AVENTURA". | OK |
| F15 | CU10 | Asignar la camioneta de 12 lugares a SAL-001 (cupo 15). | Rechazo por capacidad. | "El vehículo AD789GH tiene 12 lugares y la salida tiene cupo 15". | OK |
| F16 | CU12 | Iniciar SAL-001 sin haber emitido la lista. | `EstadoInvalidoExcepcion`. | "No se puede iniciar la salida SAL-001 sin emitir la lista de pasajeros". | OK |
| F17 | CU11/CU12 | Emitir la lista de SAL-001, iniciar y finalizar. | Archivo con 6 pasajeros; estado EN_CURSO y luego FINALIZADA. | `lista_SAL-001.txt` (6 pasajeros); EN_CURSO → FINALIZADA. | OK |
| F18 | CU13 | Liquidar a Sofía Ruiz por SAL-001 (3 días × $45.000; adicionales vendidos $25.000). | $135.000 + 5 % de $25.000 = $136.250. | 3 × $45.000 + 5 % de $25.000 = $136.250. | OK |
| F19 | CU14 | Reporte de ventas por vendedor. | Vendedores ordenados de mayor a menor venta, sin reservas anuladas. | Gómez, Laura $1.439.000; Pereyra, Martín $1.083.000. | OK |
| F20 | CU15 | Procesar vencimientos 8 días antes de SAL-002 (reserva 3 con saldo). | Reserva 3 anulada por saldo impago (sin devolución); SAL-002 todavía no se cancela. | Reserva 3 anulada por saldo impago; SAL-002 sigue (también se canceló SAL-005, que ya estaba a menos de 7 días sin cupo mínimo). | OK |
| F21 | CU15 | Procesar vencimientos 7 días antes de SAL-002. | SAL-002 cancelada por no alcanzar el cupo mínimo, con devolución del 100 %. | SAL-002 cancelada por no alcanzar el cupo mínimo (devolución 100 %). | OK |
| F22 | Persistencia | Guardar, cerrar y volver a abrir el sistema. | Se recuperan todas las salidas y reservas con su estado. | 5 salidas y 7 reservas recuperadas; SAL-001 sigue FINALIZADA. | OK |

### Error encontrado y corregido durante las pruebas

En la primera ejecución, F02 devolvió **SAL-006** en lugar de SAL-005: el intento fallido de F01 había consumido un código de salida. Se corrigió en `DatosAgencia` y `AgenciaController.programarSalida()` (el código se confirma recién cuando la salida se crea bien, igual que el número de reserva) y se agregó el test `losCodigosDeSalidaNoSeSalteanAunqueFalle`. Después de la corrección todas las pruebas dieron OK.

## 3. Prueba de la interfaz gráfica

Se abrió la aplicación y se recorrieron las cuatro pestañas (*Catálogo y salidas*, *Clientes y reservas*, *Operación* y *Reportes*): las tablas muestran los datos de prueba y al seleccionar un tour o una reserva se ve su detalle.

Además se repitieron a mano desde la interfaz los casos de error, para verificar que el mensaje aparece en un cuadro de advertencia sin cerrar el programa (lo maneja `PanelBase.ejecutar()`):

| ID | Acción en la interfaz | Resultado obtenido | Estado |
|---|---|---|---|
| F04 | Clientes y reservas → Registrar cliente con documento 35111222. | Cuadro de advertencia: "Ya existe un cliente con documento 35111222". El cliente no se registra. | OK |
| F07 | Clientes y reservas → Nueva reserva en Rafting con un pasajero de 11 años (Prueba, Nene). | Cuadro de advertencia: "Prueba, Nene tiene 11 años y el tour Rafting en el río Atuel exige 14". La reserva no se crea. | OK |
| F15 | Operación → Asignar la camioneta AD789GH (12 lugares) a SAL-001 (cupo 15). | Cuadro de advertencia: "El vehículo AD789GH tiene 12 lugares y la salida tiene cupo 15". | OK |
| F16 | Operación → Iniciar SAL-001 sin emitir la lista de pasajeros. | Cuadro de advertencia: "No se puede iniciar la salida SAL-001 sin emitir la lista de pasajeros". | OK |
