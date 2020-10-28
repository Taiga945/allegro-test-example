package com.example.allegrotest;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MainPageTest {

    @Test
    public void openPage() throws InterruptedException {
        By consentExit = By.xpath("//button[@data-role='accept-consent']");
        By searchInput = By.xpath("//input");
        By searchButton = By.xpath("//form/button");
        By blackModelCheckbox = By.xpath("//fieldset[11]/div/ul/li/label");
        By productList = By.xpath("//div[@data-box-name='items-v3']");
        By productItem = By.tagName("article");
        By productPriceContainer = By.className("_9c44d_2K6FN");
        By productPriceSpan = By.tagName("span");

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get("https://allegro.pl/");
        wait.until(ExpectedConditions.elementToBeClickable(consentExit));
        driver.findElement(consentExit).click();
        wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        driver.findElement(searchInput).sendKeys("iphone11");
        driver.findElement(searchButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(blackModelCheckbox));
        driver.findElement(blackModelCheckbox).click();
        Thread.sleep(5000);
        WebElement itemsContainer = driver.findElement(productList);
        List<WebElement> offers = itemsContainer.findElements(productItem);
        Integer allOffers = offers.size();
        Integer sponsoredOffers = (int) offers.stream().filter(MainPageTest::isSponsoredItem).count();
        System.out.println("Wszystkie oferty: " + allOffers);
        System.out.println("Ofert niesponsorowane: " + (allOffers - sponsoredOffers));
        System.out.println("Oferty sponsorowane: " + sponsoredOffers);

        DecimalFormat decimalFormat = getPriceDecimalFormat();

        Optional<BigDecimal> maxPriceOptional = offers.stream()
                .map(it -> it.findElement(productPriceContainer))
                .map(it -> it.findElement(productPriceSpan))
                .map(WebElement::getText)
                .map(it -> parsePrice(decimalFormat, it))
                .max(Comparator.naturalOrder());

        if (maxPriceOptional.isPresent()) {
            BigDecimal maxPrice = maxPriceOptional.get();
            System.out.println("Najwyższa cena: " + maxPrice);
            System.out.println("Najwyższa cena powiększona o 23%: " + maxPrice.multiply(new BigDecimal("1.23")));
        } else {
            System.out.println("Nie znalazło ceny żadnego produktu!");
        }

        driver.quit();
    }

    private static DecimalFormat getPriceDecimalFormat() {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator(',');
        formatSymbols.setGroupingSeparator(' ');
        String strange = "#,##0.00";
        return new DecimalFormat(strange, formatSymbols);
    }

    public static BigDecimal parsePrice(DecimalFormat decimalFormat, String it) {
        try {
            return new BigDecimal(decimalFormat.parse(it).toString());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse price", e);
        }
    }

    private static boolean isSponsoredItem(WebElement item) {
        String sponsoredLabel = item.getAttribute("data-analytics-view-label");
        return sponsoredLabel != null && sponsoredLabel.equals("showSponsoredItems");
    }
}