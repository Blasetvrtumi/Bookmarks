package es.codeurjc.holamundo.controller;

import es.codeurjc.holamundo.entity.Book;
import es.codeurjc.holamundo.entity.User;
import es.codeurjc.holamundo.repository.BookRepository;
import es.codeurjc.holamundo.repository.UserRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ProfilePageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/profile/{username}/**")
    public String loadProfilePage(Model model, @PathVariable String username, HttpServletRequest request) throws SQLException {

        // Get user from the database
        User user = userRepository.findByUsername(username);
        user.setProfileImageString(user.blobToString(user.getProfileImageFile()));
        user.setProfileImageString(user.blobToString(user.getProfileImageFile()));
        //User info
        List<String> userRoles = user.getRole();

        // Search for admin role or Author role
        String role = "USER";
        if (userRoles.contains("ADMIN")) {
            role = "ADMIN";
        } else if (userRoles.contains("AUTHOR")) {
            role = "AUTHOR";
        }

        boolean isCurrentUser = false;
        Authentication authentication = (Authentication) request.getUserPrincipal();
        if (authentication != null) {
            model.addAttribute("user", true);
            String currentUsername = authentication.getName();
            //User userlogged = userRepository.findByUsername(authentication.getName());
            if (currentUsername.equals(username)) {
                isCurrentUser = true;
            } else {
                isCurrentUser = false;
            }
        }
        model.addAttribute("currentUser", isCurrentUser);

        String alias = user.getAlias();
        String description = user.getDescription();
        String profileImage = user.getProfileImageString();
        String email = user.getEmail();
        String password = user.getPassword();

        model.addAttribute("username", username);
        model.addAttribute("alias", alias);
        model.addAttribute("role", role);
        model.addAttribute("description", description);
        model.addAttribute("profileImageString", profileImage);
        model.addAttribute("email", email);
        model.addAttribute("password", password);

        int nReadBooks = user.getReadBooks().size();
        int nReadingBooks = user.getReadingBooks().size();
        int nWantedBooks = user.getWantedBooks().size();
        int nReviews = user.getReviews().size();

        List<Book> readBooksList = userRepository.getReadBooks(username, PageRequest.of(0, 4)).getContent();
        List<Book> readingBooksList = userRepository.getReadingBooks(username, PageRequest.of(0, 4)).getContent();
        List<Book> wantedBooksList = userRepository.getWantedBooks(username, PageRequest.of(0, 4)).getContent();

        for (int i = 0; i < readBooksList.size(); i++) {
            readBooksList.get(i).setImageString(readBooksList.get(i).blobToString(readBooksList.get(i).getImageFile()));
        }

        for (int i = 0; i < readingBooksList.size(); i++) {
            readingBooksList.get(i).setImageString(readingBooksList.get(i).blobToString(readingBooksList.get(i).getImageFile()));
        }

        for (int i = 0; i < wantedBooksList.size(); i++) {
            wantedBooksList.get(i).setImageString(wantedBooksList.get(i).blobToString(wantedBooksList.get(i).getImageFile()));
        }

        List<Double> readBooksRatings = new ArrayList<>();
        readBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            readBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsRead", readBooksRatings);

        List<Double> readingBooksRatings = new ArrayList<>();
        readingBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            readingBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsReading", readingBooksRatings);

        List<Double> wantedBooksRatings = new ArrayList<>();
        wantedBooksList.forEach((book) -> {
            List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
            double averageRating = 0;
            if (bookRatings.size() > 0) {
                for (Double rating : bookRatings) {
                    averageRating += rating;
                }
                averageRating /= bookRatings.size();
            }
            wantedBooksRatings.add(averageRating);
        });
        model.addAttribute("ratingsWanted", wantedBooksRatings);

        model.addAttribute("nReadBooks", nReadBooks);
        model.addAttribute("nReadingBooks", nReadingBooks);
        model.addAttribute("nWantedBooks", nWantedBooks);
        model.addAttribute("nReviews", nReviews);
        model.addAttribute("ReadBooks", readBooksList);
        model.addAttribute("ReadingBooks", readingBooksList);
        model.addAttribute("WantedBooks", wantedBooksList);

        //Admin
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        //Unregistered user
        model.addAttribute("noUser", !request.isUserInRole("USER"));


        return "profilePage";
    }

    @GetMapping("/profile/{username}/loadMore")
    public String loadReadBooks(@PathVariable String username, @RequestParam(defaultValue = "default") String listType, @RequestParam int page, @RequestParam int size, Model model) throws SQLException {
        switch (listType) {
            case "read" -> {
                List<Book> readBooksList = userRepository.getReadBooks(username, PageRequest.of(page, size)).getContent();

                for (int i = 0; i < readBooksList.size(); i++) {
                    readBooksList.get(i).setImageString(readBooksList.get(i).blobToString(readBooksList.get(i).getImageFile()));
                }

                model.addAttribute("bookItem", readBooksList);

                List<Double> readBooksRatings = new ArrayList<>();
                readBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    readBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", readBooksRatings);
            }
            case "reading" -> {
                List<Book> readingBooksList = userRepository.getReadingBooks(username, PageRequest.of(page, size)).getContent();

                for (int i = 0; i < readingBooksList.size(); i++) {
                    readingBooksList.get(i).setImageString(readingBooksList.get(i).blobToString(readingBooksList.get(i).getImageFile()));
                }

                model.addAttribute("bookItem", readingBooksList);

                List<Double> readingBooksRatings = new ArrayList<>();
                readingBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    readingBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", readingBooksRatings);
            }
            case "wanted" -> {
                List<Book> wantedBooksList = userRepository.getWantedBooks(username, PageRequest.of(page, size)).getContent();

                for (int i = 0; i < wantedBooksList.size(); i++) {
                    wantedBooksList.get(i).setImageString(wantedBooksList.get(i).blobToString(wantedBooksList.get(i).getImageFile()));
                }

                model.addAttribute("bookItem", wantedBooksList);

                List<Double> wantedBooksRatings = new ArrayList<>();
                wantedBooksList.forEach((book) -> {
                    List<Double> bookRatings = bookRepository.getRatingsByBookId(book.getID());
                    double averageRating = 0;
                    if (bookRatings.size() > 0) {
                        for (Double rating : bookRatings) {
                            averageRating += rating;
                        }
                        averageRating /= bookRatings.size();
                    }
                    wantedBooksRatings.add(averageRating);
                });
                model.addAttribute("ratings", wantedBooksRatings);
            }
        }

        return "bookListsItemTemplate";


    }

    @GetMapping("/profile/{username}/delete")
    public String deleteUser(Model model, @PathVariable String username, HttpServletRequest request) throws SQLException {
        Authentication authentication = (Authentication) request.getUserPrincipal();
        if (authentication != null) {
            String currentUsername = authentication.getName();
            User user = userRepository.findByUsername(currentUsername);
            if (user.getRole().contains("ADMIN")) {
                User deletedUser = userRepository.findByUsername(username);
                if (deletedUser != null) {
                    userRepository.delete(deletedUser);
                }
            } else {
                return "redirect:/login";
            }
        } else {
            return "redirect:/login";
        }
        return "redirect:/admin";
    }

    @GetMapping("/profile/{username}/convertAuthor")
    public String getMethodName(@PathVariable String username, HttpServletRequest request) {
        Authentication authentication = (Authentication) request.getUserPrincipal();
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName());
            if (user.getRole().contains("ADMIN")) {
                User userConverted = userRepository.findByUsername(username);
                userConverted.getRole().add("AUTHOR");
                userRepository.save(userConverted);
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
        return "redirect:/";
    }

    private String convertBooksToCSV(List<Book> books) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID,Title,Author\n"); // CSV header
        for (Book book : books) {
            builder.append(book.getID()).append(",");
            builder.append(book.getTitle()).append(",");
            builder.append(book.getAuthor()).append("\n");
        }
        return builder.toString();
    }

    @GetMapping("/profile/{username}/exportLists")
    public ResponseEntity<String> exportLists(@PathVariable String username, HttpServletRequest request) throws IOException {
        //How to convert each list of books to a CSV file and download it to the browser?
        User user = userRepository.findByUsername(username);

        String readBooksCSV = convertBooksToCSV(user.getReadBooks());
        String readingBooksCSV = convertBooksToCSV(user.getReadingBooks());
        String wantedBooksCSV = convertBooksToCSV(user.getWantedBooks());

        String allBooksCSV = "Read Books:\n" + readBooksCSV + "\nReading Books:\n" + readingBooksCSV + "\nWanted Books:\n" + wantedBooksCSV;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + username + "Lists.csv\"")
                .body(allBooksCSV);
    }

    @PostMapping("/profile/{username}/importLists")
    public String importLists(@RequestBody String file, HttpServletRequest request) {
        Authentication authentication = (Authentication) request.getUserPrincipal();
        String username = authentication.getName();

        String[] lines = file.split("\n");
        List<Book> readBooks = new ArrayList<>();
        List<Book> readingBooks = new ArrayList<>();
        List<Book> wantedBooks = new ArrayList<>();
        for (String line : lines) {
            String[] fields = line.split(",");
            Long bookId = Long.parseLong(fields[1]);
            Optional<Book> optionalBook = bookRepository.findById(bookId);
            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();
                switch (fields[0]) {
                    case "Read Books":
                        readBooks.add(book);
                        break;
                    case "Reading Books":
                        readingBooks.add(book);
                        break;
                    case "Wanted Books":
                        wantedBooks.add(book);
                        break;
                }
            }
        }
        System.out.println("Read Books: " + readBooks);

        // Retrieve the user from the database
        User user = userRepository.findByUsername(username);

        // Update the user's book lists with the new books
        user.getReadBooks().clear();
        user.getReadBooks().addAll(readBooks);
        user.getReadingBooks().clear();
        user.getReadingBooks().addAll(readingBooks);
        user.getWantedBooks().clear();
        user.getWantedBooks().addAll(wantedBooks);

        // Save the updated user back to the database
        userRepository.save(user);

        return "redirect:/profile/" + username;
    }

}