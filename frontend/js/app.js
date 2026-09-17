async function renderWelcomeScreen() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <h1>Bem-vindo ao Cafélio ☕</h1>
        <p>Seu planner literário está quase pronto...</p>
        <p id="api-status">Verificando conexão com a API...</p>
        <form id="login-form">
            <h2>Entrar</h2>

            <label for="identifier">E-mail ou nome de usuário</label>
            <input id="identifier" type="text" autocomplete="username" required />

            <label for="password">Senha</label>
            <input id="password" type="password" autocomplete="current-password" required />

            <button type="submit">Entrar</button>
            <p id="login-message"></p>
        </form>
    `;

    document.getElementById("login-form").addEventListener("submit", handleLogin);

    try {
        const health = await apiGet("/health");
        document.getElementById("api-status").textContent =
        `API conectada — status: ${health.status}`;
    } catch (error) {
        document.getElementById("api-status").textContent =
        "Não foi possível conectar à API.";
    }
}

renderWelcomeScreen();

async function handleGoogleLogin(response) {
    const message = document.getElementById("login-message");
    message.textContent = "Entrando...";

    try {
        const result = await apiPost("/auth/google", { idToken: response.credential });
        localStorage.setItem("cafelio_token", result.token);
        message.textContent = "Login com Google realizado com sucesso!";
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

        localStorage.setItem("cafelio_token", result.token);
        message.textContent = "Login realizado com sucesso!";
    } catch (error) {
        message.textContent = error.message;
    }
}