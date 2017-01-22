package com.company;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
	Server.createWebServer().start();
        Connection jdbc = DriverManager.getConnection("jdbc:h2:./main");
        createTables(jdbc);

        Spark.init();

        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap publications = new HashMap();
                    Session session = request.session();
                    String userName = session.attribute("loginName");
                    String userPassword = session.attribute("loginPassword");
                    User user = selectUser(jdbc, userName);
                    if(user == null){
                        return new ModelAndView(publications, "login.html");
                    }else{
                        ArrayList<Books> book = selectBooks(jdbc, user.id);
                        publications.put("loginName", userName);
                        publications.put("userPassword", userPassword);
                        publications.put("books", book);

                        return new ModelAndView(publications, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = request.queryParams("loginName");
                    String enterPassword = request.queryParams("loginPassword");
                    String userId = request.queryParams("userId");
                    User user = selectUser(jdbc, enterName);

                    if(enterName == null || enterPassword == null){
                        throw new Exception("Enter login and password");
                    }
                    if(user == null){
                        insertUser(jdbc, enterName, enterPassword);
                    }
                    else if(!user.password.equals(enterPassword)){
                        throw new Exception("Enter valid password");
                    }
                    session.attribute("loginName", enterName);
                    session.attribute("loginPassword", enterPassword);
                    session.attribute("userId", userId);
                    session.attribute("/");
                    return "";

                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/create-book-input",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    User user = selectUser(jdbc, enterName);
                    if(user == null){
                        throw new Exception("Please log in");
                    }
                    int userId = user.id;
                    String title = request.queryParams("enterTitle");
                    String writer = request.queryParams("enterWriter");
                    String releaseDate = request.queryParams("enterReleaseDate");
                    String readString = request.queryParams("enterRead");
                    if(title == null){
                        throw new Exception("No title entered");
                    }
                    boolean read = Boolean.parseBoolean(readString);
                    insertBook(jdbc, userId, title, writer, releaseDate, read);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.get(
                "/edit-book-input",
                ((request, response) -> {
                   HashMap h = new HashMap();
                   String b = request.queryParams("bookId");
                    int bookId = Integer.parseInt(b);
                    Books book = selectBooks(jdbc, bookId);
                    h.put("book", book);
                    return new ModelAndView(h, "edit.html");

                })
        );

        Spark.post(
                "/edit",
                ((request, response) -> {
                    Session session = request.session();
                    String enterName = session.attribute("loginName");
                    int id = session.attribute("bookId");
                    User user = selectUser(jdbc, enterName);
                    if(user == null) {
                        throw new Exception("Please log in");
                    }
                    String title = request.queryParams("enterTitle");
                    String writer = request.queryParams("enterWriter");
                    String releaseDate = request.queryParams("enterReleaseDate");
                    String readString = request.queryParams("enterRead");
                    if(title == null){
                        throw new Exception("Please enter a title");
                    }
                    boolean read = Boolean.parseBoolean(readString);
                    editBookInput(jdbc, id, title, writer, releaseDate, read);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post(
                "/delete-book",
                ((request, response) -> {
                    String bookIdString = request.queryParams("deleteBookId");
                    int bookId = Integer.parseInt(bookIdString);
                    deleteBook(jdbc, bookId);
                    response.redirect("/");
                    return "";
                })
        );



    }

    public static void insertUser(Connection jdbc, String name, String password) throws SQLException{
        PreparedStatement stmt = jdbc.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.executeQuery();
    }

    public static void createTables(Connection jdbc) throws SQLException{
        Statement stmt = jdbc.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS book (book_id IDENTITY, user_id INT, title VARCHAR, writer VARCHAR, release_date VARCHAR, read BOOLEAN)");
    }

    public static User selectUser(Connection jdbc, String name) throws SQLException {
        PreparedStatement stmt = jdbc.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet queryOut = stmt.executeQuery();
        if(queryOut.next()){
            int id = queryOut.getInt("id");
            String password = queryOut.getString("password");
            return new User(id, name, password);
        }
        return null;
    }
    public static void insertBook(Connection jdbc, int userId, String title, String writer, String releaseDate, boolean read) throws SQLException{
        PreparedStatement stmt = jdbc.prepareStatement("INSERT INTO book VALUES (NULL, ?, ?, ?, ?)");
        stmt.setInt(1, userId);
        stmt.setString(2, title);
        stmt.setString(3, writer);
        stmt.setString(4, releaseDate);
        stmt.setBoolean(5, read);

    }

    public static Books selectBooks(Connection jdbc, int id) throws SQLException{
        PreparedStatement stmt = jdbc.prepareStatement("SELECT * FROM book INNER JOIN users ON book.userId = user.id WHERE book.id = ?");
        stmt.setInt(1, id);
        ResultSet queryOut = stmt.executeQuery();

        if(queryOut.next()) {
            int bookId = queryOut.getInt("book.id");
            int userId = queryOut.getInt("user.id");
            String title = queryOut.getString("title");
            String writer = queryOut.getString("writer");
            String releaseDate = queryOut.getString("release.date");
            Boolean read = queryOut.getBoolean("read");
            return new Books(bookId, userId, title, writer, releaseDate, read);
        }
        return null;

    }
    public static ArrayList<User> selectUsers (Connection jdbc) throws SQLException{
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement stmt = jdbc.prepareStatement("SELECT * FROM users");
        ResultSet queryOutput = stmt.executeQuery();
        while (queryOutput.next()){
          int id = queryOutput.getInt("users.id");
            String userName = queryOutput.getString("users.name");
            String userPassword = queryOutput.getString("users.password");
            User user = new User(id, userName,userPassword);
            users.add(user);

        }
        return users;
    }
    public static ArrayList<Books> selectBook (Connection jdbc, int id) throws SQLException{
       PreparedStatement stmt = jdbc.prepareStatement("SELECT * FROM book INNER JOIN users ON book.userId = users.id WHERE book.id = ?");
        stmt.setInt(1, id);
        ResultSet queryOutput = stmt.executeQuery();
        ArrayList<Books> selectBooks = new ArrayList<>();
        while(queryOutput.next()){
            int bookId = queryOutput.getInt("book.id");
            int userId = queryOutput.getInt("user.id");
            String title = queryOutput.getString("book.title");
            String writer = queryOutput.getString("book.writer");
            String releaseDate = queryOutput.getString("book.releaseDate");
            Boolean read = queryOutput.getBoolean("book.read");
            Books book = new Books(bookId, userId, title, writer,releaseDate, read);
            selectBooks.add(book);
        }
        return selectBooks;

    }
    public static void editBookInput (Connection jdbc, int id, String title, String writer, String releaseDate, boolean read) throws SQLException {
        PreparedStatement stmt = jdbc.prepareStatement("UPDATE books SET title =? SET writer =? SET releaseDate=? SET read=?");
        stmt.setInt(6, id);
        stmt.setString(1, title);
        stmt.setString(2, writer);
        stmt.setString(3, releaseDate);
        stmt.setBoolean(4, read);
        stmt.execute();
    }
    public static void deleteBook (Connection jdbc, int id) throws SQLException{
        PreparedStatement stmt = jdbc.prepareStatement("DELETE FROM books WHERE id = ?");
        stmt.setInt(1, id);
        System.out.println(id);
        stmt.execute();
    }


}
