package auto;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

//2.2 Перехват сетевых запросов/Практическое задание 2: Тестируем сайт с рекламой

    public class BlockTests {
        Playwright playwright;
        Browser browser;

        @BeforeEach
        void setUp() {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }

        @AfterEach
        void tearDown() {
            browser.close();
            playwright.close();
        }

        // Метод для замера времени загрузки страницы
        long measureLoadTime(BrowserContext context, boolean blockAds) {
            Page page = context.newPage();

            if (blockAds) {
                // Блокируем рекламные запросы
                context.route("**/*", route -> {
                    String url = route.request().url();
                    if (url.matches(".*(ads|doubleclick|adfox|googlesyndication|pagead).*")) {
                        route.abort();
                    } else {
                        route.resume();
                    }
                });
            }

            long start = System.currentTimeMillis();
            page.navigate("https://dzen.ru");
            page.waitForLoadState(LoadState.LOAD);
            long end = System.currentTimeMillis();
            return end - start;
        }
        @Test
        void testAdBlocking() {
            //  Создаём контекст с блокировкой рекламы
            BrowserContext contextWithAdBlock = browser.newContext();
            long blockedTime = measureLoadTime(contextWithAdBlock, true);
            Page pageWithAdBlock = contextWithAdBlock.pages().get(0);
            //  Контекст без блокировки рекламы
            BrowserContext contextWithoutAdBlock = browser.newContext();
            long normalTime = measureLoadTime(contextWithoutAdBlock, false);
            // Проверка-на странице нет элементов с классом .ad
            List<ElementHandle> ads = pageWithAdBlock.querySelectorAll(".ad");
            assertEquals(0, ads.size(), "Страница не должна содержать элементов с классом .ad при блокировке рекламы");
            // Замеряет время загрузки страницы с блокировкой и без
            System.out.println("Время загрузки с блокировкой рекламы: " + blockedTime + " мс");
            System.out.println("Время загрузки без блокировки рекламы: " + normalTime + " мс");
        }
    }

