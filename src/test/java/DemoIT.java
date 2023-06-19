import io.fabric8.junit.jupiter.api.KubernetesTest;
import io.fabric8.junit.jupiter.api.LoadKubernetesManifests;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesTest
@LoadKubernetesManifests(value = {"checker-infra.yaml", "control-infra.yaml"})
public class DemoIT {
    protected Logger logger = LoggerFactory.getLogger(getClass().getName());

    KubernetesClient client;

    String checkerImage = System.getenv("CHECKER_IMAGE");
    String controlImage = System.getenv("CONTROL_IMAGE");
    String chaosTest = Optional.ofNullable(System.getenv("CHAOS_TEST")).orElse("network-delay.yaml");

    @BeforeEach
    void beforeEach() throws Exception {
        logger.info("BeforeEach execution");
        // this needs to be executed before the pods are started
        client.configMaps().inNamespace(client.getNamespace()).withName("chaos-test").delete();
    }

    @AfterEach
    void afterEach() throws Exception {
        logger.info("AfterEach execution");

        try {
            var checkerLogs = checkerSelector().getLog();
            System.out.println("*** Checker Logs ***\n" + checkerLogs);
            System.out.println("******");
        } catch (Exception e) {
            // ignore
        }

        try {
            var controlLogs = controlSelector().getLog();
            System.out.println("*** Control Logs ***\n" + controlLogs);
            System.out.println("******");
        } catch (Exception e) {
            // ignore
        }
    }

    private static final int TOTAL_COUNT = 1 * 60 * 20; // 1 count each second, for 20 minutes

    private static void setArgs(Pod pod, String namespace) {
        pod.getSpec().getContainers().get(0).setArgs(List.of(
                "--namespace", namespace,
                "--num", Integer.toString(TOTAL_COUNT)));
    }

    private static void setImage(Pod pod, String image) {
        pod.getSpec().getContainers().get(0).setImage(image);
    }

    private PodResource checkerSelector() {
        return client.pods().inNamespace(client.getNamespace()).withName("checker");
    }
    private PodResource controlSelector() {
        return client.pods().inNamespace(client.getNamespace()).withName("checker");
    }

    @Test
    void test() throws IOException {
        logger.warn("Running test with chaos settings from: " + chaosTest);
        logger.warn("Using checker image: " + checkerImage);
        logger.warn("Using control image: " + controlImage);

        try (var is = this.getClass().getClassLoader().getResourceAsStream("checker-pod.yaml")) {
            var resources = client.load(is).resources().collect(Collectors.toList());
            resources
                    .stream()
                    .filter(r -> r.item().getKind().equals("Pod"))
                    .forEach(r -> {
                        if (checkerImage != null) {
                            setImage(((Pod) r.item()), checkerImage);
                        }
                        setArgs(((Pod) r.item()), client.getNamespace());
                        r.inNamespace(client.getNamespace()).create();
                    });
        }
        await().pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().atMost(1, TimeUnit.MINUTES).until(() -> {
            assertEquals("Running", checkerSelector().get().getStatus().getPhase());
            return true;
        });

        try (var is = this.getClass().getClassLoader().getResourceAsStream("control-pod.yaml")) {
            var resources = client.load(is).resources().collect(Collectors.toList());
            resources
                    .stream()
                    .filter(r -> r.item().getKind().equals("Pod"))
                    .forEach(r -> {
                        if (controlImage != null) {
                            setImage(((Pod) r.item()), controlImage);
                        }
                        setArgs(((Pod) r.item()), client.getNamespace());
                        r.inNamespace(client.getNamespace()).create();
                    });
        }
        await().pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().atMost(1, TimeUnit.MINUTES).until(() -> {
            assertTrue(checkerSelector().getLog().contains("Update received, and it's in the correct order, counter: 1"));
            return true;
        });

        try (var is = this.getClass().getClassLoader().getResourceAsStream(chaosTest)) {
            client.load(is).inNamespace(client.getNamespace()).createOrReplace();
        }

        await().pollInterval(10, TimeUnit.SECONDS).ignoreExceptions().atMost(30, TimeUnit.MINUTES).until(() -> {
            logger.info("Checking status");
            System.out.println("checker: " + checkerSelector().get().getStatus().getPhase());
            System.out.println("control: " + controlSelector().get().getStatus().getPhase());
            assertEquals("Succeeded", checkerSelector().get().getStatus().getPhase());
            assertEquals("Succeeded", controlSelector().get().getStatus().getPhase());
            return true;
        });
    }

}
