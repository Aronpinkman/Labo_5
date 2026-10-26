package org.example.screen;

import org.example.Connection;
import org.example.screen.components.Buttons;
import org.example.screen.components.PanelResult;
import org.example.screen.components.TextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;

/** Interfaz de calculadora basica con operaciones ejecutadas en Prolog. */
public class Window extends JFrame {

    private static final String[][] EJEMPLOS = {
            {"8 y 2", "8", "2"},
            {"12.5 y 3.5", "12.5", "3.5"},
            {"-9 y 3", "-9", "3"}
    };

    private final TextField txtNumero1 = new TextField("Ej: 8");
    private final TextField txtNumero2 = new TextField("Ej: 2");
    private final PanelResult panelResultado = new PanelResult();
    private final JLabel lblEstado = new JLabel(" ");

    public Window(Connection motor) {
        super("Calculadora básica con Prolog");

        // Presionar Enter en cualquiera de las entradas ejecuta la suma.
        txtNumero1.addActionListener(e -> ejecutar(motor, "sumar"));
        txtNumero2.addActionListener(e -> ejecutar(motor, "sumar"));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);
        agregar(contenido, etiqueta("Primer número"), 6);
        agregar(contenido, txtNumero1, 12);
        agregar(contenido, etiqueta("Segundo número"), 6);
        agregar(contenido, txtNumero2, 12);
        agregar(contenido, crearEjemplos(), 18);
        agregar(contenido, crearBotones(motor), 20);
        agregar(contenido, panelResultado, 10);
        agregar(contenido, crearPie(), 0);

        JPanel raiz = new JPanel(new BorderLayout(0, 20));
        raiz.setBackground(Theme.BG);
        raiz.setBorder(new EmptyBorder(24, 28, 24, 28));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);

        setContentPane(raiz);
        setMinimumSize(new Dimension(670, 620));
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Calculadora básica");
        titulo.setFont(Theme.sans(Font.BOLD, 24));
        titulo.setForeground(Theme.TEXT);

        JLabel subtitulo = new JLabel("Suma, resta, multiplicación y división con reglas en Prolog");
        subtitulo.setFont(Theme.sans(Font.PLAIN, 13));
        subtitulo.setForeground(Theme.MUTED);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(titulo);
        p.add(Box.createVerticalStrut(4));
        p.add(subtitulo);
        return p;
    }

    private JPanel crearEjemplos() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel lbl = new JLabel("Ejemplos:");
        lbl.setForeground(Theme.MUTED);
        lbl.setFont(Theme.sans(Font.PLAIN, 12));
        p.add(lbl);

        for (String[] ejemplo : EJEMPLOS) {
            Buttons chip = new Buttons(ejemplo[0], Theme.CARD, Theme.TEXT, 12, 6, 11);
            chip.addActionListener(e -> {
                txtNumero1.setText(ejemplo[1]);
                txtNumero2.setText(ejemplo[2]);
                txtNumero1.requestFocusInWindow();
            });
            p.add(chip);
        }
        return p;
    }

    private JPanel crearBotones(Connection motor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        p.setOpaque(false);

        crearBoton(p, motor, "Sumar (+)", "sumar", Theme.BLUE);
        crearBoton(p, motor, "Restar (-)", "restar", Theme.BLUE);
        crearBoton(p, motor, "Multiplicar (×)", "multiplicar", Theme.GREEN);
        crearBoton(p, motor, "Dividir (/)", "dividir", Theme.GREEN);
        return p;
    }

    private void crearBoton(JPanel panel, Connection motor, String texto,
                             String operacion, Color color) {
        Buttons boton = new Buttons(texto, color, Theme.BG, 13, 11, 12);
        boton.addActionListener(e -> ejecutar(motor, operacion));
        panel.add(boton);
    }

    private JPanel crearPie() {
        lblEstado.setFont(Theme.sans(Font.PLAIN, 12));
        lblEstado.setForeground(Theme.MUTED);

        Buttons btnCopiar = new Buttons("Copiar resultado", Theme.CARD, Theme.TEXT, 16, 7, 12);
        btnCopiar.addActionListener(e -> copiarResultado());

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblEstado, BorderLayout.WEST);
        p.add(btnCopiar, BorderLayout.EAST);
        return p;
    }

    private void ejecutar(Connection motor, String operacion) {
        panelResultado.setTitulo("Resultado");

        if (txtNumero1.getText().trim().isEmpty() || txtNumero2.getText().trim().isEmpty()) {
            mostrar("", "Ingresa los dos números.", Theme.AMBER);
            return;
        }

        try {
            BigDecimal numero1 = leerNumero(txtNumero1.getText());
            BigDecimal numero2 = leerNumero(txtNumero2.getText());
            String resultado = motor.consultar(operacion, numero1, numero2);
            mostrar(resultado, "Operación realizada: " + operacion, Theme.GREEN);
        } catch (NumberFormatException ex) {
            mostrar("", "Ingresa solo números válidos (ej.: -5, 3.5 o 2).", Theme.AMBER);
        } catch (ArithmeticException ex) {
            mostrar("", ex.getMessage(), Theme.RED);
        } catch (Exception ex) {
            mostrar("", "No se pudo realizar la operación: " + ex.getMessage(), Theme.RED);
        }
    }

    /** Admite tanto punto como coma decimal; evita texto o expresiones Prolog. */
    private static BigDecimal leerNumero(String entrada) {
        String valor = entrada.trim().replace(',', '.');
        if (!valor.matches("[+-]?(?:[0-9]+(?:\\.[0-9]+)?|\\.[0-9]+)")) {
            throw new NumberFormatException("Numero invalido");
        }
        return new BigDecimal(valor).stripTrailingZeros();
    }

    private void mostrar(String resultado, String estado, Color colorEstado) {
        panelResultado.setTexto(resultado);
        lblEstado.setText(estado);
        lblEstado.setForeground(colorEstado);
    }

    private void copiarResultado() {
        String texto = panelResultado.getTexto();
        if (texto.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(texto), null);
        lblEstado.setText("Copiado al portapapeles");
        lblEstado.setForeground(Theme.BLUE);
    }

    private static void agregar(JPanel panel, JComponent componente, int espacioDespues) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(componente);
        if (espacioDespues > 0) {
            panel.add(Box.createVerticalStrut(espacioDespues));
        }
    }

    private static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Theme.sans(Font.BOLD, 12));
        etiqueta.setForeground(Theme.MUTED);
        return etiqueta;
    }
}
