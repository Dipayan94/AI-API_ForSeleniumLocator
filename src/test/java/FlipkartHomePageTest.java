import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.FlipkartHomePage;
import java.time.Duration;
import java.util.logging.Logger;

public class FlipkartHomePageTest {
    private WebDriver driver;
    private static final Logger logger = Logger.getLogger(FlipkartHomePageTest.class.getName());

    @Before
    public void setUp() {
        // Set path to chromedriver if not in PATH
        // System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        logger.info("Browser launched and maximized");
    }

    @Test
    public void testSearchIphone16() {
        logger.info("Navigating to Flipkart homepage");
        driver.get("https://www.flipkart.com/");
        FlipkartHomePage homePage = new FlipkartHomePage(driver);
        logger.info("Searching for product: iPhone 16");
        homePage.searchProduct("iPhone 16");
        logger.info("Search submitted");
        // Add assertions as needed
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
    }
}
