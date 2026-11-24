package scheduler.scheduling.policies;
import java.util.*;
import scheduler.processing.SimpleProcess;

public class LCFS extends Policy{
    private Stack<SimpleProcess> pila=new Stack<>();
    @Override
    public synchronized void add(SimpleProcess p){
        pila.push(p);
        size++;
        totalProcesses++;
    }
    @Override
    public synchronized  void remove(){
        if(!pila.isEmpty()){
            pila.pop();
            size--;
        }
        
    }

    @Override
    public synchronized  SimpleProcess next(){
        if(!pila.isEmpty()){
            return pila.peek();
        }else{
            return null;
        }
    }

    @Override
    public synchronized  String toString(){
        String letrero="COLA DE LAST-COME FIRST-SERVED\n";
        for(SimpleProcess proceso:pila){
            letrero+=proceso.toString()+"\n";
        }
        return letrero;
    }
    
}
