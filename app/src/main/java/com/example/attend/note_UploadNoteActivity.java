package com.example.attend;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class note_UploadNoteActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    DocumentSnapshot TeacherDocRef;

    Context context;

    String courseSpinnerSelected, termSpinnerSelected, subjectSpinnerSelected, noteNameText, noteDescriptionText, noteFilePath;

    String currentDate_Time;

    TextView progressBarText;

    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> subjectSpinnerItems = new ArrayList<>();

    Spinner courseSpinner, termSpinner, subjectSpinner;
    EditText noteNameEditText, noteDescriptionEditText;
    TextView noteBrowseTextView;
    Button submitYearSubjectButton;

//    String TeacherId = "aniket000";
//    String TeacherRight = "admin";
//    String TeacherName = "aniket";

    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

//    HashMap<String, ArrayList<String>> adminCourseAndDurationMapForViewAttend = new HashMap<>();
//    HashMap<String, ArrayList<String>> adminSubjectMapForViewAttend = new HashMap<>();

    boolean submitButtonState = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity_upload_note);

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

        Button jumpNoteDeleteActivityButton = findViewById(R.id.jumpDeleteNoteActivityButton);
        jumpNoteDeleteActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(note_UploadNoteActivity.this, note_DeleteNoteActivity.class);
                startActivity(intent);
            }
        });

        courseSpinner = findViewById(R.id.courseSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        noteNameEditText = findViewById(R.id.noteNameEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        noteBrowseTextView = findViewById(R.id.noteBrowseTextView);
        submitYearSubjectButton = findViewById(R.id.noteUploadButton);


        context = note_UploadNoteActivity.this;

        requestForPermission();

        fetchTeacherSelectedCourseTermSubjectData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTeacherSelectedCourseTermSubjectData();
        blockSubmitButton(submitButtonState);
    }

    public void requestForPermission(){
        //        request for external storage access permission to user
        if (ActivityCompat.checkSelfPermission(note_UploadNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(note_UploadNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(note_UploadNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    public void fetchTeacherSelectedCourseTermSubjectData() {
        courseSpinnerItems.clear();
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

    @SuppressWarnings("unchecked")
    public void teacherSelectedCourseTermSubjectDataFound() {
//        fetch a array list of course for which teacher teach
        courseSpinnerItems = (ArrayList<String>) TeacherDocRef.get("course_name");
        if (courseSpinnerItems == null) {
            courseSpinnerItems = new ArrayList<>();
            courseSpinnerItems.add("No Courses Found");
        }
        courseSpinner.setOnItemSelectedListener(new note_UploadNoteActivity.courseOnItemSelectListener());
        termSpinner.setOnItemSelectedListener(new note_UploadNoteActivity.termOnItemSelectListener());
        subjectSpinner.setOnItemSelectedListener(new note_UploadNoteActivity.subjectOnItemSelectListener());
        noteBrowseTextView.setOnClickListener(new note_UploadNoteActivity.noteBrowseOnItemClickListener());
        submitYearSubjectButton.setOnClickListener(new note_UploadNoteActivity.OnClick());

        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            blockSubmitButton(true);
            try {
                courseSpinnerSelected = courseSpinnerItems.get(i);
                termSpinnerItems = ((ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected));

            } catch (IndexOutOfBoundsException | NullPointerException e) {
                termSpinnerItems = null;
                Log.d("LOOOG", "at courseOnItemSelectListener:" + Log.getStackTraceString(e));
            }
            if (termSpinnerItems == null) {
                termSpinnerItems = new ArrayList<>();
                termSpinnerItems.add("No Year Found");
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
            blockSubmitButton(true);
            termSpinnerSelected = termSpinnerItems.get(i);
            ArrayList<String> batchSpinnerItems;
            try {
                subjectSpinnerItems.clear();
                batchSpinnerItems = (ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected);
                for (String batch : batchSpinnerItems) {
                    ArrayList<String> tempSubjectList = (ArrayList<String>) TeacherDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected + "_" + batch);
                    if (tempSubjectList != null) {
                        subjectSpinnerItems.addAll(tempSubjectList);
                    }
                }
                if(subjectSpinnerItems != null){
                    subjectSpinnerItems = new ArrayList<>(new LinkedHashSet<>(subjectSpinnerItems));    //to make subjectSpinnerItems unique or non repeating
                    subjectSpinnerItems.add("other");
                }
            } catch (ClassCastException | NullPointerException e) {
                subjectSpinnerItems = null;
                Toast.makeText(context, "No Subject found", Toast.LENGTH_SHORT).show();
                Log.d("LOOOG", "Exception : No Subject found " + Log.getStackTraceString(e));
            }
            if (subjectSpinnerItems == null) {
                subjectSpinnerItems = new ArrayList<>();
                subjectSpinnerItems.add("No Subject Found");
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

            if (noteFilePath!=null && noteFilePath.length()>0) {
                blockSubmitButton(false);
            }
            if (subjectSpinnerSelected.equalsIgnoreCase("No Subject Found")) {
                blockSubmitButton(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class noteBrowseOnItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), FileChooser.class);
            //intent.setType("*/*");
            intent.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
            //intent.createChooser(intent, "Choose a file");
            startActivityForResult(intent, 1);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        noteFilePath = "";
        if (requestCode == 1 && data != null) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                noteFilePath = uri.getPath();
                noteBrowseTextView.setText(noteFilePath);
                blockSubmitButton(false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (!isNetworkConnected()) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if(noteFilePath.length()==0){
                noteBrowseTextView.setError("Select a File");
                return;
            }

            noteNameText = noteNameEditText.getText().toString().trim();
            if(noteNameText.length()==0){
                noteNameEditText.setError("Enter Valid FileName");
                noteNameEditText.requestFocus();
                return;
            }

            try {
                InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                hideKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            catch (NullPointerException e){}
            if(noteFilePath.isEmpty()){
                noteNameEditText.setError("Select a valid file");
                return;
            }

            else{
                blockScreen(true);
                clearFocusFromEditText();
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                Task<QuerySnapshot> task =  firebaseFirestore
                        .collection("NOTES")
                        .document(courseSpinnerSelected+termSpinnerSelected)
                        .collection(subjectSpinnerSelected)
                        .whereEqualTo("note_name", noteNameText)
                        .get();

                task.continueWith(new Continuation<QuerySnapshot, Object>() {
                    @Override
                    public Object then(@NonNull Task<QuerySnapshot> task) throws Exception {
                        QuerySnapshot queryDocument = task.getResult();
                        if(queryDocument.isEmpty()){
                            final String fileExtension = FilenameUtils.getExtension(noteFilePath);
                            String fileName = noteNameText.trim().concat("."+fileExtension);
                            noteDescriptionText = noteDescriptionEditText.getText().toString();
                            final String uploadPath = "NOTES/" +courseSpinnerSelected+termSpinnerSelected+ "/" +subjectSpinnerSelected+ "/" +fileName;

                            String contentType = URLConnection.guessContentTypeFromName(fileName);
                            StorageMetadata meta = new StorageMetadata.Builder().setContentType(contentType).build();

                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            final StorageReference uploadNoteReference = firebaseStorage.getReference(uploadPath);
                            UploadTask uploadTask = uploadNoteReference.putFile(Uri.fromFile(new File(noteFilePath)),meta);

                            progressBarText = findViewById(R.id.progressBarText);
                            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                    long percentage = ((long)(((float)taskSnapshot.getBytesTransferred()/(float)taskSnapshot.getTotalByteCount())*100f));
                                    progressBarText.setText(""+percentage+"%");
                                }
                            });

                            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show();
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                    uploadNoteReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                            Uri downloadUri = task.getResult();
                                            HashMap<String, String> notesData = new HashMap<>();
                                            notesData.put("note_name",noteNameText);
                                            notesData.put("description", noteDescriptionText);
                                            notesData.put("time", getCurrentDateTime());
                                            notesData.put("url", downloadUri.toString());
                                            notesData.put("teacher_id", teacherDetailHolder.getTeacherId());
                                            notesData.put("teacher_name", teacherDetailHolder.getTeacherName());
                                            notesData.put("file_extension", fileExtension);
                                            notesData.put("content_type", taskSnapshot.getMetadata().getContentType());
                                            notesData.put("storage_path", uploadPath);

                                            firebaseFirestore
                                                    .collection("NOTES")
                                                    .document(courseSpinnerSelected+termSpinnerSelected)
                                                    .collection(subjectSpinnerSelected)
                                                    .document(noteNameText)
                                                    .set(notesData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()) {
                                                                Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_LONG).show();
                                                            }
                                                            else {
                                                                Toast.makeText(context, "Upload Failed", Toast.LENGTH_LONG).show();
                                                            }
                                                            blockScreen(false);

                                                            {
                                                                noteNameEditText.setText("");
                                                                noteDescriptionEditText.setText("");
                                                                noteBrowseTextView.setText("");
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(note_UploadNoteActivity.this, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Upload Failed", Toast.LENGTH_LONG).show();
                                                            Log.d("LOOOG",Log.getStackTraceString(e));
                                                            blockScreen(false);
                                                        }
                                                    });

                                        }
                                    }).addOnFailureListener(note_UploadNoteActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(note_UploadNoteActivity.this, "Failed", Toast.LENGTH_LONG);
                                            blockScreen(false);
                                            Log.d("LOOOG",Log.getStackTraceString(e));
                                        }
                                    });
                                }
                                String getCurrentDateTime(){
                                    return new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime());
                                }
                            });
                        }
                        else{
                            noteNameEditText.setError("File Already Exist");
                            noteNameEditText.requestFocus();
                            blockScreen(false);
                        }
                        return null;
                    }
                });
            }
        }
    }

    private void blockScreen(boolean state){        //if true screen set To block otherwise unblock
        RelativeLayout inputBlocker = findViewById(R.id.inputBlocker);
        blockSubmitButton(state);
        if(state) {
            inputBlocker.setVisibility(View.VISIBLE);
        }
        else {
            inputBlocker.setVisibility(View.GONE);
        }
    }

    public void blockSubmitButton(boolean buttonstate) {
        submitYearSubjectButton.setEnabled(!buttonstate);
        submitButtonState = !buttonstate;
//        if (buttonstate) {
//            submitYearSubjectButton.setClickable(false);
//            submitYearSubjectButton.setEnabled(false);
//            submitButtonState = false;
////            submitYearSubjectButton.setBackgroundResource(R.drawable.deactive_submit_button_background);
//        } else {
//            submitYearSubjectButton.setClickable(true);
//            submitYearSubjectButton.setEnabled(false);
//            submitButtonState = true;
////            submitYearSubjectButton.setBackgroundResource(R.drawable.submit_button_background);
//        }
    }
    private boolean isNetworkConnected() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED | connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        return connected;
    }

    //clear focus from the noteNameEditText and noteDescriptionEditText
    public void clearFocusFromEditText(){
        noteNameEditText.clearFocus();
        noteDescriptionEditText.clearFocus();
    }
}
