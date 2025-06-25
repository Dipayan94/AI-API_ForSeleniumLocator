package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

public class FlipkartHomePage {
    private WebDriver driver;

    public FlipkartHomePage(WebDriver driver) {
        this.driver = driver;
    }

    public void openMenu() {
        driver.findElement(By.xpath("//a[@aria-label='Menu']")).click();
    }

    public void searchProduct(String productName) {
        WebElement searchInput = driver.findElement(By.xpath("//form[@class='_2rslOn header-form-search']//input[@name='q']"));
        searchInput.clear();
        searchInput.sendKeys(productName);
        driver.findElement(By.xpath("//form[@class='_2rslOn header-form-search']//button")).click();
    }

    // Add more methods as needed for other elements
}
