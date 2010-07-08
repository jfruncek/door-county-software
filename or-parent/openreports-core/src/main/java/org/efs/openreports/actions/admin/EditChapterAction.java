package org.efs.openreports.actions.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.UserProvider;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.ParameterAwareHelp;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class EditChapterAction extends ActionSupport implements SessionAware, ParameterAware {
	
	private static final long serialVersionUID = 829728368497092826L;

	private Map<Object, Object> session;
	
	private boolean submitSave;
	private int userId = Integer.MIN_VALUE;
	private int bookId = Integer.MIN_VALUE;
	private int reportId = Integer.MIN_VALUE;
	private int chapterId = Integer.MIN_VALUE;;
	private ReportBook reportBook;
	private Report report;
	private Map<Integer, Report> reports;
	private List<ReportGroup> groups;
	private String chapterName;
	private String exportType;
    private BookChapter chapter;
	private List<ReportParameterMap> reportParameters;
	private Map<String, Object> parameters;
	
	private UserProvider userProvider;
    private ParameterProvider parameterProvider;
    private BookProvider bookProvider;
    private ReportProvider reportProvider;

	
    @SuppressWarnings("unchecked")
	public void setSession(Map session) 
	{
		this.session = session;
	}
    
	@SuppressWarnings("unchecked")
	public void setParameters(Map parameters) 
	{
		this.parameters = parameters;
	}

	public List<ReportParameterMap> getReportParameters()
	{
		return reportParameters;
	}

	public Map<String, Object> getParametersFromReport(Report report) {
		Map<String, Object> reportParameters = new HashMap<String,Object>();
        List<ReportParameterMap> list = report.getParameters();
        for (ReportParameterMap map : list) {
        	reportParameters.put(map.getReportParameter().getName(), map.getReportParameter().getValues()[0].toString());	
        }
		return reportParameters;
	}

    public String execute() {
        try {
            ReportUser user = getUser();

            reportBook = getBook(bookId);
    		
            bookId = reportBook.getId();

    		groups = reportBook.getGroups();
    		if ( groups == null ) {
    			addActionError(getText(LocalStrings.ERROR_BOOK_GROUP_REQUIRED));
    			return INPUT;
    		}
    		
    		reports = getReports(groups);
    		
    		/* Overview of chapter editing process supported by this action.
    		 * 
    		 * Each book chapter consists of a report and saved parameters and options saved as a schedule. 
    		 * 
    		 * Edit existing chapter
    		 * 
    		 *  Default to loading existing data (saved schedule and latest report definition)
    		 *  If a new report is selected, reload report & parameters 
    		 *  If save not requested, default the chapter name to the chosen report name
    		 *  
    		 * New chapter
    		 * 
    		 *  First time through, show list of reports only
    		 *  Once report is selected, default the chapter name to the chosen report name
    		 *  If save requested, create a new schedule
    		 * 
    		*/
    		if (isExistingChapter())
    		{
    			chapter = reportBook.getChapter(chapterId);
    			int reportId = chapter.getReport().getId();
                report = reports.get(reportId); // ensures that latest report definition is used
                
                if ( report == null ) {
        			report = reportProvider.getReport( reportId );
        			if (report == null) {
                        addActionError( getText(LocalStrings.ERROR_REPORT_INVALID));
        			    return INPUT;
        			} else {
        			    reports.put(reportId, report);
                        addActionMessage(getText(LocalStrings.ERROR_REPORT_NOT_GROUP_MEMBER));
        			}
                }

                reportParameters = filterOutParametersProvidedByBook( report.getParameters() );
                parameterProvider.loadReportParameterValues( reportParameters, chapter.getParameters() );
                
                
                if ( differentReportSelectedOnPage() ) {
                	report = reports.get(reportId);
                	reportParameters = filterOutParametersProvidedByBook( report.getParameters() );
                	parameterProvider.loadReportParameterValues( reportParameters, parameters );
                	if ( !submitSave ) {
                		chapterName = report.getName();
                    	exportType = null;
                    	return INPUT;
                	}
                } else {
                	if ( !submitSave ) {
	    	            chapterName = chapter.getName();
	    	            exportType = String.valueOf(chapter.getExportType());
                	}
                }
            } else {
                if ( reportId > Integer.MIN_VALUE ) {
                	report = reports.get(reportId);
        			reportParameters = filterOutParametersProvidedByBook( report.getParameters() );
        			parameterProvider.loadReportParameterValues( reportParameters, parameters );
                	if ( submitSave ) {
            			chapter = new BookChapter();
                	} else {
                    	chapterName = report.getName();
                	}
                }
    		}

    		if( submitSave ) {
    			validateChapter( chapter );
    			if ( hasErrors() ) {
    				return INPUT;
    			}
                return saveChapter( chapter, user );
            }

    		return INPUT;

        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return INPUT;
        }

    }

    /**
     * @param originalParams
     * @return
     */
    private List<ReportParameterMap> filterOutParametersProvidedByBook( List<ReportParameterMap> originalParams ) {
        List<ReportParameterMap> results = new ArrayList<ReportParameterMap>();
        for( ReportParameterMap rpm : originalParams ) {
            if( !ORStatics.isBookProvidedParameter( rpm.getReportParameter().getName() ) ) {
                results.add( rpm );
            }
        }
        return results;
    }

    private boolean differentReportSelectedOnPage() {
		return reportId > Integer.MIN_VALUE && reportId != report.getId();
	}

	private void validateChapter(BookChapter chapter) {
		if ( exportType == null || exportType.length() < 1 ) {
			addActionError(getText(LocalStrings.ERROR_EXPORTTYPE_REQUIRED));
		}
	}

	private Map<Integer, Report> getReports(List<ReportGroup> groups) {
		Map<Integer, Report> reports = new HashMap<Integer, Report>();
		for ( ReportGroup group : groups ) {
			for ( Report report : group.getReports() ) {
				reports.put(report.getId(), report);
			}
		}
		return reports;
	}

	private ReportBook getBook(int id)  throws ProviderException {
		ReportBook book = (ReportBook) session.get( ORStatics.REPORT_BOOK );
		
		if (book == null) {
			book = bookProvider.getReportBook(id);
		}
		
        if ( book == null ) {
            throw new ProviderException(getText(LocalStrings.ERROR_BOOK_INVALID) + " " + id);
		} 
        
        return book;
	}

	private String saveChapter( BookChapter chapter, ReportUser user ) throws ProviderException {

		chapter.setReport(report);
		chapter.setParameters(makeParameterMap(report, parameters));
		chapter.setName(chapterName);	
		chapter.setReportBook( reportBook );
		chapter.setExportType(Integer.parseInt(exportType));
		if ( chapter.getId() == null ) {
			reportBook.getChapters().add(chapter);
		}
		
		bookProvider.updateReportBook(reportBook, user.getName());

		addActionError(getText(LocalStrings.MESSAGE_SCHEDULE_SUCCESSFUL));

		return SUCCESS;
	}

	/**
	 * @param report 
	 * @param parameters A String, Object map populated magically on the action
	 * @return Returns a map containing only form parameters that have the same name as a report parameter.
	 *         Additionally, returns lock flags.
	 */
    private Map<String, Object> makeParameterMap(Report report,	Map<String, Object> parameters) {
        
    	Map<String, Object> map = new HashMap<String, Object>();

    	for (String parm : parameters.keySet()) {
        	for (ReportParameterMap rpMap : report.getParameters()) {
    			String name = rpMap.getReportParameter().getName();
				if ( name.equals( parm ) || parm.equals( name + ORStatics.LOCK ) ) {
					String[] values = (String[]) parameters.get(parm);
					map.put(parm, values[0]);
    			}
			}
		}
		return map;
	}

    private boolean isExistingChapter() {
        return chapterId > Integer.MIN_VALUE;
    }

	public List<Report> getReports() {
		Set<Report> set = new TreeSet<Report>(reports.values());
		return new ArrayList<Report>(set);
	}

	public ReportBook getReportBook() {
		return reportBook;
	}

	public Report getReport() {
		return report;
	}

	public BookChapter getChapter() {
		return chapter;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}
	
	private ReportUser getUser() throws ProviderException {
        ReportUser user;
        if( userId >= 0 ) {
            user = userProvider.getUser( new Integer( userId ) );
        } else {
            user = (ReportUser) ActionContext.getContext().getSession().get( ORStatics.REPORT_USER );
        }
        return user;
    }

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getReportId() {
		return reportId;
	}

	public void setReportId(int reportId) {
		this.reportId = reportId;
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}

	public void setSubmitSave(String value) {
		if (value != null) this.submitSave = true;
	}
	
	public boolean isLocked( String parameterName ) {
		return chapter.getParameters().containsKey( parameterName + ORStatics.LOCK );
	}
	
	public String getParamValue( String parameterName ) {
		String value = null;
		if ( chapter != null && chapter.getParameters() != null) {
        	value = ParameterAwareHelp.getSingleValue( chapter.getParameters(), parameterName );
		}
        if( value == null ) {
            value = getDefaultValue( parameterName, null );
        }
        return value;
    }

	/**
     * @param parameterName
     * @param value
     * @return the default value, if a parameter exists with the given name.
     */
    private String getDefaultValue( String parameterName, String value ) {
        ReportParameterMap rpMap = getReportParameter(parameterName);
        if ( rpMap != null) {
            value = rpMap.getReportParameter().getDefaultValue(); 
        }
        return value;
    }


   /**
    * @param parameterName
    * @return the ReportParamterMap with the given name, or null.
    */
   protected ReportParameterMap getReportParameter( String parameterName ) {
       for( ReportParameterMap rpMap : getReportParameters() ) {
           if( rpMap.getReportParameter().getName().equals( parameterName ) ) {
               return rpMap;
           }
       }
       return null;
   }

	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}

	public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

	public void setBookProvider(BookProvider bookProvider) {
		this.bookProvider = bookProvider;
	}

    public void setReportProvider(ReportProvider reportProvider) {
        this.reportProvider = reportProvider;
    }

}
