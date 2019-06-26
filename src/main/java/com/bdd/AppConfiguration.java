package com.bdd;

import com.rbs.mybank.digital.platform.common.model.cookie.marshaller.CookieMarshaller;
import com.rbs.mybank.digital.platform.common.model.cookie.marshaller.CustomerCookieMarshaller;
import com.rbsgrp.camel.platform.restclient.RestTemplateFactory;
import com.rbsgrp.camel.platform.restclient.RestTemplateFactoryConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author kumakbz
 */
@ContextConfiguration
public abstract class AppConfiguration {

    @Configuration
    @ComponentScan({"com.bdd", "com.camel.platform"})
    @PropertySource(value = "classpath:environment.properties")
    static class Config implements InitializingBean {

        @Bean
        public RestTemplateFactory restTemplateFactory() {
            return new RestTemplateFactory();
        }

        @Bean(name = "routingRestTemplate")
        public RestTemplate routingRestTemplate(RestTemplateFactory restTemplateFactory) throws IOException {
            return restTemplateFactory.getRestTemplate();
        }

        @Bean
        public RestTemplateFactoryConfig restTemplateFactoryConfig() {
            return new RestTemplateFactoryConfig();
        }

        @Override
        public void afterPropertiesSet() throws Exception {

        }

        @Bean(name = {"cookieMarshaller"})
        public CookieMarshaller customerCookieMarshaller() {
            return new CustomerCookieMarshaller();
        }
    }
}
