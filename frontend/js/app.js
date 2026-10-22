// ---- Destinos da casca ----

const DESTINOS_PRINCIPAIS = [
    { id: "inicio", icone: "house", rotulo: "Início" },
    { id: "estante", icone: "books", rotulo: "Estante" },
    { id: "cadernos", icone: "notebook", rotulo: "Cadernos" },
    { id: "desafios", icone: "medal", rotulo: "Desafios" },
];

const DESTINOS_EXPLORAR = [
    { id: "citacoes", icone: "quotes", rotulo: "Citações" },
    { id: "resenhas", icone: "chat-text", rotulo: "Resenhas" },
    { id: "ranking", icone: "ranking", rotulo: "Ranking" },
    { id: "estatisticas", icone: "chart-bar", rotulo: "Estatísticas" },
    { id: "calendario", icone: "calendar-blank", rotulo: "Calendário" },
    { id: "sorteio", icone: "shuffle", rotulo: "Sorteio" },
];

const DESTINO_PERFIL = { id: "perfil", icone: "user-circle", rotulo: "Perfil" };

let usuarioAtual = null;
let destinoAtual = "inicio";

// ---- Tema claro / escuro ----

const TEMA_KEY = "cafelio_tema";

function temaSalvo() {
    try {
        return localStorage.getItem(TEMA_KEY);
    } catch (error) {
        return null;
    }
}

