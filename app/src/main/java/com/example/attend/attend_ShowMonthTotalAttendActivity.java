package com.example.attend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class attend_ShowMonthTotalAttendActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static String course, term, batch, subject, calendarMonth, calendarYear;
    ArrayList<HashMap<String, String>> studentData = new ArrayList<>();
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attend_activity_show_month_total_attend);
        //setInitialLayout();

        progressBar = findViewById(R.id.progressBar);
        Intent dataReceiverIntent = getIntent();
        course = dataReceiverIntent.getStringExtra("course");
        term = dataReceiverIntent.getStringExtra("term");
        batch = dataReceiverIntent.getStringExtra("batch");
        subject = dataReceiverIntent.getStringExtra("subject");
        calendarMonth = dataReceiverIntent.getStringExtra("month");
        calendarYear = dataReceiverIntent.getStringExtra("year");

        progressBar.setVisibility(View.VISIBLE);
        setYearSubjectDateInfo(course, term, batch, calendarMonth, calendarYear);
        fetchStudentData();

    }

    public Task<Object> studentDataFound(QuerySnapshot queryDocumentsSnapshots) {

        final String termSubjectMonthYear = term + "_" + subject + "_" + calendarMonth + "_" + calendarYear;
        final String presentFieldName = termSubjectMonthYear + "_present";
        final String absentFieldName = termSubjectMonthYear + "_absent";
        final String leaveFieldName = termSubjectMonthYear + "_leave";

        for (DocumentSnapshot document : queryDocumentsSnapshots.getDocuments()) {

            HashMap<String, String> tempStudentDataList = new HashMap<>();
            String stud_Id = document.getId();
            String stud_Name = "" + document.get("student_name");
            String stud_FatherName = "" + document.get("student_father_name");
            String stud_Mobile = "" + document.get("student_mobile");
            String present = "" + document.get(presentFieldName);
            String absent = "" + document.get(absentFieldName);
            String leave = "" + document.get(leaveFieldName);

            if (present.equals("null"))
                present = "0";
            if (absent.equals("null"))
                absent = "0";
            if (leave.equals("null"))
                leave = "0";


            if (!present.equals("0") || !absent.equals("0") || !leave.equals("0")) {
                tempStudentDataList.put("studId", stud_Id);
                tempStudentDataList.put("studName", stud_Name);
                tempStudentDataList.put("studMobile", stud_Mobile);
                tempStudentDataList.put("studFatherName", stud_FatherName);
                tempStudentDataList.put("presentTotal", present);
                tempStudentDataList.put("absentTotal", absent);
                tempStudentDataList.put("leaveTotal", leave);

                studentData.add(tempStudentDataList);
            }
        }
        if (studentData.isEmpty()) {
            Toast.makeText(attend_ShowMonthTotalAttendActivity.this, "NO MONTHLY ATTENDANCE DATA FOUND ", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            finish();
        } else {   //set total session;
            TextView totalSessionTextView = findViewById(R.id.totalSession);
            HashMap<String, String> tempHashMapForSetTotalSession = studentData.get(0);
            int totalPresentOfAnyStudent = Integer.parseInt(tempHashMapForSetTotalSession.get("presentTotal"));
            int totalAbsentOfAnyStudent = Integer.parseInt(tempHashMapForSetTotalSession.get("absentTotal"));
            int totalLeaveOfAnyStudent = Integer.parseInt(tempHashMapForSetTotalSession.get("leaveTotal"));

            int totalSession = totalPresentOfAnyStudent + totalAbsentOfAnyStudent + totalLeaveOfAnyStudent;
            totalSessionTextView.setText("Total Session : " + totalSession);
            setStudentDataOnRecyclerView();
        }

        return null;
    }

    public void fetchStudentData() {
        Task<QuerySnapshot> task = firebaseFirestore.collection("ATTENDANCE")
                .whereEqualTo("course_name", course)
                .whereEqualTo("batch", batch)
                .orderBy("student_name")
                .orderBy("student_father_name")
                .get();

        task.onSuccessTask(new SuccessContinuation<QuerySnapshot, Object>() {
            @NonNull
            @Override
            public Task<Object> then(@Nullable QuerySnapshot queryDocumentsSnapshots) {

                if (queryDocumentsSnapshots.isEmpty()) {
                    Toast.makeText(attend_ShowMonthTotalAttendActivity.this, "NO MONTHLY ATTENDANCE DATA FOUND ", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                    return null;
                } else {
                    return studentDataFound(queryDocumentsSnapshots);
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(attend_ShowMonthTotalAttendActivity.this, "NO STUDENT FOUND " + e, Toast.LENGTH_LONG).show();
                Log.d("LOOOG", "at NO STUDENT FOUND :" + Log.getStackTraceString(e));
                progressBar.setVisibility(View.GONE);
                finish();
            }
        });


    }

    private void setYearSubjectDateInfo(String course, String term, String batch, String calendarMonth, String calendarMonthYear) {
        TextView courseTermBatchInfo = findViewById(R.id.courseTermBatchInfo);
        TextView dateInfo = findViewById(R.id.dateInfo);
        TextView subInfo = findViewById(R.id.subjectInfo);

        Integer month = Integer.parseInt(calendarMonth);
        String course_year_batch = course + "_" + term + "_" + batch;
        String date = new DateFormatSymbols().getMonths()[month - 1];
        String dateMonth = date + "/" + calendarMonthYear;

        courseTermBatchInfo.setText(course_year_batch);
        dateInfo.setText(dateMonth);
        subInfo.setText(subject);
    }

    RecyclerView studentTotalAttendRecyclerView;

    public void setStudentDataOnRecyclerView() {
        studentTotalAttendRecyclerView = findViewById(R.id.recyclerViewForFillingStudent);
        studentTotalAttendRecyclerView.setLayoutManager(new LinearLayoutManager(attend_ShowMonthTotalAttendActivity.this));
        studentTotalAttendRecyclerView.setAdapter(new ShowMonthTotalAttendProgrammingAdapter(attend_ShowMonthTotalAttendActivity.this, studentData));
        progressBar.setVisibility(View.GONE);
    }
}

class ShowMonthTotalAttendProgrammingAdapter extends RecyclerView.Adapter<ShowMonthTotalAttendProgrammingAdapter.ProgViewHolder> {

    ArrayList<HashMap<String, String>> studentData;
    Context context;

    ShowMonthTotalAttendProgrammingAdapter(Context context, ArrayList<HashMap<String, String>> studentData) {
        this.context = context;
        this.studentData = studentData;
    }

    @NonNull
    @Override
    public ProgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.attend_show_total_attendance_adapter_layout, parent, false);
        return new ProgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgViewHolder holder, int position) {
        HashMap<String, String> studentDataOnPosition = studentData.get(position);

        String srNo = (position + 1) + "";
        final String id = studentDataOnPosition.get("studId");
        final String name = studentDataOnPosition.get("studName");
        final String mobile = studentDataOnPosition.get("studMobile");
        final String fatherName = studentDataOnPosition.get("studFatherName");
        final String present = studentDataOnPosition.get("presentTotal");
        final String absent = studentDataOnPosition.get("absentTotal");
        final String leave = studentDataOnPosition.get("leaveTotal");

        holder.srNo.setText(srNo);
        holder.studentName.setText(name);
        holder.presentTotal.setText(present);
        holder.absentTotal.setText(absent);
        holder.leaveTotal.setText(leave);

        holder.studentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                AlertDialog alert = alertBuilder.create();
                alert.setCancelable(true);
                alert.setIcon(android.R.drawable.ic_menu_info_details);
                alert.setTitle("Student Information.");
                alert.setMessage("Student Id = " + id + "\nStudent name = " + name + "\nFather name = " + fatherName + "\nMobile no = " + mobile);
                alert.show();
            }
        });

        holder.presentTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchStudentMonthAttend(id, name, "present");
            }
        });

        holder.absentTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchStudentMonthAttend(id, name, "absent");
            }
        });

        holder.leaveTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchStudentMonthAttend(id, name, "leave");
            }
        });
    }

    String alreadyFetchedStudentId = "";
    HashMap<String, Task<DocumentSnapshot>> prevFetchedStudentMonthlyAttendData = new HashMap<>();
    Task<DocumentSnapshot> alreadyFetchedStudentMonthlyAttendData;

    public void fetchStudentMonthAttend(final String tempStudentId, final String studentName, final String showingFor) {
        if (!prevFetchedStudentMonthlyAttendData.containsKey(tempStudentId)) {
            Toast.makeText(context, "Fetching...", Toast.LENGTH_SHORT).show();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Task<DocumentSnapshot> studentMonthAttendDetailTask = firebaseFirestore.collection("ATTENDANCE")
                    .document(tempStudentId)
                    .collection(attend_ShowMonthTotalAttendActivity.subject)
                    .document(attend_ShowMonthTotalAttendActivity.calendarMonth + "_" + attend_ShowMonthTotalAttendActivity.calendarYear)
                    .get();

            studentMonthAttendDetailTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (!(task.isSuccessful()) && !(task.getResult().exists())) {
                        Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    prevFetchedStudentMonthlyAttendData.put(tempStudentId, task);
                    studentMonthlyAttendFound(task, studentName, showingFor);
                }
            });
            studentMonthAttendDetailTask.addOnFailureListener((Activity) context, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Network Issue", Toast.LENGTH_LONG).show();
                    Log.d("Looog", Log.getStackTraceString(e));
                }
            });
        } else {
            studentMonthlyAttendFound(prevFetchedStudentMonthlyAttendData.get(tempStudentId), studentName, showingFor);
        }
