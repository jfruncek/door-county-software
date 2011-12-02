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
package org.efs.openreports;

import java.util.HashMap;
import java.util.Map;



public class ReportConstants 
{
	/**
	 * Report delivery methods. Name is used to map a delivery method to a Spring bean identifier using
	 * the following format: name + "DeliveryMethod"
	 */
	public static enum DeliveryMethod {EMAIL("email"), FILE("fileSystem"), LAN("lan"), FAX("fax"), FTP("ftp"), PRINTER("printer"), DOCUMENT_REPOSITORY("documentRepository");
	
		private final String name;

		DeliveryMethod(String name) 
		{
			this.name = name;
		}

		public String getName() 
		{
			return name;
		}
        
        public static DeliveryMethod findByName(String name) {
            for (DeliveryMethod value : DeliveryMethod.values()) {
                if (value.getName().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return null;
        }
	}
	
	/**
	 * Report export types. int code is used to support legacy export types persisted or serialized
	 * as integers in ReportSchedule and ReportLog objects
	 */
	public static enum ExportType {
        PDF( 0 ), XLS( 1 ), HTML( 2 ), CSV( 3 ), IMAGE( 4 ), RTF( 5 ), TEXT( 6 ), EXCEL( 7 ), HTML_EMBEDDED( 8 ), XLSX(
                9 );

        private final int code;

        static Map<String, ExportType> nameToValue = new HashMap<String, ExportType>();
        static Map<Integer, ExportType> codeToValue = new HashMap<Integer, ExportType>();

        static {
            for( ExportType scheduleType : ExportType.values() ) {
                nameToValue.put( scheduleType.name().toLowerCase(), scheduleType );
                codeToValue.put( scheduleType.getCode(), scheduleType );
            }
        }

        ExportType( int code ) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ExportType findByCode( int code ) {
            return codeToValue.get( code );
        }
        
        public static ExportType findByNameIgnoreCase( String name) {
            return nameToValue.get( name == null ? "" : name.toLowerCase() );
        }
    }

	/**
	 * Report schedule types. int code is used to support legacy schedule types serialized as
	 * integers in ReportSchedule objects.
	 */
	public static enum ScheduleType {
        NONE( 0, "None" ), DAILY( 1, "Daily" ), WEEKLY( 2, "Weekly" ), MONTHLY( 3, "Monthly" ), WEEKDAYS( 4, "Weekdays" ), HOURLY(
                5, "Hourly" ), CRON( 6, "Cron" ), ONCE( 7, "Once" );

        private final int code;
        private final String name;
        static Map<String, ScheduleType> nameToValue = new HashMap<String, ScheduleType>();
        static Map<Integer, ScheduleType> codeToValue = new HashMap<Integer, ScheduleType>();

        static {
            for( ScheduleType scheduleType : ScheduleType.values() ) {
                nameToValue.put( scheduleType.getName().toLowerCase(), scheduleType );
                codeToValue.put( scheduleType.getCode(), scheduleType );
            }
        }

        ScheduleType( int code, String name ) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static ScheduleType findByCode( int code ) {
            return codeToValue.get( code );
        }

        /** finds by name, case-insensitive */
        public static ScheduleType findByNameIgnoreCase( String name ) {
            return nameToValue.get( name == null ? "" : name.toLowerCase() );
        }

    }
	
	/**
	 * AM/PM indicator for scheduled request. The code corresponds to the AM and PM constants 
	 * in the java.util.Calendar class. 
	 */
	public static enum ScheduleAmPm {AM(0), PM(1);
		
		private final int code;

		ScheduleAmPm(int code) 
		{
			this.code = code;
		}

		public int getCode() 
		{
			return code;
		}
		
		public static ScheduleAmPm findByCode(int code) 
		{
			for (ScheduleAmPm scheduleAmPm : ScheduleAmPm.values()) 
			{
				if (scheduleAmPm.getCode() == code) return scheduleAmPm;
			}
			
			return null;
		}		
	}
	
	public static enum Status {SUCCESS, FAILURE, INVALID};	
}
