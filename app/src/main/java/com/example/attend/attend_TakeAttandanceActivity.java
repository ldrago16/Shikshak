package com.example.attend;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class attend_TakeAttandanceActivity extends AppCompatActivity implements Runnable {
    public static TextView totalPresentText, totalAbsentText, totalLeaveText;      //these values automatically updated in attend_ProgrammingAdapter class
    static ConstraintLayout yearSubTimeLayout;
    static ConstraintLayout studentPresentAbsentLeaveLayout;
    static ConstraintLayout palTotalLayout;
    static Bitmap bitmap;

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    String course, term, batch, courseDuration, subject, calendarMonth, calendarYear, calendarDate;

    RecyclerView recyclerViewForStudentAttendance;
    ArrayList<String> studentNameList = new ArrayList<>();
    ArrayList<String> studentIdList = new ArrayList<>();
    ArrayList<String> studentFatherNameList = new ArrayList<>();
    ArrayList<String> studentMobileNoList = new ArrayList<>();
    Button submit;
    String todayDate;
    TextView presentText, absentText, leaveText;
    attend_ProgrammingAdapter programmingAdapter;                  //holds and set information of students on recyclerView
    AlertDialog.Builder alert;
    HashMap<Integer, Integer> selectedRadioButtonInfo;      //store Which Radio fetchCancelButton teacher selected For particular position of student
    int notSetRadioButtonPosition;                          //if some radio fetchCancelButton is not selected it holds student position from-(nextLine)
    ProgressBar progressBar;

    public static HashMap<String, String> sortStudentData(HashMap<String, String> hm) {
        List<Map.Entry<String, String>> list = new LinkedList<>(hm.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {

                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        HashMap<String, String> temp = new LinkedHashMap<>();
        for (Map.Entry<String, String> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attend_activity_takeattandance);

        yearSubTimeLayout = findViewById(R.id.yearDateSubjectInfo);
        studentPresentAbsentLeaveLayout = findViewById(R.id.studentPresentAbsentInfo);
        palTotalLayout = findViewById(R.id.submitButtonContainer);

        presentText = findViewById(R.id.presentTag);
        absentText = findViewById(R.id.absentTag);
        leaveText = findViewById(R.id.leaveTag);
        submit = findViewById(R.id.submitAttandance);

        presentText.setOnClickListener(new OnPresentTextClick());
        absentText.setOnClickListener(new OnAbsentTextClick());
        leaveText.setOnClickListener(new OnLeaveTextClick());
        submit.setOnClickListener(new SubmitButtonAction());

        totalPresentText = findViewById(R.id.totalPresentTag);
        totalAbsentText = findViewById(R.id.totalAbsentTag);
        totalLeaveText = findViewById(R.id.totalLeaveTag);

        attend_TakeAttandanceActivity.totalPresentText.setText("0");
        attend_TakeAttandanceActivity.totalAbsentText.setText("0");
        attend_TakeAttandanceActivity.totalLeaveText.setText("0");

        progressBar = attend_TakeAttandanceActivity.this.findViewById(R.id.progressBar);

        //Thread recycleViewDataLoaderThread = new Thread(this);

        Intent in = getIntent();
        course = in.getStringExtra("course");
        term = in.getStringExtra("term");
        batch = in.getStringExtra("batch");
        subject = in.getStringExtra("subject");
        if (!setYearSubjectDateInfo(course, term, batch, subject)) {
            Toast.makeText(attend_TakeAttandanceActivity.this, "Time Fetching Error", Toast.LENGTH_LONG).show();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);
        blockScreen(true);
        checkSubjectAttandanceAlreadyTakenOrNot(subject);
        //attend_TakeAttandanceActivity.this.fetchStudentData();
    }

    public void checkSubjectAttandanceAlreadyTakenOrNot(final String subject) {      //finish activity if subject attendance already taken on presentDay
        firebaseFirestore.collection("ATTENDANCE").document("LastAttendanceDate")
                .get(Source.SERVER).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String lastAttaindanceDate = documentSnapshot.get(subject) != null ? documentSnapshot.get(subject).toString() : "00/00/00";
                Log.d("Looog", todayDate + " " + lastAttaindanceDate + " " + (todayDate.compareTo(lastAttaindanceDate) > 0));

                SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                try {
                    Date prevAttendandeDate = dateformat.parse(lastAttaindanceDate);
                    Date currentDate = dateformat.parse(todayDate);

                    if (prevAttendandeDate.before(currentDate)) {
                        fetchCoursesDuration();
                        attend_TakeAttandanceActivity.this.fetchStudentData();
                    } else {
                        Toast.makeText(attend_TakeAttandanceActivity.this, "Attendance Already Done", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        attend_TakeAttandanceActivity.this.finish();
                    }
                } catch (ParseException p) {
                    Toast.makeText(attend_TakeAttandanceActivity.this, "date format parse Exception", Toast.LENGTH_SHORT).show();
                    attend_TakeAttandanceActivity.this.finish();
                }
            }
        });
    }

    void fetchCoursesDuration() {
        firebaseFirestore.collection("COURSE").document(course).get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                courseDuration = task.getResult().get("duration").toString();
            }
        });

    }

    void fetchStudentData() {

        firebaseFirestore.collection("STUDENT")
                .whereEqualTo("course_name", course)
                .whereEqualTo("term", term)
                .whereEqualTo("batch", batch)
                .whereEqualTo("completion_year", "pursuing")
                .orderBy("student_name")
                .orderBy("student_father_name")
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                try {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        LinkedHashMap<String, String> tempSortedStudentId_NameMap = new LinkedHashMap<>();
                        ArrayList<String> tempStudentFatherNameList = new ArrayList<>();
                        ArrayList<String> tempStudentMobileNoList = new ArrayList<>();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.get("student_name").toString();
                            String fatherName = (documentSnapshot.get("student_father_name") == null ? "" : documentSnapshot.get("student_father_name").toString());
                            String mobileNo = (documentSnapshot.get("student_mobile") == null ? "" : documentSnapshot.get("student_mobile").toString());

                            tempStudentFatherNameList.add(fatherName);
                            tempStudentMobileNoList.add(mobileNo);

                            tempSortedStudentId_NameMap.put(id, name);
                        }
                        // tempSortedStudentId_NameMap = sortStudentData(tempSortedStudentId_NameMap);

                        ArrayList<String> studentiddata = new ArrayList<>(tempSortedStudentId_NameMap.keySet());
                        ArrayList<String> studentnamedata = new ArrayList<>(tempSortedStudentId_NameMap.values());

                        attend_TakeAttandanceActivity.this.studentIdList = studentiddata;
                        attend_TakeAttandanceActivity.this.studentNameList = studentnamedata;
                        attend_TakeAttandanceActivity.this.studentFatherNameList = tempStudentFatherNameList;
                        attend_TakeAttandanceActivity.this.studentMobileNoList = tempStudentMobileNoList;
                        attend_TakeAttandanceActivity.this.run();

                    } else {
                        Toast.makeText(attend_TakeAttandanceActivity.this, "No Student Found", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(attend_TakeAttandanceActivity.this, "Error: in fetching student in success", Toast.LENGTH_LONG).show();
                    Log.d("LOOOG", "at fetchStudentData :" + Log.getStackTraceString(e));

                }
                progressBar.setVisibility(View.GONE);
                blockScreen(false);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(attend_TakeAttandanceActivity.this, "Error: in fetching student in failure", Toast.LENGTH_LONG).show();
                Log.d("LOOOG", "at fetchStudentData :" + Log.getStackTraceString(e));
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void run() {
        recyclerViewForStudentAttendance = findViewById(R.id.recyclerViewForFillingStudent);
        recyclerViewForStudentAttendance.setLayoutManager(new LinearLayoutManager(attend_TakeAttandanceActivity.this));
        programmingAdapter = new attend_ProgrammingAdapter(studentNameList, studentFatherNameList, studentMobileNoList, studentIdList);
        recyclerViewForStudentAttendance.setAdapter(programmingAdapter);

    }


    boolean isAllRadioButtonSelected(HashMap<Integer, Integer> checkedRadioButton) {     //checks all radio fetchCancelButton is selected or not, when teacher click on submit fetchCancelButton if giveAlert is true it also give alert if one or more option is not Selected;
        int i;
        for (i = 0; i < checkedRadioButton.size(); i++) {
            try {
                if (checkedRadioButton.get(i) == 0)    //check if teacher selected all the radio fetchCancelButton or not
                {
                    notSetRadioButtonPosition = i;      //store from which postion teacher not selected radio fetchCancelButton
                    break;
                }
            } catch (NullPointerException e) {
                Toast.makeText(attend_TakeAttandanceActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                Log.d("LOOOG", "at isAllRadioButtonSelected :" + Log.getStackTraceString(e));
            }
        }
        return i == checkedRadioButton.size();      //return true if all radio fetchCancelButton selected by the user otherwise false

    }

    void setNotSelectedRadioButton(int id, HashMap<Integer, Integer> checkedRadioButton) {      //select all non selected radio buttons According To given Id//
        for (int i = notSetRadioButtonPosition; i < programmingAdapter.getItemCount(); i++) {
            try {
                if (checkedRadioButton.get(i) == 0)    //check teacher select any one radio fetchCancelButton for i th position of student or not
                {
                    //programmingAdapter.viewHolderCopy[i].radioGroup.check(R.id.absentRadioButton);  //select all non selected radio buuton to Absent mark
                    checkedRadioButton.put(i, id);
                }
            } catch (NullPointerException e) {
                Log.d("LOOOG", "at setNotSelectedRadioButton :" + Log.getStackTraceString(e));
            }
        }
        programmingAdapter.checkedRadioButtonInfo = checkedRadioButton;
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                programmingAdapter.notifyDataSetChanged();
                programmingAdapter.notifyTotalPALChange();
            }
        });
        th.run();
        try {
            th.join();
        } catch (InterruptedException e) {
            Toast.makeText(attend_TakeAttandanceActivity.this, "Thread error :" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("LOOOG", "at th.join setNotSelectedRadioButton :" + Log.getStackTraceString(e));
        }
    }

    private boolean setYearSubjectDateInfo(String course, String term, String batch, String subject) {
        TextView yearInfo = findViewById(R.id.courseTermBatchInfo);
        TextView dateInfo = findViewById(R.id.dateInfo);
        TextView subInfo = findViewById(R.id.subjectInfo);

        MyCalendar myCalendar = new MyCalendar();
        try {
            myCalendar.start();
            myCalendar.join();

            todayDate = myCalendar.getFullDate();
            calendarDate = myCalendar.getDate();
            calendarMonth = myCalendar.getMonth();
            calendarYear = myCalendar.getYear();
        } catch (Exception e) {

        }

        String course_year_batch = course + "_" + term + "_" + batch;

        if (calendarDate == null || calendarMonth == null || calendarYear == null) {
            return false;
        }

//        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
//        todayDate = df.format(Calendar.getInstance().getTime());
//        calendarDate = new SimpleDateFormat("d", Locale.ENGLISH).format(Calendar.getInstance().getTime());
//        calendarMonth = new SimpleDateFormat("M", Locale.ENGLISH).format(Calendar.getInstance().getTime());
//        calendarYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        // Toast.makeText(attend_TakeAttandanceActivity.this, todayDate , Toast.LENGTH_LONG).show();
        yearInfo.setText(course_year_batch);
        dateInfo.setText(todayDate);
        subInfo.setText(subject);
        return true;
    }

    class SubmitButtonAction implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (!isNetworkConnected()) {
                Toast.makeText(attend_TakeAttandanceActivity.this, "Check Internet Connection", Toast.LENGTH_LONG).show();
                return;
            }
            final SubmitAttaendance submitAttaendanceThread = new SubmitAttaendance();
            try {
                //selectedRadioButtonInfo = programmingAdapter.checkedRadioButtonInfo;
                //programmingAdapter.notifyDataSetChanged();
                if (isAllRadioButtonSelected(programmingAdapter.checkedRadioButtonInfo)) {
                    blockScreen(true);
                    progressBar.setVisibility(View.VISIBLE);
                    submitAttaendanceThread.start();         //*****send final results of attandance if all is fine********//
                } else {
                    try {
                        AlertDialog.Builder alert = new AlertDialog.Builder(attend_TakeAttandanceActivity.this).setMessage(R.string.dialog_msg).setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                attend_TakeAttandanceActivity.this.setNotSelectedRadioButton(R.id.absentRadioButton, programmingAdapter.checkedRadioButtonInfo);   //set All Not Selected Radio Button To Absent;
                                dialogInterface.cancel();
                                blockScreen(true);
                                progressBar.setVisibility(View.VISIBLE);
                                submitAttaendanceThread.start();              //*********send final results after all Adjustments***********//
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        AlertDialog a = alert.create();
                        a.setTitle("Alert");
                        a.show();                       //give alert when user not select any radio fetchCancelButton
                    } catch (NullPointerException e) {
                        Log.d("LOOOG", "at SubmitButtonAction In AlertDialog part onClick :" + Log.getStackTraceString(e));
                    }
                }
            } catch (NullPointerException e) {
                Toast.makeText(attend_TakeAttandanceActivity.this, "No Subject Found " + e, Toast.LENGTH_LONG).show();
                Log.d("LOOOG", "at SubmitButtonAction In submitAttendanceThread :" + Log.getStackTraceString(e));
                finish();
            }
        }

        private boolean isNetworkConnected() {

            boolean connected = false;
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED | connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
            return connected;
//            //ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            final String command = "ping -c 1 google.com";
//            try {
//                return Runtime.getRuntime().exec(command).waitFor() == 0;
//            } catch (InterruptedException e) {
//                return false;
//            } catch (IOException e) {
//                return false;
//            }
        }
    }

    class OnPresentTextClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                //selectedRadioButtonInfo = programmingAdapter.checkedRadioButtonInfo;
                boolean isAllSelected = isAllRadioButtonSelected(programmingAdapter.checkedRadioButtonInfo);
                if (!isAllSelected)
                    setNotSelectedRadioButton(R.id.presentRadioButton, programmingAdapter.checkedRadioButtonInfo);
            } catch (NullPointerException ignored) {
            }
        }
    }

    class OnAbsentTextClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                //selectedRadioButtonInfo = programmingAdapter.checkedRadioButtonInfo;
                boolean isAllSelected = isAllRadioButtonSelected(programmingAdapter.checkedRadioButtonInfo);
                if (!isAllSelected)
                    setNotSelectedRadioButton(R.id.absentRadioButton, programmingAdapter.checkedRadioButtonInfo);
            } catch (NullPointerException e) {
            }
        }
    }

    class OnLeaveTextClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            try {
                //selectedRadioButtonInfo = programmingAdapter.checkedRadioButtonInfo;
                boolean isAllSelected = isAllRadioButtonSelected(programmingAdapter.checkedRadioButtonInfo);
                if (!isAllSelected)
                    setNotSelectedRadioButton(R.id.leaveRadioButton, programmingAdapter.checkedRadioButtonInfo);
            } catch (NullPointerException e) {
            }
        }
    }

    class SubmitAttaendance extends Thread {
        //ArrayList<String> pal = new ArrayList<>();
        @Override
        public void run() {
            submit.setClickable(false);
            sendFinalResults(programmingAdapter.checkedRadioButtonInfo);
        }

        void convertAttendanceToPdfAndXmlThenUpload() {
            Runnable run = new Runnable() {
                @Override
                public void run() {

//                    PrintAttand printAttand = new PrintAttand();
//                    bitmap = printAttand.getScreenshotOfAttand(recyclerViewForStudentAttendance);
//                    ByteArrayOutputStream bitmapArrayOutputStream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapArrayOutputStream);
                    attend_PdfConverter attendPdfConverter = new attend_PdfConverter(studentIdList, studentNameList, programmingAdapter.checkedRadioButtonInfo, attend_TakeAttandanceActivity.this);
                    final File file = attendPdfConverter.ConvertToPdf(course, term, batch, subject, calendarDate, calendarMonth, calendarYear);

                    //byte[] attendImageDataInByte = bitmapArrayOutputStream.toByteArray();
                    String pdfFileName = course + "_" + term + "_" + batch + "_" + subject + ".pdf";
                    String pathForAttendPdf = "ATTENDANCE/" + calendarYear + "/" + calendarMonth + "/" + calendarDate + "/pdf/" + pdfFileName;
                    StorageReference firebaseStoragePath = firebaseStorage.getReference(pathForAttendPdf);
                    // final UploadTask uploadImageTask = firebaseStoragePath.putBytes(attendImageDataInByte);
                    final UploadTask uploadPdfTask = firebaseStoragePath.putFile(Uri.fromFile(file));

                    uploadPdfTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            file.delete(); //deletes the created pdf file after pdf is uploaded
                            attend_ExcelConverter converter = new attend_ExcelConverter(studentIdList, studentNameList, programmingAdapter.checkedRadioButtonInfo, attend_TakeAttandanceActivity.this);
                            HSSFWorkbook workbook = converter.convertToExcel(subject, calendarDate, calendarMonth, calendarYear);
                            ByteArrayOutputStream xlsArrayOutputStream = new ByteArrayOutputStream();
                            try {
                                workbook.write(xlsArrayOutputStream);
                            } catch (IOException e) {
                            }
                            byte[] attendExcelDataInByte = xlsArrayOutputStream.toByteArray();
                            String xlsFileName = course + "_" + term + "_" + batch + "_" + subject + ".xls";
                            String pathForXlsFile = "ATTENDANCE/" + calendarYear + "/" + calendarMonth + "/" + calendarDate + "/excel/" + xlsFileName;
                            StorageReference firebaseStoragePathForxls = firebaseStorage.getReference(pathForXlsFile);
                            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("application/excel").build();
                            UploadTask uploadxlsTask = firebaseStoragePathForxls.putBytes(attendExcelDataInByte, metadata);
                            uploadxlsTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(attend_TakeAttandanceActivity.this, "Attendance Submitted", Toast.LENGTH_SHORT).show();
                                    blockScreen(false);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    attend_TakeAttandanceActivity.this.finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(attend_TakeAttandanceActivity.this, "Excel Submission Failed", Toast.LENGTH_LONG).show();
                                    blockScreen(false);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(attend_TakeAttandanceActivity.this, "Pdf Submission Failed", Toast.LENGTH_LONG).show();
                            blockScreen(false);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            };
            runOnUiThread(run);
        }

        void makeToast(final String str) {
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(attend_TakeAttandanceActivity.this, str, Toast.LENGTH_SHORT).show();
                }
            });
        }

        void sendFinalResults(HashMap<Integer, Integer> checkedRadioButton) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("course_name", course);
            hashMap.put("term", term);
            hashMap.put("batch", batch);
            hashMap.put("completion_year", "pursuing");

            {
                HashMap<String, Object> setLastAttendanceDateInSubject = new HashMap<>();
                setLastAttendanceDateInSubject.put(subject, todayDate);
                firebaseFirestore.collection("ATTENDANCE").document("LastAttendanceDate").set(setLastAttendanceDateInSubject, SetOptions.merge());
                uploadCourseSubjectData();
            }

            for (int i = 0; i < studentIdList.size() && i < checkedRadioButton.size(); i++) {
                String studentId = studentIdList.get(i);
                hashMap.put("student_name", studentNameList.get(i));
                hashMap.put("student_father_name", studentFatherNameList.get(i));
                hashMap.put("student_mobile", studentMobileNoList.get(i));
                DocumentReference studentRef = firebaseFirestore.collection("ATTENDANCE").document(studentId);
                studentRef.set(hashMap, SetOptions.merge());
                try {
                    switch (checkedRadioButton.get(i)) {
                        case R.id.presentRadioButton:
                            studentRef.update(term + "_" + subject + "_" + calendarMonth + "_" + calendarYear + "_present", FieldValue.increment(1));
                            studentRef.update("attend_rank_decider", FieldValue.increment(1));
                        {       //daily attend set block
                            HashMap<String, String> setDailyAttendance = new HashMap<>();
                            setDailyAttendance.put(calendarYear + "/" + calendarMonth + "/" + calendarDate, "present");
                            studentRef.collection(subject).document(calendarMonth + "_" + calendarYear).set(setDailyAttendance, SetOptions.merge());
                        }
                        break;
                        case R.id.absentRadioButton:
                            studentRef.update(term + "_" + subject + "_" + calendarMonth + "_" + calendarYear + "_absent", FieldValue.increment(1));
                            studentRef.update("attend_rank_decider", FieldValue.increment(-1));
                        {        //daily attend set block
                            HashMap<String, String> setDailyAttendance = new HashMap<>();
                            setDailyAttendance.put(calendarYear + "/" + calendarMonth + "/" + calendarDate, "absent");
                            studentRef.collection(subject).document(calendarMonth + "_" + calendarYear).set(setDailyAttendance, SetOptions.merge());
                        }
                        break;
                        case R.id.leaveRadioButton:
                            studentRef.update(term + "_" + subject + "_" + calendarMonth + "_" + calendarYear + "_leave", FieldValue.increment(1));
                        {        //daily attend set block
                            HashMap<String, String> setDailyAttendance = new HashMap<>();
                            setDailyAttendance.put(calendarYear + "/" + calendarMonth + "/" + calendarDate, "leave");
                            studentRef.collection(subject).document(calendarMonth + "_" + calendarYear).set(setDailyAttendance, SetOptions.merge());
                        }
                        break;
                        default:
                            Toast.makeText(attend_TakeAttandanceActivity.this, "SomethingWrong...", Toast.LENGTH_SHORT).show();
                            break;
                    }

                    // studentRef.update((hashMap));
                } catch (NullPointerException e) {
                    Log.d("LOOOG", "at sendFinalResult :" + Log.getStackTraceString(e));
                }
            }
            convertAttendanceToPdfAndXmlThenUpload();
        }
    }

    void uploadCourseSubjectData() {
        DocumentReference courseSubjectDocRef = firebaseFirestore.collection("ATTENDANCE")
                .document("CourseSubjectData");

        /*here we upload course and subject data in ATTENDANCE collection so that any subject attendance can be viewed by admin even if any course or subject
        /*is deleted in future.
        */

        //course Data upload task
        {
            HashMap<String, String> courseData = new HashMap<>();
            courseData.put("course_name", course);
            courseData.put("duration", courseDuration);
            courseSubjectDocRef.collection("COURSE")
                    .document(course)
                    .set(courseData);
        }

        //subject Data upload task
        {
            HashMap<String, String> subjectData = new HashMap<>();
            subjectData.put("course_name", course);
            subjectData.put("subject_name", subject);
            subjectData.put("term", term);
            courseSubjectDocRef.collection("SUBJECT")
                    .document(subject)
                    .set(subjectData);
        }

    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);

        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
        }
    }
}

