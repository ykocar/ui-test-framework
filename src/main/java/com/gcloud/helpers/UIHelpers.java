package com.gcloud.helpers;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class UIHelpers {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public UIHelpers(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Retrieves the current progress percentage from the progress bar label or
     * shadow DOM.
     * Returns -1 if the value cannot be found or parsed.
     */
    public int getProgressPercentage(By progressTextLabel) {
        try {
            WebElement label = driver.findElement(progressTextLabel);
            String text = label.getText().trim();

            if (text.isEmpty() || !text.contains("%")) {
                text = String.valueOf(((JavascriptExecutor) driver).executeScript(
                        "return document.querySelector('wem-game-progress-bar')?.shadowRoot?.textContent || ''"));
            }

            String numericOnly = text.replaceAll("[^0-9]", "");
            return numericOnly.isEmpty() ? -1 : Integer.parseInt(numericOnly);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Scrolls all scrollable elements and the page itself to the bottom.
     * Useful for ensuring elements are in view before interacting.
     */
    public void scrollToBottom() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const scrolls = Array.from(document.querySelectorAll('*')).filter(el => getComputedStyle(el).overflowY === 'auto' || getComputedStyle(el).overflowY === 'scroll');"
                        + "if (scrolls.length > 0) { scrolls.forEach(el => el.scrollTop = el.scrollHeight); }"
                        + "window.scrollTo(0, document.body.scrollHeight);");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Clicks a button in the shadow DOM based on one or more text values.
     * Tries multiple times if the element is not immediately found.
     */
    public void clickShadowDomButton(String... texts) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (int i = 0; i < 10; i++) {
            for (String text : texts) {
                Object result = js.executeScript(
                        "function find(root) {" +
                                "const b = Array.from(root.querySelectorAll('gux-button, button')).find(el => el.textContent.includes('"
                                + text + "'));" +
                                "if(b){ b.click(); return true; }" +
                                "const srs = Array.from(root.querySelectorAll('*')).map(e => e.shadowRoot).filter(r=>r);"
                                +
                                "for(const sr of srs){ if(find(sr)) return true; } return false;" +
                                "} return find(document);");
                if ((Boolean) result)
                    return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Selects and clicks a radio button answer within a shadow DOM or fallback DOM.
     */
    public void clickAnswerInShadowDOM(String answerText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "const targetText = arguments[0];" +
                "const fields = document.querySelectorAll('gux-form-field-radio');" +
                "for (let field of fields) {" +
                "  const label = field.querySelector('label');" +
                "  if(label && label.textContent.trim() === targetText) {" +
                "    const input = field.shadowRoot.querySelector('input[type=\"radio\"]');" +
                "    if(input){ input.scrollIntoView({block:'center'}); input.click(); return true; }" +
                "  }" +
                "}" +
                "const fallback = Array.from(document.querySelectorAll('label')).find(l => l.textContent.trim() === targetText);"
                +
                "if(fallback){ fallback.scrollIntoView({block:'center'}); fallback.click(); return true; } return false;";
        js.executeScript(script, answerText);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Safely clicks an element using WebDriverWait or falls back to JavaScript
     * click.
     */
    public void safeClick(By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(locator));
        }
    }

    /**
     * Waits until the progress bar reaches a target percentage using JavaScript.
     */
    public void waitForProgress(int targetPercent, int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
            Object raw = ((JavascriptExecutor) driver).executeScript(
                    "const bar = document.querySelector('wem-game-progress-bar');" +
                            "if(!bar) return '-1';" +
                            "const text = bar.shadowRoot ? bar.shadowRoot.textContent : bar.textContent;" +
                            "const match = text.match(/(\\d+)%/);" +
                            "return match ? match[1] : '-1';");
            if (raw == null)
                return false;
            int progress = Integer.parseInt(raw.toString());
            return progress >= targetPercent;
        });
    }

    /**
     * Clicks a link based on its text content using JavaScript.
     * Useful for dynamically loaded links in the page.
     */
    public void clickLinkByText(String linkText) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement link = (WebElement) new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(d -> (WebElement) js.executeScript(
                        "return Array.from(document.querySelectorAll('a')).find(a => a.textContent.includes(arguments[0]));",
                        linkText));
        js.executeScript("arguments[0].click();", link);
    }
}
