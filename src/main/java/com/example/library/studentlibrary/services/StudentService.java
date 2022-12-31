package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.Card;
import com.example.library.studentlibrary.models.Student;
import com.example.library.studentlibrary.repositories.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {


    @Autowired
    CardService cardService4;

    @Autowired
    StudentRepository studentRepository4;

    public Student getDetailsByEmail(String email){
        Student student = null;

        student = studentRepository4.findByEmailId(email);

        return student;
    }

    public Student getDetailsById(int id){
        Student student = null;

        student = studentRepository4.findById(id).get();
        // .get() is used to get Entity from DB if it is null then throws Exception

        return student;
    }

    public void createStudent(Student student){

        studentRepository4.save(student);
    }

    public void updateStudent(Student student){

        studentRepository4.updateStudentDetails(student);
    }

    public void deleteStudent(int id){
        //Delete student and deactivate corresponding card
        Student student = studentRepository4.findById(id).get();  // first find the student by Id

        // Deactivate the card of the student
        cardService4.deactivateCard(id);

        // Now delete the student
        if (student != null) studentRepository4.deleteCustom(id);

    }
}