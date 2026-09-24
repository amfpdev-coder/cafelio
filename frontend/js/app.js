async function renderWelcomeScreen() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <h1>Bem-vindo ao Cafélio ☕</h1>
        <p>Seu planner literário está quase pronto...</p>
        <p id="api-status">Verificando conexão com a API...</p>
        <div id="auth-area"></div>
    `;

    await loadSession();

    try {
        const health = await apiGet("/health");
        document.getElementById("api-status").textContent =
        `API conectada — status: ${health.status}`;
    } catch (error) {
        document.getElementById("api-status").textContent =
        "Não foi possível conectar à API.";
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

    if (button) {
        button.style.display = visible ? "" : "none";
    }
}

function renderLoginForm(identifier = "") {
    showGoogleButton(true);

    document.getElementById("auth-area").innerHTML = `
        <form id="login-form">
            <h2>Entrar</h2>

            <label for="identifier">E-mail ou nome de usuário</label>
            <input id="identifier" type="text" autocomplete="username" required />

            <label for="password">Senha</label>
            <input id="password" type="password" autocomplete="current-password" required />

            <button type="submit">Entrar</button>
            <p id="login-message" class="form-message"></p>
            <p><a href="#" id="show-forgot">Esqueci minha senha</a></p>

            <p>Não tem conta? <a href="#" id="show-register">Criar conta</a></p>
        </form>
    `;

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
}

function renderRegisterForm() {
    showGoogleButton(true);

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
}

function renderLoggedInArea(user) {
    showGoogleButton(false);

    const acao = user.hasPassword ? "Alterar senha" : "Definir senha";

    document.getElementById("auth-area").innerHTML = `
        <h2 id="greeting"></h2>
        <p id="account-email"></p>

        <button id="show-password-form">${acao}</button>
        <button id="logout">Sair</button>

        <div id="password-area"></div>
    `;

    document.getElementById("greeting").textContent = `Olá, ${user.username}!`;
    document.getElementById("account-email").textContent = `Você entrou como ${user.email}.`;

    document.getElementById("show-password-form").addEventListener("click", () => {
        renderPasswordForm(user);
    });
    document.getElementById("logout").addEventListener("click", handleLogout);
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
}

renderWelcomeScreen();

function renderForgotPasswordForm() {
    showGoogleButton(false);

    document.getElementById("auth-area").innerHTML = `
        <form id="forgot-form">
            <h2>Esqueci minha senha</h2>
            <p>Informe seu e-mail e enviaremos um link para você escolher uma senha nova.</p>

            <label for="forgot-email">E-mail</label>
            <input id="forgot-email" type="email" autocomplete="email" required />

            <button type="submit">Enviar link</button>
            <p id="forgot-message" class="form-message"></p>

            <p><a href="#" id="back-to-login">Voltar para o login</a></p>
        </form>
    `;

    document.getElementById("forgot-form").addEventListener("submit", handleForgotPassword);
    document.getElementById("back-to-login").addEventListener("click", (event) => {
        event.preventDefault();
        renderLoginForm();
    });
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
    clearToken();
    renderLoginForm();
}

async function handleForgotPassword(event) {
    event.preventDefault();

    const message = document.getElementById("forgot-message");
    message.textContent = "Enviando...";

    try {
        await apiPost("/auth/password/reset-request", {
            email: document.getElementById("forgot-email").value,
        });

        message.textContent =
            "Se esse e-mail estiver cadastrado, enviamos um link para redefinir a senha. " +
            "O link vale por 30 minutos.";
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