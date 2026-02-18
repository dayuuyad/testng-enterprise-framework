// src/main/java/com/company/ecommerce/utils/ExcelReader.java
package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {
    
    private static final String TEST_DATA_PATH = ConfigManager.getInstance().getTestDataPath();

    /**
     * 获取Excel文件的所有sheet名称
     * @param fileName Excel文件路径
     * @return sheet名称列表
     */
    public static Iterator<String> getSheetNames(String fileName) {
        String filePath = System.getProperty("user.dir") + TEST_DATA_PATH + fileName;

        List<String> sheetNames = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sheetNames.iterator();
    }

    public static Iterator<Map<String, String>> getUserDataAsMap(String fileName, String sheetName) {
        String filePath = System.getProperty("user.dir") + TEST_DATA_PATH + fileName;

        List<Map<String, String>> testData = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(filePath))) {
            Sheet sheet = workbook.getSheet(sheetName);

            // 读取表头
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // 读取数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> dataMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    if (row.getCell(j) == null) continue;
                    dataMap.put(headers.get(j), row.getCell(j).getStringCellValue());
                }

                testData.add(dataMap);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return testData.iterator();
    }

    public static Iterator<Object[]> readTestData(String fileName, String sheetName) throws IOException {
        List<Object[]> data = new ArrayList<>();

        String filePath = System.getProperty("user.dir") + TEST_DATA_PATH + fileName;

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in file " + fileName);
            }

            Iterator<Row> rowIterator = sheet.iterator();
            // 跳过表头
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                List<Object> rowData = new ArrayList<>();

                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowData.add(cell.getDateCellValue());
                            } else {
                                rowData.add(cell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            rowData.add(cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            rowData.add(cell.getCellFormula());
                            break;
                        default:
                            rowData.add("");
                    }
                }
                data.add(rowData.toArray());
            }
        }

        return data.iterator();
    }
}