function temaDoSistema() {
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

function aplicarTema(tema) {
    document.documentElement.dataset.theme = tema;

    try {
        localStorage.setItem(TEMA_KEY, tema);
    } catch (error) {
        // Navegação privada bloqueia a gravação; o tema ainda vale nesta sessão.
    }
}

function alternarTema() {
    aplicarTema(document.documentElement.dataset.theme === "dark" ? "light" : "dark");
    renderCasca();
}

function botaoDeTema(classe) {
    const escuro = document.documentElement.dataset.theme === "dark";

    return `
        <button type="button" class="${classe}" data-tema="alternar">
            <i class="ph-light ph-${escuro ? "sun" : "moon"}"></i>
            <span>${escuro ? "Modo claro" : "Modo escuro"}</span>
        </button>
    `;
}

aplicarTema(temaSalvo() || temaDoSistema());

async function renderWelcomeScreen() {
    const app = document.getElementById("app");

    app.innerHTML = `<div id="auth-area"></div>`;

    await loadSession();

    try {
        const health = await apiGet("/health");
        console.info(`Cafélio: API conectada — status: ${health.status}`);
    } catch (error) {
        console.warn("Cafélio: não foi possível conectar à API.");
    }
}

async function loadSession() {
    const tokenDoLink = new URLSearchParams(window.location.search).get("token");

    if (tokenDoLink) {
        renderResetPasswordForm(tokenDoLink);
        return;
    }

    if (!getToken()) {
        renderBoasVindas();
        return;
    }

    try {
        const user = await apiGet("/auth/me");
        renderLoggedInArea(user);
    } catch (error) {
        renderLoginForm();
    }
}

function showGoogleButton(visible) {
    const button = document.querySelector(".g_id_signin");

    if (!button) {
        return;
    }

    // O botão do Google é criado uma única vez pela biblioteca deles. Como a
    // tela de entrada o aloja dentro do formulário, ele precisa voltar para o
    // corpo da página antes de cada troca de tela — senão o innerHTML seguinte
    // o destruiria e não haveria como recriá-lo.
    document.body.appendChild(button);
    button.style.display = visible ? "" : "none";
}

function moverBotaoDoGoogle(slotId = "google-slot") {
    const slot = document.getElementById(slotId);
    const button = document.querySelector(".g_id_signin");

    if (slot && button) {
        slot.appendChild(button);
    }
}

// ---- Tela de boas-vindas ----

const SLIDES = [
    {
        icone: "books",
        imagem: "img/estante.jpg",
        titulo: "Sua estante",
        texto: "Organize o que você leu, está lendo e quer ler — com etiquetas e seis status.",
    },
    {
        icone: "notebook",
        imagem: "img/livros-abertos.jpg",
        titulo: "Cadernos literários",
        texto: "Guarde citações, resenhas e anotações em cadernos com capa e tema próprios.",
    },
    {
        icone: "medal",
        imagem: "img/leitura-no-sofa.jpg",
        titulo: "Desafios",
        texto: "Acenda uma vela a cada livro concluído e mantenha sua sequência de leitura viva.",
    },
    {
        icone: "chart-bar",
        imagem: "img/cafe-e-livro.jpg",
        titulo: "Seu ano em números",
        texto: "Gêneros, páginas, humores e um relatório anual para compartilhar.",
    },
];

let slideAtual = 0;
let slideTimer = null;

function renderBoasVindas() {
    showGoogleButton(false);
    slideAtual = 0;

    const pontos = SLIDES
        .map((_, i) => `<button type="button" class="ponto" data-slide="${i}" aria-label="slide ${i + 1}"></button>`)
        .join("");

    document.getElementById("auth-area").innerHTML = `
        <div class="hero">
            <div class="hero-foto" id="hero-foto"></div>
            <div class="hero-veu"></div>

            <div class="hero-conteudo">
                <div class="hero-topo">
                    <div class="hero-marca">
                        <i class="ph-light ph-coffee"></i>
                        <span>Cafélio</span>
                    </div>
                    <p class="hero-frase">seu diário de leituras,<br />guardado entre um café e outro</p>
                </div>

                <div class="hero-base">
                    <div class="hero-slide" id="slide"></div>

                    <div class="hero-pontos" id="pontos">${pontos}</div>

                    <div class="hero-acoes">
                        <button type="button" id="ir-entrar">Entrar<i class="ph-light ph-arrow-right"></i></button>
                        <p>Ainda não tem conta? <a href="#" id="ir-criar">Criar conta</a></p>
                    </div>

                    <p class="hero-credito">
                        Feito a quatro mãos por
                        <a href="https://www.linkedin.com/in/amfpdev" target="_blank" rel="noopener">@amfpdev</a>
                        e
                        <a href="https://www.linkedin.com/in/amanda-cristiny" target="_blank" rel="noopener">@amanda-cristiny</a>
                    </p>
                </div>
            </div>
        </div>
    `;

    mostrarSlide(0);
    iniciarPassagemDeSlides();

    document.getElementById("pontos").addEventListener("click", (event) => {
        const ponto = event.target.closest("[data-slide]");

        if (ponto) {
            iniciarPassagemDeSlides();
            mostrarSlide(Number(ponto.dataset.slide));
        }
    });

    document.getElementById("ir-entrar").addEventListener("click", () => {
        sairDasBoasVindas(renderLoginForm);
    });

    document.getElementById("ir-criar").addEventListener("click", (event) => {
        event.preventDefault();
        sairDasBoasVindas(renderRegisterForm);
    });
}

function sairDasBoasVindas(proximaTela) {
    clearInterval(slideTimer);
    proximaTela();
}

function mostrarSlide(indice) {
    const slide = SLIDES[indice];

    slideAtual = indice;

    const foto = document.getElementById("hero-foto");

    foto.style.backgroundImage = `url("${slide.imagem}")`;

    // Tirar e repor a classe reinicia o movimento a cada troca.
    // O offsetWidth no meio obriga o navegador a recalcular antes de repor.
    foto.classList.remove("em-cena");
    void foto.offsetWidth;
    foto.classList.add("em-cena");

    document.getElementById("slide").innerHTML = `
        <span class="slide-icone"><i class="ph-light ph-${slide.icone}"></i></span>
        <h2>${slide.titulo}</h2>
        <p>${slide.texto}</p>
    `;


    [...document.querySelectorAll(".ponto")].forEach((ponto, i) => {
        ponto.classList.toggle("ponto-ativo", i === indice);
    });
}

function iniciarPassagemDeSlides() {
    clearInterval(slideTimer);
    slideTimer = setInterval(() => mostrarSlide((slideAtual + 1) % SLIDES.length), 5000);
}

function rodapeDaDupla() {
    return `
        <footer class="rodape-dupla">
            <span>Feito a quatro mãos por</span>
            <a href="https://www.linkedin.com/in/amfpdev" target="_blank" rel="noopener">@amfpdev</a>
            <span>e</span>
            <a href="https://www.linkedin.com/in/amanda-cristiny" target="_blank" rel="noopener">@amanda-cristiny</a>
        </footer>
    `;
}

function renderLoginForm(identifier = "") {
    showGoogleButton(true);

    document.getElementById("auth-area").innerHTML = `
        <div class="tela-entrada">
            <div class="painel-foto">
                <div class="painel-marca">
                    <i class="ph-light ph-coffee"></i>
                    <span>Cafélio</span>
                </div>

                <p class="painel-frase">seu diário de leituras, guardado entre um café e outro</p>
            </div>

            <div class="coluna-formulario">
                <form id="login-form">
                    <div class="form-titulo">
                        <h2>Entrar no Cafélio</h2>
                        <span class="form-subtitulo">Seu diário de leituras espera por você.</span>
                    </div>

                    <div class="campo">
                        <label for="identifier">E-mail ou nome de usuário</label>
                        <input id="identifier" type="text" autocomplete="username" required />
                    </div>

                    <div class="campo">
                        <label for="password">Senha</label>
                        <div class="campo-com-botao">
                            <input id="password" type="password" autocomplete="current-password" required />
                        </div>
                    </div>

                    <a href="#" id="show-forgot" class="link-discreto">Esqueci minha senha</a>

                    <button type="submit">Entrar</button>
                    <p id="login-message" class="form-message"></p>

                    <div class="divisor">ou</div>

                    <div class="google-envoltorio">
                        <button type="button" class="btn-google" tabindex="-1">
                            <i class="ph-fill ph-google-logo"></i>Entrar com Google
                        </button>
                        <div id="google-slot"></div>
                    </div>

                    <p class="rodape-form">Ainda não tem conta? <a href="#" id="show-register">Criar conta</a></p>
                </form>
            </div>
        </div>

        ${rodapeDaDupla()}
    `;

    moverBotaoDoGoogle();
    document.getElementById("identifier").value = identifier;
    document.getElementById("login-form").addEventListener("submit", handleLogin);
        document.getElementById("show-register").addEventListener("click", (event) => {
        event.preventDefault();
        renderRegisterForm();
    });

    document.getElementById("show-forgot").addEventListener("click", (event) => {
        event.preventDefault();
        renderForgotPasswordForm();
    });

    ativarMostrarSenha();
}

function renderRegisterForm() {
    showGoogleButton(true);

    document.getElementById("auth-area").innerHTML = `
        <form id="register-form">
            <h2>Criar conta</h2>

            <label for="register-name">Seu nome</label>
            <input id="register-name" type="text" autocomplete="name" required />

            <label for="register-username">Nome de usuário</label>
            <input id="register-username" type="text" autocomplete="username" required />

            <label for="register-email">E-mail</label>
            <input id="register-email" type="email" autocomplete="email" required />

            <label for="register-password">Senha</label>
            <input id="register-password" type="password" autocomplete="new-password" required />
            <small>Mínimo de 8 caracteres, com letra maiúscula, minúscula e número.</small>

            <label for="register-confirm">Confirmar senha</label>
            <input id="register-confirm" type="password" autocomplete="new-password" required />

            <button type="submit">Criar conta</button>
            <p id="register-message" class="form-message"></p>

            <div class="divisor">ou</div>

            <div class="google-envoltorio">
                <button type="button" class="btn-google" tabindex="-1">
                    <i class="ph-fill ph-google-logo"></i>Entrar com Google
                </button>
                <div id="google-slot-cadastro"></div>
            </div>

            <p class="rodape-form">Já tem conta? <a href="#" id="show-login">Entrar</a></p>
        </form>

        ${rodapeDaDupla()}
    `;

    moverBotaoDoGoogle("google-slot-cadastro");
    document.getElementById("register-form").addEventListener("submit", handleRegister);
    document.getElementById("show-login").addEventListener("click", (event) => {
        event.preventDefault();
        renderLoginForm();
    });

    ativarMostrarSenha();
}

function renderLoggedInArea(user) {
    showGoogleButton(false);
    usuarioAtual = user;
    renderCasca();
}

function renderCasca() {
    const user = usuarioAtual;

    document.getElementById("auth-area").innerHTML = `
        <div class="casca">
            <aside class="barra-lateral">
                <div class="barra-lateral-marca">
                    <i class="ph-light ph-coffee"></i>
                    <span>Cafélio</span>
                </div>

                <nav>${DESTINOS_PRINCIPAIS.map((d) => itemDestino(d, "destino")).join("")}</nav>

                <span class="overline">Explorar</span>

                <nav>${DESTINOS_EXPLORAR.map((d) => itemDestino(d, "destino destino-secundario")).join("")}</nav>

                <div class="barra-lateral-espaco"></div>

                ${botaoDeTema("botao-tema")}

                <div class="cartao-plus">
                    <span class="cartao-plus-titulo">
                        <i class="ph-light ph-crown-simple"></i>Cafélio Plus
                    </span>
                    <p>Temas, relatório anual e cadernos sem limite.</p>
                </div>

                <div class="barra-lateral-perfil" data-destino="perfil">
                    ${avatarDoUsuario(user)}
                    <div>
                        <div class="perfil-nome">${user.name}</div>
                        <div class="perfil-usuario">@${user.username}</div>
                    </div>
                </div>
            </aside>

            <main class="conteudo" id="conteudo"></main>

            <nav class="barra-abas">
                ${[...DESTINOS_PRINCIPAIS, DESTINO_PERFIL].map((d) => itemDestino(d, "aba")).join("")}
            </nav>
        </div>
    `;

    document.querySelector(".casca").addEventListener("click", (event) => {
        if (event.target.closest("[data-tema]")) {
            alternarTema();
            return;
        }

        const alvo = event.target.closest("[data-destino]");

        if (!alvo) {
            return;
        }

        event.preventDefault();
        navegarPara(alvo.dataset.destino);
    });

    renderDestino();
}

function itemDestino(destino, classe) {
    const ativo = destino.id === destinoAtual;
    const marca = ativo ? ` aria-current="page"` : "";
    const peso = ativo ? "ph-fill" : "ph-light";

    return `
        <a href="#" class="${classe}" data-destino="${destino.id}"${marca}>
            <i class="${peso} ph-${destino.icone}"></i>
            <span>${destino.rotulo}</span>
        </a>
    `;
}

// "Angélica Feitosa" vira AF. A última palavra é o sobrenome, não a
// segunda — "Maria da Silva" precisa dar MS, não MD.
function iniciais(nome) {
    const partes = (nome || "").trim().split(/\s+/).filter(Boolean);

    if (partes.length === 0) {
        return "?";
    }

    if (partes.length === 1) {
        return partes[0].slice(0, 2).toUpperCase();
    }

    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
}

function avatarDoUsuario(user) {
    if (user.profilePictureUrl) {
        return `<img class="avatar" src="${user.profilePictureUrl}" alt="Foto de perfil" />`;
    }

    return `<span class="avatar">${iniciais(user.name)}</span>`;
}

function navegarPara(id) {
    destinoAtual = id;
    renderCasca();
}

function renderDestino() {
    const conteudo = document.getElementById("conteudo");

    // Só o Perfil hospeda o botão do Google; qualquer outra tela o esconde
    showGoogleButton(false);

    if (destinoAtual === "perfil") {
        renderPerfil(conteudo);
        return;
    }

    if (destinoAtual === "desafios") {
        renderDesafios(conteudo);
        return;
    }

    if (destinoAtual === "inicio") {
        renderInicio(conteudo);
        return;
    }

    const destino = [...DESTINOS_PRINCIPAIS, ...DESTINOS_EXPLORAR]
        .find((d) => d.id === destinoAtual);

    conteudo.innerHTML = `
        <h2>${destino ? destino.rotulo : "Cafélio"}</h2>
        <p class="rascunho">Esta tela ainda não foi construída.</p>
    `;
}

function renderPerfil(conteudo) {
    const user = usuarioAtual;
    const acao = user.hasPassword ? "Alterar senha" : "Definir senha";
    const temFoto = Boolean(user.profilePictureUrl);

    conteudo.innerHTML = `
        <h2>Perfil</h2>

        <div class="perfil">
            <div class="perfil-cartao">
                ${fotoDePerfil(user)}

                <span class="perfil-nome">${user.name}</span>
                <span class="perfil-usuario-grande">@${user.username} · ${desdeQuando(user.createdAt)}</span>

                <div class="perfil-acoes">
                    <button type="button" id="escolher-foto">
                        <i class="ph-light ph-camera"></i>${temFoto ? "Trocar foto" : "Adicionar foto"}
                    </button>
                    ${temFoto ? `<button type="button" id="remove-picture" class="btn-perigo">Remover</button>` : ""}
                </div>

                <input id="picture-input" type="file" accept="image/jpeg,image/png,image/webp" hidden />
                <small>JPG, PNG ou WebP, até 2 MB.</small>
                <p id="picture-message" class="form-message"></p>
            </div>

            <div class="perfil-coluna">
                <form id="dados-form" class="perfil-campos">
                    <div class="campo">
                        <label for="dados-nome">Nome</label>
                        <input id="dados-nome" type="text" value="${user.name}" required />
                    </div>

                    <div class="campo">
                        <label for="dados-username">Nome de usuário</label>
                        <input id="dados-username" type="text" value="${user.username}" required />
                    </div>

                    <div class="campo">
                        <label for="email-novo">E-mail</label>
                        <input id="email-novo" type="email" value="${user.email}" />

                        <button type="button" class="botao-link" id="email-trocar">Trocar e-mail</button>
                        <p class="form-message" id="email-message"></p>
                    </div>

                    <div class="perfil-campos-rodape">
                        <p id="dados-message" class="form-message"></p>
                        <button type="submit">Salvar</button>
                    </div>
                </form>

                <div class="bloco">
                    <span class="bloco-titulo"><i class="ph-light ph-key"></i>${acao}</span>
                    <div id="password-area"></div>
                </div>

                <div class="bloco">
                    <span class="bloco-titulo"><i class="ph-light ph-palette"></i>Aparência</span>
                    ${botaoDeTema("botao-tema botao-tema-perfil")}
                </div>

                <div class="bloco">
                    <span class="bloco-titulo"><i class="ph-light ph-link-simple"></i>Conta Google</span>
                    ${blocoDoGoogle(user)}
                    <p class="form-message" id="google-message"></p>
                </div>

                <div class="cartao-plano">
                    <i class="ph-light ph-crown-simple"></i>
                    <div>
                        <div class="cartao-plano-titulo">Cafélio Plus</div>
                        <p>Cadernos ilimitados, temas visuais e relatório anual.</p>
                    </div>
                </div>

                <span class="link-sair" id="logout">
                    <i class="ph-light ph-sign-out"></i>Sair da conta
                </span>

                <div class="bloco bloco-perigo">
                    <span class="bloco-titulo"><i class="ph-light ph-warning-octagon"></i>Excluir minha conta</span>
                    <p>
                        Apaga em definitivo sua conta, seus livros, suas resenhas, sua
                        sequência de leitura e sua foto. Não há como desfazer.
                    </p>
                    <button type="button" class="btn-perigo" id="excluir-conta">Excluir minha conta</button>
                    <p class="form-message" id="excluir-message"></p>
                </div>
            </div>
        </div>
    `;

    document.getElementById("dados-form").addEventListener("submit", handleDados);

    // Delegado: o botão "Confirmar" só nasce depois que o código é pedido
    document.getElementById("dados-form").addEventListener("click", (event) => {
        if (event.target.closest("#email-trocar")) {
            pedirTrocaDeEmail();
        }
    });

    document.getElementById("escolher-foto").addEventListener("click", () => {
        document.getElementById("picture-input").click();
    });

    document.getElementById("picture-input").addEventListener("change", handleUploadPicture);

    const botaoRemover = document.getElementById("remove-picture");

    if (botaoRemover) {
        botaoRemover.addEventListener("click", handleRemovePicture);
    }

    renderPasswordForm(user);

    const botaoDesvincular = document.getElementById("google-desvincular");

    if (botaoDesvincular) {
        botaoDesvincular.addEventListener("click", desvincularGoogle);
    } else {
        showGoogleButton(true);
        moverBotaoDoGoogle("google-slot-perfil");
    }

    document.getElementById("logout").addEventListener("click", handleLogout);
    document.getElementById("excluir-conta").addEventListener("click", handleExcluirConta);
}

async function handleDados(event) {
    event.preventDefault();

    const message = document.getElementById("dados-message");
    message.textContent = "Salvando...";

    try {
        usuarioAtual = await apiPut("/auth/profile", {
            name: document.getElementById("dados-nome").value,
            username: document.getElementById("dados-username").value,
        });

        // Redesenha a casca para o nome novo aparecer na barra lateral
        renderCasca();
        document.getElementById("dados-message").textContent = "Dados salvos.";
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleExcluirConta() {
    const message = document.getElementById("excluir-message");

    // Duas perguntas de propósito: a primeira avisa, a segunda exige que a
    // pessoa escreva. Um clique sozinho não deveria apagar uma conta.
    if (!confirm("Apagar sua conta e tudo que há nela? Isso não pode ser desfeito.")) {
        return;
    }

    if (prompt('Para confirmar, escreva EXCLUIR') !== "EXCLUIR") {
        message.textContent = "Exclusão cancelada.";
        return;
    }

    message.textContent = "Excluindo...";

    try {
        await apiDelete("/auth/me");

        clearToken();
        usuarioAtual = null;
        renderWelcomeScreen();
    } catch (error) {
        message.textContent = error.message;
    }
}

function blocoDoGoogle(user) {
    if (user.googleLinked) {
        return `
            <p class="google-estado">
                <i class="ph-fill ph-check-circle"></i>
                Vinculada. Você pode entrar no Cafélio pelo Google.
            </p>
            <button type="button" class="botao-link" id="google-desvincular">Desvincular</button>
        `;
    }

    return `
        <p class="google-estado">Não vinculada. Vincule para poder entrar pelo Google.</p>

        <div class="google-envoltorio">
            <button type="button" class="btn-google" tabindex="-1">
                <i class="ph-fill ph-google-logo"></i>Vincular conta Google
            </button>
            <div id="google-slot-perfil"></div>
        </div>
    `;
}

async function desvincularGoogle() {
    const message = document.getElementById("google-message");

    if (!confirm("Desvincular sua conta Google do Cafélio?")) {
        return;
    }

    message.textContent = "Desvinculando...";

    try {
        usuarioAtual = await apiDelete("/auth/google/link");
        renderCasca();
        document.getElementById("google-message").textContent = "Conta Google desvinculada.";
    } catch (error) {
        message.textContent = error.message;
    }
}

async function vincularGoogle(idToken) {
    const message = document.getElementById("google-message");

    message.textContent = "Vinculando...";

    try {
        usuarioAtual = await apiPost("/auth/google/link", { idToken });
        renderCasca();
        document.getElementById("google-message").textContent = "Conta Google vinculada.";
    } catch (error) {
        message.textContent = error.message;
    }
}

// "marina@email.com" vira "m•••••@email.com"
function mascararEmail(email) {
    const [nome, dominio] = String(email).split("@");

    if (!dominio) {
        return email;
    }

    return `${nome.slice(0, 1)}${"•".repeat(Math.max(nome.length - 1, 1))}@${dominio}`;
}

let reenvioTimer = null;

async function pedirTrocaDeEmail() {
    const novo = document.getElementById("email-novo").value;
    const jaAberto = document.getElementById("modal-codigo");

    const message = jaAberto
        ? document.getElementById("codigo-message")
        : document.getElementById("email-message");

    message.textContent = "Enviando código...";

    try {
        await apiPost("/auth/email-change", { newEmail: novo });

        if (jaAberto) {
            message.textContent = "Enviamos outro código.";
            iniciarContagemDeReenvio(60);
            return;
        }

        message.textContent = "";
        abrirModalDoCodigo(novo);
    } catch (error) {
        message.textContent = error.message;
    }
}

function abrirModalDoCodigo(novoEmail) {
    const caixas = Array.from(
        { length: 6 },
        () => `<input class="codigo-caixa" type="text" inputmode="numeric" maxlength="1" aria-label="dígito do código" />`
    ).join("");

    document.body.insertAdjacentHTML("beforeend", `
        <div class="modal-fundo" id="modal-codigo">
            <div class="modal" role="dialog" aria-modal="true" aria-label="Digite o código">
                <button type="button" class="modal-fechar" id="codigo-fechar" aria-label="Fechar">
                    <i class="ph-light ph-x"></i>
                </button>

                <h3>Digite o código</h3>
                <p class="modal-texto">Enviamos para ${mascararEmail(usuarioAtual.email)}</p>

                <div class="codigo-caixas">${caixas}</div>

                <p class="codigo-reenvio">
                    Não chegou?
                    <button type="button" class="botao-link" id="email-reenviar" disabled>Reenviar</button>
                    <span id="codigo-contagem"></span>
                </p>

                <p class="form-message" id="codigo-message"></p>

                <button type="button" id="email-confirmar">Confirmar troca</button>
                <small>Seu e-mail passará a ser ${novoEmail}</small>
            </div>
        </div>
    `);

    const modal = document.getElementById("modal-codigo");

    modal.addEventListener("click", (event) => {
        // Clique no fundo escuro, fora do cartão, fecha
        if (event.target === modal || event.target.closest("#codigo-fechar")) {
            fecharModalDoCodigo();
        }

        if (event.target.closest("#email-reenviar")) {
            pedirTrocaDeEmail();
        }

        if (event.target.closest("#email-confirmar")) {
            confirmarTrocaDeEmail();
        }
    });

    document.addEventListener("keydown", escFechaModal);

    ativarCaixasDeCodigo();
    iniciarContagemDeReenvio(60);

    document.querySelector(".codigo-caixa").focus();
}

function escFechaModal(event) {
    if (event.key === "Escape") {
        fecharModalDoCodigo();
    }
}

function fecharModalDoCodigo() {
    clearInterval(reenvioTimer);
    document.removeEventListener("keydown", escFechaModal);
    document.getElementById("modal-codigo")?.remove();
}

// Andar sozinha entre as caixas, apagar para trás e aceitar o código colado
function ativarCaixasDeCodigo() {
    const caixas = [...document.querySelectorAll(".codigo-caixa")];

    caixas.forEach((caixa, i) => {
        caixa.addEventListener("input", () => {
            caixa.value = caixa.value.replace(/\D/g, "");

            if (caixa.value && i < caixas.length - 1) {
                caixas[i + 1].focus();
            }
        });

        caixa.addEventListener("keydown", (event) => {
            if (event.key === "Backspace" && !caixa.value && i > 0) {
                caixas[i - 1].focus();
            }
        });

        caixa.addEventListener("paste", (event) => {
            event.preventDefault();

            const digitos = event.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);

            digitos.split("").forEach((d, j) => {
                caixas[j].value = d;
            });

            caixas[Math.min(digitos.length, 5)].focus();
        });
    });
}

function iniciarContagemDeReenvio(segundos) {
    clearInterval(reenvioTimer);

    const contagem = document.getElementById("codigo-contagem");
    const botao = document.getElementById("email-reenviar");

    const tique = () => {
        if (segundos <= 0) {
            clearInterval(reenvioTimer);
            contagem.textContent = "";
            botao.disabled = false;
            return;
        }

        const m = Math.floor(segundos / 60);
        const s = String(segundos % 60).padStart(2, "0");

        contagem.textContent = `em ${m}:${s}`;
        segundos--;
    };

    tique();
    reenvioTimer = setInterval(tique, 1000);
}

function codigoDigitado() {
    return [...document.querySelectorAll(".codigo-caixa")].map((c) => c.value).join("");
}

async function confirmarTrocaDeEmail() {
    const message = document.getElementById("codigo-message");

    message.textContent = "Confirmando...";

    try {
        usuarioAtual = await apiPost("/auth/email-change/confirm", { code: codigoDigitado() });

        fecharModalDoCodigo();

        // Redesenha para o e-mail novo aparecer em todo lugar
        renderCasca();
        document.getElementById("email-message").textContent = "E-mail alterado.";
    } catch (error) {
        message.textContent = error.message;
    }
}

function fotoDePerfil(user) {
    if (user.profilePictureUrl) {
        return `<img class="perfil-foto" src="${user.profilePictureUrl}" alt="Foto de perfil" />`;
    }

    return `<span class="perfil-foto">${iniciais(user.name)}</span>`;
}

function campoDeSenha(id, rotulo, autocomplete) {
    return `
        <div class="campo">
            <label for="${id}">${rotulo}</label>
            <div class="campo-com-botao">
                <input id="${id}" type="password" autocomplete="${autocomplete}" required />
            </div>
        </div>
    `;
}

// "desde mar 2025", a partir do createdAt que a API já devolve
function desdeQuando(createdAt) {
    if (!createdAt) {
        return "";
    }

    const data = new Date(createdAt);
    const mes = data.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "");

    return `desde ${mes} ${data.getFullYear()}`;
}

function renderPasswordForm(user) {
    const campoSenhaAtual = user.hasPassword
        ? campoDeSenha("current-password", "Senha atual", "current-password")
        : `
            <p class="senha-aviso">
                Sua conta foi criada com o Google e ainda não tem senha.
                Definindo uma, você poderá entrar das duas formas.
            </p>
        `;

    document.getElementById("password-area").innerHTML = `
        <form id="password-form" class="senha-campos">
            ${campoSenhaAtual}
            ${campoDeSenha("new-password", "Nova senha", "new-password")}
            ${campoDeSenha("confirm-new-password", "Confirme", "new-password")}

            <div class="senha-rodape">
                <small>Mínimo de 8 caracteres, com letra maiúscula, minúscula e número.</small>
                <button type="submit">${user.hasPassword ? "Atualizar senha" : "Definir senha"}</button>
            </div>

            <p id="password-message" class="form-message"></p>
        </form>
    `;

    document.getElementById("password-form").addEventListener("submit", (event) => {
        handleChangePassword(event, user);
    });

    ativarMostrarSenha();
}

renderWelcomeScreen();

function renderForgotPasswordForm(email = "") {
    showGoogleButton(false);

    document.getElementById("auth-area").innerHTML = `
        <form id="forgot-form">
            <h2>Esqueci minha senha</h2>
            <p>Informe seu e-mail. Enviaremos um link e um código de 6 dígitos — você escolhe qual usar.</p>

            <label for="forgot-email">E-mail</label>
            <input id="forgot-email" type="email" autocomplete="email" required />

            <button type="submit">Enviar</button>
            <p id="forgot-message" class="form-message"></p>

            <p><a href="#" id="have-code">Já tenho um código</a></p>
            <p><a href="#" id="back-to-login">Voltar para o login</a></p>
        </form>
    `;

    document.getElementById("forgot-email").value = email;
    document.getElementById("forgot-form").addEventListener("submit", handleForgotPassword);

    document.getElementById("have-code").addEventListener("click", (event) => {
        event.preventDefault();
        renderResetByCodeForm(document.getElementById("forgot-email").value);
    });

    document.getElementById("back-to-login").addEventListener("click", (event) => {
        event.preventDefault();
        renderLoginForm();
    });
}

function renderResetByCodeForm(email = "") {
    showGoogleButton(false);

    document.getElementById("auth-area").innerHTML = `
        <form id="code-form">
            <h2>Usar o código</h2>
            <p>Digite o código de 6 dígitos que enviamos para o seu e-mail.</p>

            <label for="code-email">E-mail</label>
            <input id="code-email" type="email" autocomplete="email" required />

            <label for="code-value">Código</label>
            <input id="code-value" type="text" inputmode="numeric" maxlength="6"
                    pattern="\\d{6}" autocomplete="one-time-code" required />

            <label for="code-password">Nova senha</label>
            <input id="code-password" type="password" autocomplete="new-password" required />
            <small>Mínimo de 8 caracteres, com letra maiúscula, minúscula e número.</small>

            <label for="code-confirm">Confirmar nova senha</label>
            <input id="code-confirm" type="password" autocomplete="new-password" required />

            <button type="submit">Salvar senha</button>
            <p id="code-message" class="form-message"></p>

            <p><a href="#" id="back-to-forgot">Pedir um código novo</a></p>
        </form>
    `;

    document.getElementById("code-email").value = email;
    document.getElementById("code-form").addEventListener("submit", handleResetByCode);

    document.getElementById("back-to-forgot").addEventListener("click", (event) => {
        event.preventDefault();
        renderForgotPasswordForm(document.getElementById("code-email").value);
    });

    document.getElementById(email ? "code-value" : "code-email").focus();

    ativarMostrarSenha();
}

function renderResetPasswordForm(token) {
    showGoogleButton(false);

    document.getElementById("auth-area").innerHTML = `
        <form id="reset-form">
            <h2>Escolher nova senha</h2>

            <label for="reset-password">Nova senha</label>
            <input id="reset-password" type="password" autocomplete="new-password" required />
            <small>Mínimo de 8 caracteres, com letra maiúscula, minúscula e número.</small>

            <label for="reset-confirm">Confirmar nova senha</label>
            <input id="reset-confirm" type="password" autocomplete="new-password" required />

            <button type="submit">Salvar senha</button>
            <p id="reset-message" class="form-message"></p>
        </form>
    `;

    document.getElementById("reset-form").addEventListener("submit", (event) => {
        handleResetPassword(event, token);
    });

    ativarMostrarSenha();
}

async function handleGoogleLogin(response) {
    // O mesmo botão serve para entrar e para vincular: se já há sessão,
    // o clique veio da tela de Perfil.
    if (getToken() && usuarioAtual) {
        vincularGoogle(response.credential);
        return;
    }

    if (!document.getElementById("login-message")) {
        renderLoginForm();
    }

    const message = document.getElementById("login-message");
    message.textContent = "Entrando...";

    try {
        const result = await apiPost("/auth/google", { idToken: response.credential });
        setToken(result.token);

        const user = await apiGet("/auth/me");
        renderLoggedInArea(user);
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleLogin(event) {
    event.preventDefault();

    const message = document.getElementById("login-message");
    message.textContent = "Entrando...";

    try {
        const result = await apiPost("/auth/login", {
            identifier: document.getElementById("identifier").value,
            password: document.getElementById("password").value,
        });

        setToken(result.token);

        const user = await apiGet("/auth/me");
        renderLoggedInArea(user);
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const message = document.getElementById("register-message");
    message.textContent = "Criando conta...";

    const email = document.getElementById("register-email").value;

    try {
        await apiPost("/auth/register", {
            name: document.getElementById("register-name").value,
            username: document.getElementById("register-username").value,
            email: email,
            password: document.getElementById("register-password").value,
            confirmPassword: document.getElementById("register-confirm").value,
        });

        renderLoginForm(email);
        document.getElementById("login-message").textContent =
            "Conta criada! Agora é só entrar com sua senha.";
        document.getElementById("password").focus();
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleChangePassword(event, user) {
    event.preventDefault();

    const message = document.getElementById("password-message");
    message.textContent = "Salvando...";

    const currentPasswordInput = document.getElementById("current-password");

    try {
        await apiPut("/auth/password", {
            currentPassword: currentPasswordInput ? currentPasswordInput.value : null,
            newPassword: document.getElementById("new-password").value,
            confirmNewPassword: document.getElementById("confirm-new-password").value,
        });

        const updated = await apiGet("/auth/me");
        renderLoggedInArea(updated);
        document.getElementById("password-area").textContent = "Senha salva com sucesso!";
    } catch (error) {
        message.textContent = error.message;
    }
}

function handleLogout() {
    destinoAtual = "inicio";
    clearToken();
    renderLoginForm();
}

async function handleForgotPassword(event) {
    event.preventDefault();

    const message = document.getElementById("forgot-message");
    message.textContent = "Enviando...";

    const email = document.getElementById("forgot-email").value;

    try {
        await apiPost("/auth/password/reset-request", { email: email });

        renderResetByCodeForm(email);
        document.getElementById("code-message").textContent =
            "Se esse e-mail estiver cadastrado, enviamos um link e um código de 6 dígitos. " +
            "Valem por 30 minutos.";
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleResetPassword(event, token) {
    event.preventDefault();

    const message = document.getElementById("reset-message");
    message.textContent = "Salvando...";

    try {
        await apiPost("/auth/password/reset", {
            token: token,
            newPassword: document.getElementById("reset-password").value,
            confirmNewPassword: document.getElementById("reset-confirm").value,
        });

        window.history.replaceState({}, "", window.location.pathname);

        renderLoginForm();
        document.getElementById("login-message").textContent =
            "Senha alterada! Agora é só entrar com a senha nova.";
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleResetByCode(event) {
    event.preventDefault();

    const message = document.getElementById("code-message");
    message.textContent = "Salvando...";

    const email = document.getElementById("code-email").value;

    try {
        await apiPost("/auth/password/reset-code", {
            email: email,
            code: document.getElementById("code-value").value,
            newPassword: document.getElementById("code-password").value,
            confirmNewPassword: document.getElementById("code-confirm").value,
        });

        renderLoginForm(email);
        document.getElementById("login-message").textContent =
            "Senha alterada! Agora é só entrar com a senha nova.";
    } catch (error) {
        message.textContent = error.message;
    }
}

async function handleUploadPicture(event) {
    const arquivo = event.target.files[0];

    if (!arquivo) {
        return;
    }

    const message = document.getElementById("picture-message");
    message.textContent = "Enviando...";

    const formData = new FormData();
    formData.append("file", arquivo);

    try {
        usuarioAtual = await apiUpload("/auth/profile-picture", formData);
        renderCasca();
        document.getElementById("picture-message").textContent = "Foto atualizada!";
    } catch (error) {
        message.textContent = error.message;
    } finally {
        event.target.value = "";
    }
}

async function handleRemovePicture() {
    const message = document.getElementById("picture-message");
    message.textContent = "Removendo...";

    try {
        usuarioAtual = await apiDelete("/auth/profile-picture");
        renderCasca();
        document.getElementById("picture-message").textContent = "Foto removida.";
    } catch (error) {
        message.textContent = error.message;
    }
}


function ativarMostrarSenha() {
    const area = document.getElementById("auth-area");

    if (!area) {
        return;
    }

    area.querySelectorAll('input[type="password"]').forEach((campo) => {
        if (campo.nextElementSibling?.classList.contains("mostrar-senha")) {
            return;
        }

        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "mostrar-senha";
        botao.textContent = "Mostrar";
        botao.setAttribute("aria-label", "Mostrar senha");

        campo.insertAdjacentElement("afterend", botao);

        botao.addEventListener("click", () => {
            const escondida = campo.type === "password";

            campo.type = escondida ? "text" : "password";
            botao.textContent = escondida ? "Ocultar" : "Mostrar";
            botao.setAttribute("aria-label", escondida ? "Ocultar senha" : "Mostrar senha");

            campo.focus();
        });
    });
}

// ---- Tela de Desafios ----

async function renderDesafios(conteudo) {
    conteudo.innerHTML = `
        <h2>Desafios</h2>

        <div class="desafios">
            ${cartaoAllumez()}
            ${cartaoFlamme()}
            ${cartaoTematicos()}
        </div>
    `;

    document.getElementById("flamme-grade").addEventListener("click", (event) => {
        const celula = event.target.closest("[data-dia]");

        if (celula) {
            alternarDia(celula.dataset.dia, celula.classList.contains("flamme-dia-lido"));
        }
    });

    document.getElementById("allumez-conteudo").addEventListener("click", (event) => {
        if (event.target.closest("#allumez-definir")) {
            definirMetaAllumez();
        }

        if (event.target.closest("#allumez-alterar")) {
            document.getElementById("allumez-conteudo").innerHTML =
                formularioDeMeta(allumezAtual.ano, allumezAtual.meta);
        }
    });

    try {
        preencherAllumez(await apiGet("/challenges/allumez"));
    } catch (error) {
        document.getElementById("allumez-estado").textContent = error.message;
    }

    try {
        preencherFlamme(await apiGet("/challenges/flamme"));
    } catch (error) {
        document.getElementById("flamme-estado").textContent = error.message;
    }
}

function cartaoFlamme() {
    return `
        <div class="cartao-desafio">
            <div class="cartao-desafio-cabecalho">
                <h3>Flamme</h3>
                <span class="selo selo-gratuito">Gratuito</span>
            </div>

            <div class="flamme-numeros">
                <div class="flamme-numero">
                    <i class="ph-fill ph-fire"></i>
                    <div>
                        <span class="flamme-numero-valor" id="flamme-atual">—</span>
                        <span class="flamme-numero-rotulo">sequência atual</span>
                    </div>
                </div>

                <div class="flamme-numero flamme-numero-recorde">
                    <i class="ph-light ph-trophy"></i>
                    <div>
                        <span class="flamme-numero-valor" id="flamme-recorde">—</span>
                        <span class="flamme-numero-rotulo">maior sequência</span>
                    </div>
                </div>
            </div>

            <div class="flamme-rolagem">
                <div class="flamme-meses" id="flamme-meses"></div>
                <div class="flamme-grade" id="flamme-grade"></div>
            </div>

            <div class="flamme-legenda">
                <span class="flamme-dia"></span>sem leitura
                <span class="flamme-dia flamme-dia-lido"></span>com leitura
            </div>

            <p class="form-message" id="flamme-estado"></p>
        </div>
    `;
}

function preencherFlamme(flamme) {
    document.getElementById("flamme-atual").textContent = emDias(flamme.sequenciaAtual);
    document.getElementById("flamme-recorde").textContent = emDias(flamme.maiorSequencia);

    const lidos = new Set(flamme.dias);
    const hoje = new Date();

    // A grade mostra semanas em colunas, então ela precisa começar num domingo
    const inicio = new Date(hoje);
    inicio.setDate(hoje.getDate() - 181);

    while (inicio.getDay() !== 0) {
        inicio.setDate(inicio.getDate() - 1);
    }

    const celulas = [];
    const dia = new Date(inicio);

    while (dia <= hoje) {
        const iso = emIso(dia);
        const lido = lidos.has(iso);
        const classe = lido ? "flamme-dia flamme-dia-lido" : "flamme-dia";
        const titulo = lido ? `${iso} — clique para remover` : `${iso} — clique para marcar`;

        celulas.push(`<span class="${classe}" title="${titulo}" data-dia="${iso}"></span>`);
        dia.setDate(dia.getDate() + 1);
    }

    const grade = document.getElementById("flamme-grade");

    grade.innerHTML = celulas.join("");
    document.getElementById("flamme-meses").innerHTML = mesesDaGrade(inicio, hoje);
}

async function alternarDia(dia, estaMarcado) {
    if (estaMarcado && !confirm(`Remover o registro de leitura de ${dia}?`)) {
        return;
    }

    const caminho = `/challenges/flamme/days/${dia}`;

    try {
        preencherFlamme(estaMarcado ? await apiDelete(caminho) : await apiPost(caminho));
    } catch (error) {
        document.getElementById("flamme-estado").textContent = error.message;
    }
}

function emIso(data) {
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, "0");
    const dia = String(data.getDate()).padStart(2, "0");

    return `${ano}-${mes}-${dia}`;
}

function emDias(quantidade) {
    return quantidade === 1 ? "1 dia" : `${quantidade} dias`;
}

function mesesDaGrade(inicio, fim) {
    const nomes = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];
    const blocos = [];
    const cursor = new Date(inicio);

    // Uma coluna por semana: cada mês ocupa tantas colunas quantas semanas comecam nele
    while (cursor <= fim) {
        const mes = cursor.getMonth();
        const ultimo = blocos[blocos.length - 1];

        if (ultimo && ultimo.mes === mes) {
            ultimo.semanas++;
        } else {
            blocos.push({ mes, semanas: 1 });
        }

        cursor.setDate(cursor.getDate() + 7);
    }

    return blocos
        .map(({ mes, semanas }) => {
            // Um mês com uma semana só não tem largura para o rótulo caber
            const nome = semanas > 1 ? nomes[mes] : "";

            return `<span style="grid-column: span ${semanas}">${nome}</span>`;
        })
        .join("");
}

function cartaoAllumez() {
    return `
        <div class="cartao-desafio cartao-desafio-escuro">
            <div class="cartao-desafio-cabecalho">
                <h3>Allumez!</h3>
                <span class="selo selo-gratuito">Gratuito</span>
            </div>
            
            <p class="cartao-desafio-descricao">Uma vela acende a cada livro concluído da meta do ano.</p>

            <div id="allumez-conteudo"></div>

            <p class="form-message" id="allumez-estado"></p>
        </div>
    `;
}

let allumezAtual = null;

function preencherAllumez(allumez) {
    allumezAtual = allumez;

    document.getElementById("allumez-conteudo").innerHTML =
        allumez.meta === 0 ? formularioDeMeta(allumez.ano, 12) : velasDaMeta(allumez);

    document.getElementById("allumez-estado").textContent = "";
}

function formularioDeMeta(ano, sugestao) {
    return `
        <div class="allumez-meta">
            <label for="allumez-quantidade">Quantos livros você quer ler em ${ano}?</label>

            <div class="allumez-meta-linha">
                <input type="number" id="allumez-quantidade" min="1" max="365" value="${sugestao}">
                <button type="button" id="allumez-definir">Definir meta</button>
            </div>
        </div>
    `;
}

const VELAS_POR_BLOCO = 5;

// A prateleira mostra só o bloco de cinco em que a pessoa está.
// Ao fechar um bloco ele fica inteiro aceso, e o próximo livro abre o seguinte.
function blocoAtual(allumez) {
    const cheios = Math.floor(allumez.concluidos / VELAS_POR_BLOCO);
    const sobra = allumez.concluidos % VELAS_POR_BLOCO;
    const fechado = sobra === 0 && allumez.concluidos > 0;

    const inicio = (fechado ? cheios - 1 : cheios) * VELAS_POR_BLOCO;
    const acesas = fechado ? VELAS_POR_BLOCO : sobra;

    return {
        inicio,
        acesas,
        tamanho: Math.max(Math.min(VELAS_POR_BLOCO, allumez.meta - inicio), acesas),
    };
}

function velasDaMeta(allumez) {
    const bloco = blocoAtual(allumez);
    const velas = [];

    for (let i = 0; i < bloco.tamanho; i++) {
        const classe = i < bloco.acesas ? "vela" : "vela vela-apagada";
        const altura = 56 + (((bloco.inicio + i) * 37) % 33);

        velas.push(`
            <div class="${classe}">
                <i class="ph-fill ph-flame"></i>
                <span class="vela-pavio"></span>
                <span class="vela-corpo" style="--altura:${altura}px"></span>
            </div>
        `);
    }

    const faltam = Math.max(allumez.meta - allumez.concluidos, 0);
    const porcentagem = Math.round((allumez.concluidos / allumez.meta) * 100);

    return `
        <div class="velas-prateleira">
            <div class="velas">${velas.join("")}</div>
        </div>

        <span class="velas-bloco">livros ${bloco.inicio + 1} a ${bloco.inicio + bloco.tamanho}</span>

        <div class="velas-contagem">
            <span class="velas-numero">${allumez.concluidos}<small> de ${allumez.meta} · ${porcentagem}%</small></span>
            <button type="button" class="link-discreto" id="allumez-alterar">alterar meta</button>
        </div>

        <p class="cartao-desafio-descricao">
            ${faltam === 0
                ? `Meta de ${allumez.ano} alcançada.`
                : `Faltam ${faltam} ${faltam === 1 ? "livro" : "livros"} para a meta de ${allumez.ano}.`}
        </p>
    `;
}

async function definirMetaAllumez() {
    const quantidade = Number(document.getElementById("allumez-quantidade").value);

    try {
        preencherAllumez(await apiPut("/challenges/allumez/goal", { quantidade }));
    } catch (error) {
        document.getElementById("allumez-estado").textContent = error.message;
    }
}

function cartaoTematicos() {
    const temas = [
        { icone: "globe-hemisphere-west", nome: "Volta ao mundo", texto: "Um livro de cada país." },
        { icone: "pen-nib", nome: "Autoras em foco", texto: "Doze autoras diferentes no ano." },
        { icone: "map-trifold", nome: "Brasil que lê", texto: "Autoria de cada estado brasileiro." },
        { icone: "compass", nome: "Fora da zona", texto: "Gêneros que você nunca leu." },
    ];

    const cartoes = temas
        .map((tema) => `
            <div class="tematico">
                <i class="ph-light ph-${tema.icone}"></i>
                <h4>${tema.nome}</h4>
                <p>${tema.texto}</p>
                <div class="barra-progresso"><span style="width:0%"></span></div>
                <span class="tematico-contagem">ainda não começou</span>
            </div>
        `)
        .join("");

    return `
        <div class="cartao-tematicos">
            <div class="cartao-desafio-cabecalho">
                <h3>Temáticos</h3>
                <span class="selo selo-plus"><i class="ph-light ph-crown-simple"></i>Plus</span>
            </div>

            <div class="tematicos">${cartoes}</div>

            <p class="aviso-rascunho">
                <i class="ph-light ph-wrench"></i>Ainda não construídos — os quatro dependem de dados que o Cafélio não guarda hoje.
            </p>
        </div>
    `;
}

// ---- Tela de Início ----

async function renderInicio(conteudo) {
    const user = usuarioAtual;

    conteudo.innerHTML = `
        <div class="inicio-saudacao">
            ${avatarDoUsuario(user).replace('class="avatar"', 'class="avatar avatar-grande"')}
            <div>
                <h2>${saudacaoDoDia()}, ${user.name}</h2>
                <span class="inicio-data">${dataPorExtenso(new Date())}</span>
            </div>
        </div>

        <div class="inicio-cartoes">
            <div class="cartao-sequencia" id="cartao-sequencia">
                <span class="overline">Sequência</span>
                <p class="rascunho">Carregando…</p>
            </div>
        </div>

        <p class="aviso-rascunho">
            <i class="ph-light ph-wrench"></i>O resto da tela — páginas lidas, livro em andamento e recomendação — ainda não foi construído.
        </p>
    `;

    try {
        preencherSequencia(await apiGet("/challenges/flamme"));
    } catch (error) {
        document.getElementById("cartao-sequencia").innerHTML =
            `<p class="form-message">${error.message}</p>`;
    }
}

function preencherSequencia(flamme) {
    const registrado = flamme.dias.includes(emIso(new Date()));

    // A chama cresce com a sequência, até um teto de 45 dias
    const tamanho = Math.round(40 + Math.min(flamme.sequenciaAtual, 45) * 1.5);

    const acao = registrado
        ? `<div class="btn-li-hoje btn-li-hoje-feito">
               <i class="ph-light ph-check"></i>Leitura de hoje registrada
           </div>`
        : `<button type="button" id="li-hoje" class="btn-li-hoje">
               <i class="ph-light ph-fire"></i>Li hoje
           </button>`;

    document.getElementById("cartao-sequencia").innerHTML = `
        <span class="overline">Sequência</span>

        <div class="sequencia-chama">
            <i class="ph-fill ph-fire" style="font-size:${tamanho}px"></i>
            <div>
                <span class="sequencia-valor">${emDias(flamme.sequenciaAtual)}</span>
                <span class="sequencia-rotulo">lendo sem parar · recorde ${emDias(flamme.maiorSequencia)}</span>
            </div>
        </div>

        ${acao}
    `;

    const botao = document.getElementById("li-hoje");

    if (botao) {
        botao.addEventListener("click", handleLiHoje);
    }
}

async function handleLiHoje() {
    try {
        preencherSequencia(await apiPost("/challenges/flamme/today"));
    } catch (error) {
        document.getElementById("cartao-sequencia")
            .insertAdjacentHTML("beforeend", `<p class="form-message">${error.message}</p>`);
    }
}

function saudacaoDoDia() {
    const hora = new Date().getHours();

    if (hora < 12) {
        return "Bom dia";
    }

    return hora < 18 ? "Boa tarde" : "Boa noite";
}

function dataPorExtenso(data) {
    return data.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" });
}
