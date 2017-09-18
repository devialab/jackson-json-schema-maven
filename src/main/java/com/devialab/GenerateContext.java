package com.devialab;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
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

@Mojo(name="generate-context")
public class GenerateContext extends AbstractMojo
{
    @Parameter(required = true)
    private File outputDirectory;

    @Parameter(required = true)
    private File fileName;

    @Parameter(required = true)
    private String packageName;

    @Parameter(required = true)
    private String contentLocation;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException
    {
        try
        {
            addProjectClasspath();
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper);
            Path filePath = Paths.get(outputDirectory.getAbsolutePath(), fileName.getName());
            if (!Files.exists(filePath))
            {
                Files.createFile(filePath);
            }
            ObjectNode jsonSchema = mapper.createObjectNode();
            new FastClasspathScanner(packageName).matchAllStandardClasses((clazz) -> {
                try {
                    JsonSchema schema = jsonSchemaGenerator.generateSchema(clazz);
                    JsonNode jsonNode = mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
                    jsonSchema.set(clazz.getSimpleName(), jsonNode);
                } catch (JsonProcessingException e) {
                    getLog().error(new MojoExecutionException(e.getMessage(), e));
                }
                catch (IOException e) {
                    getLog().error(new MojoExecutionException("Error creating context file. Message: " + e.getMessage(), e));
                }
            }).scan();
            Files.write(filePath, mapper.writeValueAsBytes(jsonSchema));
            getLog().info("Context file of package '"+ packageName +"' written to: " + filePath);
        }
        catch (IOException e) {
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
