package pl.lorenotech.deploylab;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DeployLabApplicationTests {

    @Test
    void sampleTest() {
        assertThat("deploy-lab").startsWith("deploy");
    }
}
