package com.cafelio.api.service;

import com.cafelio.api.dto.FlammeResponse;
import com.cafelio.api.model.ReadingDay;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.ReadingDayRepository;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlammeServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 17);

    @Mock
    private ReadingDayRepository readingDayRepository;

    @Mock
    private UserRepository userRepository;

    private FlammeService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        Clock relogioParado = Clock.fixed(
                HOJE.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo")
        );

        service = new FlammeService(readingDayRepository, userRepository, relogioParado);
    }

    @Test
    void marcarHoje_primeiraVez_salvaODia() {
        quandoBuscarUsuaria();
        when(readingDayRepository.existsByUserAndDay(user, HOJE)).thenReturn(false);

        service.marcarHoje(userId);

        verify(readingDayRepository).save(any(ReadingDay.class));
    }

    @Test
    void marcarHoje_segundaVezNoMesmoDia_naoSalvaDeNovo() {
        quandoBuscarUsuaria();
        when(readingDayRepository.existsByUserAndDay(user, HOJE)).thenReturn(true);

        service.marcarHoje(userId);

        verify(readingDayRepository, never()).save(any());
    }

    @Test
    void consultar_semNenhumDia_sequenciaZero() {
        quandoBuscarUsuaria();
        quandoOsDiasForem();

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getSequenciaAtual()).isZero();
        assertThat(resposta.getMaiorSequencia()).isZero();
        assertThat(resposta.getTotalDeDias()).isZero();
    }

    @Test
    void consultar_tresDiasSeguidosAteHoje_sequenciaTres() {
        quandoBuscarUsuaria();
        quandoOsDiasForem(HOJE, HOJE.minusDays(1), HOJE.minusDays(2));

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getSequenciaAtual()).isEqualTo(3);
    }

    @Test
    void consultar_leuOntemMasAindaNaoHoje_sequenciaContinuaViva() {
        quandoBuscarUsuaria();
        quandoOsDiasForem(HOJE.minusDays(1), HOJE.minusDays(2));

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getSequenciaAtual()).isEqualTo(2);
    }

    @Test
    void consultar_ultimaLeituraAnteontem_sequenciaZerada() {
        quandoBuscarUsuaria();
        quandoOsDiasForem(HOJE.minusDays(2), HOJE.minusDays(3));

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getSequenciaAtual()).isZero();
    }

    @Test
    void consultar_comBuraco_maiorSequenciaIgnoraOBuraco() {
        quandoBuscarUsuaria();
        quandoOsDiasForem(
                HOJE,
                HOJE.minusDays(5), HOJE.minusDays(6), HOJE.minusDays(7), HOJE.minusDays(8)
        );

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getSequenciaAtual()).isEqualTo(1);
        assertThat(resposta.getMaiorSequencia()).isEqualTo(4);
        assertThat(resposta.getTotalDeDias()).isEqualTo(5);
    }

    @Test
    void consultar_diaMuitoAntigo_contaNoTotalMasFicaForaDaGrade() {
        quandoBuscarUsuaria();
        quandoOsDiasForem(HOJE, HOJE.minusDays(300));

        FlammeResponse resposta = service.consultar(userId);

        assertThat(resposta.getTotalDeDias()).isEqualTo(2);
        assertThat(resposta.getDias()).containsExactly(HOJE);
    }

    @Test
    void consultar_usuariaInexistente_lancaExcecao() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultar(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado");
    }

    private void quandoBuscarUsuaria() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void quandoOsDiasForem(LocalDate... dias) {
        List<ReadingDay> registros = Arrays.stream(dias)
                .map(dia -> new ReadingDay(user, dia))
                .toList();

        when(readingDayRepository.findByUserOrderByDayDesc(user)).thenReturn(registros);
    }
}