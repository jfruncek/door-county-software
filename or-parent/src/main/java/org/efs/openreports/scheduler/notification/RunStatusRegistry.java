package org.efs.openreports.scheduler.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.efs.openreports.providers.ProviderException;

/**
 * Maintains a lookup of RunStatuses representing the run of some activity. For example, a report
 * book, batch, or report itself (as element of one of the other two).
 * 
 * @author mconner
 */
public class RunStatusRegistry {
    private static Logger log = Logger.getLogger( RunStatusRegistry.class.getName() );
    private ReferenceKeyBuilder referenceKeyBuilder;
    private ConcurrentHashMap<String, RunStatus> referenceKeysToStatuses = new ConcurrentHashMap<String, RunStatus>();
    private RunStatusNotificationSupport runStatusNotificationSupport;

    public RunStatusRegistry() throws ProviderException {
        this.referenceKeyBuilder = new ReferenceKeyBuilder();
        log.info( "Created" );
    };

    public RunStatus addRunStatus( RunType runType, String name, String description ) {
        return addRunStatus( runType, name, description, null );
    }

    /**
     * @param runType
     * @param name
     * @param description
     * @param parent
     * @return
     * @throws IllegalStateExceptino if parent is non-null but does not exist in this registry.
     */
    public synchronized RunStatus addRunStatus( RunType runType, String name, String description, RunStatus parent ) {
        RunStatus newRunStatus = new RunStatus( runType, name, description );
        newRunStatus.setRefererenceKey( referenceKeyBuilder.makeReferenceKey( newRunStatus ) );
        if( parent != null ) {
            validate( parent );
            parent.addChild( newRunStatus );
        }
        referenceKeysToStatuses.put( newRunStatus.getRefererenceKey(), newRunStatus );
        return newRunStatus;
    }

    public int getRootLevelCount() {
        int count = 0;
        for( RunStatus runStatus : referenceKeysToStatuses.values() ) {
            if( runStatus.getParent() == null ) {
                count++;
            }
        }
        return count;
    }

    public synchronized List<RunStatus> getRootLevelStatuses() {
        List<RunStatus> roots = new ArrayList<RunStatus>();
        for( RunStatus runStatus : referenceKeysToStatuses.values() ) {
            if( runStatus.getParent() == null ) {
                roots.add( runStatus );
            }
        }
        return roots;
    }

    /**
     * @param referenceKey
     * @return the parent RunStatus, or null if none, or if key is null.
     */
    public RunStatus getRunStatus( String referenceKey ) {
        return ( referenceKey == null ) ? null : referenceKeysToStatuses.get( referenceKey );
    }

    public int getRunStatusCount() {
        return referenceKeysToStatuses.size();
    }

    public boolean isValidReferenceKeyFormat( String referenceKeyCandidate ) {
        return referenceKeyBuilder.isCorrectFormat( referenceKeyCandidate );
    }

    public void markRunCompleteWithNotification( String referenceKey, String statusMessage, boolean success,
            List<String> detailMessages ) {
        RunStatus rootRunStatus = markRunCompleteWithRootRemove( referenceKey, statusMessage, success, detailMessages );
        if( rootRunStatus != null ) {
            runStatusNotificationSupport.sendRunStatusCompleteNotification( rootRunStatus );
        }
    }

    /**
     * Forces a remove regardless of the status.
     * 
     * @param rootStatus
     */
    public void removeRootStatusWithNotification( RunStatus rootStatus ) {
        if( removeRootStatus( rootStatus ) ) {
            runStatusNotificationSupport.sendRunStatusIncompleteNotification( rootStatus, true );
        }
    }
    
    /**
     * Pass-through
     */
    public void sendInactivityWarning( RunStatus rootStatus ) {
        if (rootStatus.markAsInactivityWarningSent()) { 
            runStatusNotificationSupport.sendRunStatusIncompleteNotification( rootStatus, false );
        }
    }

