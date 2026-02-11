# 编码规范

## 目录结构规范

### 项目结构
```
src/test/java/com/company/ecommerce/
├── base/                    # 基础测试类
├── listeners/              # TestNG 监听器
├── utils/                  # 工具类
├── api/                    # API 测试
├── ui/                     # UI 测试
│   └── pages/             # 页面对象
└── integration/            # 集成测试
```

## 命名规范

### 类命名
- **测试类**: `[功能模块]Tests.java`
  - `UserManagementTests.java`
  - `ProductSearchTests.java`

- **页面类**: `[页面名称]Page.java`
  - `LoginPage.java`
  - `HomePage.java`

- **工具类**: `[功能]Utils.java`
  - `WebDriverUtils.java`
  - `TestDataUtils.java`

### 方法命名
- **测试方法**: `test[场景]_[条件]_[预期结果]`
  - `testLogin_WithValidCredentials_ShouldSucceed`
  - `testSearch_WithEmptyQuery_ShouldShowErrorMessage`

- **页面方法**: 动作动词 + 名词
  - `enterUsername(String username)`
  - `clickLoginButton()`
  - `isElementDisplayed()`

## 代码结构规范

### 测试类结构
```java
@Test(groups = {"ui", "regression", "user"})
public class UserManagementTests extends BaseUITest {

    // 1. 页面对象声明
    private LoginPage loginPage;
    private HomePage homePage;

    // 2. Setup 方法
    @BeforeClass
    public void setupPages() {
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);
    }

    // 3. 测试方法
    @Test(priority = 1, groups = {"smoke"})
    public void testValidLogin() {
        // Given-When-Then 模式
        String username = ConfigManager.getTestUsername();
        String password = ConfigManager.getTestPassword();

        loginPage.navigateToLoginPage();
        homePage = loginPage.login(username, password);

        Assert.assertTrue(homePage.isUserLoggedIn());
    }

    // 4. 清理方法
    @AfterMethod
    public void cleanup() {
        // 清理逻辑
    }
}
```

### 页面对象规范
```java
public class LoginPage extends BasePage {

    // 页面元素定位
    @FindBy(id = "username")
    private WebElement usernameInput;

    // 页面操作方法
    public void enterUsername(String username) {
        type(usernameInput, username, "Username Input");
    }

    // 业务流方法
    public HomePage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLogin();
    }
}
```

## 测试方法规范

### Given-When-Then 模式
```java
@Test
public void testUserRegistration() {
    // Given - 测试准备
    String email = generateUniqueEmail();
    String password = "Test@12345";

    // When - 执行操作
    RegistrationPage registrationPage = new RegistrationPage(driver);
    registrationPage.registerUser(email, password);

    // Then - 验证结果
    Assert.assertTrue(registrationPage.isSuccessMessageDisplayed());
}
```

## 数据管理

### 测试数据提供者
```java
public class TestDataProvider {

    @DataProvider(name = "loginData")
    public static Object[][] getLoginData() {
        return new Object[][] {
            {"user@example.com", "Pass123!", true, "有效凭证"},
            {"", "Pass123!", false, "空用户名"},
            {"invalid@email", "Pass123!", false, "无效邮箱"}
        };
    }
}
```

## 日志规范

```java
public class UserTests extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTests.class);

    @Test
    public void testUserLogin() {
        logger.info("开始登录测试");
        logger.debug("使用用户名: {}", username);

        try {
            // 测试逻辑
            logger.info("登录成功");
        } catch (Exception e) {
            logger.error("登录失败", e);
            throw e;
        }
    }
}
```

## 代码审查清单

- [ ] 测试方法有清晰的描述
- [ ] 遵循 Given-When-Then 模式
- [ ] 没有硬编码的值
- [ ] 适当的日志记录
- [ ] 正确的断言消息
- [ ] 资源正确清理
- [ ] 遵循命名规范
