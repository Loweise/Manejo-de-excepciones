# Práctica: Manejo de Excepciones en Java — `ProcesadorCalificaciones`

Apuntes y evidencias de la práctica sobre identificación, captura, propagación y generación de excepciones en Java.

---

## 1. Objetivo de aprendizaje

Identificar, capturar, propagar y generar excepciones en Java, aplicando buenas prácticas de manejo de errores mediante `try`, `catch`, `finally`, `throws`, `throw`, excepciones específicas y `try-with-resources`.

**Resultados de aprendizaje:**
- Diferenciar excepciones verificadas (*checked*) y no verificadas (*unchecked*).
- Utilizar correctamente `try`, `catch` y `finally`.
- Propagar excepciones con `throws`.
- Lanzar excepciones mediante `throw`.
- Crear y utilizar una excepción personalizada.
- Utilizar `try-with-resources`.
- Aplicar criterios básicos de buenas prácticas en el manejo de excepciones.

---

## 2. Situación de trabajo

Aplicación `ProcesadorCalificaciones` que lee un archivo de texto con calificaciones y calcula su promedio, manejando de forma controlada: archivo inexistente, líneas no numéricas, calificaciones fuera de rango y errores de lectura.

---

## Parte I. Observar una excepción sin manejar

Ejecución con un archivo que contiene una línea inválida (`abc`):

| Pregunta | Respuesta |
|---|---|
| ¿Qué excepción aparece? | `java.lang.NumberFormatException` |
| ¿En qué línea ocurre? | En la línea donde `Integer.parseInt(linea)` intenta convertir `"abc"` |
| ¿Continúa la ejecución? | No. Al no estar capturada, la excepción se propaga hasta `main`, la JVM la imprime y el programa termina abruptamente |
| ¿Qué información proporciona el stack trace? | El tipo de excepción, el mensaje de error, y la pila de llamadas (clases y métodos, en orden, con el número de línea donde ocurrió cada llamada) hasta el punto donde se originó |

Un objeto excepción contiene información sobre el tipo de error y el estado del programa cuando ocurrió; lanzar una excepción consiste en crear ese objeto y pasarlo al runtime.

---

## Parte II. Capturar una excepción (`try-catch`)

**Observación:** al envolver `Integer.parseInt(linea)` en un `try-catch` con `NumberFormatException`, el programa ya no termina al encontrar `"abc"`; imprime un mensaje de error y continúa procesando las líneas restantes.

### Actividad de aprendizaje 1 — comparar comportamiento

| Aspecto | Sin manejo | Con manejo |
|---|---|---|
| ¿Termina el programa? | Sí, de forma abrupta | No, continúa |
| ¿Se muestra el error? | Sí, como stack trace no controlado | Sí, como mensaje definido por el programador |
| ¿Se procesan las líneas posteriores? | No | Sí |
| ¿Puede recuperarse el programa? | No | Sí |

**Reflexión:** capturar una excepción anticipada permite que el programa siga funcionando ante datos inválidos previstos, en lugar de terminar por completo; el error se convierte en un caso controlado del flujo normal en vez de un fallo fatal.

---

## Parte III. Múltiples excepciones

- Al eliminar o renombrar `calificaciones.txt`, la creación de `new FileReader("calificaciones.txt")` lanza `java.io.FileNotFoundException`, capturada por separado.
- Orden de captura: primero la excepción más específica (`NumberFormatException`), después la más general (`IllegalArgumentException`).

**Pregunta:** ¿Por qué sería incorrecto invertir el orden?
`NumberFormatException` **extiende** `IllegalArgumentException`. Si el `catch` de `IllegalArgumentException` se coloca primero, capturaría también los casos de `NumberFormatException`, dejando el segundo `catch` inalcanzable (*unreachable code*), lo cual Java no permite: el compilador marca error porque ese bloque nunca podría ejecutarse.

---

## Parte IV. `finally`

Se agrega limpieza de recursos con `lector.close()` dentro de un bloque `finally`, protegido a su vez con su propio `try-catch` para el `IOException` que podría lanzar el cierre.

**¿En qué situaciones se ejecuta `finally`?**
Prácticamente siempre: tanto si el bloque `try` termina con éxito, como si lanza una excepción (sea capturada o no), como si el `try` contiene un `return`. Solo no se ejecuta en casos extremos como `System.exit()` dentro del `try`, o si la JVM se detiene abruptamente (caída del proceso, corte de energía, etc.).

Probado con: archivo correcto, archivo inexistente y contenido inválido — en los tres casos `finally` se ejecuta.

---

## Parte V. `try-with-resources`

**Comparación:**

