package it.unitn.disi.smatch.oracles.uby;


/**
 * A generic runtime exception. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class SmubyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    protected SmubyException(){
        super();
    }
    
    public SmubyException(Throwable tr) {
        super(tr);
    }

    public SmubyException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public SmubyException(String msg) {
        super(msg);
    }
}