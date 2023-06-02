package io.fabric8.it;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;

@Command(name = "control", mixinStandardHelpOptions = true)
public class ControlCommand implements Runnable {

    @Parameters(paramLabel = "<num>", defaultValue = "10", description = "The number to be reached to quit successfully")
    int num;

    @Parameters(paramLabel = "<namespace>", defaultValue = "default", description = "The namespace where the configMap will be created")
    String namespace;

    @Parameters(paramLabel = "<labelKey>", defaultValue = "chaos", description = "The label key to match")
    String labelKey;

    @Parameters(paramLabel = "<labelValue>", defaultValue = "test", description = "The label value to match")
    String labelValue;

    @Parameters(paramLabel = "<delay>", defaultValue = "1000", description = "The delay between each number increase")
    int delay;

    private static final String COUNTER = "counter";

    private int extractValue(ConfigMap configMap) {
        return Integer.parseInt(configMap.getData().get(COUNTER));
    }

    @Override
    public void run() {
        var client = new KubernetesClientBuilder().build();

        var defaultConfigMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName("chaos-test")
                    .withNamespace(namespace)
                    .withLabels(Map.of(labelKey, labelValue))
                .endMetadata()
                    .withData(Map.of("counter", Integer.toString(0)))
                .build();

        if (client.resource(defaultConfigMap).inNamespace(namespace).get() != null) {
            System.out.println("ConfigMap detected removing it before starting");
            client.resource(defaultConfigMap).inNamespace(namespace).delete();
        }
        client.resource(defaultConfigMap).inNamespace(namespace).create();

        while (true) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            var currentConfigMap = client.resource(defaultConfigMap).inNamespace(namespace).get();

            if (currentConfigMap == null) {
                throw new RuntimeException("Cannot find the configMap!");
            } else {
                var counter = extractValue(currentConfigMap);
                System.out.println("going to increment the value, current: " + counter);

                if (counter == num) {
                    System.out.println("I'm done here!");
                    break;
                } else if (counter > num) {
                    throw new RuntimeException("Something went wrong!");
                } else {
                    currentConfigMap.getData().put(COUNTER, Integer.toString(counter + 1));
                    client.resource(currentConfigMap).inNamespace(namespace).createOrReplace();
                    System.out.println("Counter incremented");
                }
            }
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished! deleting the ConfigMap");
        client.resource(defaultConfigMap).inNamespace(namespace).delete();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ControlCommand()).execute(args);
        System.exit(exitCode);
    }

}
