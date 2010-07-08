package org.efs.openreports.util.scripting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.script.ScriptEngine;

public class BookGroovyContext extends GroovyContext {

	public static final String MNT_RISK = "/mnt/risk";
	public static final String MNT_PUBLIC = "/mnt/public";
	public static final String MNT_ACCOUNTING = "/mnt/accounting";
	public static final String R_RISK = "R:\\Risk";
    public static final String R_PUBLIC_SPACE = "R:\\Public Space";
	public static final String T_DRIVE = "T:";
	
	public static final String AFT = "Stark Asia Fund ";
	public static final String DMAS = "Deephaven Global Multi-Strategy Master Fund LP ";
	public static final String IKOF = "Shepherd Select Asset Holding Ltd. ";
	public static final String IKON = "Stark Select Asset Fund LLC ";
	public static final String SA = "Shepherd Investments International, Ltd. ";
	public static final String SCMA = "Stark Criterion Fund ";
	public static final String SILP = "Stark Investments Limited Partnership ";
	public static final String SQPF = "Stark QIC Specialty Ltd. ";
	public static final String SVMA = "Stark Global Opportunities Fund ";
	public static final String MAST = "Stark Master Fund ";
	
	public static final String HISTORICAL_GEOGRAPHIC_DISTRIBUTION = "Historical Geographic Distribution";
	public static final String STRATEGY_DISTRIBUTION = "Strategy Distribution";
	public static final String PROFIT_ATTRIBUTION_BY_STRATEGY = "Profit Attribution by Strategy";
	public static final String PROFIT_ATTRIBUTION = "Investor Profit Attribution Report";
	public static final String EXPOSURE = "Exposure Report";
	public static final String SCENARIO = "Investor Scenario Report";
	public static final String MASTER = "Scenario Report";
	public static final String EVENT_RISK_ARBITRAGE_DISTRIBUTION = "Event Risk Arbitrage Distribution";
	public static final String INVESTOR = "Investor Report";
	public static final String EQUITY_DERIVATIVE_SCENARIO = "Equity Derivative Convertible Scenario Report";
	
	public static final String RIGOR_REPORT_DATE = "RigorReportDate";
	
	private Map<String, Object> parameters;
	private String today;

    public BookGroovyContext( Map<String, Object> parameters ) {
        this.parameters = parameters;
    }
    
    public BookGroovyContext() {
    	today = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
    }

    @Override
    protected void initEngine( ScriptEngine engine ) {
        super.initEngine( engine );
        
        // Add today's date (default for report run date)
        engine.put(RIGOR_REPORT_DATE, today);
        
        // Add output path aliases
        engine.put("risk", MNT_RISK);
        engine.put("public", MNT_PUBLIC);
        engine.put("accounting", MNT_ACCOUNTING);
        
        // Add fund aliases
        engine.put("AFT", AFT);
        engine.put("DMAS", DMAS);
        engine.put("IKOF", IKOF);
        engine.put("IKON", IKON);
        engine.put("SA", SA);
        engine.put("SCMA", SCMA);
        engine.put("SILP", SILP);
        engine.put("SQPF", SQPF);
        engine.put("SVMA", SVMA);
        engine.put("MAST", MAST);
        
        // Add report aliases
        engine.put("HGD", HISTORICAL_GEOGRAPHIC_DISTRIBUTION);
        engine.put("SD", STRATEGY_DISTRIBUTION);
        engine.put("PAS", PROFIT_ATTRIBUTION_BY_STRATEGY);
        engine.put("PA", PROFIT_ATTRIBUTION);
        engine.put("EXP", EXPOSURE);
        engine.put("SCE", SCENARIO);
        engine.put("MAS", MASTER);
        engine.put("ERA", EVENT_RISK_ARBITRAGE_DISTRIBUTION);
        engine.put("INV", INVESTOR);
        engine.put("EDS", EQUITY_DERIVATIVE_SCENARIO);
        
        
        if ( parameters == null ) return;

        // Add variables for all chapter parameters
        for (String parameter : parameters.keySet()) {
            String value = (String) parameters.get(parameter);
    		engine.put( parameter, value );
		}
    }
    
}