//        if(!(tempStudentId.equals(alreadyFetchedStudentId))) {
//            Toast.makeText(context, "Fetching...", Toast.LENGTH_SHORT).show();
//            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//            Task<DocumentSnapshot> studentMonthAttendDetailTask = firebaseFirestore.collection("ATTENDANCE")
//                    .document(tempStudentId)
//                    .collection(attend_ShowMonthTotalAttendActivity.subject)
//                    .document(attend_ShowMonthTotalAttendActivity.calendarMonth + "_" + attend_ShowMonthTotalAttendActivity.calendarYear)
//                    .get();
//            studentMonthAttendDetailTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if(!(task.isSuccessful()) && !(task.getResult().exists())){
//                        Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    alreadyFetchedStudentId = tempStudentId;
//                    alreadyFetchedStudentMonthlyAttendData = task;
//                    studentMonthlyAttendFound(task, studentName, showingFor);
//                }
//            });
//            studentMonthAttendDetailTask.addOnFailureListener((Activity) context, new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(context, "Network Issue",Toast.LENGTH_LONG ).show();
//                    Log.d("Looog", Log.getStackTraceString(e));
//                }
//            });
//        }
//        else {
//            studentMonthlyAttendFound(alreadyFetchedStudentMonthlyAttendData, studentName, showingFor);
//        }
    }

    public void studentMonthlyAttendFound(Task<DocumentSnapshot> task, String studentName, String showingFor) {
        HashMap<String, Object> studentMonthlyAttend = (HashMap<String, Object>) task.getResult().getData();
        StringBuffer storeMonthlyAttendDates = new StringBuffer();
        storeMonthlyAttendDates.append("YYYY/M/D\n\n");
        ArrayList<String> tempDatekeySet = new ArrayList<>(studentMonthlyAttend.keySet());
        ArrayList<Date> datekeySet = new ArrayList<>();

        for (String date : tempDatekeySet) {
            try {
                Date tempDate = new SimpleDateFormat("yyyy/M/d").parse(date);
                datekeySet.add(tempDate);
            }catch (ParseException e){
                Log.d("Looog", "Date Parse Exception");
            }
        }


        Collections.sort(datekeySet);

        for (Date date : datekeySet) {
            String tempDate = new SimpleDateFormat("yyyy/M/d").format(date);
            if (studentMonthlyAttend.get(tempDate).equals(showingFor)) {
                storeMonthlyAttendDates.append(tempDate + "\n");
            }
        }

        if (storeMonthlyAttendDates.length() == 0) {
            storeMonthlyAttendDates.append("No Data Found\n");
        }

        storeMonthlyAttendDates = storeMonthlyAttendDates.deleteCharAt(storeMonthlyAttendDates.length() - 1); //clear last extra \n(extra line)

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        AlertDialog alert = alertBuilder.create();
        alert.setCancelable(true);
        alert.setIcon(android.R.drawable.ic_menu_info_details);
        alert.setTitle(studentName + " " + showingFor + " date");
        alert.setMessage(storeMonthlyAttendDates);
        alert.show();
    }

    @Override
    public int getItemCount() {
        return studentData.size();
    }

    class ProgViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, srNo;
        TextView presentTotal, absentTotal, leaveTotal;

        public ProgViewHolder(@NonNull View itemView) {
            super(itemView);
            srNo = itemView.findViewById(R.id.studentSrNo);
            studentName = itemView.findViewById(R.id.studentName);
            presentTotal = itemView.findViewById(R.id.presentTextView);
            absentTotal = itemView.findViewById(R.id.absentTextView);
            leaveTotal = itemView.findViewById(R.id.leaveTextView);
        }
    }
}