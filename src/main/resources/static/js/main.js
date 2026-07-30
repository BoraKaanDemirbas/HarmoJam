    // Her tarayıcı için sabit, rastgele bir kimlik oluştur/oku (misafir modu için).
    // Bu sayede giriş yapmamış herkesin favorileri kendi tarayıcısında ayrı tutulur.
    function getDeviceId() {
        let deviceId = localStorage.getItem('harmoJamDeviceId');
        if (!deviceId) {
            deviceId = crypto.randomUUID();
            localStorage.setItem('harmoJamDeviceId', deviceId);
        }
        return deviceId;
    }

    // --- HESAP / GİRİŞ SİSTEMİ ---
    function getAuthToken() {
        return localStorage.getItem('harmoJamAuthToken');
    }
    function setAuthToken(token) {
        localStorage.setItem('harmoJamAuthToken', token);
    }
    function clearAuthToken() {
        localStorage.removeItem('harmoJamAuthToken');
    }
    // Favoriler ile ilgili her isteğe eklenecek header'lar.
    // Giriş yapılmışsa Authorization önceliklidir (backend bunu X-Device-Id'ye tercih eder),
    // yapılmamışsa misafir kimliği (X-Device-Id) kullanılır.
    function getAuthHeaders() {
        const headers = { 'X-Device-Id': getDeviceId() };
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    }

    // --- GLOBAL AYARLAR ---
    const API_URL = window.location.origin;

    let lastSearchResults = [];
    let currentAudio = null;
    let currentBtn = null;
    let favoriteSongIds = new Set();

    // HTML Elementleri
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const resultsArea = document.getElementById('resultsArea');
    const favoritesArea = document.getElementById('favoritesArea');
    const statusText = document.getElementById('statusText');
    const historyContainer = document.getElementById('historyContainer');

    // Hesap ekranı elementleri
    const authUsernameInput = document.getElementById('authUsername');
    const authPasswordInput = document.getElementById('authPassword');
    const authError = document.getElementById('authError');
    const accountLoggedOut = document.getElementById('accountLoggedOut');
    const accountLoggedIn = document.getElementById('accountLoggedIn');
    const accountUsername = document.getElementById('accountUsername');

    function doRegister() {
        authError.innerText = '';
        fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: authUsernameInput.value.trim(), password: authPasswordInput.value })
        })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || 'Registration failed');
            setAuthToken(data.token);
            onLoginSuccess(data.username);
        })
        .catch(err => { authError.innerText = err.message; });
    }

    function doLogin() {
        authError.innerText = '';
        fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: authUsernameInput.value.trim(), password: authPasswordInput.value })
        })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || 'Login failed');
            setAuthToken(data.token);
            onLoginSuccess(data.username);
        })
        .catch(err => { authError.innerText = err.message; });
    }

    function doLogout() {
        clearAuthToken();
        accountLoggedIn.style.display = 'none';
        accountLoggedOut.style.display = 'flex';
        authUsernameInput.value = '';
        authPasswordInput.value = '';
        refreshFavoriteIds(); // favori listesini tekrar misafir kimliğine göre tazele
    }

    function onLoginSuccess(username) {
        accountUsername.innerText = username;
        accountLoggedOut.style.display = 'none';
        accountLoggedIn.style.display = 'flex';
        refreshFavoriteIds(); // favori listesini üyenin kendi verisine göre tazele
    }

    // Sayfa yenilendiğinde localStorage'daki token hâlâ geçerli mi diye kontrol eder.
    function checkExistingSession() {
        const token = getAuthToken();
        if (!token) return;
        fetch(`${API_URL}/auth/me`, { headers: { 'Authorization': 'Bearer ' + token } })
            .then(async res => {
                if (!res.ok) { clearAuthToken(); return; }
                const data = await res.json();
                onLoginSuccess(data.username);
            })
            .catch(() => clearAuthToken());
    }

    // Sayfa Yüklendiğinde
    window.addEventListener('load', () => {
        checkExistingSession();
        refreshFavoriteIds();
        renderHistory();
    });

    // --- 1. FONKSİYON: MÜZİK ARAMA ---
    function performSearch() {
        const query = searchInput.value.trim();
        if (!query) return;

        saveHistory(query);

        // UI Güncelle
        document.body.classList.add('results-mode');
        searchBtn.classList.add('loading');
        resultsArea.classList.remove('show');
        statusText.innerText = "Searching across the cosmos...";

        // DÜZELTME: Türkçe karakterler için encodeURIComponent şarttır.

        //fetch(`${API_URL}/search?q=${encodeURIComponent(query)}`)

        //const safeQuery = encodeURIComponent(query.trim());
        //fetch(`${API_URL}/search?q=${safeQuery}`)

        fetch(`${API_URL}/search`, {
            method: 'POST',
            body: query,  // Direkt string olarak gönderiyoruz
            headers: {
                'Content-Type': 'text/plain; charset=UTF-8' // UTF-8 olduğunu bağırıyoruz
            }
        })

            .then(response => response.json())
            .then(data => {
                data.forEach(song => {
                    if (!song.id) {
                        // Örn: "mix_BohemianRhapsody_Queen" gibi boşluksuz bir ID oluşturur
                        let sarkiAdi = song.isim || "track";
                        let sanatci = song.sarkici || song.artist || "artist";
                        song.id = "mix_" + sarkiAdi.replace(/\s+/g, "") + "_" + sanatci.replace(/\s+/g, "");
                    }
                });
                lastSearchResults = data;
                renderCards(data, 'search');

                if (data.length === 0) {
                     statusText.innerText = `No results found for "${query}"`;
                } else {
                     statusText.innerText = `Results for "${query}"`;
                }
            })
            .catch(err => {
                console.error(err);
                statusText.innerText = "Error connecting to the stars (Backend error)";
                resultsArea.innerHTML = `<div class="result-placeholder" style="color:#ff6b6b">Backend connection failed.</div>`;
                resultsArea.classList.add('show');
            })
            .finally(() => {
                searchBtn.classList.remove('loading');
                resultsArea.classList.add('show');
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
    }

    // --- 2. FONKSİYON: MIX / ÖNERİ GETİRME ---
    function oneriGetir(trackName, artistName) {
        showSearch();
        // UI Güncelle
        searchBtn.classList.add('loading');

        // ESKİ METİN GERİ GELDİ:
        statusText.innerText = `Finding vibes similar to "${trackName}"...`;

        document.body.classList.add('results-mode');
        resultsArea.classList.remove('show');

        // URL Oluşturma (Türkçe Karakter Korumalı)
        const url = `${API_URL}/recommend?track=${encodeURIComponent(trackName)}&artist=${encodeURIComponent(artistName)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                lastSearchResults = data;
                renderCards(data, 'search');
                // ESKİ METİN GERİ GELDİ:
                statusText.innerText = `Mix based on "${trackName}"`;
            })
            .catch(err => {
                console.error(err);
                statusText.innerText = "Signal lost...";
            })
            .finally(() => {
                searchBtn.classList.remove('loading');
                resultsArea.classList.add('show');
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
    }

    // --- KARTLARI ÇİZME ---
    function renderCards(data, mode) {
        const targetArea = (mode === 'favorites') ? favoritesArea : resultsArea;
        targetArea.innerHTML = "";

        if (!data || data.length === 0) {
            targetArea.innerHTML = `<div class="result-placeholder">No music found in this galaxy.</div>`;
            return;
        }

        data.forEach((sarki) => {
            // Tırnak işaretleri JS'yi bozmasın diye temizlik
            const safeName = sarki.isim ? sarki.isim.replace(/'/g, "\\'").replace(/"/g, '&quot;') : "";
            const safeArtist = sarki.sarkici ? sarki.sarkici.replace(/'/g, "\\'").replace(/"/g, '&quot;') : "";

            // Müzik Çalar Butonu
            let playBtnHtml = `<button class="action-btn play-btn" disabled style="opacity:0.5">No Audio</button>`;
            if (sarki.muzikUrl && sarki.muzikUrl !== "null") {
                const safeUrl = sarki.muzikUrl.replace(/'/g, "\\'");
                playBtnHtml = `<button class="action-btn play-btn" onclick="togglePlay(this, '${safeUrl}', '${sarki.id}', '${safeName}', '${safeArtist}')">▶ Play</button>`;
            }

            // Favori Butonu Mantığı
            const isFav = favoriteSongIds.has(sarki.id);
            let favBtnHtml = "";

            if (mode === 'favorites') {
                if (currentPlaylistId) {
                    // Bir özel liste görüntüleniyor: sadece o listeden çıkar, favorilerden silme
                    favBtnHtml = `
                        <button onclick="removeFromPlaylist('${sarki.id}')" class="action-btn"
                                style="background: linear-gradient(45deg, #424242, #212121); color: white; border: 1px solid #555;">
                            Remove<br>from list
                        </button>`;
                } else {
                    favBtnHtml = `
                        <button onclick="toggleFavorite('${sarki.id}', true)" class="action-btn"
                                style="background: linear-gradient(45deg, #424242, #212121); color: white; border: 1px solid #555;">
                            Remove
                        </button>`;
                }
            } else {
                if (isFav) {
                    favBtnHtml = `
                        <button id="fav-btn-search-${sarki.id}" onclick="toggleFavorite('${sarki.id}', false)" class="action-btn"
                                style="background: linear-gradient(45deg, #424242, #212121); color: white; border: none;">
                            Remove
                        </button>`;
                } else {
                    favBtnHtml = `
                        <button id="fav-btn-search-${sarki.id}" onclick="toggleFavorite('${sarki.id}', false)" class="action-btn"
                                style="background: linear-gradient(45deg, #D500F9, #FF4081); color: white; border: none; font-weight: bold;">
                            Favourite
                        </button>`;
                }
            }

            // Listeye ekleme menüsü: sadece favoriler ekranında ve en az bir özel liste varsa göster
            let playlistAddHtml = "";
            if (mode === 'favorites' && userPlaylists.length > 0) {
                const options = userPlaylists.map(p => {
                    const safePName = (p.name || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
                    return `<option value="${p.id}">${safePName}</option>`;
                }).join("");
                playlistAddHtml = `
                    <div class="playlist-add-row">
                        <select onchange="handlePlaylistSelect(this, '${sarki.id}')">
                            <option value="">+ Add to list...</option>
                            ${options}
                        </select>
                    </div>`;
            }

            const cardHTML = `
                <div class="card">
                    <img src="${sarki.resimUrl}" onerror="this.src='https://via.placeholder.com/300?text=No+Image'">
                    <h3>${sarki.isim}</h3>
                    <p>${sarki.sarkici}</p>
                    <div class="card-actions">
                        ${playBtnHtml}
                        ${favBtnHtml}
                    </div>
                    <button onclick="oneriGetir('${safeName}', '${safeArtist}')" class="action-btn recommend-btn" style="margin-top:10px;">
                        Mix
                    </button>
                    ${playlistAddHtml}
                </div>
            `;
            targetArea.innerHTML += cardHTML;
        });

        if (mode === 'favorites') targetArea.classList.add('show');
    }

    // --- DİĞER FONKSİYONLAR (Aynen Korundu) ---

    // Geçmiş Kaydetme
    function saveHistory(query) {
        let history = JSON.parse(localStorage.getItem('harmoJamHistory')) || [];
        history = history.filter(item => item !== query);
        history.unshift(query);
        if (history.length > 5) history.pop();
        localStorage.setItem('harmoJamHistory', JSON.stringify(history));
        renderHistory();
    }

    // Geçmişi Ekrana Basma
    function renderHistory() {
        let history = JSON.parse(localStorage.getItem('harmoJamHistory')) || [];
        historyContainer.innerHTML = "";
        if (history.length === 0) return;

        history.forEach(term => {
            const chip = document.createElement('div');
            chip.className = 'history-chip';
            chip.innerHTML = `<span>🕒 ${term}</span>`;
            chip.onclick = () => {
                searchInput.value = term;
                performSearch();
            };
            historyContainer.appendChild(chip);
        });

        const clearBtn = document.createElement('button');
        clearBtn.className = 'clear-history-btn';
        clearBtn.innerText = 'Clear';
        clearBtn.onclick = () => {
            localStorage.removeItem('harmoJamHistory');
            renderHistory();
        };
        historyContainer.appendChild(clearBtn);
    }

    // Favori ID'lerini Senkronize Et
    function refreshFavoriteIds() {
        fetch(`${API_URL}/favorites/all`, {
            headers: getAuthHeaders()
        })
            .then(res => res.json())
            .then(data => {
                favoriteSongIds.clear();
                data.forEach(song => favoriteSongIds.add(song.id));
                updateSearchButtons();
            })
            .catch(err => console.error("Sync Error:", err));
    }

    function toggleFavorite(id, fromFavoritesPage) {
        let sarki = lastSearchResults.find(s => s.id === id);
        const isAlreadyFav = favoriteSongIds.has(id);

        if (isAlreadyFav) {
            if(fromFavoritesPage && !confirm("Remove this song?")) return;
            fetch(`${API_URL}/favorites/delete/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            })
                .then(res => {
                    if(res.ok) {
                        favoriteSongIds.delete(id);
                        if(fromFavoritesPage) getFavorites();
                        updateSearchButtons();
                    }
                });
        } else {
            if (!sarki) return;
            fetch(`${API_URL}/favorites/add`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getAuthHeaders()
                },
                body: JSON.stringify(sarki)
            })
            .then(res => {
                if(res.ok) {
                    favoriteSongIds.add(id);
                    updateSearchButtons();
                }
            });
        }
    }

    function updateSearchButtons() {
        if(!lastSearchResults) return;
        lastSearchResults.forEach(sarki => {
            const btn = document.getElementById(`fav-btn-search-${sarki.id}`);
            if (btn) {
                if (favoriteSongIds.has(sarki.id)) {
                    btn.innerHTML = "Remove";
                    btn.style.background = "linear-gradient(45deg, #424242, #212121)";
                } else {
                    btn.innerHTML = "Favourite";
                    btn.style.background = "linear-gradient(45deg, #D500F9, #FF4081)";
                }
            }
        });
    }


    // Şu an görüntülenen liste: null = varsayılan "Favoriler" (tüm favoriler), sayı = özel liste id'si
    let currentPlaylistId = null;
    let userPlaylists = [];
    let currentFavoritesData = [];

    function loadPlaylists() {
        fetch(`${API_URL}/playlists`, { headers: getAuthHeaders() })
            .then(res => res.json())
            .then(data => {
                userPlaylists = data;
                renderPlaylistBar();

                if (currentFavoritesData.length > 0) {
                    renderCards(currentFavoritesData, 'favorites');
                }

            })
            .catch(err => console.error("Playlist yükleme hatası:", err));
    }

    function renderPlaylistBar() {
        const bar = document.getElementById('playlistBar');
        let html = `<button class="playlist-pill ${currentPlaylistId === null ? 'active' : ''}" onclick="selectPlaylist(null)">VAULT</button>`;//Favorites

        userPlaylists.forEach(p => {
            const safeName = (p.name || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
            html += `
                <div class="playlist-pill ${currentPlaylistId === p.id ? 'active' : ''}">
                    <span onclick="selectPlaylist(${p.id})">${p.name} (${p.songCount})</span>
                    <span class="playlist-delete-x" onclick="event.stopPropagation(); deletePlaylistPrompt(${p.id}, '${safeName}')">✕</span>
                </div>`;
        });

        html += `<button class="playlist-pill new-playlist-btn" onclick="createPlaylistPrompt()">+ New List</button>`;
        bar.innerHTML = html;
    }

    function selectPlaylist(id) {
        currentPlaylistId = id;
        document.getElementById('favSearchInput').value = '';
        renderPlaylistBar();
        getFavorites();
    }

    function createPlaylistPrompt() {
        const name = prompt("New list name:");
        if (!name || !name.trim()) return;
        fetch(`${API_URL}/playlists`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
            body: JSON.stringify({ name: name.trim() })
        })
            .then(res => res.ok ? loadPlaylists() : alert("List could not be created."))
            .catch(err => console.error("Liste oluşturma hatası:", err));
    }

    function deletePlaylistPrompt(id, name) {
        if (!confirm(`Delete list "${name}"? (Songs stay in your favorites)`)) return;
        fetch(`${API_URL}/playlists/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        }).then(res => {
            if (res.ok) {
                if (currentPlaylistId === id) currentPlaylistId = null;
                loadPlaylists();
                getFavorites();
            }
        });
    }

    function handlePlaylistSelect(selectEl, songId) {
        const playlistId = selectEl.value;
        if (!playlistId) return;
        fetch(`${API_URL}/playlists/${playlistId}/songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
            body: JSON.stringify({ songId: songId })
        }).then(res => {
            selectEl.selectedIndex = 0;
            if (res.ok) {
                loadPlaylists(); // liste şarkı sayısını (songCount) güncellemek için
            } else {
                alert("Could not add to list.");
            }
        });
    }

    function removeFromPlaylist(songId) {
        if (!confirm("Remove this song from the list?")) return;
        fetch(`${API_URL}/playlists/${currentPlaylistId}/songs/${songId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        }).then(res => {
            if (res.ok) {
                loadPlaylists();
                getFavorites();
            }
        });
    }

    function filterFavoritesView() {
        const term = document.getElementById('favSearchInput').value.trim().toLocaleLowerCase('tr');
        if (!term) {
            if (currentFavoritesData.length === 0) {
                favoritesArea.innerHTML = '<div class="result-placeholder">This list is empty.</div>';
            } else {
                renderCards(currentFavoritesData, 'favorites');
            }
            return;
        }
        const filtered = currentFavoritesData.filter(s => {
            const name = (s.isim || '').toLocaleLowerCase('tr');
            const artist = (s.sarkici || '').toLocaleLowerCase('tr');
            return name.includes(term) || artist.includes(term);
        });
        if (filtered.length === 0) {
            favoritesArea.innerHTML = '<div class="result-placeholder">No matches in this list.</div>';
        } else {
            renderCards(filtered, 'favorites');
        }
    }

    function getFavorites() {
        favoritesArea.innerHTML = '<div class="result-placeholder">Loading...</div>';

        const endpoint = currentPlaylistId
            ? `${API_URL}/playlists/${currentPlaylistId}/songs`
            : `${API_URL}/favorites/all`;

        fetch(endpoint, { headers: getAuthHeaders() })
            .then(res => res.json())
            .then(data => {
                if (!currentPlaylistId) {
                    // Global favori ID setini sadece "tüm favoriler" görünümündeyken güncelle;
                    // bir alt-liste (playlist), tüm favorilerin sadece bir kısmını içerir.
                    favoriteSongIds.clear();
                    data.forEach(s => favoriteSongIds.add(s.id));
                }
                currentFavoritesData = data;
                if (data.length === 0) {
                    favoritesArea.innerHTML = '<div class="result-placeholder">This list is empty.</div>';
                } else {
                    renderCards(data, 'favorites');
                }
            });
    }

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
            currentAudio.onended = function() { btn.innerHTML = "▶ Play"; };
            logPlay(songId, isim, sarkici);
        }
    }

    // Preview çalma istatistiği için backend'e log at
    function logPlay(songId, isim, sarkici) {
        fetch(`${API_URL}/api/plays/log`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: songId, isim: isim, sarkici: sarkici })
        }).catch(err => console.error("Play log error:", err));
    }

    // Sayfa Geçişleri
    const searchView = document.getElementById('search-view');
    const favoritesView = document.getElementById('favorites-view');
    const accountView = document.getElementById('account-view');
    const btnSearch = document.getElementById('btn-search');
    const btnFav = document.getElementById('btn-fav');
    const btnAccount = document.getElementById('btn-account');

    function showSearch() {
        searchView.style.display = 'flex';
        favoritesView.style.display = 'none';
        accountView.style.display = 'none';
        btnSearch.classList.add('active');
        btnFav.classList.remove('active');
        btnAccount.classList.remove('active');
        updateSearchButtons();
    }

    function showFavorites() {
        searchView.style.display = 'none';
        favoritesView.style.display = 'flex';
        accountView.style.display = 'none';
        btnFav.classList.add('active');
        btnSearch.classList.remove('active');
        btnAccount.classList.remove('active');
        loadPlaylists();
        getFavorites();
    }

    function showAccount() {
        searchView.style.display = 'none';
        favoritesView.style.display = 'none';
        accountView.style.display = 'flex';
        btnAccount.classList.add('active');
        btnSearch.classList.remove('active');
        btnFav.classList.remove('active');
    }

    // Event Listeners
    searchBtn.addEventListener('click', performSearch);
    searchInput.addEventListener('keypress', (e) => { if (e.key === 'Enter') performSearch(); });

    function resetToHome() {
        // Arama kutusunu ve sonuçları temizleyip arama ekranına dön.
        // Not: eskiden burada favorites-view.innerHTML = '' yapılıyordu, bu da
        // playlist çubuğu/arama kutusu dahil favoriler ekranının tamamını kalıcı
        // olarak siliyordu (bir daha asla geri gelmiyordu). Artık sadece arama
        // ekranını sıfırlıyoruz.
        if (searchInput) searchInput.value = '';
        resultsArea.innerHTML = '';
        showSearch();
    }

