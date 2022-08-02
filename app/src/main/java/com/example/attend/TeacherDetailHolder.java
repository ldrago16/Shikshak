package com.example.attend;

public class TeacherDetailHolder {

    private String teacherName, teacherId, teacherRight;
    private static TeacherDetailHolder teacherDetailHolder;
    private TeacherDetailHolder(){

    }

    static TeacherDetailHolder getTeacherDetailHolderReference(){
        if(teacherDetailHolder != null){
            return teacherDetailHolder;
        }
        teacherDetailHolder = new TeacherDetailHolder();
        return teacherDetailHolder;
    }

    static public void clearTeacherDetail(){
        teacherDetailHolder = null;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherRight() {
        return teacherRight;
    }

    public void setTeacherRight(String teacherRight) {
        this.teacherRight = teacherRight;
    }
}
