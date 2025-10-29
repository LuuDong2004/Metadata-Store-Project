package com.vn.ms.config;

import com.vn.ms.service.DynamicStoreRegistry;
import io.jmix.core.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình để đăng ký datastore "dynamic" với Jmix
 */
@Configuration
public class DynamicDataStoreConfiguration {

    @Autowired
    private DynamicStoreRegistry registry;

    /**
     * Đăng ký datastore "dynamic" với Jmix
     */
    @Bean("dynamic")
    public DataStore dynamicDataStore() {
        return registry.getDynamicDataStore();
    }
}
