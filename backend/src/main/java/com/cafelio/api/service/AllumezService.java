package com.cafelio.api.service;

import com.cafelio.api.dto.AllumezResponse;
import com.cafelio.api.model.AnnualGoal;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.AnnualGoalRepository;
import com.cafelio.api.repository.LibraryBookRepository;
import com.cafelio.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AllumezService {

    private static final int META_MAXIMA = 365;

    private final AnnualGoalRepository annualGoalRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AllumezService(
            AnnualGoalRepository annualGoalRepository,
            LibraryBookRepository libraryBookRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.annualGoalRepository = annualGoalRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public AllumezResponse consultar(UUID userId) {
        User user = buscar(userId);
        int ano = ano();

        int meta = annualGoalRepository.findByUserAndYear(user, ano)
                .map(registro -> registro.getTarget())
                .orElse(0);

        return new AllumezResponse(ano, meta, concluidosNoAno(user, ano));
    }

    @Transactional
    public AllumezResponse definirMeta(UUID userId, int quantidade) {
        if (quantidade < 1 || quantidade > META_MAXIMA) {
            throw new IllegalArgumentException(
                    "A meta precisa ser entre 1 e " + META_MAXIMA + " livros"
            );
        }

        User user = buscar(userId);
        int ano = ano();

        AnnualGoal meta = annualGoalRepository.findByUserAndYear(user, ano)
                .orElseGet(() -> new AnnualGoal(user, ano, quantidade));

        meta.setTarget(quantidade);
        annualGoalRepository.save(meta);

        return consultar(userId);
    }

    private int concluidosNoAno(User user, int ano) {
        return libraryBookRepository.countByUserAndStatusAndFinishedAtBetween(
                user,
                ReadingStatus.READ,
                LocalDate.of(ano, 1, 1),
                LocalDate.of(ano, 12, 31)
        );
    }

    private int ano() {
        return LocalDate.now(clock).getYear();
    }

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
