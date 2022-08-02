package com.example.attend;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableHeader;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class attend_PdfConverter {

    ArrayList<String> studentNameList;
    ArrayList<String> studentIdList;
    HashMap<Integer, Integer> selectedRadioButtonInfo;
    Context context;
    byte[]pdfBytes;

    attend_PdfConverter(ArrayList<String> studentId, ArrayList<String> studentName, HashMap<Integer, Integer> selectedRadioButton, Context con) {
        studentIdList = studentId;
        studentNameList = studentName;
        selectedRadioButtonInfo = selectedRadioButton;
        context = con;
    }

    public File ConvertToPdf(String course, String term, String batch, String subject, String date, String month, String year){
        Document document = new Document();

        File file = new File((context.getExternalFilesDir("temp")+"/date-month-year.pdf"));

        try{
            FileOutputStream outputStream = new FileOutputStream(file);
            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();


            document.add(new Paragraph(course + "_" + term + "_" + batch  + "_" + subject));
            document.add(new Paragraph(date + "-" + month + "-" + year));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            float[] columnWidth = {1f, 1f, 1f, 1f};
            table.setWidths(columnWidth);

            PdfPCell sr_noCellHead = new PdfPCell(new Paragraph("sr_no"));
            sr_noCellHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            sr_noCellHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
            sr_noCellHead.setPadding(3);

            PdfPCell studIdCellHead = new PdfPCell(new Paragraph("student id"));
            studIdCellHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            studIdCellHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
            studIdCellHead.setPadding(3);

            PdfPCell studNameCellHead = new PdfPCell(new Paragraph("student name"));
            studNameCellHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            studNameCellHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
            studNameCellHead.setPadding(3);

            PdfPCell attendanceCellHead = new PdfPCell(new Paragraph("attendance"));
            attendanceCellHead.setHorizontalAlignment(Element.ALIGN_CENTER);
            attendanceCellHead.setVerticalAlignment(Element.ALIGN_MIDDLE);
            attendanceCellHead.setPadding(3);

            table.addCell(sr_noCellHead);
            table.addCell(studIdCellHead);
            table.addCell(studNameCellHead);
            table.addCell(attendanceCellHead);


            for(int i=0 ; i<studentIdList.size() && i<studentNameList.size() && i<selectedRadioButtonInfo.size() ; i++){

                PdfPCell sr_noCell = new PdfPCell(new Paragraph(""+(i+1)));
                sr_noCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                sr_noCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                sr_noCell.setPadding(3);

                Log.d("Looog",""+selectedRadioButtonInfo.get(i).equals(R.id.presentRadioButton));

                PdfPCell studIdCell = new PdfPCell(new Paragraph(studentIdList.get(i)));
                studIdCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                studIdCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                studIdCell.setPadding(3);


                PdfPCell studNameCell = new PdfPCell(new Paragraph(studentNameList.get(i)));
                studNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                studNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                studNameCell.setPadding(3);

                PdfPCell attendanceCell;
                switch (selectedRadioButtonInfo.get(i)){
                    case R.id.presentRadioButton:
                        attendanceCell = new PdfPCell(new Paragraph("present"));
                        break;
                    case R.id.absentRadioButton:
                        attendanceCell = new PdfPCell(new Paragraph("absent"));
                        break;
                    case R.id.leaveRadioButton:
                        attendanceCell = new PdfPCell(new Paragraph("leave"));
                        break;
                    default:
                        attendanceCell = new PdfPCell(new Paragraph("undefined"));
                        break;
                }
                attendanceCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                attendanceCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                attendanceCell.setPadding(3);

                table.addCell(sr_noCell);
                table.addCell(studIdCell);
                table.addCell(studNameCell);
                table.addCell(attendanceCell);

            }
            document.add(table);
            document.close();
            writer.close();

        }catch (Exception e){
            Log.d("LOOOG", Log.getStackTraceString(e));
        }
        return file;
    }

}
