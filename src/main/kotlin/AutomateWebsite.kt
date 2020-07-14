import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

class AutomateWebsite {
    fun openWebsite(activity: String) {
        val driver = ChromeDriver()
        driver.get("https://mountainproject.com")
        driver.findElementByCssSelector("input[placeholder='Search routes, forums, etc']").also {
            it.sendKeys(activity)
            it.sendKeys(Keys.RETURN)
        }

        clickWhenReady(activity, Duration.ofSeconds(10), driver)
        currentUrl = driver.currentUrl
    }

    private fun clickWhenReady(selector: String, timeout: Duration, driver: WebDriver) {
        val wait = WebDriverWait(driver, timeout)
        val element = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(selector)))
        element.click()
    }
}