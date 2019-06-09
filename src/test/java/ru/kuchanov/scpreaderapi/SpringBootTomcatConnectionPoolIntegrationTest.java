package ru.kuchanov.scpreaderapi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootTomcatConnectionPoolIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Test
    public void givenTomcatConnectionPoolInstance_whenCheckedPoolClassName_thenCorrect() {
        assertThat(dataSource.getClass().getName())
//                .isEqualTo("org.apache.tomcat.jdbc.pool.DataSource");
                .isEqualTo("com.zaxxer.hikari.HikariDataSource");
    }

    @Test
    public void javaVersion_givenMoreOrEqualsThan11_thenCorrect() {
        System.out.println("JAVA VERSION OUTPUT: "+System.getProperty("java.version"));
        assertThat(System.getProperty("java.version"))
                .isEqualTo("11");
    }
}
