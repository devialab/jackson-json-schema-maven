package com.devialab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "generate-context")
public class GenerateContext extends AbstractMojo {

    private static final String JSON_EXTENSION = ".json";

    @Parameter(required = true)
    private File outputDirectory;

    @Parameter(required = true)
    private String packageName;

    @Parameter(required = true)
    private String contentLocation;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            addProjectClasspath();
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper);
            new FastClasspathScanner(packageName).matchAllStandardClasses((clazz) -> {
                try {
                    JsonNode jsonNode = jsonSchemaGenerator.generateJsonSchema(clazz);
                    if (jsonNode.has("type")) {
                        String filename = clazz.getSimpleName().toLowerCase().concat(JSON_EXTENSION);
                        Path filePath = Paths.get(outputDirectory.getAbsolutePath(), filename);
                        if (!Files.exists(filePath)) Files.createFile(filePath);
                        Files.write(filePath, mapper.writeValueAsBytes(jsonNode));
                    }
                } catch (JsonProcessingException e) {
                    getLog().error(new MojoExecutionException(e.getMessage(), e));
                } catch (IOException e) {
                    getLog().error(new MojoExecutionException("Error creating context file. Message: " + e.getMessage(), e));
                }
            }).scan();
            getLog().info("Json schemas of package '" + packageName + "' written to: " + outputDirectory.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating context file. Message: " + e.getMessage(), e);
        }
    }

    private void addProjectClasspath() throws MalformedURLException {
        final PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
        final ClassRealm classRealm = pluginDescriptor.getClassRealm();
        final File classes = new File(project.getBuild().getOutputDirectory());
        classRealm.addURL(classes.toURI().toURL());
    }
}
