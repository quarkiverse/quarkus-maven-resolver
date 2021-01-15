# Quarkiverse Maven Resolver Extension

This extension initializes a Maven resolver implementation from the Quarkus bootstrap project and provides it as a CDI bean to applications.

This extension can be used in both the JVM and the native modes.

## Maven dependency

The extension can be added to an application by adding the following dependency

```xml
    <dependency>
      <groupId>io.quarkiverse.mavenresolver</groupId>
      <artifactId>quarkiverse-mavenresolver</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
```

## API example

```java
package org.acme.quarkus.sample;

import java.io.IOException;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;

@Path("/resolver")
public class ArtifactResolverResource {

    /**
     * Artifact resolver
     */
    @Inject
    MavenArtifactResolver resolver;

    /**
     * Provides access to various resolver settings and also original Maven resolver API
     */
    @Inject
    BootstrapMavenContext mvnCtx;

    /**
     * Resolve and return the content of an artifact
     * specified with groupId:artifactId::type:version,
     * i.e. with an empty classifier
     * 
     * @param groupId  artifact groupId
     * @param artifactId  artifact id
     * @param type  artifact type
     * @param version  artifact version
     * @return  artifact content
     */
    @GET
    @Path("/resolve/{groupId}/{artifactId}/{type}/{version}")
    @Produces(MediaType.TEXT_PLAIN)
    public String resolve(@PathParam("groupId") String groupId,
        @PathParam("artifactId") String artifactId,
        @PathParam("type") String type,
        @PathParam("version") String version) {

        return resolveInternal(groupId, artifactId, null, type, version);
    }

    /**
     * Resolve and return the content of an artifact
     * specified with groupId:artifactId:classifier:type:version
     * 
     * @param groupId  artifact groupId
     * @param artifactId  artifact id
     * @param classifier  artifact classifier
     * @param type  artifact type
     * @param version  artifact version
     * @return  artifact content
     */
    @GET
    @Path("/resolve/{groupId}/{artifactId}/{classifier}/{type}/{version}")
    @Produces(MediaType.TEXT_PLAIN)
    public String resolve(@PathParam("groupId") String groupId,
        @PathParam("artifactId") String artifactId,
        @PathParam("classifier") String classifier,
        @PathParam("type") String type,
        @PathParam("version") String version) {

        return resolveInternal(groupId, artifactId, classifier, type, version);
    }

    private String resolveInternal(String groupId, String artifactId, String classifier, String type, String version) {
        Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, type, version);
        try {
            artifact = resolver.resolve(artifact).getArtifact();
        } catch (BootstrapMavenException e) {
            throw new IllegalStateException("Failed to resolve " + artifact, e);
        }

        try {
            return Files.readString(artifact.getFile().toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + artifact.getFile(), e);
        }
    }

    /**
     * Return the path to the local Maven repository used by the resolver
     * 
     * @return  path to the local Maven repository used by the resolver
     */
    @GET
    @Path("/local-repo")
    @Produces(MediaType.TEXT_PLAIN)
    public String localRepo() {
        try {
            return mvnCtx.getLocalRepo();
        } catch (BootstrapMavenException e) {
            throw new IllegalStateException("Failed to obtain the local repo path", e);
        }
    }

    /**
     * Return the path to the user settings file used to initialize the resolver
     * 
     * @return  path to the user settings file used to initialize the resolver
     */
    @GET
    @Path("/user-settings")
    @Produces(MediaType.TEXT_PLAIN)
    public String userSettings() {
        return mvnCtx.getUserSettings().toString();
    }
}
```