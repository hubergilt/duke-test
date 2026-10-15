# duke-test

Minimal WAR to check that a Java application server deploys and runs a web app.
Duke waves, and a live panel shows the server name, Java version, servlet spec, etc.

Two independent Maven projects, same code, different servlet API namespace:

    duke-test/
    ├── jakarta/   jakarta.servlet 6.0  -> target/duke-test.war       (/duke-test)
    ├── javax/     javax.servlet 4.0.1  -> target/duke-test.war       (/duke-test)
    └── README.md

Both compile to Java 17 bytecode (run on 17, 21, 25) and bundle no dependencies
(servlet API is `provided`). Servlets are declared in `WEB-INF/web.xml`, so no
annotation scanning is needed (works on Jetty with only `ee*-deploy`).

## Which one for which server

| Folder     | Servers                                                                                                  |
| ---------- | -------------------------------------------------------------------------------------------------------- |
| `jakarta/` | Tomcat 10.1 / 11, Jetty 12 (ee10 / ee11), WildFly 27+, Payara 6, GlassFish 7, Open Liberty (servlet-6.0) |
| `javax/`   | Tomcat 9, Jetty 10 / 11 (ee8) or Jetty 12 ee8, WildFly <= 26, Payara 5, GlassFish 5/6                    |

Using the wrong one gives `NoClassDefFoundError: jakarta/servlet/http/HttpServlet`
(javax WAR missing on a jakarta server is the reverse: `javax/servlet/...`).

## Build

    cd jakarta && mvn clean package     # target/duke-test.war
    cd javax   && mvn clean package     # target/duke-test.war

## Deploy

| Server              | How                                                                                      |
| ------------------- | ---------------------------------------------------------------------------------------- |
| Tomcat              | copy WAR to `$CATALINA_BASE/webapps/`                                                    |
| Jetty               | copy WAR to `$JETTY_BASE/webapps/` (module `ee11-deploy`, `ee10-deploy` or `ee8-deploy`) |
| WildFly / JBoss EAP | copy WAR to `standalone/deployments/`                                                    |
| Payara / GlassFish  | `asadmin deploy <war>`                                                                   |
| Open Liberty        | copy to `dropins/`                                                                       |

## Test

    curl http://localhost:8080/duke-test/health            # -> OK   (jakarta)
    curl http://localhost:8080/duke-test/health            # -> OK   (javax)
    curl http://localhost:8080/duke-test/api/info          # JSON

Then open the context path in a browser: Duke plus the live info panel.
