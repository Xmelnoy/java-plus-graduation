package ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(value = {"ewm", "client"})
public class MainServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApplication.class, args);
    }
}
