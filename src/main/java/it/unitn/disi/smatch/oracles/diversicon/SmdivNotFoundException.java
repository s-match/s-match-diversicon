package it.unitn.disi.smatch.oracles.diversicon;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @since 0.1.0
 * @author <a rel="author" href="http://davidleoni.it/">David Leoni</a>
 */
public class SmdivNotFoundException extends SmdivException {
    
    private static final long serialVersionUID = 1L;

    private SmdivNotFoundException(){
        super();
    }
    
    /**
     * Creates the NotFoundException using the provided throwable
     * @since 0.1.0
     */
    public SmdivNotFoundException(Throwable tr) {
        super(tr);
    }

    /**
     * Creates the NotFoundException using the provided message and throwable
     * @since 0.1.0
     */
    public SmdivNotFoundException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * Creates the NotFoundException using the provided message
     * @since 0.1.0
     */
    public SmdivNotFoundException(String msg) {
        super(msg);
    }
}