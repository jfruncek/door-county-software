package org.efs.openreports.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.scheduler.notification.RunStatus;

/**
 * Tracks initialization of a RunStatus during initialization for a report using a report schedule
 * or report name.
 * 
 * Threre's a bit of back and forth when creating a RunStatus for a report schedule. If we can't
 * find a report, or some other object needed to schedule the report, we still want an entry in the
 * RunStatus of a larger item (i.e. book or batch), so that the failure to even schedule it is
 * tracked as an error. But we also need to create the run status so that we can get the reference
 * key from to tie it to the schedule (via the requestId). We also need to do all this before we
 * actually schedule any of the reports within a run, since the larger status needs to be set up
 * completely before scheduled reports running (in another thread) and start updating the status.
 * 
 * TODO this should exist outside of the notification. it is used to build it from a schedule or
 * report name.
 * 
 * @author mconner
 * 
 */
public class ReportRunStatusInit {
    List<String> errorMessages = new ArrayList<String>();

    /** only applies if no schedule */
    String reportName;

    RunStatus runStatus;

    /** only applies if no schedule */
    private String requestId;

    private ReportSchedule schedule;

    public ReportRunStatusInit( ReportSchedule schedule ) {
        this.schedule = schedule;
    }

    public ReportRunStatusInit( String reportName ) {
        this.reportName = reportName;
    }

    public void addError( String errorMessage ) {
        errorMessages.add( errorMessage );
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public String getReportName() {
        return hasSchedule() ? schedule.getReport().getName() : reportName;
    }

    public String getRequestId() {
        return ( hasSchedule() ) ? schedule.getRequestId() : requestId;
    }

    public RunStatus getRunStatus() {
        return runStatus;
    }

    public ReportSchedule getSchedule() {
        return schedule;
    }

    public String getScheduleDescription() {
        return hasSchedule() ? schedule.getScheduleDescription() : "na";
    }

    public String getScheduleName() {
        return hasSchedule() ? schedule.getScheduleName() : "na";
    }

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public boolean hasSchedule() {
        return schedule != null;
    }

    public void setRequestId( String requestId ) {
        if( hasSchedule() ) {
            schedule.setRequestId( requestId );
        } else {
            this.requestId = requestId;
        }
    }

    /**
     * As a side-effect, sets the reference key of the run status as the requestId.
     * 
     * @param runStatus
     */
    public void setRunStatus( RunStatus runStatus ) {
        this.runStatus = runStatus;
        setRequestId( runStatus.getRefererenceKey() );
    }
}
