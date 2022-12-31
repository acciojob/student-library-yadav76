package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.*;
import com.example.library.studentlibrary.repositories.BookRepository;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    int max_allowed_books;

    @Value("${books.max_allowed_days}")
    int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id

        //Note that the error message should match exactly in all cases

        String transId="";
        Optional<Book> book=bookRepository5.findById(bookId);
        if(book.isPresent() && book.get().isAvailable()) {
            Optional<Card> card = cardRepository5.findById(cardId);
            if (card.isPresent() && card.get().getCardStatus() == CardStatus.ACTIVATED) {
                if(card.get().getBooks().size() < max_allowed_books) {
                    Transaction t = Transaction.builder().book(book.get()).card(card.get()).transactionStatus(TransactionStatus.SUCCESSFUL).isIssueOperation(true).transactionDate(new Date()).build();

                    List<Transaction> listOfTransactions = book.get().getTransactions();
                    listOfTransactions.add(t);

                    // Operations After the Issuing the Book
                    book.get().setAvailable(false);
                    book.get().setTransactions(listOfTransactions);
                    book.get().setCard(card.get());

                    List<Book> listOfBooks = card.get().getBooks();
                    listOfBooks.add(book.get());
                    card.get().setBooks(listOfBooks);

                    t.setCard(card.get());

                    bookRepository5.updateBook(book.get());

                    transId = t.getTransactionId();
                    transactionRepository5.save(t);   // save Transaction to DB
                }else {
                    // Else directly save Transaction to DB and Throw Exception
                    Transaction t = Transaction.builder().transactionDate(new Date()).isIssueOperation(true).book(book.get()).card(card.get()).transactionStatus(TransactionStatus.FAILED).build();

                    transactionRepository5.save(t);
                    throw new Exception("Book limit has reached for this card");

                }
            }else {

                Transaction t = Transaction.builder().transactionDate(new Date()).book(book.get()).isIssueOperation(true).transactionStatus(TransactionStatus.FAILED).build();

                throw new Exception("Card is invalid");

            }
        }else {
            Transaction t = Transaction.builder().transactionDate(new Date()).isIssueOperation(true).transactionStatus(TransactionStatus.FAILED).build();

            throw new Exception("Book is either unavailable or not present");
        }

       return transId; //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        Date dateOfIssue = transaction.getTransactionDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOfIssue);    // Mark the date to Calender

        cal.add(Calendar.DATE,getMax_allowed_days);
        Date alloweDate = cal.getTime();

        Date presentDate = new Date();

        // Setting difference in long because output comes in seconds and it can be any large value
        long difference = presentDate.getTime() - alloweDate.getTime();

        long differenceInDays = (difference/(1000*24*60*60)%365);

        // Calculate fine
        int fine = (int)differenceInDays*fine_per_day;

        // Get Details of Student
        Card c = cardRepository5.findById(cardId).get();
        Book b = bookRepository5.findById(bookId).get();

        List<Book> listOfBooks = c.getBooks();
        listOfBooks.remove(b);  // Remove book from Student Account
        c.setBooks(listOfBooks);  // add other books to Student account

        b.setAvailable(true);  // set book available for other students
        b.setCard(null);      // Set card of book to null
        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction  = null;

        returnBookTransaction = Transaction.builder().transactionDate(new Date()).book(b).card(c)
                .transactionDate(presentDate).fineAmount(fine).transactionStatus(TransactionStatus.SUCCESSFUL).isIssueOperation(false).build();

        List<Transaction> listOfTrans = b.getTransactions();
        listOfTrans.add(returnBookTransaction);   // set new fresh Transaction to book
        b.setTransactions(listOfTrans);

        // update book transaction to DB
        bookRepository5.updateBook(b);
        transactionRepository5.save(returnBookTransaction);

        return returnBookTransaction; //return the transaction after updating all details
    }
}