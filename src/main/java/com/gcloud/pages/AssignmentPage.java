package com.gcloud.pages;

import com.gcloud.helpers.UIHelpers;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import java.time.Duration;

public class AssignmentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final UIHelpers uiHelpers;

    private final By menuButton = By.id("navigation-menu");
    private final By analyticsTab = By.id("navBar.commandView.analytics.title");
    private final By workspaceTab = By.id("navBar.commandView.analytics.subMenu.analyticsWorkspace");
    private final By progressTextLabel = By.xpath("//*[contains(text(), '% complete')]");

    public AssignmentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.uiHelpers = new UIHelpers(driver, 20);
    }

    public void navigateToAssignment() {
        driver.switchTo().defaultContent();
        boolean menuOpened = false;
        for (int i = 0; i < 3; i++) {
            uiHelpers.safeClick(menuButton);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(analyticsTab));
                menuOpened = true;
                break;
            } catch (TimeoutException ignored) {
            }
        }
        if (!menuOpened)
            throw new RuntimeException("Failed to open the navigation menu.");

        uiHelpers.safeClick(analyticsTab);
        uiHelpers.safeClick(workspaceTab);

        By analyticsIframe = By.cssSelector("frame-router.main-iframe.visible iframe, iframe[title='Analytics UI']");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(analyticsIframe));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement assignmentLink = wait.until(d -> (WebElement) js.executeScript(
                "return Array.from(document.querySelectorAll('a')).find(a => a.textContent.includes('Test Assignment'));"));
        js.executeScript("arguments[0].click();", assignmentLink);
    }

    public void completeAssignmentFlow() {

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        uiHelpers.clickShadowDomButton("Start Module");

        wait.until(ExpectedConditions.visibilityOfElementLocated(progressTextLabel));
        wait.until(d -> uiHelpers.getProgressPercentage(progressTextLabel) >= 0);
        Assert.assertEquals(uiHelpers.getProgressPercentage(progressTextLabel), 0, "Initial progress should be 0%.");

        // scroll to bottom to trigger loading of progress bar updates
        uiHelpers.scrollToBottom();

        // wait for progress to update to 33% after scrolling
        wait.until(d -> uiHelpers.getProgressPercentage(progressTextLabel) == 33);

        // after scrolling, click next to trigger progress update
        uiHelpers.clickShadowDomButton("Next");

        wait.until(d -> uiHelpers.getProgressPercentage(progressTextLabel) > 33);

        uiHelpers.clickShadowDomButton("Next");

        // ANSWER RADIO QUESTION
        By assessmentIframe = By.id("assessment-builder");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(assessmentIframe));

        uiHelpers.clickAnswerInShadowDOM("7");

        // FIRST CHECK (75%)
        driver.switchTo().defaultContent();
        wait.until(d -> {
            String text = String.valueOf(((JavascriptExecutor) d).executeScript(
                    "return document.body.innerText + (document.querySelector('wem-game-progress-bar')?.shadowRoot?.textContent || '')"));
            return text.contains("75");
        });

        // ENTER TEXT IN IFRAME
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("assessment-builder")));

        WebElement textArea = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const labelText = 'What is WHO?';" +
                        "const fields = Array.from(document.querySelectorAll('gux-form-field-textarea'));" +
                        "for (let f of fields) {" +
                        "   const content = f.innerText + (f.querySelector('gux-truncate')?.textContent || '');" +
                        "   if (content.includes(labelText)) {" +
                        "       return f.querySelector('textarea[slot=\"input\"]') || f.querySelector('textarea');" +
                        "   }" +
                        "}" +
                        "return document.querySelector('textarea');");

        if (textArea != null) {
            textArea.clear();
            textArea.sendKeys("World Health Organization");

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new KeyboardEvent('keyup', { bubbles: true, key: 'n' }));" +
                            "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                    textArea);
        }

        // SECOND CHECK (83%)
        driver.switchTo().defaultContent();
        wait.until(d -> {
            String text = String.valueOf(((JavascriptExecutor) d).executeScript(
                    "return document.body.innerText + (document.querySelector('wem-game-progress-bar')?.shadowRoot?.textContent || '')"));
            return text.contains("83");
        });

        Object rawProgress83 = ((JavascriptExecutor) driver).executeScript(
                "const text = document.body.innerText + (document.querySelector('wem-game-progress-bar')?.shadowRoot?.textContent || '');"
                        +
                        "const match = text.match(/(\\d+)%?\\s*complete/i) || text.match(/(\\d+)/);" +
                        "return match ? match[1] : '-1';");

        int progressAt83 = Integer.parseInt(rawProgress83.toString());
        Assert.assertTrue(progressAt83 >= 83, "Progress should be at least 83% after text entry.");

        // FINAL RADIO QUESTION
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("assessment-builder")));
        uiHelpers.clickAnswerInShadowDOM("Yes");

        // 91% STATE CHECK
        driver.switchTo().defaultContent();
        wait.until(d -> {
            String text = String.valueOf(((JavascriptExecutor) d).executeScript(
                    "return document.body.innerText + (document.querySelector('wem-game-progress-bar')?.shadowRoot?.textContent || '')"));
            return text.contains("91");
        });

        // CLICK GROUP 2 NAVIGATION
        WebElement group2Sidebar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(), '2. Group 2')]//ancestor::button | //*[text()='2. Group 2']")));
        group2Sidebar.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("wem-document-viewer-legacy")));

        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("assessment-builder")));

        By eastOption = By.xpath("//span[text()='East']/ancestor::gux-form-field-radio//input[@type='radio']");
        WebElement eastRadio = wait.until(ExpectedConditions.elementToBeClickable(eastOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", eastRadio);
        eastRadio.click();
        wait.until(ExpectedConditions.elementToBeSelected(eastRadio));

        driver.switchTo().defaultContent();

        By progressBar = By.cssSelector("wem-game-progress-bar[fill-percent='100']");
        wait.until(ExpectedConditions.presenceOfElementLocated(progressBar));

        WebElement progress = driver.findElement(progressBar);
        String percent = progress.getAttribute("fill-percent");
        Assert.assertEquals(percent, "100", "Progress bar did not reach 100%");

        driver.switchTo().defaultContent();

        ((JavascriptExecutor) driver).executeScript(
                "const guxButton = document.querySelector('gux-button.complete-assignment');" +
                        "const internalButton = guxButton.shadowRoot.querySelector('button');" +
                        "internalButton.click();");

        wait.until(d -> (boolean) ((JavascriptExecutor) d).executeScript(
                "const submitBtn = Array.from(document.querySelectorAll('button, gux-button'))" +
                        ".find(btn => btn.innerText.trim() === 'Submit');" +
                        "if (submitBtn) { submitBtn.click(); return true; }" +
                        "return false;"));

        driver.switchTo().defaultContent();

        WebElement resultsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Congratulations')]")));
        assert resultsHeader.isDisplayed() : "Results page header not found!";

        wait.until(d -> {
            String script = "const elements = Array.from(document.querySelectorAll('*'));" +
                    "return elements.some(el => el.innerText && el.innerText.includes('100%'));";
            return (Boolean) ((JavascriptExecutor) d).executeScript(script);
        });
    }
}
