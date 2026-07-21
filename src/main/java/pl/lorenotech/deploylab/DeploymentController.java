package pl.lorenotech.deploylab;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeploymentController {

    @Value("${APP_VERSION:local}")
    private String appVersion;

    @Value("${HOSTNAME:unknown}")
    private String hostname;

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "deploy-lab",
                "version", appVersion,
                "pod", hostname,
                "time", Instant.now().toString(),
                "message", "Aplikacja działa poprawnie 11"
        );
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Jenkins and Kubernetes lab!");
    }
}
