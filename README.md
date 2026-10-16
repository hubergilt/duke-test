# duke-test

Minimal WAR to check that a Java application server deploys and runs a web app.
Duke waves, and a live panel shows the server name, Java version, servlet spec, etc.

Three independent Maven projects, same code, different servlet API generation.
All produce `duke-test.war` (context path `/duke-test`): deploy only the one that
matches your server.

    duke-test/
    ├── jakarta/        jakarta.servlet 6.0   Java 17  -> target/duke-test.war  (/duke-test)
    ├── javax/          javax.servlet 4.0.1   Java 8   -> target/duke-test.war  (/duke-test)
    ├── javax-legacy/   javax.servlet 3.0.1   Java 8   -> target/duke-test.war  (/duke-test)
    └── README.md

Neither bundles dependencies (servlet API is `provided`). Servlets are declared in
`WEB-INF/web.xml`, so no annotation scanning is needed (works on Jetty with only
`ee*-deploy`).

## Which one for which server

| Folder | Servlet / EE | Servers |
|---|---|---|
| `jakarta/` | Servlet 6.0 / Jakarta EE 10 (also EE 11) | Tomcat 10.1 / 11, Jetty 12 (ee10 / ee11), WildFly 27+, Payara 6, GlassFish 7, Open Liberty (servlet-6.0) |
| `javax/` | Servlet 4.0 / Java EE 8 | Tomcat 9, Jetty 10 / 11 or Jetty 12 ee8, WildFly <= 26, Payara 5, GlassFish 5 / 6 |
| `javax-legacy/` | Servlet 3.0 / Java EE 6 | Resin 4 (Java 8), Tomcat 7 / 8, older Jetty 9, JBoss AS 7 / EAP 6, GlassFish 3 / 4 |

Rule of thumb: Tomcat 10+ and anything on `jakarta.*` -> `jakarta/`. Tomcat 9 and other
`javax.*` servers -> `javax/`. Servers that reject the EE 7/8 `web.xml` namespace
(`xmlns.jcp.org`), like Resin 4 -> `javax-legacy/`.

Using the wrong one gives `NoClassDefFoundError: jakarta/servlet/http/HttpServlet`
(javax WAR on a jakarta server fails the other way round: `javax/servlet/...`).

## Build

    cd jakarta       && mvn clean package   # target/duke-test.war
    cd javax         && mvn clean package   # target/duke-test.war
    cd javax-legacy  && mvn clean package   # target/duke-test.war

## Deploy

| Server | How |
|---|---|
| Tomcat | copy WAR to `$CATALINA_BASE/webapps/` |
| Resin | copy WAR to `<resin>/webapps/` |
| Jetty | copy WAR to `$JETTY_BASE/webapps/` (module `ee11-deploy`, `ee10-deploy` or `ee8-deploy`) |
| WildFly / JBoss EAP | copy WAR to `standalone/deployments/` |
| Payara / GlassFish | `asadmin deploy <war>` |
| Open Liberty | copy to `dropins/` |

## Test

    curl http://localhost:8080/duke-test/health            # -> OK
    curl http://localhost:8080/duke-test/api/info          # JSON

Then open the context path in a browser: Duke plus the live info panel.
