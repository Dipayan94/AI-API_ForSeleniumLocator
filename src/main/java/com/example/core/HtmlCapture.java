package com.example.core;
import org.openqa.selenium.WebDriver;
import java.time.Duration;

public class HtmlCapture {

    public static PageContext getPageContext(WebDriver driver, String url) {
        driver.get(url);
        // It's good practice to add a wait here for the page to fully load
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        String pageSource = driver.getPageSource();
        String pageTitle = driver.getTitle();
        String currentUrl = driver.getCurrentUrl();

        return new PageContext(pageSource, pageTitle, currentUrl);
    }

    public static class PageContext {
        private String htmlContent;
        private String pageTitle;
        private String pageUrl;

        public PageContext(String htmlContent, String pageTitle, String pageUrl) {
            this.htmlContent = htmlContent;
            this.pageTitle = pageTitle;
            this.pageUrl = pageUrl;
        }

        public String getHtmlContent() {
            return htmlContent;
        }

        public String getPageTitle() {
            return pageTitle;
        }

        public String getPageUrl() {
            return pageUrl;
        }
    }
}