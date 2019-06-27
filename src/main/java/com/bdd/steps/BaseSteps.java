package com.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.camel.platform.exception.PlatformSystemException;
import com.camel.platform.restclient.PojoMapper;
import com.camel.platform.util.CommonUtil;
import com.bdd.AppConfiguration;
import com.bdd.PlatformRegistry;
import cucumber.api.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author kumakbz
 */
public class BaseSteps extends AppConfiguration {

    private static final String REQUEST = "requests/";

    private static String env;
    private static String auth;
    private static final Properties properties = new Properties();

    String channelHost;
    String appMgmtServiceEndPoint;

    static {
        env = System.getProperty("env");
        if (StringUtils.isEmpty(env) || CommonUtil.isNull(env)) {
            env = "dev";
        }
        System.out.println("##################-- ENV:" + env + "--##################");
        auth = System.getProperty("auth");
        System.out.println("##################-- AUTH:" + auth + "--##################");
        System.out.println();
        try {
            properties.load(BaseSteps.class.getClassLoader().getResourceAsStream("environment.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error during env properties : " + e.getMessage());
        }
    }

    @Autowired
    @Qualifier("routingRestTemplate")
    private RestTemplate restTemplate;

    private Scenario scenario;

    String urlToTest;

    @PostConstruct
    public void resetChannelHost() {

        channelHost = properties.getProperty(env + ".base.path");
        appMgmtServiceEndPoint =
                properties.getProperty(env + ".domain.applicationManagement.service.endpoint");
    }

    ResponseEntity executeGet(String endPoint, HttpHeaders httpHeaders, Class<String> clazz, String route) {
        ResponseEntity responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(endPoint, HttpMethod.GET, buildGetBody(httpHeaders), clazz);
            PlatformRegistry.getInstance().putValue(route, responseEntity);
            System.out.println("##################--Response Start for endpoint: " + endPoint + "--##################");
            System.out.println(CommonUtil.prettyJson(responseEntity.getBody()));
            System.out.println("##################--Response End--##################");
        } catch (Exception e) {
            throw new RuntimeException("Error during REST call for endpoint : " + endPoint);
        }
        return responseEntity;
    }

    <T> ResponseEntity executePost(T request, String endPoint, HttpHeaders httpHeaders, Class clazz, String route) {
        ResponseEntity responseEntity = null;
        try {
            System.out.println("##################--Request Start for Endpoint: " + endPoint + "--##################");
            System.out.println(CommonUtil.prettyJson(request));
            System.out.println("##################--Request End--##################");
            responseEntity = restTemplate.exchange(endPoint, HttpMethod.POST, buildPostBody(request, httpHeaders), clazz);
            PlatformRegistry.getInstance().putValue(route, responseEntity);
            System.out.println("##################--Response Start for endpoint: " + endPoint + "--##################");
            System.out.println(CommonUtil.prettyJson(responseEntity.getBody()));
            System.out.println("##################--Response End--##################");
        } catch (Exception e) {
            throw new RuntimeException("Error during REST call for endpoint : " + endPoint);
        }
        return responseEntity;
    }

    ResponseEntity executeDelete(String endPoint, HttpHeaders httpHeaders, Class<String> clazz, String route) {
        ResponseEntity responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(endPoint, HttpMethod.DELETE, buildGetBody(httpHeaders), clazz);
            PlatformRegistry.getInstance().putValue(route, responseEntity);
            System.out.println("##################--Response Start for endpoint: " + endPoint + "--##################");
            System.out.println(CommonUtil.prettyJson(responseEntity.getBody()));
            System.out.println("##################--Response End--##################");
        } catch (Exception e) {
            throw new RuntimeException("Error during REST call for endpoint : " + endPoint);
        }
        return responseEntity;
    }

    private HttpEntity buildGetBody(HttpHeaders httpHeaders) {
        return new HttpEntity(httpHeaders);
    }

    private <T> HttpEntity buildPostBody(T request, HttpHeaders httpHeaders) {
        return new HttpEntity(request, httpHeaders);
    }

    Map<String, String> buildCookieHeaders(ResponseEntity responseEntity, String iam_subject) {
        final HttpHeaders headers = responseEntity.getHeaders();
        final String cookie = headers.get("Set-Cookie").stream().findFirst().get();
        System.out.println("##################-- Cookie:" + cookie + "--##################");
        return ImmutableMap.of(
                "Cookie", cookie,
                "iam_subject", iam_subject);
    }

    HttpHeaders buildHeaders(Map<String, String> headersValues) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headersValues.forEach(headers::set);
        if (StringUtils.isNotEmpty(auth) || CommonUtil.isNotNull(auth)) {
            headers.set("Authorization", "Bearer " + auth);
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        return (T) PlatformRegistry.getInstance().getValue(name);
    }

    public void putValue(String name, Object object) {
        PlatformRegistry.getInstance().putValue(name, object);
    }

    public void putMapValue(String name, String key, Object object) {
        @SuppressWarnings("unchecked")
        Map<String, Object> mapValue = (Map<String, Object>) PlatformRegistry.getInstance().getValue(name);
        if (mapValue == null) {
            mapValue = new LinkedHashMap<>();
            PlatformRegistry.getInstance().putValue(name, mapValue);
        }
        mapValue.put(validateKey(key, mapValue), object);
    }

    private String validateKey(String key, Map<String, Object> mapValue) {
        String newKey = key;
        for (int i = 0; i < mapValue.size(); i++) {
            if (mapValue.containsKey(newKey)) {
                newKey = newKey + "+1";
            }
        }
        return newKey;
    }

    public void removeValue(String name) {
        PlatformRegistry.getInstance().removeValue(name);
    }

    public void clear() {
        PlatformRegistry.getInstance().clear();
    }

    @SuppressWarnings("unchecked")
    public <T> Object loadRequestData(String objectName, String basePath) {
        Class<T> clazz;
        Object instance;
        try {
            clazz = (Class<T>) Class.forName(basePath + objectName);
            String path = ClassLoader.getSystemResource(
                    REQUEST + objectName + ".json").getPath();

            String request = new String(Files.readAllBytes(new File(path).toPath()), Charset.defaultCharset());
            instance = new ObjectMapper().readValue(request, clazz);
        } catch (ClassNotFoundException | IOException e) {
            throw new PlatformSystemException(String.format("Failed to load file for api: %s and exception: %s", objectName, e));
        }
        return instance;
    }

    <T> T mapToObject(String response, Class<T> type) throws IOException {
        return new PojoMapper().fromJson(response, type);
    }

    <T> ResponseEntity<T> mapToResponseEntity(ResponseEntity<String> responseEntity, Class<T> type) throws IOException {
        return new ResponseEntity<>(mapToObject(responseEntity.getBody(), type), responseEntity.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    public <T> Object loadRequestData(Class objectName, String fileName, String basePath) {
        Object instance;
        try {
            String path = REQUEST + fileName;
            instance = new PojoMapper().fromJsonFile(path, objectName);
        } catch (IOException e) {
            throw new PlatformSystemException(String.format("Failed to load file for api: %s and exception: %s", objectName, e));
        }
        return instance;
    }

}