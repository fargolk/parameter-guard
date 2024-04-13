package configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {
    private static AgentConfig config;

    static {
        try {
            InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("argument-guard.yaml");
            System.out.println(System.getProperty("java.class.path"));
            if (inputStream == null) {
                inputStream = new FileInputStream("/usr/local/tomcat/webapps/argument-guard.yaml");
                //throw new IllegalStateException("Could not find config.yaml");
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            config = mapper.readValue(inputStream, AgentConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static AgentConfig getConfig() {
        return config;
    }
}