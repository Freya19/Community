package site.pyyf.community.config;

import com.alibaba.dubbo.config.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.pyyf.community.service.ICloudPanService;

@Configuration
public class MyDubboConfig {

	@Bean
	public ServiceConfig<ICloudPanService> userServiceConfig(ICloudPanService cloudPanService){
		ServiceConfig<ICloudPanService> serviceConfig = new ServiceConfig<>();
		serviceConfig.setInterface(ICloudPanService.class);
		serviceConfig.setRef(cloudPanService);
		serviceConfig.setVersion("1.0.0");

		return serviceConfig;
	}

}
