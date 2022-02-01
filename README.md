# Quarkus Maven Resolver Extension
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

This extension initializes a Maven resolver implementation from the Quarkus bootstrap project and provides it as a CDI bean to applications.

This extension can be used in both the JVM and the native modes.

## Dependency info

### Maven

The extension can be added to a Maven project by adding the following dependency:

```xml
    <dependency>
      <groupId>io.quarkiverse.mavenresolver</groupId>
      <artifactId>quarkus-maven-resolver</artifactId>
      <version>0.0.3-SNAPSHOT</version>
    </dependency>
```

### Gradle

In case of a Gradle project, the following should be included into the `build.gradle` script:

```
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.quarkiverse.mavenresolver:quarkus-maven-resolver:0.0.3-SNAPSHOT'
}
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

## Configuring extra Maven repositories

By default, the resolver will be initialized from the Maven settings available in the environment, for example `~/.m2/settings.xml`. However, it is also possible to configure extra
Maven repositories in the application configuration using the following format:
```
quarkus.maven-resolver.repositories.<repo-id>=<repo-url>
```

The configured repositories will be added to the already available Maven repositories unless they are already present in the user Maven settings.

## Resolver initialization

The resolver will be eagerly initialized at application boot time by reading the local user settings file, typically located at `~/.m2/settings.xml`.

### Initialization in native mode

The resolver implementation is based on the Maven resolver API. Simply speaking, the key components of the resolver are a `RepositorySystem`, a `RepositorySystemSession` and a `RemoteRepository`.
The `RepositorySystem` is not supposed to depend on the environment in which the application is going to be launched. So the `RepositorySystem` part of the resolver is initialized
during the application build time and is serialized into the native image. The other parts of the resolver will be initialized on every boot of the application consulting the local Maven settings.
## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/aloubyansky"><img src="https://avatars1.githubusercontent.com/u/323379?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Alexey Loubyansky</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkiverse-maven-resolver/commits?author=aloubyansky" title="Code">💻</a> <a href="#maintenance-aloubyansky" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
