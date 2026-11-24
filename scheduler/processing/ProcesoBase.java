package scheduler.processing;
public abstract class ProcesoBase extends SimpleProcess{
    protected long tiempoTotal;
    protected long tiempoRestante;
    protected String tipo;

    public ProcesoBase(int id,long tiempoTotal, String tipo){
        super(id);
        this.tiempoTotal=tiempoTotal;
        this.tiempoRestante=tiempoTotal;
        this.tipo=tipo;
    }

    public long getTiempoTotal(){
        return this.tiempoTotal;
    }

    public long getTiempoRestante(){
        return this.tiempoRestante;
    }


    public String getTipo(){
        return this.tipo;
    }

    public void reducir(long quantum){
        if(this.tiempoRestante-quantum>=0){
            this.tiempoRestante=this.tiempoRestante-quantum;
        }else{
            this.tiempoRestante=0;
        }
    }

    @Override
    public String toString(){
        return ("[id:"+super.id+"|"+this.tipo+"|"+this.tiempoRestante+"]");
    }

}