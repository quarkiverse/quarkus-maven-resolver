package io.quarkiverse.mavenresolver;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.RemoteRepositoryManager;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;

@Singleton
public class BootstrapMavenContextProducer {

    private final BootstrapMavenContext mvnCtx;
    private MavenArtifactResolver resolver;

    public BootstrapMavenContextProducer(RepositorySystem repoSystem, RemoteRepositoryManager remoteRepoManager) {
        try {
            mvnCtx = new BootstrapMavenContext(BootstrapMavenContext.config()
                    .setWorkspaceDiscovery(false)
                    .setRepositorySystem(repoSystem)
                    .setRemoteRepositoryManager(remoteRepoManager));
        } catch (BootstrapMavenException e) {
            throw new IllegalStateException("Failed to initialize bootstrap Maven context", e);
        }
    }

    @Produces
    public MavenArtifactResolver produceResolver(BootstrapMavenContext mvnCtx) {
        return resolver == null ? resolver = newResolver() : resolver;
    }

    private MavenArtifactResolver newResolver() {
        try {
            return new MavenArtifactResolver(mvnCtx);
        } catch (BootstrapMavenException e) {
            throw new IllegalStateException("Failed to initialize Maven artifact resolver", e);
        }
    }

    @Produces
    public BootstrapMavenContext bootstrapMavenContext() {
        return mvnCtx;
    }
}
