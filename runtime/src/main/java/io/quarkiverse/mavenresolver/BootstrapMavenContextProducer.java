package io.quarkiverse.mavenresolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

import io.quarkiverse.mavenresolver.MavenResolverRuntimeConfig.RepositoryRuntimeConfig;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;

@Singleton
public class BootstrapMavenContextProducer {

    private final BootstrapMavenContext mvnCtx;
    private MavenArtifactResolver resolver;

    public BootstrapMavenContextProducer(RepositorySystem repoSystem, RemoteRepositoryManager remoteRepoManager,
            MavenResolverRuntimeConfig config) {

        try {
            final BootstrapMavenContext tmp = new BootstrapMavenContext(BootstrapMavenContext.config()
                    .setWorkspaceDiscovery(false)
                    .setRepositorySystem(repoSystem)
                    .setRemoteRepositoryManager(remoteRepoManager));
            final List<RemoteRepository> missingRepos = getMissingRepos(tmp, config.repositories.repos);
            if (missingRepos.isEmpty()) {
                mvnCtx = tmp;
            } else {
                tmp.getRemoteRepositoryManager().aggregateRepositories(tmp.getRepositorySystemSession(), missingRepos,
                        tmp.getRemoteRepositories(), false);
                mvnCtx = new BootstrapMavenContext(BootstrapMavenContext.config()
                        .setWorkspaceDiscovery(false)
                        .setRepositorySystem(repoSystem)
                        .setRemoteRepositoryManager(remoteRepoManager)
                        .setRepositorySystemSession(tmp.getRepositorySystemSession())
                        .setRemoteRepositories(tmp.getRemoteRepositoryManager().aggregateRepositories(
                                tmp.getRepositorySystemSession(), missingRepos, tmp.getRemoteRepositories(), false)));
            }

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

    private static List<RemoteRepository> getMissingRepos(BootstrapMavenContext mavenContext,
            Map<String, RepositoryRuntimeConfig> extraRepos)
            throws BootstrapMavenException {
        if (extraRepos.isEmpty()) {
            return List.of();
        }

        final List<RemoteRepository> extraList = new ArrayList<>(extraRepos.size());
        for (Map.Entry<String, RepositoryRuntimeConfig> r : extraRepos.entrySet()) {
            if (!isRepositoryConfigured(mavenContext.getRemoteRepositories(), r.getValue().url)) {
                extraList.add(new RemoteRepository.Builder(r.getKey(), "default", r.getValue().url).build());
            }
        }
        if (extraList.isEmpty()) {
            return List.of();
        }
        return mavenContext.getRemoteRepositoryManager()
                .aggregateRepositories(mavenContext.getRepositorySystemSession(), List.of(), extraList, true);
    }

    private static boolean isRepositoryConfigured(List<RemoteRepository> repos, String url) {
        for (RemoteRepository repo : repos) {
            if (isRepositoryConfigured(repo, url)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRepositoryConfigured(RemoteRepository repo, String url) {
        if (repo.getUrl().startsWith(url)) {
            return true;
        }
        return !repo.getMirroredRepositories().isEmpty() && isRepositoryConfigured(repo.getMirroredRepositories(), url);
    }
}
