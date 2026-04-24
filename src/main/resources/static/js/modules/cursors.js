/**
 * Live cursors - shows the mouse pointer of every other user currently
 * looking at the web panel. Coordinates are sent normalized (0..1) over a
 * WebSocket on /ws/cursors and rendered as little floating pointers with a
 * pseudo-random name + color per session.
 */
(function () {
    const ADJECTIVES = [
        'Funky', 'Sleepy', 'Groovy', 'Cosmic', 'Spicy', 'Mellow', 'Jazzy',
        'Glitchy', 'Velvet', 'Neon', 'Lofi', 'Disco', 'Retro', 'Snazzy',
        'Wobbly', 'Bouncy', 'Cheesy', 'Sparkly', 'Smooth', 'Loud'
    ];
    const ANIMALS = [
        'Otter', 'Panda', 'Falcon', 'Llama', 'Hedgehog', 'Axolotl', 'Octopus',
        'Walrus', 'Penguin', 'Capybara', 'Yak', 'Narwhal', 'Sloth', 'Frog',
        'Raccoon', 'Goose', 'Tapir', 'Quokka', 'Lemur', 'Badger'
    ];

    function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }

    function loadIdentity() {
        try {
            const cached = JSON.parse(localStorage.getItem('liveCursorIdentity') || 'null');
            if (cached && cached.name && cached.color) return cached;
        } catch (e) { /* ignore */ }
        const hue = Math.floor(Math.random() * 360);
        const identity = {
            name: pick(ADJECTIVES) + ' ' + pick(ANIMALS),
            color: `hsl(${hue}, 85%, 60%)`
        };
        try { localStorage.setItem('liveCursorIdentity', JSON.stringify(identity)); } catch (e) { /* ignore */ }
        return identity;
    }

    const identity = loadIdentity();
    const peers = new Map(); // id -> { el, lastX, lastY, targetX, targetY }
    let myId = null;
    let socket = null;
    let layer = null;
    let pendingPayload = null;
    let sendTimer = null;
    let lastNormX = null;
    let lastNormY = null;
    let reconnectDelay = 800;
    const SEND_INTERVAL_MS = 50;
    // Canonical identifier for the current page so peers on /, /index.html
    // (the spring root view forwards / -> /index.html) and other variants
    // are bucketed together correctly. The web panel is a SPA that uses
    // history.pushState, so we recompute it on every navigation.
    let currentPage = canonicalPage(location.pathname);

    function canonicalPage(path) {
        let p = (path || '/').toLowerCase();
        // strip trailing slashes
        p = p.replace(/\/+$/, '');
        // strip .html extension
        p = p.replace(/\.html?$/i, '');
        // collapse "" or "/index" -> "index"
        if (p === '' || p === '/' || p === '/index') return 'index';
        // strip leading slash
        return p.replace(/^\/+/, '');
    }

    /** Recompute currentPage; if it changed, drop every visible peer (they
     *  were on the old page) and announce our new location. */
    function refreshCurrentPage() {
        const next = canonicalPage(location.pathname);
        if (next === currentPage) return;
        currentPage = next;
        for (const id of Array.from(peers.keys())) removePeer(id);
        announcePresence();
    }

    function announcePresence() {
        if (!socket || socket.readyState !== WebSocket.OPEN) return;
        const payload = {
            name: identity.name,
            color: identity.color,
            page: currentPage
        };
        if (lastNormX !== null && lastNormY !== null) {
            payload.x = lastNormX;
            payload.y = lastNormY;
        }
        try { socket.send(JSON.stringify(payload)); } catch (e) { /* ignore */ }
    }

    function ensureLayer() {
        if (layer) return layer;
        layer = document.createElement('div');
        layer.id = 'live-cursors-layer';
        Object.assign(layer.style, {
            position: 'fixed',
            inset: '0',
            pointerEvents: 'none',
            zIndex: '2147483600',
            overflow: 'hidden'
        });
        document.body.appendChild(layer);
        return layer;
    }

    function buildCursorElement(name, color) {
        const wrap = document.createElement('div');
        Object.assign(wrap.style, {
            position: 'absolute',
            left: '0',
            top: '0',
            transform: 'translate(-9999px,-9999px)',
            transition: 'transform 90ms linear',
            willChange: 'transform',
            pointerEvents: 'none'
        });
        wrap.innerHTML = `
            <svg width="22" height="22" viewBox="0 0 22 22" style="display:block;filter:drop-shadow(0 1px 2px rgba(0,0,0,.35));">
                <path d="M2 2 L2 18 L7 14 L10 20 L13 19 L10 13 L17 13 Z"
                      fill="${color}" stroke="white" stroke-width="1.2" stroke-linejoin="round"/>
            </svg>
            <div class="live-cursor-label" style="
                margin: 2px 0 0 14px;
                padding: 2px 6px;
                font: 600 11px/1.2 system-ui, sans-serif;
                color: #fff;
                background: ${color};
                border-radius: 6px;
                white-space: nowrap;
                box-shadow: 0 1px 2px rgba(0,0,0,.35);
            ">${escapeHtml(name)}</div>
        `;
        return wrap;
    }

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function upsertPeer(id, name, color) {
        let peer = peers.get(id);
        if (!peer) {
            const el = buildCursorElement(name || 'Anonymous', color || '#888');
            ensureLayer().appendChild(el);
            peer = { el, normX: null, normY: null };
            peers.set(id, peer);
        }
        return peer;
    }

    function removePeer(id) {
        const peer = peers.get(id);
        if (!peer) return;
        peer.el.remove();
        peers.delete(id);
    }

    /**
     * Returns information about the shared layout anchor (the .main-content
     * container, which has the same intrinsic max-width on every client).
     *
     *  - rect : viewport-relative bounding rect of the visible portion
     *  - cw   : content width  (logical horizontal extent shared between peers)
     *  - ch   : content height (logical vertical extent, may exceed the rect
     *           if the container is internally scrollable)
     *  - sx   : current horizontal scroll offset inside the container
     *  - sy   : current vertical scroll offset inside the container
     */
    function getAnchor() {
        const el = document.querySelector('.main-content');
        if (!el) {
            return {
                rect: { left: 0, top: 0, width: window.innerWidth, height: window.innerHeight },
                cw: window.innerWidth, ch: window.innerHeight, sx: 0, sy: 0
            };
        }
        const r = el.getBoundingClientRect();
        return {
            rect: { left: r.left, top: r.top, width: r.width, height: r.height },
            cw: Math.max(1, el.clientWidth),
            ch: Math.max(1, el.scrollHeight || el.clientHeight),
            sx: el.scrollLeft || 0,
            sy: el.scrollTop || 0
        };
    }

    function placePeer(peer, normX, normY) {
        peer.normX = normX;
        peer.normY = normY;
        const a = getAnchor();
        // normalized coords are in the shared logical content space; convert
        // them to a position inside our own container, then to viewport px.
        const localX = normX * a.cw;
        const localY = normY * a.ch;
        const x = Math.round(a.rect.left + localX - a.sx);
        const y = Math.round(a.rect.top + localY - a.sy);
        // Hide cursors that fall outside the visible content area.
        if (y < a.rect.top - 40 || y > a.rect.top + a.rect.height + 40) {
            peer.el.style.opacity = '0';
        } else {
            peer.el.style.opacity = '1';
        }
        peer.el.style.transform = `translate(${x}px, ${y}px)`;
    }

    /** Re-place every known peer; called on scroll/resize so cursors stick to
     *  their logical content position rather than to the viewport. */
    function reflowPeers() {
        for (const peer of peers.values()) {
            if (peer.normX !== null && peer.normY !== null) {
                placePeer(peer, peer.normX, peer.normY);
            }
        }
    }

    function handleMessage(raw) {
        let msg;
        try { msg = JSON.parse(raw); } catch (e) { return; }
        if (!msg || !msg.type) return;
        if (msg.type === 'hello') {
            myId = msg.id;
            return;
        }
        if (msg.type === 'leave') {
            removePeer(msg.id);
            return;
        }
        if (msg.type === 'move') {
            // Strict page filter: drop messages from peers on a different
            // page (or from older clients that don't advertise their page).
            if (msg.page !== currentPage) {
                removePeer(msg.id);
                return;
            }
            // Move messages without coordinates (e.g. click-only) shouldn't
            // teleport the cursor to (0,0).
            const hasCoords = typeof msg.x === 'number' && typeof msg.y === 'number';
            if (!hasCoords && !peers.has(msg.id)) return;
            const peer = upsertPeer(msg.id, msg.name, msg.color);
            if (hasCoords) placePeer(peer, msg.x, msg.y);
            if (msg.click) flashClick(peer, msg.color);
            return;
        }
    }

    function flashClick(peer, color) {
        const ring = document.createElement('div');
        Object.assign(ring.style, {
            position: 'absolute',
            left: '0', top: '0',
            width: '6px', height: '6px',
            borderRadius: '50%',
            border: `2px solid ${color || '#fff'}`,
            transform: peer.el.style.transform,
            opacity: '0.9',
            pointerEvents: 'none',
            transition: 'transform 400ms ease-out, opacity 400ms ease-out'
        });
        ensureLayer().appendChild(ring);
        // Force layout then animate
        requestAnimationFrame(() => {
            const m = /translate\(([-\d.]+)px,\s*([-\d.]+)px\)/.exec(peer.el.style.transform);
            const x = m ? parseFloat(m[1]) : 0;
            const y = m ? parseFloat(m[2]) : 0;
            ring.style.transform = `translate(${x - 14}px, ${y - 14}px) scale(6)`;
            ring.style.opacity = '0';
        });
        setTimeout(() => ring.remove(), 450);
    }

    function scheduleSend() {
        if (sendTimer) return;
        sendTimer = setTimeout(() => {
            sendTimer = null;
            if (pendingPayload && socket && socket.readyState === WebSocket.OPEN) {
                try { socket.send(JSON.stringify(pendingPayload)); } catch (e) { /* ignore */ }
                pendingPayload = null;
            }
        }, SEND_INTERVAL_MS);
    }

    function attachInputListeners() {
        window.addEventListener('mousemove', e => {
            const a = getAnchor();
            // Convert viewport coords -> position inside the shared content
            // (taking our own scroll into account), then normalize.
            lastNormX = (e.clientX - a.rect.left + a.sx) / a.cw;
            lastNormY = (e.clientY - a.rect.top + a.sy) / a.ch;
            pendingPayload = {
                x: lastNormX,
                y: lastNormY,
                name: identity.name,
                color: identity.color,
                page: currentPage
            };
            scheduleSend();
        }, { passive: true });

        window.addEventListener('mousedown', () => {
            // Always include the last known coordinates so peers don't see us
            // teleport to (0,0) when we click without having moved recently.
            const payload = {
                name: identity.name,
                color: identity.color,
                page: currentPage,
                click: true
            };
            if (lastNormX !== null && lastNormY !== null) {
                payload.x = lastNormX;
                payload.y = lastNormY;
            }
            if (socket && socket.readyState === WebSocket.OPEN) {
                try { socket.send(JSON.stringify(payload)); } catch (e) { /* ignore */ }
            }
            // The next mousemove will refresh pendingPayload; clear any stale one.
            pendingPayload = null;
            if (sendTimer) { clearTimeout(sendTimer); sendTimer = null; }
        });

        // Hide our pointer to other users when leaving the page tab
        window.addEventListener('blur', () => {
            if (socket && socket.readyState === WebSocket.OPEN) {
                // Send a "leave" by claiming we're on a sentinel page; peers
                // will remove our cursor.
                try { socket.send(JSON.stringify({ name: identity.name, color: identity.color, page: '__away__' })); } catch (e) { /* ignore */ }
            }
        });

        // Keep peers visually anchored to the shared content when our own
        // viewport changes (resize, scroll inside .main-content, page scroll).
        window.addEventListener('resize', reflowPeers);
        window.addEventListener('scroll', reflowPeers, { passive: true, capture: true });
        const mc = document.querySelector('.main-content');
        if (mc) mc.addEventListener('scroll', reflowPeers, { passive: true });
    }

    function connect() {
        const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const url = `${proto}//${location.host}/ws/cursors`;
        try {
            socket = new WebSocket(url);
        } catch (e) {
            // Server not ready yet (e.g. during the bot's initial boot); retry quickly.
            setTimeout(connect, 800);
            return;
        }
        socket.addEventListener('open', () => {
            reconnectDelay = 800;
            // Make our presence known on this page right away so peers don't
            // have to wait for the first mousemove to see us.
            announcePresence();
        });
        socket.addEventListener('message', e => handleMessage(e.data));
        socket.addEventListener('close', () => {
            for (const id of Array.from(peers.keys())) removePeer(id);
            setTimeout(connect, reconnectDelay);
            // Cap at 5s but stay aggressive at first so the cursors appear
            // quickly during the bot's initial startup.
            reconnectDelay = Math.min(reconnectDelay * 2, 5000);
        });
        socket.addEventListener('error', () => { try { socket.close(); } catch (e) { /* ignore */ } });
    }

    function start() {
        ensureLayer();
        attachInputListeners();
        // Listen for SPA navigations (the panel uses history.pushState).
        window.addEventListener('popstate', refreshCurrentPage);
        const wrap = (name) => {
            const orig = history[name];
            if (typeof orig !== 'function') return;
            history[name] = function () {
                const ret = orig.apply(this, arguments);
                refreshCurrentPage();
                return ret;
            };
        };
        wrap('pushState');
        wrap('replaceState');
        connect();
    }

    // Connect as soon as the script is parsed so the initial cursor exchange
    // happens in parallel with the rest of the page bootstrap. Falls back to
    // DOMContentLoaded if document.body isn't available yet.
    if (!document.body) {
        document.addEventListener('DOMContentLoaded', start);
    } else {
        start();
    }
})();
