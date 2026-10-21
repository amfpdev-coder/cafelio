package com.cafelio.api.service;

import com.cafelio.api.model.EmailChangeRequest;
import com.cafelio.api.model.User;
import com.cafelio.api.repository.EmailChangeRequestRepository;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailChangeServiceTest {

    private static final String ATUAL = "atual@email.com";
    private static final String NOVO = "novo@email.com";
    private static final String CODIGO = "123456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailChangeRequestRepository requestRepository;

    @Mock
    private EmailService emailService;

    private EmailChangeService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        user.setEmail(ATUAL);
        service = new EmailChangeService(userRepository, requestRepository, emailService);
    }

    // ---- pedirTroca ----

    @Test
    void pedirTroca_emailLivre_mandaOCodigoParaOEnderecoAtual() {
        quandoBuscarUsuaria();
        when(userRepository.existsByEmail(NOVO)).thenReturn(false);

        service.pedirTroca(userId, "  Novo@Email.com  ");

        // O código prova que quem pede é a dona da conta, então vai para o
        // endereço de hoje — nunca para o que ela quer passar a usar.
        verify(emailService).enviarConfirmacaoDeEmail(eq(ATUAL), eq(NOVO), anyString());
    }

    @Test
    void pedirTroca_apagaOPedidoAnteriorAntesDeGravarONovo() {
        quandoBuscarUsuaria();
        when(userRepository.existsByEmail(NOVO)).thenReturn(false);

        service.pedirTroca(userId, NOVO);

        // user_id é único nessa tabela: sem o flush, o Hibernate mandaria o
        // insert antes do delete e esbarraria no pedido velho.
        InOrder ordem = inOrder(requestRepository);

        ordem.verify(requestRepository).deleteByUser(user);
        ordem.verify(requestRepository).flush();
        ordem.verify(requestRepository).save(any(EmailChangeRequest.class));
    }

    @Test
    void pedirTroca_mesmoEmail_recusa() {
        quandoBuscarUsuaria();

        assertThatThrownBy(() -> service.pedirTroca(userId, "ATUAL@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esse já é o seu e-mail");

        verify(requestRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void pedirTroca_emailDeOutraPessoa_recusa() {
        quandoBuscarUsuaria();
        when(userRepository.existsByEmail(NOVO)).thenReturn(true);

        assertThatThrownBy(() -> service.pedirTroca(userId, NOVO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esse e-mail já está em uso");

        verify(requestRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // ---- confirmar ----

    @Test
    void confirmar_codigoCerto_trocaOEmailEAvisaOAntigo() {
        quandoBuscarUsuaria();
        quandoHouverPedido(pedido(CODIGO));
        when(userRepository.existsByEmail(NOVO)).thenReturn(false);

        service.confirmar(userId, CODIGO);

        assertThat(user.getEmail()).isEqualTo(NOVO);
        verify(userRepository).save(user);
        verify(emailService).avisarTrocaDeEmail(ATUAL, NOVO);
    }

    @Test
    void confirmar_codigoCerto_apagaOPedido() {
        quandoBuscarUsuaria();
        EmailChangeRequest pedido = pedido(CODIGO);
        quandoHouverPedido(pedido);
        when(userRepository.existsByEmail(NOVO)).thenReturn(false);

        service.confirmar(userId, CODIGO);

        verify(requestRepository).delete(pedido);
    }

    @Test
    void confirmar_codigoErrado_contaATentativaENaoTroca() {
        quandoBuscarUsuaria();
        EmailChangeRequest pedido = pedido(CODIGO);
        quandoHouverPedido(pedido);

        assertThatThrownBy(() -> service.confirmar(userId, "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido");

        assertThat(pedido.getAttempts()).isEqualTo(1);
        assertThat(user.getEmail()).isEqualTo(ATUAL);
        verify(requestRepository).save(pedido);
    }

    @Test
    void confirmar_pedidoExpirado_apagaERecusa() {
        quandoBuscarUsuaria();
        EmailChangeRequest pedido = pedido(CODIGO);
        pedido.setExpiresAt(Instant.now().minusSeconds(1));
        quandoHouverPedido(pedido);

        assertThatThrownBy(() -> service.confirmar(userId, CODIGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O código expirou. Peça um novo.");

        verify(requestRepository).delete(pedido);
        assertThat(user.getEmail()).isEqualTo(ATUAL);
    }

    @Test
    void confirmar_cincoTentativasGastas_apagaERecusa() {
        quandoBuscarUsuaria();
        EmailChangeRequest pedido = pedido(CODIGO);
        pedido.setAttempts(EmailChangeRequest.MAX_TENTATIVAS);
        quandoHouverPedido(pedido);

        assertThatThrownBy(() -> service.confirmar(userId, CODIGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Muitas tentativas. Peça um novo código.");

        verify(requestRepository).delete(pedido);
    }

    @Test
    void confirmar_semPedidoPendente_recusa() {
        quandoBuscarUsuaria();
        when(requestRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmar(userId, CODIGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nenhuma troca de e-mail pendente");
    }

    @Test
    void confirmar_emailTomadoNoMeioTempo_apagaERecusa() {
        quandoBuscarUsuaria();
        EmailChangeRequest pedido = pedido(CODIGO);
        quandoHouverPedido(pedido);

        // Entre o pedido e a confirmação passam até 30 minutos: nesse
        // intervalo outra pessoa pode ter se cadastrado com o endereço.
        when(userRepository.existsByEmail(NOVO)).thenReturn(true);

        assertThatThrownBy(() -> service.confirmar(userId, CODIGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esse e-mail já está em uso");

        verify(requestRepository).delete(pedido);
        assertThat(user.getEmail()).isEqualTo(ATUAL);
    }

    // ---- auxiliares ----

    private void quandoBuscarUsuaria() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void quandoHouverPedido(EmailChangeRequest pedido) {
        when(requestRepository.findByUser(user)).thenReturn(Optional.of(pedido));
    }

    private EmailChangeRequest pedido(String codigo) {
        EmailChangeRequest pedido = new EmailChangeRequest();

        pedido.setUser(user);
        pedido.setNewEmail(NOVO);
        pedido.setCodeHash(hash(codigo));
        pedido.setExpiresAt(Instant.now().plusSeconds(600));

        return pedido;
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
