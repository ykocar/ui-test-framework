package com.gcloud.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class BaseTest {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static Properties config = new Properties();
    public static Properties credentials = new Properties();

    // Load property files
    static {
        loadProperties("config.properties", config);
        loadProperties("credentials.properties", credentials);
    }

    private static void loadProperties(String fileName, Properties props) {
        try (InputStream input = BaseTest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                props.load(input);
                System.out.println("Loaded " + fileName);
            } else {
                System.out.println(fileName + " not found. Falling back to environment variables.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fileName, e);
        }
    }

    // Property Access Methods
    public static String getConfig(String key) {
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        return envValue != null ? envValue : config.getProperty(key);
    }

    public static String getCredential(String key) {
        String envKey = key.toUpperCase();
        String envValue = System.getenv(envKey);
        return envValue != null ? envValue : credentials.getProperty(key);
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    // Test Setup
    @BeforeMethod
    public void initializeDriver() {

        String browser = getConfig("browser") != null ? getConfig("browser") : "chrome";
        boolean headless = Boolean.parseBoolean(
                getConfig("headless") != null ? getConfig("headless") : "false");

        if (browser.equalsIgnoreCase("chrome")) {

            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            if (headless) {
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
            } else {
                options.addArguments("--start-maximized");
            }

            options.addArguments("--use-fake-ui-for-media-stream");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver.set(new ChromeDriver(options));

        } else {
            throw new RuntimeException("Unsupported browser: " + browser);
        }

        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        String implicitWait = getConfig("implicit_wait") != null ? getConfig("implicit_wait") : "2";
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(implicitWait)));

        String baseUrl = getConfig("base_url");
        if (baseUrl == null) {
            throw new RuntimeException("base_url is not defined in config.properties or environment variables.");
        }

        getDriver().get(baseUrl);
    }

    // Tear Down
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
