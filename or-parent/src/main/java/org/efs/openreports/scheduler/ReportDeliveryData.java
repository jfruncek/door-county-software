package org.efs.openreports.scheduler;

import java.util.Date;

public interface ReportDeliveryData {

    String getDeliveryMethod();

    public Date getEndTime();

    public Date getStartTime();

    String getStatus();

    String getMessage();


}
