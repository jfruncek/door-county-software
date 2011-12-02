package org.efs.openreports.scheduler.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.efs.openreports.objects.MailMessage;

/**
 * Represents the status for a run of some kind (i.e. having a start and end). Methods refer to the
 * status of this node, and does not include the status of children, unless indicated by the method
 * name (e.g. getSubtree...)
 * 
 * @author mconner
 * 
 */
public class RunStatus {
    protected String description;

    /** optional detail about this status */
    protected ArrayList<String> detailMessages = new ArrayList<String>();

    protected String name;

    protected RunStatus parent;
    /** An identifier by which this status may be referenced in a larger collection (i.e. registry) */
    protected String refererenceKey;
    protected String statusMessage;
    /** Non-null if complete, null otherwise */
    protected Boolean success;
    Map<String, RunStatus> refKeysToChildren = new ConcurrentHashMap<String, RunStatus>();
    private long completeTime = Long.MAX_VALUE;
    private long createTime;

    private String notificationRecipients;
    private RunType runType;

    private boolean inactivityWarningSent = false;

    public RunStatus( RunType runType, String name, String description ) {
        this( runType, name, description, null );
    }

    public RunStatus( RunType runType, String name, String description, String referenceKey ) {
        this.runType = runType;
        this.refererenceKey = referenceKey;
        this.createTime = System.currentTimeMillis();
        this.name = name;
        this.description = description;
    }

    public void addChild( RunStatus child ) {
        child.setParent( this );
        refKeysToChildren.put( child.getRefererenceKey(), child );
    }

    public Collection<RunStatus> getChildren() {
        return refKeysToChildren.values();
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getdetailMessages() {
        return detailMessages;
    }

    public long getLastActivity() {
        long lastActivity = hasValidCompleteTime() ? completeTime : createTime;
        for( RunStatus child : getChildren() ) {
            lastActivity = Math.max( lastActivity, child.getLastActivity() );
        }
        return lastActivity;
    }

    public String getName() {
        return name;
    }

    public String getNotificationRecipients() {
        return notificationRecipients;
    }

    public RunStatus getParent() {
        return parent;
    }

    public String getRefererenceKey() {
        return refererenceKey;
    }

    public RunStatus getRootAncestor() {
        RunStatus node = this;
        while( true ) {
            if( node.getParent() == null )
                return node;
            node = node.getParent();
        }
    }

    /**
     * @return the run type for the status. Note that a RunType may change during the lifetime of a
     *         RunStatus (e.g. REPORT may turn out to be a BATCH report).
     */
    public RunType getRunType() {
        return runType;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Statistics getSubtreeStatistics() {
        return addSubtreeStatistics( new Statistics() );
    }

    public boolean hasChildren() {
        return !refKeysToChildren.isEmpty();
    }

    public boolean isComplete() {
        return hasValidCompleteTime();
    }

    public boolean isFailure() {
        return Boolean.FALSE.equals( success );
    }

    public boolean isLeaf() {
        return !hasChildren();
    }

    public boolean isNonLeaf() {
        return hasChildren();
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isSubtreeComplete() {
        if( isComplete() ) {
            if( hasChildren() ) {
                for( RunStatus child : refKeysToChildren.values() ) {
                    if( !child.isSubtreeComplete() ) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isSuccess() {
        return Boolean.TRUE.equals( success );
    }

    /**
     * @param statusMessage
     * @param success
     * @param detailMessages .
     */
    public void markComplete( String statusMessage, boolean success, List<String> detailMessages ) {
        this.completeTime = System.currentTimeMillis();
        this.statusMessage = statusMessage;
        this.success = success;
        this.detailMessages =
                ( detailMessages != null ) ? new ArrayList<String>( detailMessages ) : new ArrayList<String>();
    }

    public void setCompleteTime( long completeTime ) {
        this.completeTime = completeTime;
    }

    public void setCreateTime( long createTime ) {
        this.createTime = createTime;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @param notificationRecipients a list of recipients email addresses, separated as per
     *            {@link MailMessage#EMAIL_SEPARATORS}
     */
    public void setNotificationRecipients( String notificationRecipients ) {
        this.notificationRecipients = notificationRecipients;
    }

    public void setRefererenceKey( String refererenceKey ) {
        this.refererenceKey = refererenceKey;
    }

    public void setRunType( RunType runType ) {
        this.runType = runType;
    }

    public void setStatusMessage( String statusMessage ) {
        this.statusMessage = statusMessage;
    }

    public void setSuccess( boolean success ) {
        this.success = success;
    }

    public boolean markAsInactivityWarningSent() {
        if (inactivityWarningSent) {
            return false;
        }
        inactivityWarningSent = true;
        return true;
    }

    protected void setParent( RunStatus parent ) {
        this.parent = parent;
    }

    /**
     * @return the Statistics
     */
    private Statistics addSubtreeStatistics( Statistics statistics ) {
        if( isLeaf() ) {
            statistics.leafCount++;
            statistics.leafFailureCount += isFailure() ? 1 : 0;
            statistics.leafSuccessCount += isSuccess() ? 1 : 0;

        } else {
            statistics.nonLeafCount++;
            statistics.nonLeafFailureCount += isFailure() ? 1 : 0;
            statistics.nonLeafSuccessCount += isSuccess() ? 1 : 0;
            for( RunStatus child : refKeysToChildren.values() ) {
                child.addSubtreeStatistics( statistics );
            }
        }
        return statistics;
    }

    private boolean hasValidCompleteTime() {
        return completeTime < Long.MAX_VALUE;
    }
    
    public static class Statistics {
        private int leafCount;
        private int leafFailureCount;
        private int leafSuccessCount;
        private int nonLeafCount;
        private int nonLeafFailureCount;
        private int nonLeafSuccessCount;

        public int getCount() {
            return leafCount + nonLeafCount;
        }

        public int getFailureCount() {
            return leafFailureCount + nonLeafFailureCount;
        }

        public int getIncompleteCount() {
            return getCount() - ( getSuccessCount() + getFailureCount() );
        }

        public int getLeafCount() {
            return leafCount;
        }

        public int getLeafFailureCount() {
            return leafFailureCount;
        }

        public int getLeafSuccessCount() {
            return leafSuccessCount;
        }

        public int getNonLeafCount() {
            return nonLeafCount;
        }

        public int getNonLeafFailureCount() {
            return nonLeafFailureCount;
        }

        public int getNonLeafSuccessCount() {
            return nonLeafSuccessCount;
        }

        public int getSuccessCount() {
            return leafSuccessCount + nonLeafSuccessCount;
        }

        public boolean hasFailures() {
            return getFailureCount() > 0;
        }

        public boolean hasIncompletes() {
            return getIncompleteCount() > 0;
        }

        public boolean hasLeafSuccesses() {
            return getLeafSuccessCount() > 0;
        }

        public boolean hasSuccesses() {
            return getSuccessCount() > 0;
        }

    }

}