class attend_ProgrammingAdapter extends RecyclerView.Adapter<attend_ProgrammingAdapter.ProgrammingViewHolder> {

    HashMap<Integer, Integer> checkedRadioButtonInfo; //store the information of which radio fetchCancelButton Teacher Selected for particular Student
    HashMap<String, String> attandanceInfo = new HashMap<>();
    //ProgrammingViewHolder[] viewHolderCopy;                              //store copy of each viewHolder for each Position
    Thread countTotalPAL;
    TotalRadioCount totalRadioCount = new TotalRadioCount();
    private ArrayList<String> studentNameList, studentFatherNameList, studentMobileNoList, studentIdList;   //store students names
    android.os.Handler handler = new Handler(Looper.myLooper());

    public attend_ProgrammingAdapter(ArrayList<String> tempStudentNameList, ArrayList<String> tempStudentFatherNameList, ArrayList<String> tempStudentMobileNoList, ArrayList<String> tempStudentIdList) {
        this.studentNameList = tempStudentNameList;
        this.studentFatherNameList = tempStudentFatherNameList;
        this.studentMobileNoList = tempStudentMobileNoList;
        this.studentIdList = tempStudentIdList;
        //viewHolderCopy = new ProgrammingViewHolder[e.size()];
        setcurrentRadioList();  //initialize all student initial attandance as not selected or 0(zero)
    }

