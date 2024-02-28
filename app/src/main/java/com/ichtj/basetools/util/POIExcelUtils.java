package com.ichtj.basetools.util;

import com.face_chtj.base_iotutils.KLog;
import com.ichtj.basetools.entity.ExcelEntity;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create on 2020/6/28
 * author chtj
 * desc xls xlsx文件读取
 */
public class POIExcelUtils {

    private static final String TAG="POIExcelUtils";
    /**
     * 读取excel   （xls和xlsx）
     *
     * @return
     */
    public static List<ExcelEntity> readExcel(String path) {
        List<ExcelEntity> entityList = new ArrayList<>();
        File file = new File(path);
        String filePath = file.getAbsolutePath();
        Sheet sheet = null;
        Row row = null;
        Row rowHeader = null;
        List<Map<String, String>> list = null;
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            }
            if (wb != null) {
                // 用来存放表中数据
                //list = new ArrayList<Map<String, String>>();
                // 获取第一个sheet
                sheet = wb.getSheetAt(0);
                KLog.d(TAG, "sheet[0] name: "+sheet.getSheetName());
                // 获取最大行数
                int rownum = sheet.getPhysicalNumberOfRows();
                // 获取第一行
                rowHeader = sheet.getRow(0);
                row = sheet.getRow(0);
                // 获取最大列数
                int colnum = row.getPhysicalNumberOfCells();
                for (int i = 1; i < rownum; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        //if(columns[j].equals(getCellFormatValue(rowHeader.getCell(j)))){
                        String cellData1 = (String) getCellFormatValue(row
                                .getCell(0));
                        KLog.d(TAG, "readExcel: cellData1="+cellData1);
                        String cellData2 = (String) getCellFormatValue(row
                                .getCell(1));
                        KLog.d(TAG, "readExcel: cellData2="+cellData2);
                        String cellData3 = (String) getCellFormatValue(row
                                .getCell(2));
                        KLog.d(TAG, "readExcel: cellData3="+cellData3);
                        String cellData4 = (String) getCellFormatValue(row
                                .getCell(3));
                        KLog.d(TAG, "readExcel: cellData4="+cellData4);
                        ExcelEntity excelEntity = new ExcelEntity(cellData1, cellData2, cellData3, cellData4);
                        entityList.add(excelEntity);
                    } else {
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            KLog.d(TAG, "errMeg1: "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            KLog.d(TAG, "errMeg2: "+e.getMessage());
        }
        return entityList;
    }

    /**
     * 获取单个单元格数据
     *
     * @param cell
     * @return
     * @author lizixiang ,2018-05-08
     */
    public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            // 判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        // 数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }


    private static String excelPath = "/sdcard/data.xlsx";

    public static boolean createExcelFile() {
        boolean isCreateSuccess = false;
        Workbook workbook = null;
        try {
            //判断该文件是否存在
            File file=new File(excelPath);
            if(!file.exists()){
                file.createNewFile();
            }
            /***
             * 请注意此处的方法 按需选择 XSSFWork()或者HSSFWorkbook()
             */
            // XSSFWork used for .xslx (>= 2007), HSSWorkbook for 03 .xsl
            workbook = new HSSFWorkbook();//XSSFWork();//HSSFWorkbook();//WorkbookFactory.create(inputStream);
        }catch(Exception e) {
            KLog.d(TAG," It cause Error on CREATING excel workbook");
            e.printStackTrace();
        }
        if(workbook != null) {
            Sheet sheet = workbook.createSheet("testdata");
            Row row0 = sheet.createRow(0);
            for(int i = 0; i < 11; i++) {
                Cell cell_1 = row0.createCell(i, Cell.CELL_TYPE_STRING);
                CellStyle style = getStyle(workbook);
                cell_1.setCellStyle(style);
                cell_1.setCellValue("HELLO" + i + "Column");
                //sheet.autoSizeColumn(i);
            }
            for (int rowNum = 1; rowNum < 200; rowNum++) {
                Row row = sheet.createRow(rowNum);
                for(int i = 0; i < 11; i++) {
                    Cell cell = row.createCell(i, Cell.CELL_TYPE_STRING);
                    cell.setCellValue("cell" + String.valueOf(rowNum+1) + String.valueOf(i+1));
                }
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(excelPath);
                workbook.write(outputStream);
                outputStream.flush();
                outputStream.close();
                isCreateSuccess = true;
            } catch (Exception e) {
                System.out.println("It cause Error on WRITTING excel workbook: erreg");
                e.printStackTrace();
            }
        }
        File sss = new File(excelPath);
        System.out.println(sss.getAbsolutePath());
        return isCreateSuccess;
    }
    private static CellStyle getStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 设置单元格字体
        Font headerFont = workbook.createFont(); // 字体
        headerFont.setFontHeightInPoints((short)14);
        headerFont.setColor(HSSFColor.RED.index);
        headerFont.setFontName("宋体");
        style.setFont(headerFont);
        style.setWrapText(true);

        // 设置单元格边框及颜色
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);
        style.setWrapText(true);
        return style;
    }
}
