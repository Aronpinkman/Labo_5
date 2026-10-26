package org.example;

import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Set;

/** Conecta la interfaz Java con las reglas aritmeticas definidas en Prolog. */
public class Connection {
    private static final Set<String> OPERACIONES = Set.of(
            "sumar", "restar", "multiplicar", "dividir");

    private final Prolog engine = new Prolog();

    public Connection() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/calculo.pl")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el archivo calculo.pl");
            }
            engine.setTheory(new Theory(in));
        }
    }

    public String consultar(String operacion, BigDecimal numero1, BigDecimal numero2) throws Exception {
        if (!OPERACIONES.contains(operacion)) {
            throw new IllegalArgumentException("Operación no permitida");
        }
        if (operacion.equals("dividir") && numero2.signum() == 0) {
            throw new ArithmeticException("No se puede dividir entre cero.");
        }

        // Los numeros se validan en Java antes de usarlos en la consulta Prolog.
        String consulta = operacion + "(" + numero1.toPlainString() + ", "
                + numero2.toPlainString() + ", R).";
        SolveInfo info = engine.solve(consulta);
        if (!info.isSuccess()) {
            throw new IllegalStateException("La operación no pudo resolverse.");
        }

        String resultado = info.getTerm("R").toString();
        // Evitar ceros decimales innecesarios al mostrar valores numericos.
        try {
            return new BigDecimal(resultado).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException ex) {
            return resultado;
        }
    }
}