    public void setRunStatusNotificationSupport( RunStatusNotificationSupport runStatusNotificationSupport ) {
        this.runStatusNotificationSupport = runStatusNotificationSupport;
    }

    protected synchronized RunStatus markRunCompleteWithRootRemove( String referenceKey, String statusMessage,
            boolean success, List<String> detailMessages ) {
        RunStatus runStatus = markRunStatusComplete( referenceKey, statusMessage, success, detailMessages );
        if( runStatus != null ) {
            RunStatus root = runStatus.getRootAncestor();
            if( root.isSubtreeComplete() ) {
                removeAllInTree( root );
                return root;
            }
        }
        return null;
    }

    /**
     * @param referenceKey
     * @param statusMessage
     * @param isSuccess
     * @param detailMessages
     * @return the RunStatus that was updated, or null if none found.
     */
    protected synchronized RunStatus markRunStatusComplete( String referenceKey, String statusMessage,
            boolean isSuccess, List<String> detailMessages ) {
        RunStatus runStatus = getRunStatus( referenceKey );
        if( runStatus == null ) {
            log.info( "No RunStatus found for reference key, " + referenceKey + ". Cannot update run status." );
            return null;
        }
        runStatus.markComplete( statusMessage, isSuccess, detailMessages );
        log.info( "marking " + runStatus.getRunType().getLabel() + ": " + runStatus.getName() + ", "
                + runStatus.getDescription() + " complete" );
        return runStatus;
    }

    /**
     * Removes all the RunStatus elements with the referencesKeys in the given subtree.
     * 
     * @param subTree
     */
    protected synchronized void removeAllInTree( RunStatus subtree ) {
        RunStatus removed = referenceKeysToStatuses.remove( subtree.getRefererenceKey() );
        if( removed == null ) {
            log.warn( "In removeAllInTree, no RunStatus found with referenceKey: " + subtree.getRefererenceKey() );
        } else if( removed != subtree ) {
            log.warn( "In removeAllInTree, RunStatus removed is not same object as requested for referenceKey: ["
                    + subtree.getRefererenceKey() + "] " );
        }
        for( RunStatus child : subtree.getChildren() ) {
            removeAllInTree( child );
        }
    }

    private synchronized boolean removeRootStatus( RunStatus rootStatus ) {
        RunStatus runStatus = getRunStatus( rootStatus.getRefererenceKey() );
        if( runStatus == null ) {
            log.warn( "Attempted to remove run status," + rootStatus.getRefererenceKey() + ", name: "
                    + rootStatus.getName() + ", that doesn't exist in the registry" );
        } else if( runStatus != rootStatus ) {
            log.warn( "Attempted to remove run status," + rootStatus.getRefererenceKey() + ", name: "
                    + rootStatus.getName() + ", that does not match that in registry: name: " + runStatus.getName() );
        } else if( ! runStatus.isRoot() ) {
            log.warn( "Attempted to remove as a root run status," + rootStatus.getRefererenceKey() + ", name: "
                    + rootStatus.getName() + ", that is not actually a root status" );
        } else {
            removeAllInTree( runStatus );
            return true;
        }
        return false;
    }

    /**
     * 
     * @param runStatus
     * @throws IllegalStateException if runStatus is not found in this registry.
     */
    private void validate( RunStatus runStatus ) {
        if( runStatus != null ) {

            RunStatus mappedValue = referenceKeysToStatuses.get( runStatus.getRefererenceKey() );
            if( mappedValue == null ) {
                throw new IllegalStateException( "No object with reference key, " + runStatus.getRefererenceKey()
                        + ", mapped in RunStatusRegistry" );
            }
            if( mappedValue != runStatus ) {
                throw new IllegalStateException( "Object mapped with reference key, " + runStatus.getRefererenceKey()
                        + ", is different than object being validated in RunStatusRegistry" );
            }
        }
    }

}
