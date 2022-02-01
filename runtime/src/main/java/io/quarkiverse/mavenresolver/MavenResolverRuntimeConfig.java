package io.quarkiverse.mavenresolver;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "maven-resolver", phase = ConfigPhase.RUN_TIME)
public class MavenResolverRuntimeConfig {

    /**
     * List of extra Maven repositories to enabled
     */
    @ConfigItem
    RepositoriesRuntimeConfig repositories;

    @ConfigGroup
    public static class RepositoriesRuntimeConfig {
        /**
         * List of extra Maven repositories to enabled
         */
        @ConfigItem(name = ConfigItem.PARENT)
        public Map<String, RepositoryRuntimeConfig> repos;
    }

    @ConfigGroup
    public static class RepositoryRuntimeConfig {

        /**
         * Repository URL
         */
        @ConfigItem(name = "url")
        public String url;
    }
}
