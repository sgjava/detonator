/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data loader is used to load test data.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataLoader {

    /**
     * Logger.
     */
    private static final Logger logger = LogManager.getLogger(DataLoader.class);

    /**
     * DataSource.
     */
    private final DataSource dataSource;

    /**
     * Construct with DataSource.
     *
     * @param dataSource DataSource to use.
     */
    public DataLoader(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Process SQL script and execute each line based on delimiter. Lines are accumulated until a delimiter is reached. SQL comments
     * "--", "/*" and blank lines are skipped.
     *
     * @param fileName SQL script to process.
     * @param delimiter Line delimiter.
     * @param removeDelimiter True to remove delimiter from statement
     * @return Number of statements executed.
     */
    public int execScript(final String fileName, final String delimiter, final boolean removeDelimiter) {
        int statements = 0;
        String sql = "";
        try (final Connection connection = dataSource.getConnection()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                // Process all lines in file
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        // Build SQL line
                        sql += line;
                        if (line.endsWith(delimiter)) {
                            // Remove delimiter from statement
                            if (removeDelimiter) {
                                sql = sql.substring(0, sql.length() - 1);
                            }
                            // Execute SQL statement
                            try (Statement statement = connection.createStatement()) {
                                logger.debug("Executing {}", sql);
                                statement.execute(sql);
                            } 
                            sql = "";
                        } else {
                            sql += "\n";
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Script file exception: fileName=%s", fileName), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("SQL exception: sql=%s", sql), e);
        }
        return statements;
    }
}
