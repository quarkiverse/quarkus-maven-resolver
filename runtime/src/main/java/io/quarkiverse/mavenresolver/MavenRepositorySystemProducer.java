package io.quarkiverse.mavenresolver;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.RemoteRepositoryManager;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;

@Singleton
public class MavenRepositorySystemProducer {

    private final RepositorySystem repoSystem;
    private final RemoteRepositoryManager remoteRepoManager;

    public MavenRepositorySystemProducer() {
        try {
            final BootstrapMavenContext mvnCtx = new BootstrapMavenContext(
                    BootstrapMavenContext.config().setWorkspaceDiscovery(false));
            repoSystem = mvnCtx.getRepositorySystem();
            remoteRepoManager = mvnCtx.getRemoteRepositoryManager();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Maven context", e);
        }
    }

    @Produces
    public RepositorySystem getRepositorySystem() {
        return repoSystem;
    }

    @Produces
    public RemoteRepositoryManager getRemoteRepositoryManager() {
        return remoteRepoManager;
    }
}
