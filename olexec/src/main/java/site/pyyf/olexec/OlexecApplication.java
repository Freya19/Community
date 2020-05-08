package site.pyyf.olexec;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@EnableConfigurationProperties
@EnableDubbo(scanBasePackages="site.pyyf.olexec")
@EnableHystrix
@SpringBootApplication
public class OlexecApplication {

    public static void main(String[] args) {
        SpringApplication.run(OlexecApplication.class, args);
    }

}
