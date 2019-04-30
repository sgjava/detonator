/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
@Mojo(name = "testGenerate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, threadSafe = true, requiresDependencyResolution
        = ResolutionScope.TEST)
public class TestGenMojo extends BaseGenMojo {
    /**
     * Location of generated sources dir.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources", property = "genSrcDir", required = true)
    private String genSrcDir;
    /**
     * Location of generated resources dir.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-test-resources", property = "genResDir", required = true)
    private String genResDir;
    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

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
        mavenProject.addTestCompileSourceRoot(genSrcDir);
        // Add generated resources
        final var resource = new Resource();
        resource.setDirectory(genResDir);
        mavenProject.addTestResource(resource);
    }
}
