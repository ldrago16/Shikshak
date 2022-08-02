package com.example.attend;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.Nullable;

public class note_DeleteNoteActivity extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentSnapshot TeacherDocRef;

    note_DeleteNoteActivity context;

    Spinner courseSpinner, termSpinner, subjectSpinner;
    String courseSpinnerSelected, termSpinnerSelected, subjectSpinnerSelected;

    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> subjectSpinnerItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity_delete_note);

//        Intent intent = getIntent();
//        String TeacherId = intent.getStringExtra("TeacherId");
//        String TeacherRight = intent.getStringExtra("Rights");
//        String TeacherName = intent.getStringExtra("Name");

//        String TeacherId = "aniket000";
//        String TeacherRight = "admin";
//        String TeacherName = "aniket";
//
//        teacherDetailHolder.setTeacherId(TeacherId);
//        teacherDetailHolder.setTeacherRight(TeacherRight);
//        teacherDetailHolder.setTeacherName(TeacherName);

        courseSpinner = findViewById(R.id.courseSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);

        fetchTeacherSelectedCourseTermSubjectData();
        context = note_DeleteNoteActivity.this;
    }

    public void fetchTeacherSelectedCourseTermSubjectData() {
        courseSpinnerItems.clear();
        blockScreen(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("TEACHERS").document(teacherDetailHolder.getTeacherId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Toast.makeText(attend_SelectYearSubActivity.this, "TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                    TeacherDocRef = documentSnapshot;
                    teacherSelectedCourseTermSubjectDataFound();
                } else {
                    Toast.makeText(context, "NO TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void teacherSelectedCourseTermSubjectDataFound() {
//        fetch a array list of course for which teacher teach
        blockScreen(false);
        courseSpinnerItems = (ArrayList<String>) TeacherDocRef.get("course_name");
        if (courseSpinnerItems == null) {
            courseSpinnerItems = new ArrayList<>();
            courseSpinnerItems.add("No Courses Found");
        }
        courseSpinner.setOnItemSelectedListener(new note_DeleteNoteActivity.courseOnItemSelectListener());
        termSpinner.setOnItemSelectedListener(new note_DeleteNoteActivity.termOnItemSelectListener());
        subjectSpinner.setOnItemSelectedListener(new note_DeleteNoteActivity.subjectOnItemSelectListener());
        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                courseSpinnerSelected = courseSpinnerItems.get(i);
                termSpinnerItems = ((ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected));

            } catch (IndexOutOfBoundsException | NullPointerException e) {
                termSpinnerItems = null;
                Log.d("LOOOG", "at courseOnItemSelectListener:" + Log.getStackTraceString(e));
            }
            if (termSpinnerItems == null) {
                termSpinnerItems = new ArrayList<>();
                termSpinnerItems.add("no year found");
            }
            Collections.sort(termSpinnerItems);
            termSpinner.setAdapter(new MySpinnerAdapter(context, termSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class termOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
            termSpinnerSelected = termSpinnerItems.get(i);
            ArrayList<String> batchSpinnerItems;
            try {
                subjectSpinnerItems.clear();
                batchSpinnerItems = (ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected);
                for (String batch : batchSpinnerItems) {
                    ArrayList<String> tempSubjectList = (ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected + "_" + batch);
                    if (tempSubjectList != null) {
                        subjectSpinnerItems.addAll(tempSubjectList);
                        subjectSpinnerItems.add("other");
                    }
                }
            } catch (ClassCastException | NullPointerException e) {
                subjectSpinnerItems = null;
                Toast.makeText(context, "no subject found", Toast.LENGTH_SHORT).show();
                Log.d("LOOOG", "Exception : no subject found " + Log.getStackTraceString(e));
            }
            if (subjectSpinnerItems == null) {
                subjectSpinnerItems = new ArrayList<>();
                subjectSpinnerItems.add("no subject found");
            }
            subjectSpinner.setAdapter(new MySpinnerAdapter(context, subjectSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class subjectOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            subjectSpinnerSelected = subjectSpinnerItems.get(i);
            if (subjectSpinnerSelected.equalsIgnoreCase("no subject found")) {
                recyclerViewPopulateWithNotesData(new ArrayList<HashMap<String, String>>(), "", "");
                return;
            }

            final String courseTerm = courseSpinnerSelected + termSpinnerSelected;
            final String subject = subjectSpinnerSelected;
            fetchNotesDataAndPopulateRecycleView(courseTerm, subject);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void fetchNotesDataAndPopulateRecycleView(final String courseTerm, final String subject) {
        blockScreen(true);
        Task<QuerySnapshot> fetchNoteDataTask = firestore.collection("NOTES")
                .document(courseTerm)
                .collection(subject)
                .orderBy("time", Query.Direction.DESCENDING)
                .get();

        fetchNoteDataTask.continueWith(new Continuation<QuerySnapshot, Nullable>() {
            @Override
            public Nullable then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(context, "No Notes Found", Toast.LENGTH_SHORT).show();
                    return null;
                }

                ArrayList<HashMap<String, String>> tempNotesDetail = new ArrayList<>();
                for (QueryDocumentSnapshot noteSnap : task.getResult()) {
                    if (!noteSnap.exists())
                        continue;

                    HashMap<String, String> noteData = new HashMap<>();
                    noteData.put("name", "" + noteSnap.get("note_name"));
                    noteData.put("description", "" + noteSnap.get("description"));
                    noteData.put("teacher_id", "" + noteSnap.get("teacher_id"));
                    noteData.put("teacher_name", "" + noteSnap.get("teacher_name"));
                    noteData.put("time", "" + noteSnap.get("time"));
                    noteData.put("url", "" + noteSnap.get("url"));
                    noteData.put("fileExtension", "." + noteSnap.get("file_extension"));
                    noteData.put("fileContentType", "" + noteSnap.get("content_type"));
                    noteData.put("fileStoragePath", "" + noteSnap.get("storage_path"));
                    tempNotesDetail.add(noteData);
                }

                recyclerViewPopulateWithNotesData(tempNotesDetail, subject, courseTerm);
                return null;
            }
        });

        fetchNoteDataTask.addOnFailureListener(context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "No Notes Found", Toast.LENGTH_SHORT).show();
                Log.d("Looog", Log.getStackTraceString(e));
            }
        });
    }

    public void recyclerViewPopulateWithNotesData(ArrayList<HashMap<String, String>> fetchedNotesDetail, String selectedSubject, String selectedCourseTerm) {
        RecyclerView noteRecycleView = findViewById(R.id.notesRecyclerView);
        noteRecycleView.setLayoutManager(new LinearLayoutManager(context));
        noteRecycleView.setAdapter(new noteRecyclerAdapter(fetchedNotesDetail, context, selectedSubject, selectedCourseTerm));
        blockScreen(false);
    }

    void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.yearSubSubmitProgressBar);
        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}

class noteRecyclerAdapter extends RecyclerView.Adapter<noteRecyclerAdapter.StudentNoteViewholder> {

    private ArrayList<HashMap<String, String>> notesData;
    private note_DeleteNoteActivity activityContext;
    private String subject;
    private String courseTerm;
    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

    noteRecyclerAdapter(ArrayList<HashMap<String, String>> tempNotesData, note_DeleteNoteActivity context, String selectedSubject, String selectedCourseTerm) {
        notesData = tempNotesData;
        activityContext = context;
        subject = selectedSubject;
        courseTerm = selectedCourseTerm;
    }

    @NonNull
    @Override
    public StudentNoteViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.note_adapter_layout, parent, false);
        return new StudentNoteViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentNoteViewholder holder, final int position) {
        final HashMap<String, String> noteDataAtPosition = notesData.get(position);
        final String name = noteDataAtPosition.get("name");
        final String time = noteDataAtPosition.get("time");
        final String description = noteDataAtPosition.get("description");
        final String teacher_id = noteDataAtPosition.get("teacher_id");
        final String teacher_name = noteDataAtPosition.get("teacher_name");
        final String url = noteDataAtPosition.get("url");
        final String file_extension = noteDataAtPosition.get("fileExtension");
        final String file_contentType = noteDataAtPosition.get("fileContentType");
        final String file_storagePath = noteDataAtPosition.get("fileStoragePath");

        holder.name.setText(name);
        holder.time.setText(time);
        holder.description.setText(description);
        holder.teacher.setText(teacher_name);

        holder.downloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()) {
                    Toast.makeText(activityContext, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                downloadNote(name, description, url, file_contentType, teacher_name, file_extension);
            }
        });

        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()) {
                    Toast.makeText(activityContext, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder deleteAlertBuilder = new AlertDialog.Builder(activityContext)
                        .setTitle("Warning");
                //teacher who delete the note is the person who upload the note
                if (!teacher_id.equals(teacherDetailHolder.getTeacherId())) {
                    if (teacherDetailHolder.getTeacherRight().equals("regular")) { // if teacher is not admin so he is not permitted to deleted other teacher uploaded notes
                        Toast.makeText(activityContext, "You are not authorized for delete this content", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        deleteAlertBuilder.setMessage("You didn't uploaded this content but because you are admin you are allowed to delete this.\nDo you want to continue.");
                    }
                } else {
                    deleteAlertBuilder.setMessage("This note will permanently deleted\nDo you want to continue.");
                }


                deleteAlertBuilder
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNote(name, file_storagePath);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog deleteAlert = deleteAlertBuilder.create();
                deleteAlert.show();

            }
        });
    }

    public void downloadNote(String name, String description, String url, String file_contentType, String teacher_name, String file_extension) {
        Toast.makeText(activityContext, "downloading...", Toast.LENGTH_SHORT).show();
        final Uri noteUri = Uri.parse(url);
        DownloadManager downloadManager = (DownloadManager) activityContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(noteUri);
        request.setTitle(name);
        request.setDescription(description);
        request.setMimeType(file_contentType);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        final String fileName = name + "_by_" + teacher_name + file_extension;
        String filePath = subject + "/" + fileName;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filePath);
        // final File excelDestination = new File(activityContext.getExternalFilesDir("Excel")+"/"+dateSpinnerSelected+ "_" +monthSpinnerSelected+ "_" + yearSpinnerSelected, fileName);
        //request.setDestinationUri(Uri.fromFile(excelDestination));
        downloadManager.enqueue(request);
    }

    public void deleteNote(final String name, final String file_storagePath) {
        Toast.makeText(activityContext, "Note Deleting...", Toast.LENGTH_SHORT).show();
        activityContext.blockScreen(true);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Task<Void> firestoreDeleteTask = firebaseFirestore.collection("NOTES")
                .document(courseTerm)
                .collection(subject)
                .document(name)
                .delete();
        firestoreDeleteTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    Task<Void> storageDeleteTask = firebaseStorage.getReference(file_storagePath).delete();
                    storageDeleteTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(activityContext, "Note Deleted", Toast.LENGTH_LONG).show();
                                activityContext.fetchNotesDataAndPopulateRecycleView(courseTerm, subject);
                            } else {
                                Toast.makeText(activityContext, "Storage:Note Deletion Failed", Toast.LENGTH_LONG).show();
                                activityContext.blockScreen(false);
                            }
                        }
                    });
                    storageDeleteTask.addOnFailureListener(activityContext, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            activityContext.blockScreen(false);
                            Toast.makeText(activityContext, "Storage:Note Deletion Exception", Toast.LENGTH_LONG).show();
                            Log.d("Looog", Log.getStackTraceString(e));
                        }
                    });
                } else {
                    activityContext.blockScreen(false);
                    Toast.makeText(activityContext, "DataBase:Note Deletion Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
        firestoreDeleteTask.addOnFailureListener(activityContext, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                activityContext.blockScreen(false);
                Toast.makeText(activityContext, "firestore:Note Deletion Exception", Toast.LENGTH_LONG).show();
                Log.d("Looog", Log.getStackTraceString(e));
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesData.size();
    }

    public class StudentNoteViewholder extends RecyclerView.ViewHolder {

        TextView name, time, description, teacher;
        ImageView downloadIcon;
        ImageView deleteIcon;

        public StudentNoteViewholder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.noteName);
            time = itemView.findViewById(R.id.noteTime);
            description = itemView.findViewById(R.id.noteDescription);
            teacher = itemView.findViewById(R.id.noteTeacherName);
            deleteIcon = itemView.findViewById(R.id.deleteNoteIcon);
            downloadIcon = itemView.findViewById(R.id.downloadNoteIcon);
        }
    }

    private boolean isNetworkConnected() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) activityContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED | connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        return connected;
    }
}
