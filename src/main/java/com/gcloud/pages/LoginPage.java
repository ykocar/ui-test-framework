package com.gcloud.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private By moreLoginOptions = By.linkText("More Login Options");
    private By org = By.id("org");
    private By nextButton = By.cssSelector("button.select-org");
    private By email = By.id("email");
    private By password = By.id("password");
    private By loginButton = By.xpath("//button[@type='submit']");
    private By menuButton = By.id("navigation-menu");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void login(String user, String pass, String organization) {

        // click "More Login Options"
        wait.until(ExpectedConditions.elementToBeClickable(moreLoginOptions)).click();

        // enter Organization name and click next
        WebElement orgInput = wait.until(ExpectedConditions.visibilityOfElementLocated(org));
        orgInput.clear();
        orgInput.sendKeys(organization);

        wait.until(ExpectedConditions.elementToBeClickable(nextButton)).click();

        // enter Email and Password on final page
        wait.until(ExpectedConditions.visibilityOfElementLocated(email)).sendKeys(user);
        driver.findElement(password).sendKeys(pass);
        driver.findElement(loginButton).click();
    }

    public void verifyLandingPageLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(menuButton));
    }
}
