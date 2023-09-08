package io.quarkiverse.mavenresolver.deployment;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import io.quarkiverse.mavenresolver.BootstrapMavenContextProducer;
import io.quarkiverse.mavenresolver.BootstrapMavenContextRecorder;
import io.quarkiverse.mavenresolver.MavenRepositorySystemProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
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
        resources.produce(new RuntimeInitializedClassBuildItem("org.apache.http.impl.auth.NTLMEngineImpl"));
    }

    @BuildStep
    void extensionSslNativeSupport(BuildProducer<ExtensionSslNativeSupportBuildItem> ssl) {
        ssl.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> resources) {
        var named = "META-INF/sisu/javax.inject.Named";
        var classNames = new ArrayList<String>();
        for (var e : QuarkusClassLoader.getElements(named, false)) {
            if (e.isRuntime()) {
                e.apply(tree -> {
                    tree.accept(named, visit -> {
                        if (visit != null) {
                            try {
                                classNames.addAll(Files.readAllLines(visit.getPath()));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    return null;
                });
            }
        }
        resources.produce(ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).build());
    }

    @BuildStep
    NativeImageResourceBuildItem nativeImageResources() {
        return new NativeImageResourceBuildItem("META-INF/sisu/javax.inject.Named");
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
