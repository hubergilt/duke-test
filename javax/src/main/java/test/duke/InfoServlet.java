package test.duke;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Returns runtime information as JSON so you can see which server/JVM is running the WAR. */
public class InfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = getServletContext();

        // Session counter: proves sessions work (and shows cluster/sticky behavior if any).
        HttpSession session = req.getSession(true);
        int hits = session.getAttribute("hits") == null ? 1 : (Integer) session.getAttribute("hits") + 1;
        session.setAttribute("hits", hits);

        Duration uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("server", ctx.getServerInfo());
        info.put("servletSpec", ctx.getMajorVersion() + "." + ctx.getMinorVersion());
        info.put("contextPath", ctx.getContextPath());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("jvm", System.getProperty("java.vm.name"));
        info.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        info.put("cpus", Runtime.getRuntime().availableProcessors());
        info.put("maxMemoryMB", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        info.put("jvmUptime", String.format("%dh %dm %ds",
                uptime.toHours(), uptime.toMinutesPart(), uptime.toSecondsPart()));
        info.put("serverTime", OffsetDateTime.now().toString());
        info.put("thread", Thread.currentThread().getName());
        info.put("clientAddress", req.getRemoteAddr());
        info.put("protocol", req.getProtocol());
        info.put("sessionHits", hits);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        PrintWriter out = resp.getWriter();
        out.print(toJson(info));
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v instanceof Number) sb.append(v);
            else sb.append('"').append(escape(String.valueOf(v))).append('"');
        }
        return sb.append('}').toString();
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
