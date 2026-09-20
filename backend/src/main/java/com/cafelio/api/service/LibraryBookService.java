package com.cafelio.api.service;

import com.cafelio.api.dto.request.LibraryBookCreateRequest;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.exception.LibraryBookAlreadyExistsException;
import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.LibraryBookRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LibraryBookService {

    private final AuthService authService;
    private final LibraryBookRepository libraryBookRepository;

    public LibraryBookService(AuthService authService, LibraryBookRepository libraryBookRepository){
        this.authService = authService;
        this.libraryBookRepository = libraryBookRepository;
    }

    public LibraryBookResponse createLibraryBook(LibraryBookCreateRequest book){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        UUID userId = (UUID) principal;
        User user = authService.findById(userId);

        if(libraryBookRepository.existsByUserAndOpenLibraryId(user, book.openLibraryId())){
            throw new LibraryBookAlreadyExistsException("Livro já adicionado na biblioteca");
        }

        LibraryBook libraryBook = new LibraryBook(user, book.openLibraryId(), book.title(), book.firstPublishYear(), book.coverUrl(), book.status(),book.authors(), book.tags());
        LibraryBook savedBook = libraryBookRepository.save(libraryBook);
        LibraryBookResponse libraryBookResponse = new LibraryBookResponse(savedBook.getId(), savedBook.getOpenLibraryId(), savedBook.getTitle(), savedBook.getFirstPublishYear(), savedBook.getCoverUrl(), savedBook.getStatus(), savedBook.getAuthors(), savedBook.getTags());

        return libraryBookResponse;
    }

    public List<LibraryBookResponse> listBooks(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        UUID userId = (UUID) principal;
        User user = authService.findById(userId);

        List<LibraryBook> listOfBooks = libraryBookRepository.findByUser(user);

        List<LibraryBookResponse> list = listOfBooks.stream().map(book -> new LibraryBookResponse(
                book.getId(),
                book.getOpenLibraryId(),
                book.getTitle(),
                book.getFirstPublishYear(),
                book.getCoverUrl(),
                book.getStatus(),
                book.getAuthors(),
                book.getTags()
        )).toList();

        return list;
    }
}
