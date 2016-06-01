package it.unitn.disi.smatch.oracles.uby;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class SmubyNotFoundException extends SmubyException {
    
    private static final long serialVersionUID = 1L;

    private SmubyNotFoundException(){
        super();
    }
    
    /**
     * Creates the NotFoundException using the provided throwable
     */
    public SmubyNotFoundException(Throwable tr) {
        super(tr);
    }

    /**
     * Creates the NotFoundException using the provided message and throwable
     */
    public SmubyNotFoundException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * Creates the NotFoundException using the provided message
     */
    public SmubyNotFoundException(String msg) {
        super(msg);
    }
}