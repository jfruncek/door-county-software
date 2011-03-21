/*
 * Copyright (C) 2007 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.efs.openreports.util.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.apache.xbean.spring.context.FileSystemXmlApplicationContext;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.SchedulerProvider;

public class ScheduleMigrator {
	
	protected static Logger log = Logger.getLogger( ScheduleMigrator.class );

	Options options = new Options();
	CommandLineParser parser = new PosixParser();
	CommandLine commandLine;
	FileSystemXmlApplicationContext context;
	BookProvider bookProvider;
	List<ReportBook> reportBooks;
	SchedulerProvider schedulerProvider;
	List<ReportSchedule> schedules;

	public ScheduleMigrator(String[] args) {
		initOptions();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			log.info("Could not parse command line");
		}
		createBeans();
	}

	void initOptions() {
		options.addOption("m", false, "migrate schedules");
	}

	boolean migrateRequested() {
		return commandLine.hasOption('m');
	}

	void loadBooks() {
		log.info("Loading Books...");
		try {
			reportBooks = bookProvider.getReportBooks();
		} catch (ProviderException e) {
			log.info("Could not get books: " + e);
		}
	}

	void migrateBooks() {
		log.info("Migrating Books...");
		for (ReportBook reportBook : reportBooks) {
			log.info("Migrating book: " + reportBook.getName());

			try {
				schedules = schedulerProvider.getScheduledReports(reportBook.getId()
						.toString());
			} catch (ProviderException e) {
				log.info("Could not get schedules");
			}
			
			ArrayList<BookChapter> chapters = new ArrayList<BookChapter>();

			for (ReportSchedule schedule : schedules) {
				log.info("Adding schedule: " + schedule.getScheduleName()
						+ "(" + schedule.getScheduleDescription() + ")"
						+ " jobGroup: " + schedule.getJobGroup());
				
				BookChapter chapter = new BookChapter();
//				chapter.setReportBook(reportBook);
				chapter.setExportType(schedule.getExportType());
				chapter.setName(schedule.getScheduleDescription());
				chapter.setReport(schedule.getReport());
				chapters.add(chapter);
				
				Map<String, Object> parameters = new HashMap<String, Object>();
				Map<String, Object> parms = schedule.getReportParameters();
				for (String parmName : parms.keySet()) {
					String[] values = (String[]) parms.get(parmName);
					parameters.put(parmName, values[0]);
				}
				chapter.setParameters(parameters);
			}

			reportBook.setChapters(chapters);
			try {
				bookProvider.updateReportBook(reportBook, "admin");
				log.info("Updated book " + reportBook);
				
			} catch (ProviderException e) {
				log.info("Could not update book: " + reportBook + " due to: " + e);
			}
			
			try {
				deleteBookReportSchedules(reportBook.getId().toString());
			} catch (ProviderException e) {
				log.info("Could not delete reports for book: " + reportBook.getName());
			}
			//System.exit(0);
		}
	}
	
	void deleteBookReportSchedules(String bookId) throws ProviderException {
		List<ReportSchedule> schedules = schedulerProvider.getScheduledReports( bookId );
		for (ReportSchedule reportSchedule : schedules) {
			schedulerProvider.deleteScheduledReport( bookId, reportSchedule.getScheduleName() );
			log.info("Deleted book report schedule: " + reportSchedule.getScheduleName());
		}
	}

	void createBeans() {
		log.info("Initializing Spring Context...");
		try {
			context = new FileSystemXmlApplicationContext(
					"database/spring/scheduleMigrationApplicationContext.xml");
			// UserProvider userProvider = (UserProvider)
			// appContext.getBean("userProvider", UserProvider.class);
			// userProvider.insertUser(user, "AdminUserCreator");
		} catch (Exception e) {
			log.info("Could not initialize Spring context: " + e);
		}
		
		BasicDataSource dataSource = (BasicDataSource) context.getBean("hibernateDataSource");
		log.info("DB url: " + dataSource.getUrl());

		bookProvider = (BookProvider) context.getBean("bookProvider",
				BookProvider.class);
		schedulerProvider = (SchedulerProvider) context.getBean(
				"schedulerProvider", SchedulerProvider.class);
		
		/*ReportLogProvider logProvider = (ReportLogProvider) context.getBean("reportLogProvider");
		ReportLog reportLog = null;
		try {
			reportLog = logProvider.getReportLog(241692);
		} catch (ProviderException e) {
			log.error("Could not get specific report log");
			System.exit(0);
		}
		log.info("Got report log 241692; message: " + reportLog.getMessage());
		List<ReportDeliveryLog> deliveryLogs = reportLog.getDeliveryLogs();
		
		ReportDeliveryLog deliveryLog = new ReportDeliveryLog();
		deliveryLog.setMessage("This is a test");
		deliveryLogs.add(deliveryLog);
		reportLog.setDeliveryLogs(deliveryLogs);
		try {
			logProvider.updateReportLog(reportLog);
		} catch (ProviderException e) {
			log.error("Could not update specific report log");
		}*/
	}
	
	void printMemoryStatus() {
		log.info("Heap: " + Runtime.getRuntime().freeMemory());
	}

	public static void main(String[] args) {
		ScheduleMigrator migrator = new ScheduleMigrator(args);

		/*
		 * ReportUser user = new ReportUser(); user.setName(args[0]);
		 * user.setPassword(args[1]); user.setEmail(args[2]);
		 * user.setRootAdmin(true);
		 */
		migrator.loadBooks();

		if (migrator.migrateRequested()) {
			migrator.migrateBooks();
		}
		
		System.exit(0);
	}
}
