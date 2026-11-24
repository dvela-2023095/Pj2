package scheduler.scheduling.policies;

import java.util.concurrent.ConcurrentLinkedQueue;

import scheduler.processing.SimpleProcess;

public class RoundRobin extends Policy{
    private ConcurrentLinkedQueue<SimpleProcess> cola=new ConcurrentLinkedQueue<>();
    private long quantum;

    public RoundRobin(long quantum){
        this.quantum=quantum;
    }

    public long getQuantum(){
        return this.quantum;
    }
    @Override 
    public synchronized void add(SimpleProcess p){
        cola.add(p);
        size++;
        totalProcesses++;
    }
    @Override
    public synchronized void remove(){
        if(!cola.isEmpty()){
            cola.poll();
            size--;
        }
    }
    @Override
    public synchronized SimpleProcess next(){
        return cola.peek(); 
    }

    @Override
    public synchronized  String toString(){
        System.out.println("PROCESOS EN COLA:");
        String letrero="";
        for(SimpleProcess proceso:cola){
            letrero+= proceso.toString()+"\n";
        }
        return letrero;
    }

    public synchronized void rencolar(SimpleProces p){
        cola.add(p);
        size++;
    }
}
