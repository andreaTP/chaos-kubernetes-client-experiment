import io.fabric8.junit.jupiter.api.KubernetesTest;
import io.fabric8.junit.jupiter.api.LoadKubernetesManifests;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesTest
@LoadKubernetesManifests("demo.yaml")
public class DemoIT {
    protected Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Test
    public void testDeploy() {
        logger.info("Running testDeploy test.");

        assertTrue(true);
    }

}
