package com.ichtj.basetools.util;

import android.os.Environment;

import com.face_chtj.base_iotutils.KLog;
import com.ichtj.basetools.entity.ExcelEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Create on 2020/6/28
 * author chtj
 * desc jxl.jar Excel xls xlsx文件操作
 */
public class JXLExcelUtils {
    private static final String TAG = "JXLExcelUtils";

    public static WritableFont arial14font = null;

    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";


    /**
     * 导出excel
     */
    public static void exportExcel() {
        //标题栏
        String[] headerTitle = { "ID","Name","Sex","Age"};
        //创建指定目录
        File file = new File(Environment.getExternalStorageDirectory() + "/Record");
        JXLExcelUtils.makeDir(file);
        //初始化
        JXLExcelUtils.initExcel(file.toString() + "/table.xls", headerTitle);
        String fileName = Environment.getExternalStorageDirectory() + "/Record/table.xls";
        JXLExcelUtils.writeObjListToExcel(JXLExcelUtils.getRecordData(), fileName);
    }



    /**
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     * 模拟一些数据
     * @return
     */
    public static ArrayList<ArrayList<String>> getRecordData() {
        ArrayList<ArrayList<String>> recordList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ArrayList<String> beanList = new ArrayList<String>();
            beanList.add("student.id" + i);
            beanList.add("student.name" + i);
            beanList.add("student.sex" + i);
            beanList.add("student.age" + i);
            recordList.add(beanList);
        }
        return recordList;
    }

    /**
     * 创建目录
     * @param dir
     */
    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }


    /***
     * 确保读取的文件类型为xls xlsx还未支持
     * @return 多行数据列表
     */
    public static List<ExcelEntity> readExcelxlsx(String path) {
        List<ExcelEntity> excelEntities = new ArrayList<>();
        File file = new File(path);
        try {
            Workbook book = Workbook.getWorkbook(file);
            // 获取表页数
            final int bookPage = book.getNumberOfSheets();
            KLog.e("bookPage", "bookPage = " + bookPage);
            // 获得第一个工作表对象
            Sheet sheet = book.getSheet(0);
            int Rows = sheet.getRows();
            for (int i = 1; i < Rows; ++i) {
                String assetNumber = (sheet.getCell(0, i)).getContents();
                KLog.d(TAG, "assetNumber: " + assetNumber);
                String assetName = (sheet.getCell(1, i)).getContents();
                KLog.d(TAG, "assetName: " + assetName);
                String assetClassification = (sheet.getCell(2, i)).getContents();
                KLog.d(TAG, "assetClassification: " + assetClassification);
                String nationalStandardClassification = (sheet.getCell(3, i)).getContents();
                KLog.d(TAG, "nationalStandardClassification: " + nationalStandardClassification);
                ExcelEntity excelEntity = new ExcelEntity(assetNumber, assetName, assetClassification, nationalStandardClassification);
                excelEntities.add(excelEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
            KLog.d(TAG, "errMeg1: " + e.getMessage());
        } catch (BiffException e) {
            e.printStackTrace();
            KLog.d(TAG, "errMeg2: " + e.getMessage());
        }
        return excelEntities;
    }


    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12font = new WritableFont(WritableFont.ARIAL, 10);
            arial12format = new WritableCellFormat(arial12font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);//对齐格式
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN); //设置边框

        } catch (WriteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化Excel
     *
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("成绩表", 0);
            //创建标题栏
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            sheet.setRowView(0, 340); //设置行高

            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList, String fileName) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = writebook.getSheet(0);

//              sheet.mergeCells(0,1,0,objList.size()); //合并单元格
//              sheet.mergeCells()

                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i), arial12format));
                        if (list.get(i).length() <= 5) {
                            sheet.setColumnView(i, list.get(i).length() + 8); //设置列宽
                        } else {
                            sheet.setColumnView(i, list.get(i).length() + 5); //设置列宽
                        }
                    }
                    sheet.setRowView(j + 1, 350); //设置行高
                }

                writebook.write();
                KLog.d(TAG, "successful: 导出到手机存储中文件夹Record成功");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
