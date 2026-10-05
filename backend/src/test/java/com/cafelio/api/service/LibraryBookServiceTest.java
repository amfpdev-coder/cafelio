package com.cafelio.api.service;

import com.cafelio.api.dto.request.LibraryBookCreateRequest;
import com.cafelio.api.dto.request.LibraryBookUpdateRequest;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.exception.LibraryBookAlreadyExistsException;
import com.cafelio.api.exception.LibraryBookNotFoundException;
import com.cafelio.api.exception.NoReadingGoalBooksException;
import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.LibraryBookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibraryBookServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    User user;

    @InjectMocks
    private LibraryBookService libraryBookService;

    @Test
    void deveCriarLivroQuandoNaoExisteDuplicidade(){
        UUID userId = UUID.randomUUID();
        when(authService.findById(userId)).thenReturn(user);

        LibraryBookCreateRequest request = new LibraryBookCreateRequest(
                "0L123M",
                "Livro Teste",
                2026,
                "https://capa-teste.com",
                ReadingStatus.WANT_TO_READ,
                List.of("Autor Teste"),
                Set.of(BookTag.WANTED)
        );


        when(libraryBookRepository.existsByUserAndOpenLibraryId(user, request.openLibraryId())).thenReturn(false);

        LibraryBook savedBook = new LibraryBook(
                user,
                request.openLibraryId(),
                request.title(),
                request.firstPublishYear(),
                request.coverUrl(),
                request.status(),
                request.authors(),
                request.tags()
        );

        when(libraryBookRepository.save(any(LibraryBook.class))).thenReturn(savedBook);

        LibraryBookResponse response = libraryBookService.createLibraryBook(request, userId);

        assertNotNull(response);
        assertEquals(request.title(), response.title());
        assertEquals(request.openLibraryId(), response.openLibraryId());
        assertEquals(request.firstPublishYear(), response.firstPublishYear());
        assertEquals(request.status(), response.status());
        assertEquals(request.authors(), response.authors());
        assertEquals(request.tags(), response.tags());

        verify(libraryBookRepository, times(1))
                .save(any(LibraryBook.class));
    }

    @Test
    void deveLancarExcecaoQuandoLivroJaExiste(){
        UUID userId = UUID.randomUUID();
        when(authService.findById(userId)).thenReturn(user);

        LibraryBookCreateRequest request = new LibraryBookCreateRequest(
                "0L123M",
                "Livro Teste",
                2026,
                "https://capa-teste.com",
                ReadingStatus.WANT_TO_READ,
                List.of("Autor Teste"),
                Set.of(BookTag.WANTED)
        );

        when(libraryBookRepository.existsByUserAndOpenLibraryId(
                user,
                request.openLibraryId()
        )).thenReturn(true);

        LibraryBookAlreadyExistsException exception = assertThrows(
                LibraryBookAlreadyExistsException.class,
                () -> libraryBookService.createLibraryBook(request, userId)
        );

        assertEquals(
                "Livro já adicionado na biblioteca",
                exception.getMessage()
        );

        verify(libraryBookRepository, never())
                .save(any(LibraryBook.class));
    }

    @Test
    void deveLancarExcecaoQuandoLivroNaoPertenceAoUsuario() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByIdAndUser(bookId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                LibraryBookNotFoundException.class,
                () -> libraryBookService.getById(bookId, userId)
        );

        verify(libraryBookRepository, times(1))
                .findByIdAndUser(bookId, user);
    }

    @Test
    void deveAtualizarSomenteStatus() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LibraryBook book = new LibraryBook(
                user,
                "OL123",
                "Livro Teste",
                2020,
                "capa",
                ReadingStatus.READING,
                List.of("Autor"),
                Set.of(BookTag.FAVORITE)
        );

        LibraryBookUpdateRequest request =
                new LibraryBookUpdateRequest(
                        ReadingStatus.READ,
                        null
                );

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByIdAndUser(bookId, user))
                .thenReturn(Optional.of(book));

        when(libraryBookRepository.save(any(LibraryBook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LibraryBookResponse response =
                libraryBookService.updateBook(bookId, userId, request);

        assertEquals(ReadingStatus.READ, response.status());
        assertEquals(Set.of(BookTag.FAVORITE), response.tags());

        verify(libraryBookRepository, times(1)).save(book);
    }

    @Test
    void deveAtualizarSomenteTags() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LibraryBook book = new LibraryBook(
                user,
                "OL123",
                "Livro Teste",
                2020,
                "capa",
                ReadingStatus.READING,
                List.of("Autor"),
                Set.of(BookTag.FAVORITE)
        );

        LibraryBookUpdateRequest request =
                new LibraryBookUpdateRequest(
                        null,
                        Set.of(BookTag.WANTED)
                );

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByIdAndUser(bookId, user))
                .thenReturn(Optional.of(book));

        when(libraryBookRepository.save(any(LibraryBook.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LibraryBookResponse response =
                libraryBookService.updateBook(bookId, userId, request);

        assertEquals(ReadingStatus.READING, response.status());
        assertEquals(Set.of(BookTag.WANTED), response.tags());
    }

    @Test
    void deveExcluirLivroExistente() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LibraryBook book = new LibraryBook(
                user,
                "OL123",
                "Livro Teste",
                2020,
                "capa",
                ReadingStatus.READING,
                List.of("Autor"),
                Set.of(BookTag.FAVORITE)
        );

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByIdAndUser(bookId, user))
                .thenReturn(Optional.of(book));

        libraryBookService.deleteBook(bookId, userId);

        verify(libraryBookRepository, times(1))
                .delete(book);
    }

    @Test
    void deveSortearLivroDaMetaLiteraria() {
        UUID userId = UUID.randomUUID();

        LibraryBook book = new LibraryBook(
                user,
                "OL123",
                "Dom Casmurro",
                1899,
                "capa",
                ReadingStatus.READING_GOAL,
                List.of("Machado de Assis"),
                Set.of(BookTag.WANTED)
        );

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByUserAndStatus(
                user,
                ReadingStatus.READING_GOAL
        )).thenReturn(List.of(book));

        LibraryBookResponse response =
                libraryBookService.readingGoal(userId);

        assertNotNull(response);
        assertEquals("Dom Casmurro", response.title());
        assertEquals(ReadingStatus.READING_GOAL, response.status());
    }

    @Test
    void deveLancarExcecaoQuandoMetaLiterariaEstiverVazia() {
        UUID userId = UUID.randomUUID();

        when(authService.findById(userId)).thenReturn(user);

        when(libraryBookRepository.findByUserAndStatus(
                user,
                ReadingStatus.READING_GOAL
        )).thenReturn(List.of());

        assertThrows(
                NoReadingGoalBooksException.class,
                () -> libraryBookService.readingGoal(userId)
        );
    }
}
