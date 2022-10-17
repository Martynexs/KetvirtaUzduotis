import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static junit.framework.TestCase.assertFalse;

public class ShoppingTest {
    private WebDriver m_driver;
    private static String m_email;
    private static String m_password;

    @BeforeClass
    public static void init()
    {
        WebDriverManager.chromedriver().setup();
        registerUser();
    }

    @Before
    public void setUp()
    {
        m_driver = getChromeDriver();
    }

    @After
    public void tearDown()
    {
        m_driver.quit();
    }

    @Test
    public void test1() throws FileNotFoundException
    {
        var items = getItemsFromFile("data1.txt");
        logIn(m_driver);
        m_driver.findElement(By.xpath("//li[@class = 'inactive']/a[@href = '/digital-downloads']")).click();
        orderItemsFromList(m_driver, items);
        assertFalse(m_driver.findElements(By.xpath("//strong[contains(text(), 'Your order has been successfully processed!')]")).isEmpty());
    }

    @Test
    public void test2() throws FileNotFoundException
    {
        var items = getItemsFromFile("data2.txt");
        logIn(m_driver);
        m_driver.findElement(By.xpath("//li[@class = 'inactive']/a[@href = '/digital-downloads']")).click();
        orderItemsFromList(m_driver, items);
        assertFalse(m_driver.findElements(By.xpath("//strong[contains(text(), 'Your order has been successfully processed!')]")).isEmpty());
    }

    private void orderItemsFromList(WebDriver driver, List<String> items)
    {
        var wait = new WebDriverWait(m_driver, Duration.ofSeconds(10));

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

        clickButton(driver, wait, "new-address-next-step-button");
        clickButton(driver, wait, "payment-method-next-step-button");
        clickButton(driver, wait, "payment-info-next-step-button");
        clickButton(driver, wait, "confirm-order-next-step-button");
    }

    private void clickButton(WebDriver driver, WebDriverWait wait, String className)
    {
        var button = driver.findElement(By.className(className));
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
    }

    private static void registerUser()
    {
        m_email = UUID.randomUUID() + "@email.net";
        m_password = UUID.randomUUID().toString();

        var driver = getChromeDriver();
        driver.get("https://demowebshop.tricentis.com/ ");
        driver.findElement(By.className("ico-login")).click();
        driver.findElement(By.className(("register-button"))).click();
        driver.findElement(By.xpath("//input[@id = 'gender-male']")).click();
        driver.findElement(By.xpath("//input[@id = 'FirstName']")).sendKeys(UUID.randomUUID().toString());
        driver.findElement(By.xpath("//input[@id = 'LastName']")).sendKeys(UUID.randomUUID().toString());
        driver.findElement(By.xpath("//input[@id = 'Email']")).sendKeys(m_email);
        driver.findElement(By.xpath("//input[@id = 'Password']")).sendKeys(m_password);
        driver.findElement(By.xpath("//input[@id = 'ConfirmPassword']")).sendKeys(m_password);
        driver.findElement(By.className(("register-next-step-button"))).click();
        driver.findElement(By.className(("register-continue-button"))).click();
        driver.quit();
    }

    private void logIn(WebDriver driver)
    {
        driver.get("https://demowebshop.tricentis.com/");
        driver.findElement(By.className("ico-login")).click();
        driver.findElement(By.className("email")).sendKeys(m_email);
        driver.findElement(By.className("password")).sendKeys(m_password);
        driver.findElement(By.className("login-button")).click();
    }

    private List<String> getItemsFromFile(String fileName) throws FileNotFoundException
    {
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

    private static WebDriver getChromeDriver()
    {
        var options = new ChromeOptions();
        options.setHeadless(true);
        var driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        return driver;
    }
}
