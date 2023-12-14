import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;


public class XMWebTester {
    final String xm_url = "https://xm.com";
    WebDriver driver;
    WebDriverWait waiter;

    @BeforeClass
    void setupDriver() {
        this.driver = new FirefoxDriver();
        this.waiter = new WebDriverWait(this.driver, Duration.ofSeconds(10));
        navigateHomePage();
        removeCookiePopup();
    }

    @AfterClass
    void tearDown() {
        this.driver.close();
    }

    @Test
    void testNavigationToEconomicCalendar(){
        String expected_url = "https://www.xm.com/research/economicCalendar";
        navigateToEconomicCalendar();
        assert (this.driver.getCurrentUrl().equals(expected_url));
    }

    @Test
    void testNavigationToEducationalVideos(){
        String expected_url = "https://www.xm.com/educational-videos";
        navigateToEducationalVideos();
        assert (this.driver.getCurrentUrl().equals(expected_url));
    }

    @Test
    void testCalendarSlider(){
        assert false;
    }

    @Test
    void testVideoPlayback(){
        assert false;
    }

    public void navigateHomePage() {
        this.driver.get(xm_url);
    }

    public void removeCookiePopup() {
        this.driver.findElement(By.xpath("//*[text()='ACCEPT ALL']")).click();
    }

    public void setWindowResolution(int width, int height) {
        Dimension dim = new Dimension(width, height);
        this.driver.manage().window().setSize(dim);
    }

    public void openResearchEducationMenu()  {
        String html_name = "main_nav_research";   // class name of the element in HTML
        WebElement research = this.driver.findElement(By.className(html_name));
        research.click();
    }

    public void navigateToEconomicCalendar() {
        openResearchEducationMenu();
        this.waiter.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(text(),'Economic Calendar')]")));
        this.driver.findElement(By.xpath(
                "//a[@href='https://www.xm.com/research/economicCalendar']")).click();
    }

    public void navigateToEducationalVideos() {
        openResearchEducationMenu();
        this.waiter.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(text(),'Educational Videos')]")));
        this.driver.findElement(By.xpath("//*[text()='Educational Videos']")).click();
    }


    public void testCalendarSlider(String day) throws InterruptedException {
        Thread.sleep(250000);
        WebElement slider = this.driver.findElement(By.className("mat-slider-track-wrapper"));
        WebElement date_display = this.driver.findElement(By.className("\"tc-economic-calendar-item-header-left-title tc-normal-text"));
        String date_string = date_display.getAttribute("outerHtml");

        System.out.println("Found the date " + date_string);

        // offset tracks how many times we need to hit the right key to get the desired view
        int offset = 0;
        switch (day) {
            case "Today":
                System.out.println("Today");
                offset = 1;
                break;
            case "Tomorrow":
                System.out.println("Tomorrow");
                offset = 2;
                break;
            case "Next Week":
                System.out.println("Next Week");
                offset = 4;
                break;
            default:
                System.out.println("Not a value under test, skipping...");
        }

        for (int i = 0; i <= offset; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }
    }

    public void testEducationalVideo() {
        String target = "Lesson 1.1 “Introduction to the Financial Markets.”";
        //data-video="trd-s1|d49ddcb31d1be2c35c"
        this.driver.findElement(By.xpath("//tag_name[@data-video = \"trd-s1|d49ddcb31d1be2c35c\"]")).click();
        //class="xm-videos__player"
        WebElement player = this.driver.findElement(By.className("xm-videos__player"));
        player.click();

    }

}
