package org.efs.openreports.providers.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.efs.openreports.providers.ExcelExportProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.DisplayProperty;

public class POIExcelExportProvider implements ExcelExportProvider {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void export(Iterator<DynaBean> data, DisplayProperty[] properties, OutputStream output, String exportId) throws ProviderException {
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyle style = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		HSSFSheet sheet = wb.createSheet("Results");
		int row = 0;
		HSSFRow ssRow = sheet.createRow(row++);
		int col = 0;
		for (DisplayProperty prop : properties) {
			HSSFCell cell = ssRow.createCell(col++);
			cell.setCellValue(prop.getName());
			cell.setCellStyle(style);
		}
		
		while(data.hasNext()) {
		    DynaBean bean = data.next();
			ssRow = sheet.createRow(row++);
			for (int i = 0; i < properties.length; i++) {
				HSSFCell cell = ssRow.createCell(i);
				Class clazz = bean.getDynaClass().getDynaProperty(properties[i].getName()).getType();
				Object value = bean.get(properties[i].getName());
				if (value != null) {
	                if (clazz.equals(BigDecimal.class)) {
	                    cell.setCellValue(((BigDecimal) value).doubleValue());
	                } else if (clazz.equals(String.class)) {
	                    cell.setCellValue(new HSSFRichTextString((String) value));
	                } else if (clazz.equals(java.sql.Timestamp.class)) {
	                    Date date = new Date(((java.sql.Timestamp) value).getTime());
	                    cell.setCellValue(date);
	                }
	                else {
	                    cell.setCellValue(new HSSFRichTextString(value.toString())); 
	                }
				}
			}
		}

		try {
			wb.write(output);
		} catch (IOException e) {
			throw new ProviderException( e );
		}
	}

}
