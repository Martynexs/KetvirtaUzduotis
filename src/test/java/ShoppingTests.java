import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ShoppingTests {
    private static final String m_loginFileName = "loginInfo.txt";
    private WebDriver m_driver;
    private WebDriverWait m_wait;

    @BeforeAll
    public static void init() throws IOException {
        WebDriverManager.chromedriver().setup();
        registerUser();
    }

    @BeforeEach
    public void setUp()
    {
        WebDriverManager.chromedriver().setup();
        m_driver = new ChromeDriver();
        m_driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        m_wait = new WebDriverWait(m_driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown()
    {
        m_driver.quit();
    }

    @Test
    public void test1() throws FileNotFoundException {
        var items = getItemsFromFile("data1.txt");
        m_driver.get("https://demowebshop.tricentis.com/");
        logIn(m_driver);
        m_driver.findElement(By.xpath("//li[@class = 'inactive']/a[@href = '/digital-downloads']")).click();
        orderItemsFromList(m_driver, m_wait, items);
        assertFalse(m_driver.findElements(By.xpath("//strong[contains(text(), 'Your order has been successfully processed!')]")).isEmpty());
    }

    @Test
    public void test2() throws FileNotFoundException {
        var items = getItemsFromFile("data2.txt");
        m_driver.get("https://demowebshop.tricentis.com/");
        logIn(m_driver);
        m_driver.findElement(By.xpath("//li[@class = 'inactive']/a[@href = '/digital-downloads']")).click();
        orderItemsFromList(m_driver, m_wait, items);
        assertFalse(m_driver.findElements(By.xpath("//strong[contains(text(), 'Your order has been successfully processed!')]")).isEmpty());
    }

    private void orderItemsFromList(WebDriver driver, WebDriverWait wait, List<String> items)
    {
        var loadingBar = driver.findElement(By.className("ajax-loading-block-window"));
        for (var item : items) {
            driver.findElement(By.xpath(String.format("//div[@class = 'product-item']//h2[@class = 'product-title']/a[text() = '%s']/following::input[@value='Add to cart']", item))).click();
            wait.until(ExpectedConditions.invisibilityOf(loadingBar));
        }
        driver.findElement(By.className("ico-cart")).click();
        driver.findElement(By.id("termsofservice")).click();
        driver.findElement(By.id("checkout")).click();

        var savedAddressDoesntExists = driver.findElements(By.id("billing-address-select")).isEmpty();
        if(savedAddressDoesntExists)
        {
            Select countrySelect = new Select(driver.findElement(By.id("BillingNewAddress_CountryId")));
            countrySelect.selectByIndex(3);
            driver.findElement(By.id("BillingNewAddress_City")).sendKeys(UUID.randomUUID().toString());
            driver.findElement(By.id("BillingNewAddress_Address1")).sendKeys(UUID.randomUUID().toString());
            driver.findElement(By.id("BillingNewAddress_ZipPostalCode")).sendKeys(UUID.randomUUID().toString());
            driver.findElement(By.id("BillingNewAddress_PhoneNumber")).sendKeys(UUID.randomUUID().toString());
        }
        else
        {
            driver.findElement(By.className("new-address-next-step-button")).click();
        }

        driver.findElement(By.className("new-address-next-step-button")).click();

        var button = driver.findElement(By.className("payment-method-next-step-button"));
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
        driver.findElement(By.className("payment-info-next-step-button")).click();
        driver.findElement(By.className("confirm-order-next-step-button")).click();
    }

    private static void registerUser() throws IOException {
        var email = UUID.randomUUID().toString() + "@email.net";
        var password = UUID.randomUUID().toString();

        var file = new File(m_loginFileName);
        file.createNewFile();
        var writer = new FileWriter(file);
        writer.write(email + '\n');
        writer.write(password);
        writer.close();

        var driver = new ChromeDriver();
        driver.get("https://demowebshop.tricentis.com/ ");
        driver.findElement(By.className("ico-login")).click();
        driver.findElement(By.className(("register-button"))).click();
        driver.findElement(By.xpath("//input[@id = 'gender-male']")).click();
        driver.findElement(By.xpath("//input[@id = 'FirstName']")).sendKeys(UUID.randomUUID().toString());
        driver.findElement(By.xpath("//input[@id = 'LastName']")).sendKeys(UUID.randomUUID().toString());
        driver.findElement(By.xpath("//input[@id = 'Email']")).sendKeys(email);
        driver.findElement(By.xpath("//input[@id = 'Password']")).sendKeys(password);
        driver.findElement(By.xpath("//input[@id = 'ConfirmPassword']")).sendKeys(password);
        driver.findElement(By.className(("register-next-step-button"))).click();
        driver.findElement(By.className(("register-continue-button"))).click();
        driver.quit();
    }

    private void logIn(WebDriver driver) throws FileNotFoundException {
        var file = new File(m_loginFileName);
        var reader = new Scanner(file);
        var email = reader.nextLine();
        var password = reader.nextLine();
        reader.close();

        driver.findElement(By.className("ico-login")).click();
        driver.findElement(By.className("email")).sendKeys(email);
        driver.findElement(By.className("password")).sendKeys(password);
        driver.findElement(By.className("login-button")).click();
    }

    private List<String> getItemsFromFile(String fileName) throws FileNotFoundException {
        var items = new ArrayList<String>();
        var file = new File(fileName);
        var reader = new Scanner(file);
        while (reader.hasNextLine())
        {
            String item = reader.nextLine();
            items.add(item);
        }
        reader.close();
        return items;
    }
}
