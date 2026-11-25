/* Generador.java */
/**
 ** Hecho por: Mario Rolando Guerrero Motta
 ** Carnet: [26000668]
 ** Seccion: [CN]
 **/

package scheduler;

import java.util.Random;
import scheduler.processing.ArithmeticProcess;
import scheduler.processing.ConditionalProcess;
import scheduler.processing.IOProcess;
import scheduler.processing.LoopProcess;
import scheduler.processing.SimpleProcess;
import scheduler.scheduling.policies.Policy;

/**
 * Hilo encargado de generar procesos de tipo aleatorio en intervalos de tiempo
 * aleatorios dentro de un rango establecido, asignandoles un ID correlativo
 * e ingresandolos a la politica de calendarizacion.
 */
public class Generador implements Runnable {

    private Policy politica;
    private Procesador procesador;
    private long minTiempoMs;
    private long maxTiempoMs;
    private volatile boolean activo;
    private int contadorId;
    private Random random;

    /**
     * Inicializa el generador de procesos con la politica destino, la referencia
     * al procesador para notificar cambios y el rango de tiempo de llegada en ms.
     *
     * @param politica    Politica donde se encolaran los nuevos procesos
     * @param procesador  Procesador asociado para desplegar el estado en consola
     * @param minTiempoMs Tiempo minimo de espera entre llegadas (en milisegundos)
     * @param maxTiempoMs Tiempo maximo de espera entre llegadas (en milisegundos)
     */
    public Generador(Policy politica, Procesador procesador, long minTiempoMs, long maxTiempoMs) {
        this.politica = politica;
        this.procesador = procesador;
        this.minTiempoMs = minTiempoMs;
        this.maxTiempoMs = maxTiempoMs;
        this.activo = true;
        this.contadorId = 1;
        this.random = new Random();
    }

    /**
     * Ejecuta el ciclo de generacion de procesos: espera un tiempo aleatorio
     * dentro del rango, instancia un proceso aleatorio con ID correlativo,
     * lo agrega a la cola y actualiza la vista en consola.
     */
    @Override
    public void run() {
        while (activo) {
            long espera = calcularTiempoAleatorio();

            try {
                Thread.sleep(espera);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (!activo) {
                break;
            }

            SimpleProcess nuevoProceso = crearProcesoAleatorio(contadorId++);
            politica.add(nuevoProceso);
            procesador.mostrarEstado("Nuevo proceso ingresado a la cola -> " + nuevoProceso.toString());
        }
    }

    /**
     * Calcula un tiempo de espera aleatorio en milisegundos dentro del rango
     * [minTiempoMs, maxTiempoMs].
     *
     * @return Tiempo de espera en milisegundos
     */
    private long calcularTiempoAleatorio() {
        if (maxTiempoMs <= minTiempoMs) {
            return minTiempoMs;
        }
        long diferencia = maxTiempoMs - minTiempoMs;
        return minTiempoMs + (long) (random.nextDouble() * diferencia);
    }

    /**
     * Crea una instancia aleatoria entre ArithmeticProcess, IOProcess,
     * ConditionalProcess y LoopProcess con el ID correlativo indicado.
     *
     * @param id Numero correlativo unico del proceso
     * @return Instancia del proceso creado
     */
    private SimpleProcess crearProcesoAleatorio(int id) {
        int tipo = random.nextInt(4);
        switch (tipo) {
            case 0:
                return new ArithmeticProcess(id);
            case 1:
                return new IOProcess(id);
            case 2:
                return new ConditionalProcess(id);
            default:
                return new LoopProcess(id);
        }
    }

    /**
     * Detiene la ejecucion del hilo generador de manera segura.
     */
    public void detener() {
        this.activo = false;
    }
}