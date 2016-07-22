package it.unitn.disi.smatch.oracles.diversicon;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class SmdivNotFoundException extends SmdivException {
    
    private static final long serialVersionUID = 1L;

    private SmdivNotFoundException(){
        super();
    }
    
    /**
     * Creates the NotFoundException using the provided throwable
     */
    public SmdivNotFoundException(Throwable tr) {
        super(tr);
    }

    /**
     * Creates the NotFoundException using the provided message and throwable
     */
    public SmdivNotFoundException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * Creates the NotFoundException using the provided message
     */
    public SmdivNotFoundException(String msg) {
        super(msg);
    }
}