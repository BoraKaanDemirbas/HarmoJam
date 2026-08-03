// shared.js — link ile paylaşılan bir playlist'i, GİRİŞ YAPMADAN görüntüleyen sayfa.
// main.js'ten kasıtlı olarak bağımsız: burada favori/hesap/arama sistemi yok,
// sadece "shareToken" ile gelen salt-okunur bir liste var.

const API_URL = window.location.origin;
let currentAudio = null;
let currentBtn = null;

function getTokenFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('token');
}

// main.js'teki togglePlay ile aynı mantık — burada favori/istatistik butonları yok,
// sadece önizleme çalma + play log'u (login gerektirmiyor, herkes için açık).
function togglePlay(btn, url, songId, isim, sarkici) {
    if (currentAudio && currentAudio.src === url) {
        if (currentAudio.paused) { currentAudio.play(); btn.innerHTML = "⏸ Stop"; }
        else { currentAudio.pause(); btn.innerHTML = "▶ Play"; }
    } else {
        if (currentAudio) { currentAudio.pause(); if (currentBtn) currentBtn.innerHTML = "▶ Play"; }
        currentAudio = new Audio(url);
        currentAudio.play();
        currentBtn = btn;
        btn.innerHTML = "⏸ Stop";
        currentAudio.onended = function () { btn.innerHTML = "▶ Play"; };

        fetch(`${API_URL}/api/plays/log`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: songId, isim: isim, sarkici: sarkici })
        }).catch(() => {});
    }
}

function renderSharedCards(songs) {
    const area = document.getElementById('sharedArea');
    area.innerHTML = "";

    if (!songs || songs.length === 0) {
        area.innerHTML = `<div class="result-placeholder">This list is empty.</div>`;
        return;
    }

    songs.forEach((sarki, index) => {
        const safeName = sarki.isim ? sarki.isim.replace(/'/g, "\\'").replace(/"/g, '&quot;') : "";
        const safeArtist = sarki.sarkici ? sarki.sarkici.replace(/'/g, "\\'").replace(/"/g, '&quot;') : "";

        let playBtnHtml = `<button class="action-btn play-btn" disabled style="opacity:0.5">No Audio</button>`;
        if (sarki.muzikUrl && sarki.muzikUrl !== "null") {
            const safeUrl = sarki.muzikUrl.replace(/'/g, "\\'");
            playBtnHtml = `<button class="action-btn play-btn" onclick="togglePlay(this, '${safeUrl}', '${sarki.id}', '${safeName}', '${safeArtist}')">▶ Play</button>`;
        }

        const staggerIndex = Math.min(index, 10); // aynı yumuşak-giriş efekti burada da çalışsın diye
        const cardHTML = `
            <div class="card" style="--i:${staggerIndex}">
                <img src="${sarki.resimUrl}" onerror="this.src='https://via.placeholder.com/300?text=No+Image'">
                <h3>${sarki.isim}</h3>
                <p>${sarki.sarkici}</p>
                <div class="card-actions">
                    ${playBtnHtml}
                </div>
            </div>`;
        area.innerHTML += cardHTML;
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const token = getTokenFromUrl();
    const titleEl = document.getElementById('sharedPlaylistTitle');

    if (!token) {
        titleEl.innerText = "No share link provided.";
        return;
    }

    fetch(`${API_URL}/public/playlists/${token}`)
        .then(res => {
            if (!res.ok) {
                return res.json().then(data => Promise.reject(new Error(data.error || "This share link is invalid or no longer active.")));
            }
            return res.json();
        })
        .then(data => {
            titleEl.innerText = `"${data.name}" — shared list`;
            renderSharedCards(data.songs);
        })
        .catch(err => {
            titleEl.innerText = err.message || "This share link is invalid or no longer active.";
        });
});
