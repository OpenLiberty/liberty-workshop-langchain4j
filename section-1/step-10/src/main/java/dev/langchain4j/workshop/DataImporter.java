package dev.langchain4j.workshop;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class DataImporter {
    private static final Logger logger = LoggerFactory.getLogger(DataImporter.class);

    @Resource(lookup = "jdbc/postgresql")
    private DataSource dataSource;

    @PostConstruct
    public void importData() {
        logger.info("Importing data from import.sql");

        try ( Connection connection = dataSource.getConnection()
            ; InputStream is = getClass().getClassLoader().getResourceAsStream("import.sql")
            ) {
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setSendFullScript(false);
            scriptRunner.setStopOnError(true);
            scriptRunner.setAutoCommit(true);
            scriptRunner.runScript(new InputStreamReader(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
