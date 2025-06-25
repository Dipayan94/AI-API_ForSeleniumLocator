package com.example.core;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PageObjectFactory {

    private WebDriver driver;
    private Map<String, By> pageElements;
    private static final String LOCATORS_FILE_PATH = "src/test/resources/locators.json";

    public PageObjectFactory(WebDriver driver) {
        this.driver = driver;
        this.pageElements = new HashMap<>();
        loadLocatorsFromFile(); // Load locators during object instantiation
    }

    private void loadLocatorsFromFile() {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();

        try (FileReader reader = new FileReader(LOCATORS_FILE_PATH)) {
            Map<String, String> xPaths = gson.fromJson(reader, type);
            if (xPaths != null && !xPaths.isEmpty()) {
                xPaths.forEach((name, xpath) -> {
                    if (!"NOT_FOUND".equalsIgnoreCase(xpath)) {
                        pageElements.put(name, By.xpath(xpath));
                        System.out.println("Loaded Locator: " + name + " -> " + xpath);
                    } else {
                        System.out.println("Skipped Locator: " + name + " (XPath was NOT_FOUND)");
                    }
                });
            } else {
                System.err.println("Locators JSON file is empty or malformed: " + LOCATORS_FILE_PATH);
            }
        } catch (IOException e) {
            System.err.println("Error loading locators from file " + LOCATORS_FILE_PATH + ": " + e.getMessage());
            throw new RuntimeException("Could not load locators from file. Ensure the file exists and is valid JSON.", e);
        }
    }

    /**
     * Locates a WebElement using the dynamically loaded locator.
     * @param elementName The descriptive name of the element as defined in locators.json.
     * @return The WebElement found.
     * @throws IllegalArgumentException if the locator for the element name is not found.
     */
    public WebElement getElement(String elementName) {
        By locator = pageElements.get(elementName);
        if (locator == null) {
            throw new IllegalArgumentException("Locator for element '" + elementName + "' not found in " + LOCATORS_FILE_PATH + ". Please ensure AI generated it correctly.");
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Example implicit wait
        return driver.findElement(locator);
    }

    // Common interaction methods
    public void clickElement(String elementName) {
        getElement(elementName).click();
    }

    public void typeIntoElement(String elementName, String text) {
        WebElement element = getElement(elementName);
        element.clear();
        element.sendKeys(text);
    }

    public String getElementText(String elementName) {
        return getElement(elementName).getText();
    }
}