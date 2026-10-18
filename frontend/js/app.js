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
        renderLoginForm();
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

function moverBotaoDoGoogle() {
    const slot = document.getElementById("google-slot");
    const button = document.querySelector(".g_id_signin");

    if (slot && button) {
        slot.appendChild(button);
    }
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
                        <h2>Que bom te ver de novo</h2>
                        <span class="form-subtitulo">Entre para continuar de onde parou.</span>
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

                    <div id="google-slot"></div>

                    <p class="rodape-form">Ainda não tem conta? <a href="#" id="show-register">Criar conta</a></p>
                </form>
            </div>
        </div>
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
    showGoogleButton(false);

    document.getElementById("auth-area").innerHTML = `
        <form id="register-form">
            <h2>Criar conta</h2>

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

            <p>Já tem conta? <a href="#" id="show-login">Entrar</a></p>
        </form>
    `;

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

                <div class="cartao-plus">
                    <span class="cartao-plus-titulo">
                        <i class="ph-light ph-crown-simple"></i>Cafélio Plus
                    </span>
                    <p>Temas, relatório anual e cadernos sem limite.</p>
                </div>

                <div class="barra-lateral-perfil" data-destino="perfil">
                    ${avatarDoUsuario(user)}
                    <div>
                        <div class="perfil-nome">${user.username}</div>
                        <div class="perfil-usuario">${user.email}</div>
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

function avatarDoUsuario(user) {
    if (user.profilePictureUrl) {
        return `<img class="avatar" src="${user.profilePictureUrl}" alt="" />`;
    }

    return `<span class="avatar">${(user.username || "?").slice(0, 2).toUpperCase()}</span>`;
}

function navegarPara(id) {
    destinoAtual = id;
    renderCasca();
}

function renderDestino() {
    const conteudo = document.getElementById("conteudo");

    if (destinoAtual === "perfil") {
        renderPerfil(conteudo);
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

                <span class="perfil-nome">${user.username}</span>
                <span class="perfil-email">${user.email}</span>

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
                <div class="bloco">
                    <span class="bloco-titulo"><i class="ph-light ph-key"></i>${acao}</span>
                    <button type="button" id="show-password-form">${acao}</button>
                    <div id="password-area"></div>
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
            </div>
        </div>
    `;

    document.getElementById("escolher-foto").addEventListener("click", () => {
        document.getElementById("picture-input").click();
    });

    document.getElementById("picture-input").addEventListener("change", handleUploadPicture);

    const botaoRemover = document.getElementById("remove-picture");

    if (botaoRemover) {
        botaoRemover.addEventListener("click", handleRemovePicture);
    }

    document.getElementById("show-password-form").addEventListener("click", () => {
        renderPasswordForm(user);
    });

    document.getElementById("logout").addEventListener("click", handleLogout);
}

function fotoDePerfil(user) {
    if (user.profilePictureUrl) {
        return `<img class="perfil-foto" src="${user.profilePictureUrl}" alt="Foto de perfil" />`;
    }

    return `<span class="perfil-foto">${(user.username || "?").slice(0, 2).toUpperCase()}</span>`;
}

function renderPasswordForm(user) {
    const titulo = user.hasPassword ? "Alterar senha" : "Definir senha";

    const campoSenhaAtual = user.hasPassword
        ? `
            <label for="current-password">Senha atual</label>
            <input id="current-password" type="password" autocomplete="current-password" required />
        `
        : `
            <p>Sua conta foi criada com o Google e ainda não tem senha. Definindo uma, você poderá entrar das duas formas.</p>
        `;

    document.getElementById("password-area").innerHTML = `
        <form id="password-form">
            <h3>${titulo}</h3>

            ${campoSenhaAtual}

            <label for="new-password">Nova senha</label>
            <input id="new-password" type="password" autocomplete="new-password" required />
            <small>Mínimo de 8 caracteres, com letra maiúscula, minúscula e número.</small>

            <label for="confirm-new-password">Confirmar nova senha</label>
            <input id="confirm-new-password" type="password" autocomplete="new-password" required />

            <button type="submit">Salvar</button>
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