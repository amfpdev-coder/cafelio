package com.cafelio.api.service;

import com.cafelio.api.dto.FlammeResponse;
import com.cafelio.api.model.ReadingDay;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.ReadingDayRepository;
import com.cafelio.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FlammeService {

    private static final int DIAS_NA_GRADE = 182;

    private final ReadingDayRepository readingDayRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public FlammeService(
            ReadingDayRepository readingDayRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.readingDayRepository = readingDayRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }


    @Transactional
    public void marcarHoje(UUID userId) {
        marcar(userId, hoje());
    }

    @Transactional
    public void marcar(UUID userId, LocalDate dia) {
        if (dia.isAfter(hoje())) {
            throw new IllegalArgumentException("Não é possível alterar um dia futuro");
        }

        User user = buscar(userId);

        if (readingDayRepository.existsByUserAndDay(user, dia)) {
            return;
        }

        readingDayRepository.save(new ReadingDay(user, dia));
    }

    @Transactional
    public void desmarcar(UUID userId, LocalDate dia) {
        if (dia.isAfter(hoje())) {
            throw new IllegalArgumentException("Não é possível alterar um dia futuro");
        }

        readingDayRepository.deleteByUserAndDay(buscar(userId), dia);
    }

    public FlammeResponse consultar(UUID userId) {
        User user = buscar(userId);
        LocalDate hoje = hoje();

        List<LocalDate> todos = readingDayRepository.findByUserOrderByDayDesc(user)
                .stream()
                .map(dia -> dia.getDay())
                .toList();

        Set<LocalDate> marcados = new HashSet<>(todos);
        LocalDate inicioDaGrade = hoje.minusDays(DIAS_NA_GRADE - 1L);

        List<LocalDate> grade = todos.stream()
                .filter(dia -> !dia.isBefore(inicioDaGrade))
                .toList();

        return new FlammeResponse(
                sequenciaAtual(marcados, hoje),
                maiorSequencia(todos),
                todos.size(),
                grade
        );
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }

    private int sequenciaAtual(Set<LocalDate> marcados, LocalDate hoje) {
        LocalDate dia = marcados.contains(hoje) ? hoje : hoje.minusDays(1);
        int sequencia = 0;

        while (marcados.contains(dia)) {
            sequencia++;
            dia = dia.minusDays(1);
        }

        return sequencia;
    }

    private int maiorSequencia(List<LocalDate> diasDecrescente) {
        int maior = 0;
        int atual = 0;
        LocalDate anterior = null;

        for (LocalDate dia : diasDecrescente) {
            atual = (anterior != null && anterior.minusDays(1).equals(dia)) ? atual + 1 : 1;
            maior = Math.max(maior, atual);
            anterior = dia;
        }

        return maior;
    }

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}