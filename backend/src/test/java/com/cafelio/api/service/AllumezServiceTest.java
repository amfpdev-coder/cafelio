package com.cafelio.api.service;

import com.cafelio.api.dto.AllumezResponse;
import com.cafelio.api.model.AnnualGoal;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.AnnualGoalRepository;
import com.cafelio.api.repository.LibraryBookRepository;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllumezServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 21);
    private static final int ANO = 2026;

    @Mock
    private AnnualGoalRepository annualGoalRepository;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private UserRepository userRepository;

    private AllumezService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        Clock relogioParado = Clock.fixed(
                HOJE.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo")
        );

        service = new AllumezService(
                annualGoalRepository,
                libraryBookRepository,
                userRepository,
                relogioParado
        );
    }

    @Test
    void consultar_semMetaDefinida_metaZero() {
        quandoBuscarUsuaria();
        when(annualGoalRepository.findByUserAndYear(user, ANO)).thenReturn(Optional.empty());

        AllumezResponse resposta = service.consultar(userId);

        assertThat(resposta.getAno()).isEqualTo(ANO);
        assertThat(resposta.getMeta()).isZero();
    }

    @Test
    void consultar_comMeta_devolveMetaEConcluidos() {
        quandoBuscarUsuaria();
        quandoAMetaFor(10);
        quandoOsConcluidosForem(3);

        AllumezResponse resposta = service.consultar(userId);

        assertThat(resposta.getMeta()).isEqualTo(10);
        assertThat(resposta.getConcluidos()).isEqualTo(3);
    }

    @Test
    void consultar_contaDeJaneiroADezembroDoAnoCorrente() {
        quandoBuscarUsuaria();
        when(annualGoalRepository.findByUserAndYear(user, ANO)).thenReturn(Optional.empty());

        service.consultar(userId);

        verify(libraryBookRepository).countByUserAndStatusAndFinishedAtBetween(
                user,
                ReadingStatus.READ,
                LocalDate.of(ANO, 1, 1),
                LocalDate.of(ANO, 12, 31)
        );
    }

    @Test
    void definirMeta_primeiraVez_salvaComAQuantidadePedida() {
        quandoBuscarUsuaria();
        when(annualGoalRepository.findByUserAndYear(user, ANO)).thenReturn(Optional.empty());

        service.definirMeta(userId, 12);

        ArgumentCaptor<AnnualGoal> salva = ArgumentCaptor.forClass(AnnualGoal.class);
        verify(annualGoalRepository).save(salva.capture());

        assertThat(salva.getValue().getTarget()).isEqualTo(12);
        assertThat(salva.getValue().getYear()).isEqualTo(ANO);
    }

    @Test
    void definirMeta_jaExistia_atualizaSemCriarOutra() {
        quandoBuscarUsuaria();
        AnnualGoal existente = quandoAMetaFor(5);

        service.definirMeta(userId, 20);

        assertThat(existente.getTarget()).isEqualTo(20);
        verify(annualGoalRepository).save(existente);
    }

    @Test
    void definirMeta_zero_lancaExcecao() {
        assertThatThrownBy(() -> service.definirMeta(userId, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A meta precisa ser entre 1 e 365 livros");

        verify(annualGoalRepository, never()).save(any());
    }

    @Test
    void definirMeta_acimaDoMaximo_lancaExcecao() {
        assertThatThrownBy(() -> service.definirMeta(userId, 366))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A meta precisa ser entre 1 e 365 livros");

        verify(annualGoalRepository, never()).save(any());
    }

    private void quandoBuscarUsuaria() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private AnnualGoal quandoAMetaFor(int quantidade) {
        AnnualGoal meta = new AnnualGoal(user, ANO, quantidade);
        when(annualGoalRepository.findByUserAndYear(user, ANO)).thenReturn(Optional.of(meta));
        return meta;
    }

    private void quandoOsConcluidosForem(int quantidade) {
        when(libraryBookRepository.countByUserAndStatusAndFinishedAtBetween(
                any(), any(), any(), any()
        )).thenReturn(quantidade);
    }
}
