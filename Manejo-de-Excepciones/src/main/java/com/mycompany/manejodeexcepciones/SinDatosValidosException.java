package com.mycompany.manejodeexcepciones;

public class SinDatosValidosException extends Exception {

    public SinDatosValidosException(String mensaje) {
        super(mensaje);
    }
}
