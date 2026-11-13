# Rumbo Sur - Gestión de tours y reservas

Trabajo Práctico Obligatorio de **Programación Orientada a Objetos** (UADE, 2° cuatrimestre 2026). Propuesta B: Agencia de Turismo.

Aplicación de escritorio en Java (interfaz gráfica Swing) para la agencia "Rumbo Sur": catálogo de tours, salidas, reservas con cálculo de precio y promociones, pagos, cancelaciones, asignación de guías y vehículos, lista de pasajeros, liquidación de guías y reportes.

## Integrantes

| Nombre | Legajo |
|---|---|
| Núñez, Manuel | 1138585 |
| Vasquez, Victoria | 1218864 |
| Giammarino, Francisco | 1160277 |

Docente: León, María Ángela.

## Requisitos

- **JDK 17 o superior.**
- **IntelliJ IDEA** (recomendado) o **Maven 3.8+**. Maven descarga JUnit la primera vez, así que hace falta conexión a internet.

## Cómo ejecutarlo

### Con IntelliJ IDEA

1. *File → Open* y elegir la carpeta del proyecto (la que tiene `pom.xml`). Aceptar *Open as Project* / *Trust Project*.
2. Esperar a que IntelliJ termine de importar el proyecto Maven.
3. Abrir `src/turismo/main/Main.java` y ejecutar con el botón ▶ junto a `main`.

### Con Maven desde la consola

```bash
mvn package
java -jar target/agencia-turismo.jar
```

Los datos se guardan en la carpeta `datos/`, dentro de la carpeta desde donde se ejecuta. Para volver a los datos de prueba iniciales, cerrá el programa y borrá esa carpeta.

## Cómo correr las pruebas

- **IntelliJ:** clic derecho sobre la carpeta `test` → *Run 'All Tests'*.
- **Maven:** `mvn test`

Son 77 pruebas unitarias de la lógica de negocio (JUnit 4). El plan de pruebas funcionales y sus resultados están en [`docs/Plan_de_Pruebas.md`](docs/Plan_de_Pruebas.md) y en el informe final.

## Datos de prueba

La primera vez que se ejecuta, el sistema carga datos de ejemplo con fechas calculadas desde el día actual:

| Tipo | Datos |
|---|---|
| Tours | NAT-01 Parque Nacional Talampaya (naturaleza, 3 días) · AVE-01 Rafting en el río Atuel (aventura, edad mínima 14, seguro obligatorio) · CUL-01 Caminos del vino en Luján de Cuyo (cultural, 1 día) |
| Salidas | SAL-001 Talampaya en 20 días (confirmada, con guía y vehículo) · SAL-002 Rafting en 40 días (programada) · SAL-003 Bodegas en 75 días (confirmada) · SAL-004 Talampaya en 110 días (sin reservas) |
| Personal | Vendedores Laura Gómez y Martín Pereyra · Guías Sofía Ruiz (aventura y naturaleza) y Diego Fernández (cultural y naturaleza) · Coordinadora Ana Torres · Administrador Carlos Díaz |
| Clientes | Lucía Méndez, Pablo Souza (Brasil), Juan Ibáñez |
| Flota | Micro AB123CD (40), Combi AC456EF (15), Camioneta AD789GH (12) |

### Para probar rápido

- **Promoción anticipada:** reservar en SAL-004 (más de 60 días) aplica el 15 %.
- **Promoción grupo:** 6 o más pasajeros en una misma reserva aplica el 10 % (si también corresponde la anticipada, se aplica la mejor).
- **Edad mínima:** un pasajero menor de 14 años en Rafting es rechazado.
- **Operación completa:** en *Operación*, con SAL-001: *Emitir lista de pasajeros* → *Iniciar salida* → *Finalizar salida* → *Liquidar guía*.

## Estructura del repositorio

```
/docs            enunciado, documento de diseño (Etapa 1), informe final (Etapa 2) y plan de pruebas
/docs/uml        diagramas en StarUML (.mdj) y exportados como imagen (.png)
/src/turismo     código fuente
    interfaces/      Promocion, PoliticaCancelacion, RepositorioAgencia
    clases/          modelo del dominio y enumeraciones
    excepciones/     excepciones de negocio y de persistencia
    persistencia/    guardado en archivo, exportación a texto y datos de prueba
    servicios/       controlador (AgenciaController) y reportes
    ui/              interfaz gráfica Swing
    main/            Main (punto de entrada)
/test            pruebas unitarias JUnit
pom.xml          configuración de Maven (Java 17, JUnit 4)
```

## Patrones aplicados

- **SOLID:** responsabilidad única (SRP), abierto/cerrado (OCP) e inversión de dependencias (DIP).
- **GRASP:** Experto en información, Creador, Controlador y Polimorfismo.

La justificación de cada uno está en el documento de diseño (`docs/Documento_Diseno_Etapa1.pdf`, sección 5). Los cambios respecto del diseño, las dificultades, la distribución de tareas y la reflexión del grupo están en el informe final (`docs/TPO-POO-Entrega2.pdf`).