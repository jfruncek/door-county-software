package org.efs.openreports.actions.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.struts2.interceptor.ParameterAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.DeliveryMethod;
import org.efs.openreports.ReportConstants.ScheduleType;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.SchedulerProvider;
import org.efs.openreports.scheduler.ReportRunStatusInit;
import org.efs.openreports.scheduler.notification.RunStatus;
import org.efs.openreports.scheduler.notification.RunStatusRegistry;
import org.efs.openreports.scheduler.notification.RunType;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.scripting.BookGroovyContext;
import org.efs.openreports.util.scripting.GroovyContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class RunBookAction extends ActionSupport implements ParameterAware  {

	private static final long serialVersionUID = 5580128221420499072L;

	private int id;
	private boolean submitRun;
	private ReportBook reportBook;
	private Map<String, Object> parameters;
	private List<ReportParameterMap> commonReportParameters;
	private List<ReportParameterMap> otherReportParameters;
	private String outputPath;
	private Map<String, Object> scheduleParameters;
	private String actionName;
	private List<Report> changedReports = new ArrayList<Report>();
	private Map<String, Integer> usageCount = new HashMap<String, Integer>();
	private BookProvider bookProvider;
	private SchedulerProvider schedulerProvider;
    private ParameterProvider parameterProvider;
    private RunStatusRegistry runStatusRegistry;
    private ReportProvider reportProvider;
	private BookGroovyContext scriptContext;

	private ReportUser user;

    @SuppressWarnings("unchecked")
	public void setParameters(Map parameters) 
	{
		this.parameters = parameters;
	}

	public Set<Entry<String,Object>> getScheduleParameters() {
		return scheduleParameters.entrySet();
	}

	@Override
	public String execute() {
		try {
			actionName = ActionContext.getContext().getName();
			user = (ReportUser) ActionContext.getContext().getSession().get(ORStatics.REPORT_USER);
			reportBook = bookProvider.getReportBook(new Integer(id));
			refreshReportParameterValues();
			makeCommonAndOtherParameters();
			outputPath = makeUserFriendlyPath();
			
			if ( submitRun ) {
				if ( "".equals(outputPath.trim())) {
					addActionError( getText( LocalStrings.ERROR_OUTPUTPATH_REQUIRED ) );
					return INPUT;
				}
				replaceScheduleParameters();
				
				scheduleReports(user);
				return SUCCESS;
			}
			return INPUT;
		} catch (Exception e) {            
		    ActionHelper.addExceptionAsError( this, e );
			return INPUT;
		}
	}

	private List<ReportSchedule> createSchedulesFromChapters(ReportBook book) {
		
		ArrayList<ReportSchedule> schedules = new ArrayList<ReportSchedule>();
		for (BookChapter chapter : book.getChapters()) {
			ReportSchedule reportSchedule = new ReportSchedule();
			Report report = chapter.getReport();
			reportSchedule.setReport(report);
			reportSchedule.setReportParameters((Map<String, Object>) chapter.getParameters());
			reportSchedule.setUser(user);
			reportSchedule.setScheduleName(report.getId() + "|" + new Date().getTime());	
			reportSchedule.setJobGroup( Integer.toString( reportBook.getId() ) );
			reportSchedule.setScheduleDescription(chapter.getName());
			reportSchedule.setExportType(chapter.getExportType());
	        reportSchedule.setScheduleType(ScheduleType.NONE.getCode());
			List<String> deliveryMethods = new ArrayList<String>();
			deliveryMethods.add( DeliveryMethod.LAN.getName() );
			reportSchedule.setDeliveryMethods(deliveryMethods.toArray( new String[]{} ));
			
			schedules.add(reportSchedule);
		}
		
		return schedules;
	}

	private String makeUserFriendlyPath() throws Exception {
		String path = GroovyContext.evaluateScriptableElement(scriptContext, reportBook.getOutputPath() + "").replace(
				BookGroovyContext.MNT_PUBLIC,
				BookGroovyContext.R_PUBLIC_SPACE).replace(
				BookGroovyContext.MNT_RISK,
				BookGroovyContext.R_RISK).replace(
				BookGroovyContext.MNT_ACCOUNTING,
				BookGroovyContext.T_DRIVE).replace("/", "\\");
		return path;
	}
	
	private void refreshReportParameterValues() throws ProviderException {
		for (BookChapter chapter : reportBook.getChapters()) {
			Report report = chapter.getReport();
			for (ReportParameterMap map : report.getParameters()) {
				 map.getReportParameter().setValues(null);
			}
			parameterProvider.loadReportParameterValues( report.getParameters(), null );
		}
	}

	private void scheduleReports(ReportUser reportUser) throws Exception {
        long time = new Date().getTime();
        String uniqueID = reportBook.getId() + "|" + reportUser.getId() + "/" + time;
        
        RunStatus bookRunStatus = runStatusRegistry.addRunStatus( RunType.BOOK, reportBook.getName(), reportBook.getName());
        setNotificationRecipients(bookRunStatus, reportUser);

        int index = 0;
        List<ReportRunStatusInit> runStatusInitDatas = new ArrayList<ReportRunStatusInit>();
        List<ReportSchedule> reportSchedules = createSchedulesFromChapters(reportBook);
        for (ReportSchedule schedule : reportSchedules) {
		    schedule.setScheduleType(ScheduleType.ONCE.getCode());
			Report report = schedule.getReport();
            schedule.setScheduleName("BOOK:" + reportBook.getId() + ", REPORT:"+ report.getId() + ", ind:" + index++);
			schedule.setJobGroup( uniqueID );
			schedule.setUser( reportUser );
			schedule.setOutputPaths( makeOutputFileName(schedule) );
            Map<String, Object>parameters = schedule.getReportParameters();
            parameters.put( ORStatics.BASE_OUTPUT_PATH, outputPath );
			
            ReportRunStatusInit reportRunStatusInitData = new ReportRunStatusInit(schedule);
            RunStatus reportRunStatus =
                    runStatusRegistry.addRunStatus( RunType.REPORT, report.getName(), report.getDescription(),
                            bookRunStatus );
            reportRunStatusInitData.setRunStatus( reportRunStatus );
	        parameters.put( ORStatics.REPORT_RUN_REFERERENCE_KEY, reportRunStatusInitData.getRequestId());
	        
            runStatusInitDatas.add( reportRunStatusInitData );
		}
        scheduleReports(runStatusInitDatas);
        markFailedReportsAsComplete(runStatusInitDatas);
        markBookRunStatusAsComplete( bookRunStatus, runStatusInitDatas );
	}
	
    private void markBookRunStatusAsComplete( RunStatus runStatus, List<? extends ReportRunStatusInit> initDatas ) {
        boolean hasErrors = hasSchedulingErrors( initDatas );
        String status = hasErrors ? "error" : "success";
        List<String> detailMessages =
                ( hasErrors ) ? Collections.singletonList( "Errors occured in scheduling" ) : null;
        runStatusRegistry.markRunCompleteWithNotification( runStatus.getRefererenceKey(), status, !hasErrors,
                detailMessages );
    }

    private boolean hasSchedulingErrors( List<? extends ReportRunStatusInit> runStatusInitDatas ) {
        for( ReportRunStatusInit reportRunStatusInit : runStatusInitDatas ) {
            if( reportRunStatusInit.hasErrors() ) {
                return true;
            }
        }
        return false;
    }


	/**
     * after everything has been scheduled, mark any reports that could not be scheduled (and
     * therefore will never generate a NotifyingScheduledReportCallback), as failed.
     * 
     * @param runStatusInitDatas
     */
    private void markFailedReportsAsComplete( List<? extends ReportRunStatusInit> runStatusInitDatas ) {
        for( ReportRunStatusInit runStatusInitData : runStatusInitDatas ) {
            if( runStatusInitData.hasErrors() ) {
                runStatusRegistry.markRunCompleteWithNotification( runStatusInitData.getRequestId(), "error", false,
                        runStatusInitData.getErrorMessages() );
            }
        }
    }

    private void setNotificationRecipients( RunStatus runStatus, ReportUser reportUser ) {
        String emailAddress = ( reportUser == null ) ? null : reportUser.getEmail();
        runStatus.setNotificationRecipients( emailAddress );
    }

    private void scheduleReports( List<ReportRunStatusInit> runStatusInitDatas ) {
        for( ReportRunStatusInit runStatusInitData : runStatusInitDatas ) {
            if( runStatusInitData.hasSchedule() ) {
                try {
                    schedulerProvider.scheduleReport( runStatusInitData.getSchedule() );
                } catch( ProviderException e ) {
                    runStatusInitData.addError( "Error scheduling Report" + e.getMessage() );
                }
            }
        }
    }

	private String makeOutputFileName(ReportSchedule schedule) throws Exception {
		String path = GroovyContext.evaluateScriptableElement(scriptContext, reportBook.getOutputPath() ); 
		path += ("/".equals(outputPath.endsWith("/"))) ? "" : "/";
		String fileName = GroovyContext.evaluateScriptableElement(scriptContext, schedule.getScheduleDescription() );
		return path + fileName;
	}

	private void replaceScheduleParameters() {
		for (BookChapter chapter : reportBook.getChapters()) {
			Map<String, Object> parameters = chapter.getParameters();
			Set<String> locks = removeLocks(parameters);
			for (String name : parameters.keySet()) {
				boolean override = ! locks.contains( name + ORStatics.LOCK );
				String value = ActionHelper.getParamValue(this.commonReportParameters, this.parameters, name);
				if (value == null || "".equals(value) ) {
					value = ActionHelper.getParamValue(this.otherReportParameters, this.parameters, name);
				}
				if ( value != null && !"".equals(value) && override ) {
					parameters.put(name, value);
					scheduleParameters.put(name, value);
				}
			}
		}
		scriptContext = new BookGroovyContext(scheduleParameters);
	}

	private Set<String> removeLocks(Map<String, Object> map) {
		HashSet<String> set = new HashSet<String>();
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String parm = iterator.next();
			if ( parm.endsWith(ORStatics.LOCK) ) {
				set.add(parm);
				iterator.remove();
			}
		}
		return set;
	}

	private void makeCommonAndOtherParameters() throws ProviderException {
		scheduleParameters = new HashMap<String, Object>();
		for (BookChapter chapter : reportBook.getChapters()) {
			Map<String, Object> parameters = chapter.getParameters();
			scheduleParameters.putAll(parameters);
		}
		scriptContext = new BookGroovyContext(scheduleParameters);
		
		makeUsageCounts();
		Map<String, ReportParameterMap> commonParameters = new HashMap<String, ReportParameterMap>();
		Map<String, ReportParameterMap> otherParameters = new HashMap<String, ReportParameterMap>();
		
		for (BookChapter chapter : reportBook.getChapters()) {
			Report report = chapter.getReport();
			
			if (isReportDefinitionChanged(report) && ! changedReports.contains(report)) {
				changedReports.add(report);
			}
			
			List<ReportParameterMap> reportParameters = report.getParameters();
			for (ReportParameterMap reportParameterMap : reportParameters) {
				reportParameterMap.setReport(report);  // want to have report available to the page (this is missing & we need to find out why)  
				ReportParameter parm = reportParameterMap.getReportParameter();
                if (!ORStatics.isBookProvidedParameter( parm.getName() ) ) {
    				if ( showAsCommonParameter(parm.getName()) ) {
    					if ( commonParameters.get(parm.getName()) == null ) {
    						commonParameters.put(parm.getName(), reportParameterMap);
    					}
    				} else if ( showAsOtherParameter(parm.getName()) ) {
    					if ( otherParameters.get(parm.getName()) == null ) {
    						otherParameters.put(parm.getName(), reportParameterMap);
    					}
    				}
                }
			}
		}
		this.commonReportParameters = new ArrayList<ReportParameterMap>();
		this.commonReportParameters.addAll(commonParameters.values());
		Collections.sort(this.commonReportParameters, new CommonParameterComparator());
		this.otherReportParameters = new ArrayList<ReportParameterMap>();
		this.otherReportParameters.addAll(otherParameters.values());
		Collections.sort(this.otherReportParameters, new OtherParameterComparator());
	}

	/**
	 * For purposes of providing information to the user when a book is run, "changed" means 
	 * a parameter is defined on the current report but not on the book (report schedule).
	 * 
	 * @param report
	 * @return whether a report defined in the book has changed
	 * @throws ProviderException
	 */
	private boolean isReportDefinitionChanged(Report report) throws ProviderException {
		List<ReportParameter> bookReportParameters = new ArrayList<ReportParameter>();
		for (ReportParameterMap pm : report.getParameters()) {
			bookReportParameters.add(pm.getReportParameter());
		}
		Report currentReport = reportProvider.getReport(report.getId());
		List<ReportParameterMap> currentReportParameters = currentReport.getParameters();
		for (ReportParameterMap reportParameterMap : currentReportParameters) {
			ReportParameter parm = reportParameterMap.getReportParameter();
			if ( ! bookReportParameters.contains(parm)) {
				return true;
			}
		}
		return false;
	}

	private boolean showAsOtherParameter(String parm) {
		/* Show as other parameter if least one usage is unlocked */
		return usageCount.get(parm) > getLockedUsageCount(parm);
	}

	private boolean showAsCommonParameter(String parm) {
		/* Show as common parameter if usage count more than one and at least one usage is unlocked */
		return usageCount.get(parm) > 1 && usageCount.get(parm) > getLockedUsageCount(parm);
	}

	private Integer getLockedUsageCount(String parm) {
		int count = usageCount.get(parm + ORStatics.LOCK) == null ? 0 : usageCount.get(parm + ORStatics.LOCK);
		return count;
	}
	
	class CommonParameterComparator implements Comparator<ReportParameterMap> {

		/**
		 * Sort dates to the top, then by parameter order
		 */
		public int compare(ReportParameterMap map1, ReportParameterMap map2) {
			if (isDateParameter(map1)) {
				if (isDateParameter(map2)) {
					return map1.compareTo(map2);
				} else {
					return -1;
				}
			} else if (isDateParameter(map2)) {
				return 1;
			} else {
				return map1.compareTo(map2);
			}
		}

		private boolean isDateParameter(ReportParameterMap parm) {
			return parm.getReportParameter().getName().contains("Rigor") || parm.getReportParameter().getName().contains("Date");
		}
		
	}
	
	class OtherParameterComparator implements Comparator<ReportParameterMap> {

		/** 
		 * Sort by report name, then by parameter order
		 */
		public int compare(ReportParameterMap map1, ReportParameterMap map2) {
			int compare = map1.getReport().getName().compareTo(map2.getReport().getName());
			
			if (compare == 0) {
				compare = map1.compareTo(map2);
			}
			
			return compare;
		}
		
	}
	
	private void makeUsageCounts() {
		for (BookChapter chapter : reportBook.getChapters()) {
			//List<ReportParameterMap> reportParameters = schedule.getReport().getParameters();
			//for (ReportParameterMap reportParameterMap : reportParameters) {
			for ( String parm : chapter.getParameters().keySet() ) {
				//ReportParameter parm = reportParameterMap.getReportParameter();
				//Integer value = usageCount.get(parm.getName());
				Integer value = usageCount.get(parm);
				usageCount.put(parm, (value == null) ? 1 : value.intValue() + 1 );
//				value = usageCount.get(parm.getName() + ORStatics.LOCK);
//				usageCount.put(parm.getName() + ORStatics.LOCK, (value == null) ? 1 : value.intValue() + 1 );
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSubmitRun(String value) {
		if (value != null) this.submitRun = true;
	}

	public List<BookChapter> getChapters()
	{
		return reportBook.getChapters();
	}

	public ReportBook getReportBook() {
		return reportBook;
	}

	public List<ReportParameterMap> getCommonReportParameters()
	{
		return commonReportParameters;
	}

	public String getCommonParamValue( String parameterName ) {
		return ActionHelper.getParamValue(commonReportParameters, parameters, parameterName);
	}

	public List<ReportParameterMap> getOtherReportParameters()
	{
		return otherReportParameters;
	}

	public String getOtherParamValue( String parameterName ) {
		return ActionHelper.getParamValue(otherReportParameters, parameters, parameterName);
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getActionName() {
		return actionName;
	}

	public String getChangedReports() {
		String answer = "";
		Iterator<Report> iterator = changedReports.iterator();
		if ( iterator.hasNext() ) {
			answer = iterator.next().getName();
		}
		while (iterator.hasNext()) {
			answer += ", " + iterator.next().getName();
			
		}
		return answer;
	}

	public void setBookProvider(BookProvider groupProvider)
	{
		this.bookProvider = groupProvider;
	}

	public void setSchedulerProvider(SchedulerProvider schedulerProvider)
	{
		this.schedulerProvider = schedulerProvider;
	}

	public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

	public void setReportProvider(ReportProvider reportProvider) {
		this.reportProvider = reportProvider;
	}

	public void setRunStatusRegistry( RunStatusRegistry runStatusRegistry ) {
        this.runStatusRegistry = runStatusRegistry;
    }
	
}
