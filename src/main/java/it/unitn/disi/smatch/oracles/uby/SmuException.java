package it.unitn.disi.smatch.oracles.uby;


/**
 * A generic runtime exception. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class SmuException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    protected SmuException(){
        super();
    }
    
    public SmuException(Throwable tr) {
        super(tr);
    }

    public SmuException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public SmuException(String msg) {
        super(msg);
    }
}