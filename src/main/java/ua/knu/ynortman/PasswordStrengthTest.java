package ua.knu.ynortman;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PasswordStrengthTest {

    private static final String API_URL = "https://api.pwnedpasswords.com/range/";
    private static final int PSWRDS_NUM = 200;

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
//        generateFileWithPaswords();
//        int safePasswordsNum = checkPasswords();
//        log.info("Safe Passwords Num: {}", safePasswordsNum);
        int[] result = seleniumTest();
        log.info("Result: {}", result);
    }

    private static void generateFileWithPaswords() throws IOException, InterruptedException {
        PasswordGeneratorBot passwordGeneratorBot = new PasswordGeneratorBot();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter( "passwords.txt"))) {
            for (int i = 0; i < PSWRDS_NUM; i++) {
                String password = passwordGeneratorBot.generatePassword(true);
                log.info("Password {}: {}", i, password);
                writer.write(password);
                writer.newLine();
                Thread.sleep(20000);
            }
        }
    }

    private static int checkPasswords() throws IOException, NoSuchAlgorithmException {
        String fileName = "passwords.txt";
        int safePswrdsNum = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String password;
            while ((password = br.readLine()) != null) {
                if (isPasswordPwned(password)) {
                    System.out.println("Password is pwned: " + password);
                } else {
                    System.out.println("Password is safe: " + password);
                    safePswrdsNum++;
                }
            }
        }
        return safePswrdsNum;
    }

    private static boolean isPasswordPwned(String password) throws IOException, NoSuchAlgorithmException {
        String sha1Hash = getSHA1Hash(password);
        String prefix = sha1Hash.substring(0, 5);
        String suffix = sha1Hash.substring(5);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(API_URL + prefix);
        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        return responseBody.contains(suffix);
    }

    private static String getSHA1Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    @SneakyThrows
    private static int[] seleniumTest() {
//        System.setProperty("webdriver.chrome.driver", "шлях/до/chromedriver.exe");
        int[] result = new int[4];
        URL resourceUrl = PasswordStrengthTest.class.getClassLoader().getResource("chromedriver.exe");

        if (resourceUrl != null) {
            System.setProperty("webdriver.chrome.driver", resourceUrl.getPath());
        } else {
            System.out.println("Не вдалося знайти chromedriver.exe у папці ресурсів");
            return null;
        }

        String fileName = "passwords.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String password;
            while ((password = br.readLine()) != null) {
                log.info("Password: {}", password);
                WebDriver driver = new ChromeDriver();
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                driver.get("https://www.allthingssecured.com/password-checker/"); // Замініть на URL сторінки, яку потрібно протестувати

                WebElement inputField = driver.findElement(By.xpath("/html/body/div/div/div/main/article/div/div[1]/div/div[1]/input")); // Замініть на відповідний ідентифікатор поля введення
                inputField.sendKeys(password);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                WebElement appearedText = driver.findElement(By.xpath("/html/body/div/div/div/main/article/div/div[1]/div/div[1]/span")); // Замініть на відповідний ідентифікатор елемента, де з'являється текст
                String text = appearedText.getText();
                log.info("Text: " + text);

                driver.quit();
                if("Very Weak".equals(text)) {
                    result[0]++;
                } else if("Medium".equals(text)) {
                    result[1]++;
                } else if("Strong".equals(text)) {
                    result[2]++;
                } else if("Very Strong".equals(text)) {
                    result[3]++;
                } else {
                    log.info("Password {} is {}", password, text);
                }
            }
        }


        return result;
    }
}
