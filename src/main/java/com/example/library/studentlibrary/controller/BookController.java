package com.example.library.studentlibrary.controller;

import com.example.library.studentlibrary.models.Author;
import com.example.library.studentlibrary.models.Book;
import com.example.library.studentlibrary.models.Genre;
import com.example.library.studentlibrary.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Add required annotations

@RestController
public class BookController {

    @Autowired
    BookService bookService;

    //Write createBook API with required annotations
    @PostMapping("/createBook")
    public ResponseEntity<String> createBook(Book b) {
        bookService.createBook(b);

        return new ResponseEntity<>("Success",HttpStatus.CREATED);
    }

    //Add required annotations
    @GetMapping("/addBook")
    public ResponseEntity getBooks(@RequestParam(value = "genre", required = false) String genre,
                                   @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
                                   @RequestParam(value = "author", required = false) String author){

        List<Book> bookList = null; //find the elements of the list by yourself

        bookList = bookService.getBooks(genre,available,author);

        return new ResponseEntity<>(bookList, HttpStatus.OK);

    }
}
