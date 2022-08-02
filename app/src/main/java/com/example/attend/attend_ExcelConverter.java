package com.example.attend;

import android.content.Context;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class attend_ExcelConverter {

    ArrayList<String> studentNameList;
    ArrayList<String> studentIdList;
    HashMap<Integer, Integer> selectedRadioButtonInfo;
    Context context;

    attend_ExcelConverter(ArrayList<String> studentId, ArrayList<String> studentName, HashMap<Integer, Integer> selectedRadioButton, Context con) {
        studentIdList = studentId;
        studentNameList = studentName;
        selectedRadioButtonInfo = selectedRadioButton;
        context = con;
    }

    public HSSFWorkbook convertToExcel(String sheetNameSubject, String sheetNameDate, String sheetNameMonth, String sheetNameYear) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetNameSubject + "@" + sheetNameDate + "-" + sheetNameMonth + "-" + sheetNameYear);
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("sr_no");
        titleRow.createCell(1).setCellValue("id");
        titleRow.createCell(2).setCellValue("name");
        titleRow.createCell(3).setCellValue("attendance");

        for (int i = 1; i <= studentNameList.size(); i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue(studentIdList.get(i - 1));
            row.createCell(2).setCellValue(studentNameList.get(i - 1));
            if (selectedRadioButtonInfo.get(i - 1) == R.id.presentRadioButton)
                row.createCell(3).setCellValue("present");
            else if (selectedRadioButtonInfo.get(i - 1) == R.id.absentRadioButton)
                row.createCell(3).setCellValue("absent");
            else if (selectedRadioButtonInfo.get(i - 1) == R.id.leaveRadioButton)
                row.createCell(3).setCellValue("leave");
        }
        try {
            //FileOutputStream fos = new FileOutputStream(file);
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            workbook.write(fos);
            return workbook;
        } catch (IOException e) {
            return null;
        }
    }
}
