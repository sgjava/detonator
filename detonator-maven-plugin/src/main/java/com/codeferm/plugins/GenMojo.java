/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import java.util.Locale;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;
import org.apache.maven.model.Resource;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Goal to generate DTO, ID classes and properties based on SQL.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true, requiresDependencyResolution
        = ResolutionScope.COMPILE)
public class GenMojo extends BaseGenMojo {

    /**
     * Location of generated sources dir.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources", property = "genSrcDir", required = true)
    private String genSrcDir;
    /**
     * Location of generated resources dir.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-resources", property = "genResDir", required = true)
    private String genResDir;
    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /* Default constructor */
    public GenMojo() {
    }

    /**
     * Execute mojo.
     *
     * @throws MojoExecutionException Possible exception.
     */
    @Override
    public void execute() throws MojoExecutionException {
        final var genCode = getGenCode();
        genCode.setGenResDir(genResDir);
        genCode.setGenSrcDir(genSrcDir);
        genCode.execute();
        // Add generated sources
        mavenProject.addCompileSourceRoot(genSrcDir);
        // Add generated resources
        final var resource = new Resource();
        resource.setDirectory(genResDir);
        mavenProject.addResource(resource);
    }
}
