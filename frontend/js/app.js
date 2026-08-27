async function renderWelcomeScreen() {
    const app = document.getElementById("app");

    app.innerHTML = `
        <h1>Bem-vindo ao Cafélio ☕</h1>
        <p>Seu planner literário está quase pronto...</p>
        <p id="api-status">Verificando conexão com a API...</p>
    `;

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
    try {
        const result = await apiPost("/auth/google", { idToken: response.credential });
        localStorage.setItem("cafelio_token", result.token);
        alert("Login com Google realizado com sucesso!");
    } catch (error) {
        alert("Erro ao fazer login com Google.");
    }
}