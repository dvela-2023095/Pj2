/* Procesador.java */
/**
 ** Hecho por: Mario Rolando Guerrero Motta
 ** Carnet: [26000668]
 ** Seccion: [CN]
 **/

package scheduler;

import scheduler.processing.ProcesoBase;
import scheduler.processing.SimpleProcess;
import scheduler.scheduling.policies.Policy;
import scheduler.scheduling.policies.RoundRobin;

/**
 * Hilo encargado de simular la atencion de los procesos almacenados en la cola
 * de una politica de calendarizacion, llevando el control de estadisticas y
 * mostrando el estado actual en consola.
 */
public class Procesador implements Runnable {

    private Policy politica;
    private String nombrePolitica;
    private volatile boolean activo;
    private ProcesoBase procesoActual;
    private int procesosAtendidos;
    private long tiempoTotalAtencion;

    /**
     * Inicializa el procesador con la politica seleccionada y su nombre descriptivo.
     *
     * @param politica       Instancia de la politica de calendarizacion a ejecutar
     * @param nombrePolitica Nombre legible de la politica utilizada
     */
    public Procesador(Policy politica, String nombrePolitica) {
        this.politica = politica;
        this.nombrePolitica = nombrePolitica;
        this.activo = true;
        this.procesoActual = null;
        this.procesosAtendidos = 0;
        this.tiempoTotalAtencion = 0;
    }

    /**
     * Ejecuta el ciclo principal del procesador: extrae el siguiente proceso segun
     * la politica, simula su tiempo de ejecucion con Thread.sleep() y actualiza
     * las estadisticas.
     */
    @Override
    public void run() {
        while (activo) {
            SimpleProcess siguiente = null;

            synchronized (politica) {
                siguiente = politica.next();
                if (siguiente != null) {
                    politica.remove();
                    if (siguiente instanceof ProcesoBase) {
                        procesoActual = (ProcesoBase) siguiente;
                    }
                }
            }

            if (procesoActual != null) {
                mostrarEstado("Iniciando atencion de proceso");

                long tiempoEjecucion = procesoActual.getTiempoRestante();
                boolean esRoundRobin = (politica instanceof RoundRobin);

                if (esRoundRobin) {
                    long quantum = ((RoundRobin) politica).getQuantum();
                    if (tiempoEjecucion > quantum) {
                        tiempoEjecucion = quantum;
                    }
                }

                try {
                    Thread.sleep(tiempoEjecucion);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (!activo) {
                    if (esRoundRobin) {
                        procesoActual.reducir(tiempoEjecucion);
                        if (procesoActual.getTiempoRestante() > 0) {
                            ((RoundRobin) politica).rencolar(procesoActual);
                        } else {
                            procesosAtendidos++;
                            tiempoTotalAtencion += procesoActual.getTiempoTotal();
                        }
                    }
                    procesoActual = null;
                    break;
                }

                if (esRoundRobin) {
                    RoundRobin rr = (RoundRobin) politica;
                    procesoActual.reducir(tiempoEjecucion);

                    if (procesoActual.getTiempoRestante() > 0) {
                        ProcesoBase temp = procesoActual;
                        procesoActual = null;
                        rr.rencolar(temp);
                        mostrarEstado("Quantum agotado, proceso reencolado");
                    } else {
                        procesosAtendidos++;
                        tiempoTotalAtencion += procesoActual.getTiempoTotal();
                        procesoActual = null;
                        mostrarEstado("Proceso finalizado por completo");
                    }
                } else {
                    procesosAtendidos++;
                    tiempoTotalAtencion += procesoActual.getTiempoTotal();
                    procesoActual = null;
                    mostrarEstado("Proceso finalizado");
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Despliega de forma sincronizada y ordenada en consola el estado actual
     * de la simulacion: politica, proceso en atencion, cola de espera y atendidos.
     *
     * @param evento Descripcion breve de la accion que disparo la actualizacion
     */
    public synchronized void mostrarEstado(String evento) {
        System.out.println("==========================================================");
        System.out.println("EVENTO: " + evento);
        System.out.println("Politica: " + nombrePolitica);

        if (procesoActual != null) {
            System.out.println("Atendiendo ahora: " + procesoActual.toString()
                    + " | Tiempo total: " + (procesoActual.getTiempoTotal() / 1000.0) + "s"
                    + " | Restante: " + (procesoActual.getTiempoRestante() / 1000.0) + "s");
        } else {
            System.out.println("Atendiendo ahora: [Ninguno - Procesador en espera]");
        }

        System.out.println("Procesos atendidos hasta el momento: " + procesosAtendidos);
        synchronized (politica) {
            System.out.println(politica.toString());
        }
        System.out.println("==========================================================\n");
    }

    /**
     * Detiene la ejecucion del hilo procesador de manera segura.
     */
    public void detener() {
        this.activo = false;
    }

    /**
     * Imprime el resumen estadistico final requerido al detener la simulacion
     * con la tecla 'q'.
     */
    public synchronized void imprimirEstadisticasFinales() {
        double promedioSegundos = 0.0;
        if (procesosAtendidos > 0) {
            promedioSegundos = (tiempoTotalAtencion / (double) procesosAtendidos) / 1000.0;
        }

        System.out.println("\n================ RESUMEN DE LA SIMULACION ================");
        System.out.println("Politica utilizada: " + nombrePolitica);
        System.out.println("Procesos atendidos por completo: " + procesosAtendidos);
        System.out.println("Procesos sin atender en cola: " + politica.size());
        System.out.printf("Tiempo promedio de atencion por proceso: %.3f segundos%n", promedioSegundos);
        System.out.println("==========================================================");
    }

    /**
     * Devuelve la cantidad de procesos atendidos por completo.
     *
     * @return Numero entero de procesos finalizados
     */
    public int getProcesosAtendidos() {
        return procesosAtendidos;
    }

    /**
     * Devuelve el tiempo promedio de atencion en milisegundos.
     *
     * @return Promedio de tiempo de atencion en ms, o 0.0 si no se atendio ninguno
     */
    public double getTiempoPromedioMs() {
        if (procesosAtendidos == 0) {
            return 0.0;
        }
        return (double) tiempoTotalAtencion / procesosAtendidos;
    }
}