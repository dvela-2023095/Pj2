package scheduler.processing;

public class IOProcess extends ProcesoBase {
    public static long tiempoDefault;
    public IOProcess(int id){
        super(id,tiempoDefault,"IO");
    }
}
