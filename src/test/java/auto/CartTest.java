package auto;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//Практическое задание 3: Создание теста с визуальными артефактами
//Практическое задание 4: Интеграция скриншотов с Allure
public class CartTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private static Path outputDir;

    @BeforeAll
    static void setupAll() throws IOException {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        outputDir = Paths.get("artifacts", timestamp);
        Files.createDirectories(outputDir);
    }

    @BeforeEach
    void setup() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(outputDir)
                .setRecordVideoSize(1280, 720));
        page = context.newPage();
    }

    @Test
    void testCartActions() {
        page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");

        // Добавление элемента
        page.click("button[onclick='addElement()']");
        Locator addedButton = page.locator(".added-manually");
        addedButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));

        // Скриншот после добавления
        addedButton.screenshot(new Locator.ScreenshotOptions()
                .setPath(getPath("cart_after_add.png")));

        // Удаление элемента
        page.click(".added-manually");

        // Ждём, пока кнопка удалится
        addedButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED)
                .setTimeout(5000));

        // Скриншот после удаления
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(getPath("cart_after_remove.png")));
    }

    @AfterEach
    void teardownAndAttach(TestInfo testInfo) {
        try {
            // Скриншот для Allure, если тест не упал
            if (page != null && page.isClosed() == false) {
                byte[] screenshot = page.screenshot();
                Allure.addAttachment("Final Screenshot", "image/png",
                        new ByteArrayInputStream(screenshot), ".png");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании скриншота: " + e.getMessage());
        }

        // Сохраняем видео после закрытия контекста
        Video video = null;
        if (context != null) {
            if (!context.pages().isEmpty()) {
                video = context.pages().get(0).video();
            }
            context.close(); // сначала закрываем context (и страницы вместе с ним)
        }

        if (video != null) {
            try {
                video.saveAs(getPath("test_video.webm"));
            } catch (Exception e) {
                System.err.println("Ошибка при сохранении видео: " + e.getMessage());
            }
        }
    }

    @AfterAll
    static void teardownAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private Path getPath(String filename) {
        return outputDir.resolve(filename);
    }
}