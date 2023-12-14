import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class XMWebTester {

    final String xm_url = "https://xm.com";
    // Calendar Slider Values for days, based on the aria-value
    final int TODAY = 1;
    final int TOMORROW = 2;
    final int NEXT_WEEK = 4;
    WebDriver driver;
    WebDriverWait waiter;
    JavascriptExecutor js;


    @BeforeClass
    void setupDriver() {
        this.driver = getWebDriver("Firefox");
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        this.waiter = new WebDriverWait(this.driver, Duration.ofSeconds(10));  // to wait for specific events
        this.js = (JavascriptExecutor) this.driver;
        navigateHomePage();
        removeCookiePopup();
    }

    @BeforeMethod
    void freshStart() {
        navigateHomePage();
    }

    @AfterClass
    void tearDown() {
        this.driver.close();
    }

    @Test
    void testNavigationToEconomicCalendar() {
        String expected_url = "https://www.xm.com/research/economicCalendar";
        navigateToEconomicCalendar();
        assert (this.driver.getCurrentUrl().equals(expected_url));
    }

    @Test
    void testNavigationToEducationalVideos() {
        String expected_url = "https://www.xm.com/educational-videos";
        navigateToEducationalVideos();
        assert (this.driver.getCurrentUrl().equals(expected_url));
    }

    @Test
    void testCalendarSlider() {
        navigateToEconomicCalendar();
        switchTocalendarFrame();
        assert validateCalendarSlider(TODAY);
        assert validateCalendarSlider(TOMORROW);
        assert validateCalendarSlider(NEXT_WEEK);

    }

    @Test
    void testVideoPlayback5Seconds() throws InterruptedException {
        navigateToEducationalVideos();
        displayLesson1Video();
        playVideoForDuration(5000); //play for 5 seconds
        WebElement progress_time = driver.findElement(By.className("player-progress-time"));
        String time_text = progress_time.getAttribute("textContent");
        System.out.println("Time... " + time_text);
        // We validate  for +/- 1 second from the target to account for asynchronicity
        assert time_text.equals("00:05") || time_text.equals("00:04") || time_text.equals("00:06");

    }

    public WebDriver getWebDriver(String choice) {
        if (choice.equals("Chrome")) {
            return new ChromeDriver();
        } else return new FirefoxDriver();
    }

    public void navigateHomePage() {
        this.driver.get(xm_url);
    }

    public void removeCookiePopup() {
        WebElement cookie_button = this.driver.findElement(By.xpath("//*[text()='ACCEPT ALL']"));
        this.waiter.until(ExpectedConditions.elementToBeClickable(cookie_button));
        cookie_button.click();
    }

    public void setWindowResolution(int width, int height) {
        Dimension dim = new Dimension(width, height);
        this.driver.manage().window().setSize(dim);
    }

    public void openResearchEducationMenu() {
        String html_name = "main_nav_research";   // class name of the element in HTML
        WebElement research_tab = this.driver.findElement(By.className(html_name));
        js.executeScript("arguments[0].scrollIntoView();", research_tab);
        this.waiter.until(ExpectedConditions.elementToBeClickable(research_tab));
        research_tab.click();
    }

    public void navigateToEconomicCalendar() {
        openResearchEducationMenu();
        WebElement economic_calendar = this.driver.findElement(By.xpath("//a[contains(text(),'Economic Calendar')]"));
        this.waiter.until(ExpectedConditions.elementToBeClickable(economic_calendar));
        economic_calendar.click();
    }

    public void navigateToEducationalVideos() {
        openResearchEducationMenu();
        WebElement educational_videos = driver.findElement(By.xpath("//a[contains(text(),'Educational Videos')]"));
        this.waiter.until(ExpectedConditions.elementToBeClickable(educational_videos));
        educational_videos.click();
    }

    public String getISOFormat(String date) {
        DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH);
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate d = LocalDate.parse(date, input);
        return d.format(iso);
    }

    public boolean validateDate(String date, int day) {
        date = getISOFormat(date.trim());
        LocalDate today = LocalDate.now();
        LocalDate expected = today;
        if (day == TOMORROW) {
            expected = today.plusDays(1);
        } else if (day == NEXT_WEEK) {
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            int daysUntilNextWeek = DayOfWeek.SUNDAY.getValue() - dayOfWeek.getValue() + 1;
            expected = today.plusDays(daysUntilNextWeek);
        }
        System.out.println("Expected  date " + expected);
        System.out.println("Received date " + date);
        return date.equals(expected.toString());
    }

    public void switchTocalendarFrame() {
        WebElement calendar_frame = this.driver.findElement(By.id("iFrameResizer0"));
        driver.switchTo().frame(calendar_frame);
    }

    public boolean validateCalendarSlider(int day) {
        WebElement slider = this.driver.findElement(By.xpath("//mat-slider[@role='slider']"));
        setSlider(slider, day);
        return validateDate(getCalendarDate(), day);
    }

    void setSlider(WebElement slider, int value) {
        clearSlider(slider);
        for (int i = 0; i < value; i++) {
            slider.sendKeys(Keys.ARROW_RIGHT);
        }
    }

    void clearSlider(WebElement slider) {
        for (int i = 0; i < 10; i++) {
            slider.sendKeys(Keys.ARROW_LEFT);
        }
    }

    String getCalendarDate() {
        WebElement date_display = this.driver.findElement(By.
                xpath("//span[@class='tc-economic-calendar-item-header-left-title tc-normal-text']"));
        String date_string = date_display.getAttribute("textContent");
        System.out.println("Found the date:" + date_string);
        return date_string;
    }

    public void displayLesson1Video() {
        WebElement intro_to_market_drop = driver.findElement(By.xpath("//button[normalize-space()='Intro to the Markets']"));
        intro_to_market_drop.click();
        WebElement video_1_icon = driver.findElement(By.xpath("//a[normalize-space()='Lesson 1.1']"));
        video_1_icon.click();
    }

    public void playVideoForDuration(long duration) throws InterruptedException {
        WebElement player = this.driver.findElement(By.className("sproutvideo-player"));
        driver.switchTo().frame(player);
        WebElement play_button = driver.findElement(By.className("player-big-play-button"));
        this.waiter.until(ExpectedConditions.elementToBeClickable(play_button));
        play_button.click();
        Thread.sleep(duration);
    }

}