    void setcurrentRadioList() {
        checkedRadioButtonInfo = new HashMap<>();
        for (int i = 0; i < studentNameList.size(); i++) {
            checkedRadioButtonInfo.put(i, 0);
        }
    }

    public String wordCaseConvert(String tempName) {
        String[] nameArray = tempName.split(" ");
        String realNameWordCaseConverted = "";
        for (int i = 0; i < nameArray.length; i++) {
            nameArray[i] = (nameArray[i].substring(0, 1).toUpperCase()) + nameArray[i].substring(1);
            realNameWordCaseConverted = realNameWordCaseConverted + nameArray[i] + " ";
        }

        return realNameWordCaseConverted.trim();
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.attend_student_take_attend_adapter_layout, parent, false);
        return new ProgrammingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProgrammingViewHolder holder, final int position) {
        final String stud_Name = studentNameList.get(position);
        final String stud_Father = studentFatherNameList.get(position);
        final String stud_Id = studentIdList.get(position);

        holder.studentName.setText(wordCaseConvert(stud_Name));
        holder.srNo.setText("" + (position + 1));
        try {
            if (checkedRadioButtonInfo.get(position) != 0) {
                holder.radioGroup.check(checkedRadioButtonInfo.get(position));
            } else {
                holder.radioGroup.clearCheck();
            }
        } catch (NullPointerException e) {
        }

        holder.studentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(holder.studentName.getContext());
                android.app.AlertDialog alert = alertDialogBuilder.create();
                alert.setCancelable(true);
                alert.setIcon(android.R.drawable.ic_menu_info_details);
                alert.setTitle("Student Information.");
                alert.setMessage("Student Id = " + stud_Id + "\nStudent name = " + stud_Name + "\nFather name = " + stud_Father + "\nMobile no = " + studentMobileNoList.get(position));
                alert.show();
            }
        });

        holder.presentRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedRadioButtonInfo.put(position, R.id.presentRadioButton);
                //new Thread(totalRadioCount).start();
                handler.postDelayed(new TotalRadioCount(), 300);
            }
        });

        holder.absentRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedRadioButtonInfo.put(position, R.id.absentRadioButton);
                //new Thread(totalRadioCount).start();
                handler.postDelayed(new TotalRadioCount(), 300);
            }
        });

        holder.leaveRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedRadioButtonInfo.put(position, R.id.leaveRadioButton);
                //new Thread(totalRadioCount).start();
                handler.postDelayed(new TotalRadioCount(), 300);
            }
        });
