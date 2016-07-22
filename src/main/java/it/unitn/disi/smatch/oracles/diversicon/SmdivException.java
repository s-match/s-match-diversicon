package it.unitn.disi.smatch.oracles.diversicon;


/**
 * A generic runtime exception. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class SmdivException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    protected SmdivException(){
        super();
    }
    
    public SmdivException(Throwable tr) {
        super(tr);
    }

    public SmdivException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public SmdivException(String msg) {
        super(msg);
    }
}