import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import com.example.core.GeminiApiIntegration;
import com.example.core.HtmlCapture;
import java.time.Duration;

public class SeleniumLocatorGenerator {

    public static void main(String[] args) {
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("GEMINI_API_KEY environment variable not set. Please set it before running.");
            return;
        }

        // Set the path to your ChromeDriver executable - Not required as we are using webdriver-manager

        WebDriver driver = null; // Initialize driver to null for proper finally block handling

        try {
            String targetUrl = "https://www.flipkart.com/"; // Your target application URL

            // --- Phase 1: Generate & Save Locators (Run this when UI changes or for initial setup) ---
            System.out.println("--- Phase 1: Generating and Saving Locators ---");
            driver = new ChromeDriver();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            
            // Get page context including HTML, title, and URL
            HtmlCapture.PageContext pageContext = HtmlCapture.getPageContext(driver, targetUrl);
            System.out.println("Captured HTML from: " + targetUrl);

            // Generate and save locators with a unique name based on page title
            // Set 'true' to use page context for naming, 'false' for timestamp-based naming
            GeminiApiIntegration.generateAndSaveXPathsWithAI(pageContext, geminiApiKey, true);
            
            driver.quit(); 
            driver = null; // Set to null to indicate it's closed
        } catch (RuntimeException e) {
            System.err.println("A runtime error occurred, likely during locator loading: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
                System.out.println("Browser closed.");
            }
        }
    }
}