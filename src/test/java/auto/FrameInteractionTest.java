package auto;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FrameInteractionTest {

    Playwright playwright;
    Browser browser;
    BrowserContext context;
    Page page;



    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(true));
        context = browser.newContext();
        page = context.newPage();
    }
    @Test
    void testNestedFrames() {
        page.navigate("https://the-internet.herokuapp.com/nested_frames");

        // Переключение на фрейм left
        Frame leftFrame = page.frame("frame-left");
        String leftText = leftFrame.locator("body").textContent();
        Assertions.assertTrue(leftText.contains("LEFT"));

        // Переключение на фрейм middle
        Frame middleFrame = page.frame("frame-middle");
        String middleText = middleFrame.locator("body").textContent();
        Assertions.assertTrue(middleText.contains("MIDDLE"));

        // Открытие новой вкладки
        Page newPage = page.context().newPage();
        newPage.navigate("https://the-internet.herokuapp.com");

        // Закрытие новой вкладки
        newPage.close();
    }
    @AfterEach
    void tearDown() {
        browser.close();
        playwright.close();
    }
}
