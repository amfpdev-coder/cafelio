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