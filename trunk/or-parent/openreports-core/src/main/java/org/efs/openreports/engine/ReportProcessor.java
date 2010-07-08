package org.efs.openreports.engine;

import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.querycache.QueryResults;


/**
 * Handles the data retrieved by a ReportProcessorReportEngine.
 * Provides a hook to do something in response to the data.  A processor may choose to
 * modify the data.  This allows a report to be executed, its values acted upon, and the
 * results of those actions to be recorded so that they may be reflected in the report.
 * @author mconner
 */
public interface ReportProcessor {
    void handleResults(QueryResults queryResults, ReportEngineInput reportInput);
}
