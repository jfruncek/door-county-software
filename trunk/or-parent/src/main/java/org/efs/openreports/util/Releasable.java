package org.efs.openreports.util;

/**
 * Something that has resources that should be released, (e.g. in a finally block.)
 * 
 * @author mconner
 * 
 */
public interface Releasable {
    
    void release();
}
