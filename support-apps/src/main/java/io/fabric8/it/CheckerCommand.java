package io.fabric8.it;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "checker", mixinStandardHelpOptions = true)
public class CheckerCommand implements Runnable {

    @CommandLine.Option(names = {"--num"}, paramLabel = "<num>", defaultValue = "10", description = "The number to be reached to quit successfully")
    int num;

    @CommandLine.Option(names = {"--namespace"}, paramLabel = "<namespace>", defaultValue = "default", description = "The namespace where the configMap will be created")
    String namespace;

    @CommandLine.Option(names = {"--labelkey"}, paramLabel = "<labelKey>", defaultValue = "chaos", description = "The label key to match")
    String labelKey;

    @CommandLine.Option(names = {"--labelvalue"}, paramLabel = "<labelValue>", defaultValue = "test", description = "The label value to match")
    String labelValue;

    private static final String COUNTER = "counter";

    private int extractValue(ConfigMap configMap) {
        return Integer.parseInt(configMap.getData().get(COUNTER));
    }

    @Override
    public void run() {
        System.out.println("Running Checker App");
        var client = new KubernetesClientBuilder().build();

        var latch = new CountDownLatch(1);
        var counter = new AtomicInteger();

        var handler = new ResourceEventHandler<ConfigMap>() {
            @Override
            public void onAdd(ConfigMap configMap) {
                System.out.println("Ready to start");
                var initialCount = extractValue(configMap);
                if (initialCount == 0) {
                    System.out.println("Counter is 0");
                    counter.set(0);
                } else {
                    throw new RuntimeException("Counter is NOT ZERO at the start! Found value: " + initialCount);
                }
            }

            @Override
            public void onUpdate(ConfigMap oldConfigMap, ConfigMap newConfigMap) {
                var currentValue = extractValue(newConfigMap);
                if (counter.compareAndSet(currentValue - 1, currentValue)) {
                    System.out.println("Update received, and it's in the correct order, counter: " + currentValue);
                } else {
                    if (currentValue > counter.get()) {
                        System.out.println("Update received, NOT in the correct order but compatible: " + currentValue);
                        counter.set(currentValue);
                    } else {
                        throw new RuntimeException("Update received in an incorrect order: " + currentValue);
                    }
                }

                if (currentValue == num) {
                    System.out.println("Last update received!");
                } else if (currentValue > num) {
                    throw new RuntimeException("Current value is > than the expected value, " + currentValue);
                }
            }

            @Override
            public void onDelete(ConfigMap configMap, boolean deletedFinalStateUnknown) {
                if (counter.compareAndSet(num, 0)) {
                    System.out.println("Experiment should successfully end");
                    latch.countDown();
                } else {
                    throw new RuntimeException("Expected " + num + " but reached " + counter.get());
                }
            }
        };

        var defaultConfigMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName("chaos-test")
                    .withNamespace(namespace)
                    .withLabels(Map.of(labelKey, labelValue))
                .endMetadata()
                .withData(Map.of("counter", Integer.toString(0)))
                .build();

        var configMapInformer = client.configMaps().inNamespace(namespace).withLabel(labelKey, labelValue).inform(handler);
        configMapInformer.start();

        try {
            latch.await(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Terminating successfully!");
        System.exit(0);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CheckerCommand()).execute(args);
        System.exit(exitCode);
    }

}
