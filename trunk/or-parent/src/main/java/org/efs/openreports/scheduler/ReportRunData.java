package org.efs.openreports.scheduler;

import java.util.Date;
import java.util.List;

import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportUser;

/**
 * Provides data about a report run.
 * @author mconner
 */
public interface ReportRunData {

    Report getReport();

    String getRequestId();

    String getStatus();

    Date  getStartTime();

    Date  getEndTime();

    String getMessage();

    ReportUser getUser();

    boolean isSuccess();

    List<? extends ReportDeliveryData>getDeliveryDatas();

}
