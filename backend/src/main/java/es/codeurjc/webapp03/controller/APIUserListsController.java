package es.codeurjc.webapp03.controller;

import com.fasterxml.jackson.annotation.JsonView;
import es.codeurjc.webapp03.entity.Book;
import es.codeurjc.webapp03.entity.Genre;
import es.codeurjc.webapp03.entity.User;
import es.codeurjc.webapp03.service.BookService;
import es.codeurjc.webapp03.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class APIUserListsController {

    // PUT will be used, understanding that what we are replacing are each list

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    // Book existence checker
    private boolean bookExists(long id) {
        return bookService.getBook(id) != null;
    }

    // Add book to read list
    @Operation(summary = "Add book to read list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book added to read list", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid book ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    })
    @JsonView(Book.BasicInfo.class)
    @PostMapping("/api/books/{id}/read")
    public ResponseEntity<Book> addReadBook(@PathVariable long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        // Check if book exists
        if (!bookExists(id)) {
            return ResponseEntity.notFound().build();
        }

        if (userService.checkLoggedIn(request)) {
            // Check that the id is valid
            if (id < 0) {
                return ResponseEntity.badRequest().build();
            }

            userService.moveBookToList(userService.getUser(principal.getName()), bookService.getBook(id), "read");
            return ResponseEntity.ok(bookService.getBook(id));
        } else { // If a user is not logged in
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Add book to reading list
    @Operation(summary = "Add book to reading list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book added to reading list", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid book ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    })
    @JsonView(Book.BasicInfo.class)
    @PostMapping("/api/books/{id}/reading")
    public ResponseEntity<Book> addReadingBook(@PathVariable long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        // Check if book exists
        if (!bookExists(id)) {
            return ResponseEntity.notFound().build();
        }

        if (userService.checkLoggedIn(request)) {
            if (id < 0) {
                return ResponseEntity.badRequest().build();
            }

            userService.moveBookToList(userService.getUser(principal.getName()), bookService.getBook(id), "reading");
            return ResponseEntity.ok(bookService.getBook(id));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Add book to wanted list
    @Operation(summary = "Add book to wanted list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book added to wanted list", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid book ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    })
    @JsonView(Book.BasicInfo.class)
    @PostMapping("/api/books/{id}/wanted")
    public ResponseEntity<Book> addWantedBook(@PathVariable long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        // Check if book exists
        if (!bookExists(id)) {
            return ResponseEntity.notFound().build();
        }

        if (userService.checkLoggedIn(request)) {
            if (id < 0) {
                return ResponseEntity.badRequest().build();
            }
            userService.moveBookToList(userService.getUser(principal.getName()), bookService.getBook(id), "wanted");
            return ResponseEntity.ok(bookService.getBook(id));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Remove book from all lists
    @Operation(summary = "Remove book from all lists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book removed from all lists", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid book ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    })

    @JsonView(Book.BasicInfo.class)
    @DeleteMapping("/api/books/{id}/lists")
    public ResponseEntity<Book> removeBookFromLists(@PathVariable long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        // Check if book exists
        if (!bookExists(id)) {
            return ResponseEntity.notFound().build();
        }

        if (userService.checkLoggedIn(request)) {
            if (id < 0) {
                return ResponseEntity.badRequest().build();
            }
            userService.removeBookFromAllLists(userService.getUser(principal.getName()), bookService.getBook(id));
            return ResponseEntity.ok(bookService.getBook(id));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    //Get a user's specific list
    @Operation(summary = "Get a user's specific list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid list")

    })
    @JsonView(Book.BasicInfo.class)
    @GetMapping("/api/users/{username}/books")
    public ResponseEntity<?> getUserLists(@PathVariable String username,
                                         @RequestParam("list") String list,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size) {

        // Check if the user exists
        if (userService.getUser(username) == null) {
            return ResponseEntity.notFound().build();
        }

        //Check if the list is valid
        if (!list.equals("read") && !list.equals("reading") && !list.equals("wanted")) {
            return ResponseEntity.badRequest().build();
        }

        List<Book> booksFromList = userService.getListPageable(userService.getUser(username), list, PageRequest.of(page, size));
        return ResponseEntity.ok(booksFromList);
    }

    // Get current user's specific list
    @Operation(summary = "Get current user's specific list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid list")

    })

    @JsonView(Book.BasicInfo.class)
    @GetMapping("/api/books/me")
    public ResponseEntity<?> getCurrentUserLists(HttpServletRequest request,
                                            @RequestParam("list") String list,
                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size){

            Principal principal = request.getUserPrincipal();

            if (principal == null) {
                return ResponseEntity.status(401).build();
            } else {
                //Check if the list is valid
                if (!list.equals("read") && !list.equals("reading") && !list.equals("wanted")) {
                    return ResponseEntity.badRequest().build();
                }
                List<Book> booksFromList = userService.getListPageable(userService.getUser(principal.getName()), list, PageRequest.of(page, size));
                return ResponseEntity.ok(booksFromList);
            }
    }


    // TODO: Move this to a different controller (this is for testing)
    // Get current user's info
    @Operation(summary = "Get current user's info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized")

    })
    @JsonView(User.BasicInfo.class)
    @GetMapping("/api/users/me")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(401).build();
        } else {
            return ResponseEntity.ok(userService.getUser(principal.getName()));
        }
    }
}
