package uk.cam.lib.cdl.loading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LoadingUIApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadingUIApplication.class, args);
    }
}