| Pregunta | Respuesta |
|---|---|
| ¿Qué código desapareció? | La variable `lector` declarada fuera del `try`, el bloque `finally` y el `try-catch` anidado para cerrar el recurso |
| ¿Quién cierra ahora el archivo? | El propio bloque `try-with-resources`: al implementar `BufferedReader` la interfaz `AutoCloseable`, Java llama automáticamente a `close()` al finalizar el bloque, incluso si hubo excepción |
| ¿Qué versión resulta más clara? | La de `try-with-resources`: menos código repetitivo, menor riesgo de olvidar cerrar el recurso y menos anidamiento |

---

## Parte VI. Lanzar excepciones con `throw`

Se agrega `validarCalificacion(int calificacion)`, que lanza `IllegalArgumentException` si el valor está fuera de 0–100. `throw` requiere un objeto `Throwable` y se usa para lanzar explícitamente una excepción en el punto donde se detecta la condición de error.

### Actividad de aprendizaje 2 — diseñar validaciones

| Condición | Excepción | Mensaje |
|---|---|---|
| Línea vacía | `IllegalArgumentException` | "La línea no puede estar vacía." |
| Valor no numérico | `NumberFormatException` (lanzada por `Integer.parseInt`) | "For input string: ..." (mensaje propio al capturarla: "Dato no numérico: <línea>") |
| Valor < 0 | `IllegalArgumentException` / `CalificacionInvalidaException` | "Calificación fuera de rango: <valor>" |
| Valor > 100 | `IllegalArgumentException` / `CalificacionInvalidaException` | "Calificación fuera de rango: <valor>" |

Se recomienda usar excepciones lo más específicas posible y evitar declarar genéricamente `throws Exception`.

---

## Parte VII. Propagar excepciones con `throws`

Se separa la responsabilidad de lectura en `procesarArchivo(String nombreArchivo) throws IOException`, dejando que `main` decida cómo manejar el error. Esto es útil cuando el método que detecta el problema no tiene suficiente contexto para decidir la mejor forma de resolverlo, y conviene que un método superior en la pila de llamadas lo maneje.

---

## Parte VIII. Excepción personalizada

```java
public class CalificacionInvalidaException extends Exception {
    public CalificacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
```

Al extender `Exception` (no `RuntimeException`), es una excepción **verificada (checked)**: todo método que pueda lanzarla debe declararlo con `throws` o capturarla obligatoriamente.

---

## Parte IX. Actividad integradora

Comportamiento esperado con el archivo de ejemplo (85, 90, abc, 78, 110, 72, -5, 95):

```
Calificación válida: 85
Calificación válida: 90
Dato ignorado: abc
Calificación válida: 78
Calificación fuera de rango: 110
Calificación válida: 72
Calificación fuera de rango: -5
Calificación válida: 95

Promedio de valores válidos: 84.00
```

La solución implementa: `try`, al menos dos bloques `catch`, `try-with-resources`, `throws`, `throw`, `NumberFormatException`, `IOException`, una excepción personalizada (`CalificacionInvalidaException`) y mensajes descriptivos, siguiendo el código base sugerido en la práctica.

---

## Parte X. Buenas prácticas

### Code review (Actividad de aprendizaje 3)

```java
try {
    int numero = Integer.parseInt(valor);
} catch (Throwable e) {
}
```

**Problemas identificados:**
1. Captura `Throwable`, un tipo demasiado amplio que incluye `Error` (fallos graves de la JVM, como `OutOfMemoryError`) que normalmente no deberían capturarse.
2. El bloque `catch` está vacío: ignora completamente la excepción sin registrar ni informar nada, ocultando el problema.
3. No usa una excepción específica (`NumberFormatException`), perdiendo precisión sobre qué falló realmente.
4. No proporciona ninguna información del error (ni mensaje, ni log, ni contexto) para diagnosticar el problema.

**Versión mejorada:**
```java
try {
    int numero = Integer.parseInt(valor);
} catch (NumberFormatException e) {
    System.err.println("Valor no numérico: " + valor + " (" + e.getMessage() + ")");
}
```

### Documentación con `@throws`

```java
/**
 * Valida una calificación.
 *
 * @param calificacion valor a validar
 * @throws CalificacionInvalidaException si el valor está fuera del rango 0-100
 */
public static void validarCalificacion(int calificacion) throws CalificacionInvalidaException {
    // ...
}
```

Se documentó de la misma forma `calcularPromedio()`, indicando que puede lanzar `IOException`.

### Checklist de buenas prácticas

| Buena práctica | Cumple |
|---|---|
| Utiliza excepciones específicas | ☑ |
| No captura `Throwable` | ☑ |
| No ignora excepciones | ☑ |
| Usa mensajes descriptivos | ☑ |
| Captura primero excepciones específicas | ☑ |
| Utiliza `try-with-resources` | ☑ |
| Documenta `throws` | ☑ |
| No utiliza `throws Exception` sin necesidad | ☑ |

Nota importante del material: no conviene registrar (loggear) una excepción y volver a lanzarla sin necesidad, porque puede generar múltiples mensajes para el mismo problema; si se requiere agregar contexto, es mejor **envolver** la excepción original preservando la causa (`new MiExcepcion("contexto", causaOriginal)`).

