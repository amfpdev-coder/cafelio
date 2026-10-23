package com.cafelio.api.service;

import com.cafelio.api.dto.HomeResponse;
import com.cafelio.api.dto.response.LibraryBookResponse;
import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.LibraryBookRepository;
import com.cafelio.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class HomeService {

    private static final int DIAS_DA_SEMANA = 7;

    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public HomeService(
            LibraryBookRepository libraryBookRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.libraryBookRepository = libraryBookRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public HomeResponse consultar(UUID userId) {
        User user = buscar(userId);
        LocalDate hoje = LocalDate.now(clock);

        long noAno = libraryBookRepository.somarPaginas(
                user,
                ReadingStatus.READ,
                hoje.withDayOfYear(1),
                hoje
        );

        // A semana é a janela dos últimos sete dias, hoje incluído
        long naSemana = libraryBookRepository.somarPaginas(
                user,
                ReadingStatus.READ,
                hoje.minusDays(DIAS_DA_SEMANA - 1L),
                hoje
        );

        List<LibraryBookResponse> lendoAgora = libraryBookRepository
                .findByUserAndStatus(user, ReadingStatus.READING)
                .stream()
                .map(this::paraResposta)
                .toList();

        return new HomeResponse(hoje.getYear(), noAno, naSemana, lendoAgora);
    }

    private LibraryBookResponse paraResposta(LibraryBook livro) {
        return new LibraryBookResponse(
                livro.getId(),
                livro.getOpenLibraryId(),
                livro.getTitle(),
                livro.getFirstPublishYear(),
                livro.getCoverUrl(),
                livro.getStatus(),
                livro.getAuthors(),
                livro.getTags(),
                livro.getPages(),
                livro.getCreatedAt()
        );
    }

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
