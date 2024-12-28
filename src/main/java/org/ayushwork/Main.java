package org.ayushwork;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        FileWriter csvWriter = new FileWriter("google_job_ids2.csv");

        try {
            driver.get("https://www.linkedin.com/login");
            driver.manage().window().maximize();

            WebElement emailField = driver.findElement(By.id("username"));
            emailField.sendKeys("ayushsinha11@gmail.com");

            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("*********");

            driver.findElement(By.xpath("//button[@type='submit']")).click();

            Thread.sleep(10000);

            if (!Objects.requireNonNull(driver.getCurrentUrl()).contains("feed")){
                try{
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter the OTP sent to your registered device: ");
                    String otp = scanner.nextLine();

                    driver.findElement(By.xpath("//input[@id='input__phone_verification_pin']")).sendKeys(otp);

                    WebElement otpSubmitButton = driver.findElement(By.xpath("//button[@type='submit']"));
                    otpSubmitButton.click();
                }catch (Exception e){
                    System.out.println("OTP verification failed!!");
                }
            }else{
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                driver.findElement(By.xpath("//a[contains(@href, 'jobs')]")).click();
                wait.until(ExpectedConditions.urlContains("jobs"));
                WebElement companyFilter = driver.findElement(By.xpath("//input[contains(@id, 'jobs-search-box')]"));
                companyFilter.sendKeys("Google");
                companyFilter.sendKeys(Keys.ENTER);

                Thread.sleep(10000);

                int maxResults = 950;
                int currentPage = 1;

                String[] headers = {"JobID", "Job Role Name", "is_verified", "Role issued by", "Location", "posted_when", "raw_text"};
                csvWriter.append(String.join(",", headers));
                csvWriter.append("\n");

                for(int i=0; i<=maxResults; i=i+25){

                    Thread.sleep(7000);

                    List<WebElement> searchResults = driver.findElements(By.xpath("//div[@data-job-id]"));
                    for(WebElement currentListing : searchResults){
                        String jobId = currentListing.getAttribute("data-job-id");

                        List<String> jobDesc = List.of(currentListing.getText().split("\n"));
                        StringBuilder line = new StringBuilder();

                        line.append(jobId)
                                .append(",")
                                .append(jobDesc.get(0).replaceAll(",", "."))
                                .append(",")
                                .append(checkVerifiedStatus(jobDesc.get(1)))
                                .append(",")
                                .append(jobDesc.get(2).replaceAll(",", "."))
                                .append(",")
                                .append(jobDesc.get(3).replaceAll(",", "."))
                                .append(",")
                                .append(jobDesc.get(4).replaceAll(",", "."))
                                .append(",")
                                .append(currentListing.getText()
                                        .replaceAll(",",".")
                                        .replaceAll("\n","."))
                                .append(",")
                                .append("\n");

                        csvWriter.append(line);

                    }

                    driver.findElement(By.xpath(String.format("//li[contains(@class, 'artdeco-pagination')]/button[@aria-label='Page %d']", currentPage + 1))).click();

                    Thread.sleep(3000);

                    currentPage++;

                }
                System.out.println("Job IDs have been written to 'job_ids.csv'");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            csvWriter.flush();
            csvWriter.close();
        }
    }

    private static boolean checkVerifiedStatus(String s) {
        return s.toLowerCase().contains("verification");
    }
}