###Connection pool issue

try to add this to application.properties

```
#connection pool
#maximum number of milliseconds that a client will wait for a connection
spring.datasource.hikari.connection-timeout=20000
#minimum number of idle connections maintained by HikariCP in a connection pool
spring.datasource.hikari.minimum-idle=10
#maximum pool size
spring.datasource.hikari.maximum-pool-size=20
#maximum idle time for connection
spring.datasource.hikari.idle-timeout=10000
# maximum lifetime in milliseconds of a connection in the pool after it is closed.
spring.datasource.hikari.max-lifetime=1000
#default auto-commit behavior.
spring.datasource.hikari.auto-commit=true
#connection pool END
```