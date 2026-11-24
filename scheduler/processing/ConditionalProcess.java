package scheduler.processing;

public class ConditionalProcess extends ProcesoBase{
    public static long tiempoDefault;
    public ConditionalProcess(int id){
        super(id,tiempoDefault,"C");
    }
}
