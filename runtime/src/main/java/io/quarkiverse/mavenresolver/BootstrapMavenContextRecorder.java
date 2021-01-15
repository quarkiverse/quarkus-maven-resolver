package io.quarkiverse.mavenresolver;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BootstrapMavenContextRecorder {

    public void initBootstrapMavenContextProducer(BeanContainer beanContainer) {
        beanContainer.instance(BootstrapMavenContextProducer.class);
    }

    public void initMavenRepositorySystemProducer(BeanContainer beanContainer) {
        beanContainer.instance(MavenRepositorySystemProducer.class);
    }
}
