/* ProcessScheduler.java */

/**
 ** Hecho por: Alejandro Kolleff
 ** Carnet: 26003679
 ** Seccion: CN
 **/

/*
 * Clase principal del Calendarizador de Procesos:
 * Lee los argumentos ingresados en la consola, verifica la cantidad
 * de argumentos, configura los tiempos de cada tipo de
 * proceso, crea la politica, el procesador y el generador, luego espera la
 * entrada (q + Enter) para detener la simulacion e imprimir el reporte final.
 *
 * Uso:
 *   java ProcessScheduler -fcfs rango_ingreso arith io cond loop
 *   java ProcessScheduler -lcfs rango_ingreso arith io cond loop
 *   java ProcessScheduler -rr   rango_ingreso arith io cond loop quantum
 * Todos los tiempos se indican en segundos (admiten decimales); el
 * rango_ingreso tiene la forma min-max, por ejemplo 1.5-3.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import scheduler.Generador;
import scheduler.Procesador;
import scheduler.processing.ArithmeticProcess;
import scheduler.processing.ConditionalProcess;
import scheduler.processing.IOProcess;
import scheduler.processing.LoopProcess;
import scheduler.scheduling.policies.FCFS;
import scheduler.scheduling.policies.LCFS;
import scheduler.scheduling.policies.Policy;
import scheduler.scheduling.policies.RoundRobin;

public class ProcessScheduler {

    /**
     * Método "main"
     * Recibe: args argumentos de consola: -politica rango arith io cond loop [quantum]
     * Devuelve: void, no devuelve nada
     * Valida los argumentos, inicializa tiempos, politica, procesador y
     * generador, arranca los hilos y espera q + Enter para detenerlos y
     * mostrar el resumen final.
     */

    public static void main(String[] args) {
        if (args.length < 1) {
            uso();
            return;
        }
        String bandera = args[0].toLowerCase(), nombrePolitica = "";
        boolean esRR = bandera.equals("-rr");
        boolean esFCFS = bandera.equals("-fcfs");
        boolean esLCFS = bandera.equals("-lcfs");

        if (!esRR && !esFCFS && !esLCFS) {
            if (bandera.equals("-pp")) {
                System.out.println("La politica -pp (Priority Policy) no esta disponible en esta version.");
            } else {
                System.out.println("Politica desconocida: " + args[0]);
            }
            uso();
            return;
        }
        int esperados = esRR ? 7 : 6;
        if (args.length != esperados) {
            System.out.println("Cantidad de argumentos incorrecta (se esperaban " + esperados + ").");
            uso();
            return;
        }

        long minMs, maxMs, arith, io, cond, loop, quantum = 0;
        try {
            String[] rango = args[1].split("-");
            if (rango.length != 2) {
                throw new NumberFormatException("rango invalido");
            }
            minMs = milis(rango[0]);
            maxMs = milis(rango[1]);
            arith = milis(args[2]);
            io = milis(args[3]);
            cond = milis(args[4]);
            loop = milis(args[5]);
            if (esRR) {
                quantum = milis(args[6]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: los tiempos deben ser numeros positivos y el rango de la forma min-max.");
            uso();
            return;
        }
        if (minMs > maxMs || minMs <= 0 || arith <= 0 || io <= 0 || cond <= 0 || loop <= 0
                || (esRR && quantum <= 0)) {
            System.out.println("Error: los tiempos deben ser mayores que 0 y min <= max en el rango.");
            uso();
            return;
        }

        // Inicializacion: tiempos por tipo de proceso
        ArithmeticProcess.tiempoDefault = arith;
        IOProcess.tiempoDefault = io;
        ConditionalProcess.tiempoDefault = cond;
        LoopProcess.tiempoDefault = loop;

        // Politica, procesador y generador
        Policy policy;
        if (esRR) {
            policy = new RoundRobin(quantum);
            nombrePolitica = "Round Robin (RR), quantum = " + quantum + " ms";
        } else if (esLCFS) {
            policy = new LCFS();
            nombrePolitica = "Last-Come First-Served (LCFS)";
        } else {
            policy = new FCFS();
            nombrePolitica = "First-Come First-Served (FCFS)";
        }
        Procesador procesador = new Procesador(policy, nombrePolitica);
        Generador generador = new Generador(policy, procesador, minMs, maxMs);

        Thread hiloProcesador = new Thread(procesador, "Procesador");
        Thread hiloGenerador = new Thread(generador, "Generador");

        System.out.println("Simulacion iniciada con la politica: " + nombrePolitica);
        System.out.println("Escriba q y presione Enter para detener.");
        hiloProcesador.start();
        hiloGenerador.start();

        // Espera de la entrada (q + Enter); EOF tambien detiene la simulacion
        BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));
        try {
            String linea;
            while ((linea = consola.readLine()) != null) {
                if (linea.trim().equalsIgnoreCase("q")) {
                    break;
                }
            }
        } catch (IOException e) {
            // también se detiene la simulacion
        }

        // Parada y reporte final
        generador.detener();
        procesador.detener();
        try {
            hiloGenerador.join();
            hiloProcesador.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // El resumen final (atendidos, en cola, promedio y politica) lo imprime el Procesador
        procesador.imprimirEstadisticasFinales();
    }

    /**
     * Método "milis"
     * Recibe: texto numero en segundos (admite decimales), por ejemplo "1.5"
     * Devuelve: el tiempo equivalente en milisegundos
     * Convierte un argumento de segundos a milisegundos.
     */

    private static long milis(String texto) {
        return Math.round(Double.parseDouble(texto) * 1000);
    }

    /**
     * Método "uso"
     * Recibe: (ninguno)
     * Devuelve: void, no devuelve nada
     * Imprime en pantalla la forma correcta de ejecutar el programa.
     */

    private static void uso() {
        System.out.println("Uso:");
        System.out.println("  java ProcessScheduler -fcfs rango_ingreso arith io cond loop");
        System.out.println("  java ProcessScheduler -lcfs rango_ingreso arith io cond loop");
        System.out.println("  java ProcessScheduler -rr   rango_ingreso arith io cond loop quantum");
        System.out.println("Tiempos en segundos; rango_ingreso con forma min-max (ej. 1.5-3).");
    }
}
