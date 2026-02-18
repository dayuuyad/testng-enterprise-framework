// src/test/java/com/company/ecommerce/ui/UserManagementTests.java
package com.company.ecommerce.ui;

import com.company.ecommerce.base.BaseUITest;
import com.company.ecommerce.ui.pages.LoginPage;
import com.company.ecommerce.ui.pages.HomePage;
import com.company.ecommerce.ui.pages.UserProfilePage;
import com.company.ecommerce.config.ConfigManager;
import com.company.ecommerce.utils.testdata.TestDataProvider;
import org.testng.Assert;
import org.testng.annotations.*;

public class UserManagementTests extends BaseUITest {

    private LoginPage loginPage;
    private HomePage homePage;
    private UserProfilePage profilePage;

    @BeforeClass
    public void initPages() {
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);
        profilePage = new UserProfilePage(driver);
    }

    @Test(
            groups = {"smoke", "regression", "user"},
            description = "验证用户能够成功登录系统",
            priority = 1
    )
    public void testValidUserLogin() {
        // Given
        String username = ConfigManager.getInstance().getProperty("test.username");
        String password = ConfigManager.getInstance().getProperty("test.password");

        // When
        loginPage.navigateTo();
        HomePage homePage = loginPage.loginWithValidCredentials(username, password);

        // Then
        Assert.assertTrue(homePage.isUserLoggedIn(), "用户应该成功登录");
        Assert.assertEquals(homePage.getWelcomeMessage(),
                "Welcome, " + username, "欢迎消息应该正确显示");
    }

    @Test(
            groups = {"regression", "user"},
            dataProvider = "loginData",
            dataProviderClass = TestDataProvider.class,
            description = "使用不同测试数据验证登录功能"
    )
    public void testLoginWithDataProvider(String username, String password, boolean expected) {
        loginPage.navigateTo();
        loginPage.login(username, password);

        if (expected) {
            Assert.assertTrue(homePage.isUserLoggedIn(),
                    "用户 " + username + " 应该登录成功");
        } else {
            Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                    "用户 " + username + " 应该登录失败并显示错误信息");
        }
    }

    @Test(
            groups = {"user", "profile"},
            dependsOnMethods = "testValidUserLogin",
            description = "验证用户能够更新个人资料"
    )
    public void testUpdateUserProfile() {
        // Given
        String newFirstName = "Updated" + System.currentTimeMillis();
        String newLastName = "User" + System.currentTimeMillis();

        // When
        profilePage.navigate();
        profilePage.updateProfile(newFirstName, newLastName);

        // Then
        Assert.assertEquals(profilePage.getSuccessMessage(),
                "Profile updated successfully", "应该显示更新成功消息");
        Assert.assertEquals(profilePage.getFirstName(), newFirstName, "名字应该更新");
        Assert.assertEquals(profilePage.getLastName(), newLastName, "姓氏应该更新");
    }

    @Test(
            groups = {"security", "user"},
            description = "验证登录失败后的安全限制"
    )
    public void testLoginSecurityAfterMultipleFailures() {
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        // 连续5次错误登录
        for (int i = 0; i < 5; i++) {
            loginPage.navigateTo();
            loginPage.login(username, wrongPassword);
            Assert.assertTrue(loginPage.isErrorMessageDisplayed());
        }

        // 第6次尝试应该被锁定
        loginPage.navigateTo();
        loginPage.login(username, "anypassword");
        Assert.assertEquals(loginPage.getErrorMessage(),
                "Account locked due to multiple failed attempts",
                "账户应该被锁定");
    }
}