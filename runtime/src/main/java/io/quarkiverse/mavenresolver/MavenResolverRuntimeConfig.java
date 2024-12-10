package io.quarkiverse.mavenresolver;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.maven-resolver")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MavenResolverRuntimeConfig {

    /**
     * List of extra Maven repositories to enabled
     */
    RepositoriesRuntimeConfig repositories();

    @ConfigGroup
    interface RepositoriesRuntimeConfig {
        /**
         * List of extra Maven repositories to enabled
         */
        @WithParentName
        Map<String, RepositoryRuntimeConfig> repos();
    }

    @ConfigGroup
    interface RepositoryRuntimeConfig {

        /**
         * Repository URL
         */
        String url();
    }
}
