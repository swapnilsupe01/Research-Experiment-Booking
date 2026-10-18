package com.researchbooking.research_booking_system.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SeleniumJourneyChecks {

    private static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:8083");

    private WebDriver driver;
    private WebDriverWait wait;

    @RegisterExtension
    final FailureScreenshotExtension screenshotExtension =
            new FailureScreenshotExtension();

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--window-size=1440,1000",
                "--disable-gpu"
        );

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        screenshotExtension.driver = driver;
        driver.get(BASE_URL + "/ui");
    }

    @AfterEach
    void tearDown() {
        try {
            if (driver != null && screenshotExtension.failure != null) {
                saveFailureScreenshot();
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
            screenshotExtension.driver = null;
            screenshotExtension.failure = null;
        }
    }

    @Test
    void dashboardShouldDisplayExperiments() {
        wait.until(d ->
                !d.findElements(By.cssSelector("#experiments article.experiment-card"))
                        .isEmpty()
        );

        WebElement heading = driver.findElement(By.tagName("h1"));

        assertEquals(
                "INTENTIONALLY BROKEN - Research Experiment Booking System",
                heading.getText()
        );

        assertTrue(
                driver.findElements(
                        By.cssSelector("#experiments article.experiment-card")
                ).size() > 0,
                "At least one experiment should be displayed."
        );
    }

    @Test
    void experimentDetailsAndSlotsShouldLoad() {
        wait.until(d ->
                !d.findElements(
                        By.cssSelector("#experiments article.experiment-card")
                ).isEmpty()
        );

        WebElement detailsButton = driver.findElement(
                By.cssSelector("#experiments article.experiment-card button")
        );

        detailsButton.click();

        wait.until(d -> {
            ListHelper details = new ListHelper(
                    d.findElements(By.cssSelector("#details h3"))
            );
            return details.hasVisibleText();
        });

        String detailTitle = driver.findElement(
                By.cssSelector("#details h3")
        ).getText();

        assertFalse(detailTitle.isBlank(),
                "Experiment details should contain a title.");

        wait.until(d ->
                !d.findElement(By.id("slots"))
                        .getText().equals("Loading slots...")
        );

        String slotsText = driver.findElement(By.id("slots")).getText();

        assertFalse(slotsText.isBlank(),
                "The slots area should display a result.");
    }

    @Test
    void registrationFormShouldValidateRequiredFields() {
        WebElement form = driver.findElement(By.id("registration-form"));

        Boolean valid = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].checkValidity();", form);

        assertFalse(valid,
                "An empty registration form should fail browser validation.");

        assertNotNull(driver.findElement(By.id("fullName"))
                .getAttribute("required"));

        assertNotNull(driver.findElement(By.id("email"))
                .getAttribute("required"));

        assertNotNull(driver.findElement(By.id("password"))
                .getAttribute("required"));
    }

    @Test
    void studentShouldRegisterSuccessfully() {
        String uniqueEmail = "selenium."
                + UUID.randomUUID().toString().replace("-", "")
                + "@example.com";

        driver.findElement(By.id("fullName"))
                .sendKeys("Selenium Test Student");

        driver.findElement(By.id("email"))
                .sendKeys(uniqueEmail);

        driver.findElement(By.id("password"))
                .sendKeys("TestPass123!");

        driver.findElement(By.id("register-button")).click();

        wait.until(d -> d.findElement(By.id("message"))
                .getText().startsWith("Registration successful!"));

        String message = driver.findElement(By.id("message")).getText();

        assertTrue(message.contains("User ID:"),
                "Successful registration should return a user ID.");

        assertTrue(driver.findElement(By.id("message"))
                        .getAttribute("class").contains("success"),
                "The registration message should have the success class.");
    }

    private void saveFailureScreenshot() {
        try {
            Path directory = Path.of("target", "selenium-screenshots");
            Files.createDirectories(directory);

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            );

            String fileName = screenshotExtension.testName
                    .replaceAll("[^a-zA-Z0-9_-]", "_")
                    + "-" + timestamp + ".png";

            Path destination = directory.resolve(fileName);

            Path temporaryScreenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE).toPath();

            Files.copy(temporaryScreenshot, destination,
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Failure screenshot saved: "
                    + destination.toAbsolutePath());

        } catch (Exception screenshotError) {
            System.err.println("Could not save failure screenshot: "
                    + screenshotError.getMessage());
        }
    }

    private static class ListHelper {
        private final java.util.List<WebElement> elements;

        ListHelper(java.util.List<WebElement> elements) {
            this.elements = elements;
        }

        boolean hasVisibleText() {
            return elements.stream().anyMatch(
                    e -> e.isDisplayed() && !e.getText().isBlank()
            );
        }
    }

    private static class FailureScreenshotExtension
            implements TestExecutionExceptionHandler {

        WebDriver driver;
        Throwable failure;
        String testName = "selenium-test";

        @Override
        public void handleTestExecutionException(
                ExtensionContext context, Throwable throwable)
                throws Throwable {

            failure = throwable;
            testName = context.getRequiredTestMethod().getName();
            throw throwable;
        }
    }
}

