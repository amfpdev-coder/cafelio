package com.cafelio.api.service;

import com.cafelio.api.dto.HomeResponse;
import com.cafelio.api.model.BookTag;
import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.LibraryBookRepository;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 21);

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private UserRepository userRepository;

    private HomeService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        Clock relogioParado = Clock.fixed(
                HOJE.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo")
        );

        service = new HomeService(libraryBookRepository, userRepository, relogioParado);
    }

    @Test
    void consultar_somaDoAno_vaiDePrimeiroDeJaneiroAteHoje() {
        quandoBuscarUsuaria();

        service.consultar(userId);

        verify(libraryBookRepository).somarPaginas(
                user,
                ReadingStatus.READ,
                LocalDate.of(2026, 1, 1),
                HOJE
        );
    }

    @Test
    void consultar_somaDaSemana_pegaSeteDiasComHojeIncluso() {
        quandoBuscarUsuaria();

        service.consultar(userId);

        // 15 a 21 de outubro são sete dias, não oito
        verify(libraryBookRepository).somarPaginas(
                user,
                ReadingStatus.READ,
                LocalDate.of(2026, 10, 15),
                HOJE
        );
    }

    @Test
    void consultar_devolveOsNumerosEOAno() {
        quandoBuscarUsuaria();

        when(libraryBookRepository.somarPaginas(any(), any(), eq(LocalDate.of(2026, 1, 1)), any()))
                .thenReturn(4286L);
        when(libraryBookRepository.somarPaginas(any(), any(), eq(LocalDate.of(2026, 10, 15)), any()))
                .thenReturn(312L);

        HomeResponse resposta = service.consultar(userId);

        assertThat(resposta.ano()).isEqualTo(2026);
        assertThat(resposta.paginasNoAno()).isEqualTo(4286);
        assertThat(resposta.paginasNaSemana()).isEqualTo(312);
    }

    @Test
    void consultar_lendoAgora_trazOsLivrosEmLeitura() {
        quandoBuscarUsuaria();

        LibraryBook livro = new LibraryBook(
                user, "OL1", "Torto Arado", 2019, "capa",
                ReadingStatus.READING, List.of("Itamar Vieira Junior"), Set.of(BookTag.FAVORITE), 264
        );

        when(libraryBookRepository.findByUserAndStatus(user, ReadingStatus.READING))
                .thenReturn(List.of(livro));

        HomeResponse resposta = service.consultar(userId);

        assertThat(resposta.lendoAgora()).hasSize(1);
        assertThat(resposta.lendoAgora().get(0).title()).isEqualTo("Torto Arado");
        assertThat(resposta.lendoAgora().get(0).pages()).isEqualTo(264);
    }

    @Test
    void consultar_semNadaLendo_devolveListaVazia() {
        quandoBuscarUsuaria();

        HomeResponse resposta = service.consultar(userId);

        assertThat(resposta.lendoAgora()).isEmpty();
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
}