//        holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {     //if user select a radio fetchCancelButton it will store info of which fetchCancelButton (present, absent, leave) is selected by user
//            @Override
//            synchronized public void onCheckedChanged(RadioGroup radioGroup, int i) {
//
//                /*set checkedRadioButtonInfo when user click on radio fetchCancelButton */
//                if (i == R.id.presentRadioButton) {
//                    checkedRadioButtonInfo.put(position, R.id.presentRadioButton);
//                    // Log.d("LOOG", "P "+position);
//                } else if (i == R.id.absentRadioButton) {
//                    checkedRadioButtonInfo.put(position, R.id.absentRadioButton);
//                    //  Log.d("LOOG", "A "+position);
//                } else if (i == R.id.leaveRadioButton) {
//                    checkedRadioButtonInfo.put(position, R.id.leaveRadioButton);
//                    //  Log.d("LOOG", "L "+position);
//                }
//
//                countTotalPAL = new Thread(totalRadioCount);
//                countTotalPAL.start();
//            }
//        });
        // viewHolderCopy[position] = holder;
    }

    @Override
    public int getItemCount() {
        return studentNameList.size();
    }           //return size of studentNameList

    public void notifyTotalPALChange() {
        countTotalPAL = new Thread(totalRadioCount);
        handler.postDelayed(new TotalRadioCount(), 0);
        //countTotalPAL.start();
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder {

        TextView studentName, studentFatherName;
        RadioGroup radioGroup;
        RadioButton presentRadioButton, absentRadioButton, leaveRadioButton; //==//
        TextView srNo;

        public ProgrammingViewHolder(@NonNull final View itemView) {
            super(itemView);
            // setIsRecyclable(false);
            studentName = itemView.findViewById(R.id.studentName);
            studentFatherName = itemView.findViewById(R.id.studentFatherName);
            radioGroup = itemView.findViewById(R.id.presentAbsentLeaveRadioGroup);
            presentRadioButton = itemView.findViewById(R.id.presentRadioButton); //==//
            absentRadioButton = itemView.findViewById(R.id.absentRadioButton);
            leaveRadioButton = itemView.findViewById(R.id.leaveRadioButton);
            srNo = itemView.findViewById(R.id.studentSrNo);
        }
    }

    /*This method is used by PrintAttand class to take info of selected radio fetchCancelButton for print attand image*/
    @SuppressWarnings("ConstantConditions")
//    public void bindViewHolderForPrintAttand(@NonNull final ProgrammingViewHolder holder, final int position) {
//        String stud_Name = studentNameList.get(position);
//        String father_Name = studentFatherNameList.get(position).split(" ")[0];
//        holder.studentName.setText(wordCaseConvert(stud_Name));
//        holder.studentFatherName.setVisibility(View.VISIBLE);
//        holder.studentFatherName.setText("- Mr."+wordCaseConvert(father_Name));
//        holder.srNo.setText("" + (position + 1));
//        int checkedRadioButton = checkedRadioButtonInfo.get(position);
//        RadioButton p = holder.presentRadioButton;
//        RadioButton a = holder.absentRadioButton;
//        RadioButton l =  holder.leaveRadioButton;
//        if(checkedRadioButton==p.getId()){
//            p.setChecked(true);
//            a.setVisibility(View.INVISIBLE);
//            l.setVisibility(View.INVISIBLE);
//        }
//       else if(checkedRadioButton==a.getId()){
//           p.setVisibility(View.INVISIBLE);
//           a.setChecked(true);
//           l.setVisibility(View.INVISIBLE);
//        }
//       else if (checkedRadioButton==l.getId()){
//            p.setVisibility(View.INVISIBLE);
//            a.setVisibility(View.INVISIBLE);
//            l.setChecked(true);
//        }
//    }

    public class TotalRadioCount implements Runnable {

        @Override
        public void run() {
            int totalPresent = 0;
            int totalAbsent = 0;
            int totalLeave = 0;

            for (int i=0; i<getItemCount(); i++) {
                Integer getRadioButtonId = checkedRadioButtonInfo.get(i);
                switch (getRadioButtonId){
                    case R.id.presentRadioButton:
                        totalPresent++;
                        break;
                    case R.id.absentRadioButton:
                        totalAbsent++;
                        break;
                    case R.id.leaveRadioButton:
                        totalLeave++;
                        break;
                }
//                if (getRadioButtonId == R.id.presentRadioButton) {
//                    totalPresent++;
//                } else if (getRadioButtonId == R.id.absentRadioButton) {
//                    totalAbsent++;
//                } else if (getRadioButtonId == R.id.leaveRadioButton) {
//                    totalLeave++;
//                }
            }
            attend_TakeAttandanceActivity.totalPresentText.setText("" + totalPresent);
            attend_TakeAttandanceActivity.totalAbsentText.setText("" + totalAbsent);
            attend_TakeAttandanceActivity.totalLeaveText.setText("" + totalLeave);
        }
    }
}
