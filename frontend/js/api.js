const API_BASE_URL = "http://127.0.0.1:8000";
const TOKEN_KEY = "cafelio_token";

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

async function apiRequest(method, path, data) {
    const headers = {};
    const token = getToken();

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    if (data !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        method,
        headers,
        body: data === undefined ? undefined : JSON.stringify(data),
    });

    const body = await response.json().catch(() => null);

    if (response.status === 401) {
        clearToken();
    }

    if (!response.ok) {
        const message =
            body?.error ||
            Object.values(body || {}).join("\n") ||
            `Erro na API: ${response.status}`;

        throw new Error(message);
    }

    return body;
}

async function apiGet(path) {
    return apiRequest("GET", path);
}

async function apiPost(path, data) {
    return apiRequest("POST", path, data);
}

async function apiPut(path, data) {
    return apiRequest("PUT", path, data);
}
