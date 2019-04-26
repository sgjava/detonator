/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import com.codeferm.detonator.MakeDto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;
import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Goal to generate DTO and ID classes based on SQL.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Mojo(name = "dto", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class DtoMojo extends AbstractMojo {

    /**
     * Properties used by mojo.
     */
    @Parameter(property = "propertyFile", required = true)
    private String propertyFile;
    /**
     * Location of generated sources.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java", property = "outputDir", required = true)
    private String outputDir;
    /**
     * FreeMarker templates path.
     */
    @Parameter(property = "templatesDir", required = true)
    private String templatesDir;
    /**
     * DTO template.
     */
    @Parameter(property = "dtoTemplate", required = true)
    private String dtoTemplate;
    /**
     * ID template.
     */
    @Parameter(property = "idTemplate", required = true)
    private String idTemplate;
    /**
     * Map of class name (key) and SQL (value).
     */
    @Parameter(property = "sqlMap", required = true)
    private Map<String, String> sqlMap;
    /**
     * Package name to use for generated classes.
     */
    @Parameter(property = "packageName", required = true)
    private String packageName;
    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /* Default constructor */
    public DtoMojo() {
    }

    /**
     * Load properties from file. If file not found then try to load from class path.
     *
     * @param fileName Name of property file.
     * @return Properties from file.
     * @throws IOException Possible exception.
     */
    public final Properties loadProperties(final String fileName) throws IOException {
        final var properties = new Properties();
        final var path = Paths.get(fileName);
        // Load properties from file path
        if (Files.exists(path)) {
            getLog().info(String.format("Loading properties from %s", fileName));
            try (final var reader = new FileReader(fileName)) {
                properties.load(reader);
            }
        } else {
            getLog().info(String.format("Loading properties from classpath %s", path.getFileName()));
            // Get properties from classpath
            try (final var stream = DtoMojo.class.getClassLoader().getResourceAsStream(path.getFileName().toString())) {
                properties.load(stream);
            }
        }
        return properties;
    }

    /**
     * Execute mojo.
     *
     * @throws MojoExecutionException Possible exception.
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            final var properties = loadProperties(propertyFile);
            // Create DBCP DataSource
            final var dataSource = new BasicDataSource();
            dataSource.setDriverClassName(properties.getProperty("db.driver"));
            dataSource.setUsername(properties.getProperty("db.user"));
            dataSource.setPassword(properties.getProperty("db.password"));
            dataSource.setUrl(properties.getProperty("db.url"));
            dataSource.setMaxTotal(Integer.parseInt(properties.getProperty("db.pool.size")));
            // Make dirs
            final var classDir = String.format("%s/%s", outputDir, packageName.replace('.', '/'));
            final var dir = new File(classDir);
            dir.mkdirs();
            final var makeDto = new MakeDto(dataSource, templatesDir);
            // Generate classes based on SQL Map
            for (final var entry : sqlMap.entrySet()) {
                // Use FileWriter for DTO output
                try (final var out = new BufferedWriter(new FileWriter(String.format("%s/%s.java", classDir, entry.getKey())))) {
                    makeDto.dtoTemplate(dtoTemplate, entry.getValue(), packageName, entry.getKey(), out);
                }
                // Use StringWriter in case ID is empty (i.e. no PK or composite SQL)
                final var out = new StringWriter();
                makeDto.idTemplate(idTemplate, entry.getValue(), packageName, String.format("%sId", entry.getKey()), out);
                final var idStr = out.toString();
                // Check for empty result
                if (!idStr.isEmpty()) {
                    // Use FileWriter for ID output
                    try (final var idOut = new BufferedWriter(new FileWriter(String.format("%s/%sId.java", classDir, entry.getKey())))) {
                        idOut.write(idStr);
                    }
                }
            }
            // Close DataSource
            try {
                ((BasicDataSource) dataSource).close();
            } catch (SQLException e) {
                throw new RuntimeException("Close DataSource", e);
            }
            // Add generated sources
            mavenProject.addCompileSourceRoot(outputDir);
        } catch (IOException e) {
            getLog().error(e.getMessage());
        }

    }
}
