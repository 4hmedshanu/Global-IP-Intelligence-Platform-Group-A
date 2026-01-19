package com.ipintelligence.service.impl;

import java.util.Collections;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class TmViewSeleniumService {

    public List<com.ipintelligence.dto.IpAssetDto> searchTrademark(String query) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);
        List<com.ipintelligence.dto.IpAssetDto> results = new ArrayList<>();
        try {
            driver.get("https://www.tmdn.org/tmview/#/tmview");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement searchBox = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[data-test-id='basic-search-input']"))
            );
            searchBox.sendKeys(query);
            searchBox.sendKeys(Keys.RETURN);

            // Wait for either results table or 'no results' message
            wait.until(driver1
                    -> driver1.findElements(By.cssSelector("div.ReactTable .rt-table")).size() > 0
                    || driver1.findElements(By.xpath("//*[contains(text(),'No results') or contains(text(),'No records found') or contains(text(),'No data available')]")).size() > 0
            );

            // Check if 'no results' message is present
            List<WebElement> noResults = driver.findElements(By.xpath("//*[contains(text(),'No results') or contains(text(),'No records found') or contains(text(),'No data available') or contains(@class,'no-data') or contains(@class,'noRows') or contains(@class,'no-results') or contains(@class,'noRecords') or contains(@class,'noData') or contains(@class,'noResult') or contains(@class,'noRecord') or contains(@class,'no-row') or contains(@class,'no-row-found') or contains(@class,'no-row-message') or contains(@class,'no-row-msg')]"));
            if (!noResults.isEmpty()) {
                System.out.println("[SELENIUM-DEBUG] No results message found");
                return Collections.emptyList();
            }

            // Extract rows if table is present
            List<WebElement> rows = driver.findElements(By.cssSelector("div.ReactTable .rt-tbody .rt-tr-group"));
            System.out.println("[SELENIUM-DEBUG] Found " + rows.size() + " result rows");
            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.cssSelector(".rt-td"));
                // Print all cell values for debug
                StringBuilder debugRow = new StringBuilder();
                for (int i = 0; i < cells.size(); i++) {
                    debugRow.append("[" + i + ": '" + cells.get(i).getText() + "'] ");
                }
                System.out.println("[SELENIUM-DEBUG] Row cells: " + debugRow);
                if (cells.size() >= 10) {
                    com.ipintelligence.dto.IpAssetDto dto = new com.ipintelligence.dto.IpAssetDto();
                    dto.setExternalId(cells.get(8).getText());
                    dto.setTitle(cells.get(3).getText());
                    dto.setApplicationNumber(cells.get(8).getText());
                    dto.setAssignee(cells.get(9).getText());
                    dto.setPatentOffice(cells.get(7).getText());
                    dto.setStatus(cells.get(6).getText());
                    dto.setAssetType(com.ipintelligence.model.IpAsset.AssetType.TRADEMARK);
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return results;
    }
}
