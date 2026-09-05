
package com.mycompany.manejodeexcepciones;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ProcesadorCalificaciones {

    private static final int CALIFICACION_MINIMA = 0;
    private static final int CALIFICACION_MAXIMA = 100;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Uso:");
            System.out.println("java ProcesadorCalificaciones <archivo>");
            return;
        }

        String archivo = args[0];

        try {
            double promedio = calcularPromedio(archivo);
            System.out.printf("%nPromedio de valores válidos: %.2f%n", promedio);

        } catch (FileNotFoundException e) {
            System.err.println("No se encontró el archivo: " + archivo);

        } catch (SinDatosValidosException e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            System.err.println("Error: no se pudo leer el archivo: " + e.getMessage());
        }
    }

    public static double calcularPromedio(String archivo)
            throws IOException, SinDatosValidosException {

        double suma = 0;
        int contador = 0;
        int numeroLinea = 0;

        try (
            BufferedReader lector =
                new BufferedReader(new FileReader(archivo))
        ) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                try {
                    if (linea.isBlank()) {
                        throw new IllegalArgumentException(
                            "Error: Línea " + numeroLinea + ": la línea no puede estar vacía."
                        );
                    }

                    int calificacion = Integer.parseInt(linea.trim());
                    validarCalificacion(calificacion);

                    System.out.println("Calificación válida: " + calificacion);
                    suma += calificacion;
                    contador++;

                } catch (NumberFormatException e) {
                    System.err.println(
                        "Error: Línea " + numeroLinea + ": dato no numérico ignorado -> \"" + linea + "\""
                    );

                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());

                } catch (CalificacionInvalidaException e) {
                    System.err.println("Error: Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }

        if (contador == 0) {
            throw new SinDatosValidosException(
                "Error: El archivo \"" + archivo + "\" no contiene calificaciones válidas"
            );
        }

        return suma / contador;
    }

    public static void validarCalificacion(int calificacion)
            throws CalificacionInvalidaException {

        if (calificacion < CALIFICACION_MINIMA || calificacion > CALIFICACION_MAXIMA) {
            throw new CalificacionInvalidaException(
                "Error: Calificación fuera de rango: " + calificacion
            );
        }
    }
}