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

    const body = await response.json().catch(() => null);

    if (!response.ok) {
        const message =
            body?.error ||
            Object.values(body || {}).join("\n") ||
            `Erro na API: ${response.status}`;

        throw new Error(message);
    }

    return body;
}