---

## 19. Reto final (opcional)

Modificar `main` para recibir el nombre del archivo por argumento (`args[0]`), mostrando el mensaje de uso si falta:

```
Uso:
java ProcesadorCalificaciones <archivo>
```

Casos adicionales a manejar: archivo sin ninguna calificación válida, mediante una excepción propia `SinDatosValidosException` (por ejemplo, lanzada cuando `contador == 0` antes de dividir para calcular el promedio, evitando así una `ArithmeticException` por división entre cero / `NaN`).

---

## 20. Entregables

- [ ] Liga a repositorio
- [ ] `ProcesadorCalificaciones.java`
- [ ] `CalificacionInvalidaException.java`
- [ ] `SinDatosValidosException.java` (si se realiza el reto)
- [ ] `calificaciones.txt`
- [ ] Evidencia de las ejecuciones
- [ ] Respuestas a las preguntas de reflexión (sección 21)
- [ ] Explicación de qué excepciones son *checked* y cuáles *unchecked* en la solución:
  - **Checked:** `IOException`, `FileNotFoundException`, `CalificacionInvalidaException` (extiende `Exception`)
  - **Unchecked:** `NumberFormatException`, `IllegalArgumentException` (extienden `RuntimeException`)

---

## 21. Preguntas de reflexión

1. **¿Qué diferencia existe entre lanzar y capturar una excepción?**
   Lanzar (`throw`) es crear un objeto excepción y entregarlo al runtime cuando se detecta un problema; capturar (`catch`) es interceptar esa excepción en un punto de la pila de llamadas para manejarla en vez de dejar que el programa termine.

2. **¿Qué función tiene `try`?**
   Delimita el bloque de código en el que se vigilan posibles excepciones durante su ejecución.

3. **¿Qué función tiene `catch`?**
   Define el manejo específico a ejecutar cuando ocurre una excepción del tipo indicado dentro del `try` asociado.

4. **¿Cuándo resulta útil `finally`?**
   Cuando se necesita garantizar que cierto código se ejecute siempre —típicamente liberar recursos (cerrar archivos, conexiones, streams)— sin importar si hubo o no excepción.

5. **¿Qué ventaja tiene `try-with-resources`?**
   Cierra automáticamente los recursos `AutoCloseable` al salir del bloque, reduciendo código repetitivo y evitando fugas de recursos por olvido de cierre manual.

6. **¿Cuál es la diferencia entre `throw` y `throws`?**
   `throw` lanza una excepción concreta en un punto del código; `throws` es una declaración en la firma de un método que indica qué excepciones checked puede propagar sin capturarlas internamente.

7. **¿Por qué conviene utilizar excepciones específicas?**
   Porque permiten manejar cada tipo de error de forma distinta y precisa, dan información más clara sobre la causa real del problema y evitan capturar (y ocultar) errores no previstos.

8. **¿Cuándo tiene sentido crear una excepción personalizada?**
   Cuando ninguna excepción estándar de Java describe adecuadamente una condición de error propia del dominio de la aplicación (por ejemplo, "calificación fuera de rango"), y se quiere expresar esa regla de negocio de forma explícita.

9. **¿Por qué no se recomienda capturar `Throwable`?**
   Porque incluye tanto `Exception` como `Error`; los `Error` representan problemas graves de la JVM (como `OutOfMemoryError`) que generalmente no deben ni pueden manejarse en el código de la aplicación, y capturarlos puede ocultar fallos serios.

10. **¿Qué efecto tiene ignorar una excepción?**
    Oculta el problema: el programa puede continuar en un estado inconsistente o inválido sin que nadie se entere, dificultando enormemente el diagnóstico posterior de errores.

11. **¿Qué información debería proporcionar un buen mensaje de excepción?**
    Qué ocurrió, en qué dato o contexto ocurrió (por ejemplo el valor inválido) y, cuando sea útil, una pista de cómo resolverlo o dónde se originó.

12. **¿En qué casos conviene propagar una excepción en lugar de capturarla inmediatamente?**
    Cuando el método actual no tiene el contexto o la responsabilidad para decidir cómo manejar el error, y es más apropiado que lo maneje un método superior en la pila de llamadas, que sí conoce el contexto general de la operación.

---

## 22. Criterios de evaluación (referencia)

| Criterio | Ponderación |
|---|---|
| Identificación correcta de situaciones excepcionales | 10% |
| Uso correcto de `try-catch` | 20% |
| Uso de excepciones específicas | 15% |
| Uso correcto de `throw` y `throws` | 15% |
| Implementación de excepción personalizada | 10% |
| Uso de `try-with-resources` | 10% |
| Aplicación de buenas prácticas | 10% |
| Calidad y claridad del código | 5% |
| Reflexión y evidencias | 5% |
| **Total** | **100%** |