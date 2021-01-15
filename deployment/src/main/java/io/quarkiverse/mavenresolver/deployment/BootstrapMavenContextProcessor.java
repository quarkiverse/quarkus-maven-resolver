package io.quarkiverse.mavenresolver.deployment;

import io.quarkiverse.mavenresolver.BootstrapMavenContextProducer;
import io.quarkiverse.mavenresolver.BootstrapMavenContextRecorder;
import io.quarkiverse.mavenresolver.MavenRepositorySystemProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

class BootstrapMavenContextProcessor {

    private static final String FEATURE = "quarkiverse-maven-resolver";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(MavenRepositorySystemProducer.class, BootstrapMavenContextProducer.class).build());
    }

    @BuildStep
    void registerRuntimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> resources) {
        resources.produce(new RuntimeInitializedClassBuildItem("org.apache.maven.wagon.providers.http.HttpWagon"));
        resources.produce(new RuntimeInitializedClassBuildItem("org.apache.maven.wagon.shared.http.AbstractHttpClientWagon"));
    }

    @BuildStep
    void extensionSslNativeSupport(BuildProducer<ExtensionSslNativeSupportBuildItem> ssl) {
        ssl.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> resources) {
        resources.produce(new ReflectiveClassBuildItem(false, false, "org.apache.maven.wagon.providers.http.HttpWagon"));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void recordStaticInit(BootstrapMavenContextRecorder recorder, BeanContainerBuildItem beanContainer) {
        recorder.initMavenRepositorySystemProducer(beanContainer.getValue());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void recordRuntimeStaticInit(BootstrapMavenContextRecorder recorder, BeanContainerBuildItem beanContainer) {
        recorder.initBootstrapMavenContextProducer(beanContainer.getValue());
    }
}
