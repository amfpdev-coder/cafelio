const API_BASE_URL = "http://127.0.0.1:8000";

async function apiGet(path) {
    const response = await fetch(`${API_BASE_URL}${path}`);

    if (!response.ok) {
        throw new Error(`Erro na API: ${response.status}`);
    }

    return response.json();
}

async function apiPost(path, data) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(`Erro na API: ${response.status}`);
    }

    return response.json();
}