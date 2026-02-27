// src/test/java/com/company/ecommerce/ui/pages/HomePage.java
package com.company.ecommerce.ui.pages;

import com.company.ecommerce.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class HomePage extends BasePage {

    // 页面元素 - 顶部导航栏
    @FindBy(css = ".header-logo, .logo, [data-test='logo']")
    private WebElement logo;

    @FindBy(id = "search-input")
    private WebElement searchInput;

    @FindBy(css = ".search-button, button[type='submit'], [data-test='search-button']")
    private WebElement searchButton;

    @FindBy(css = ".cart-icon, .shopping-cart, [data-test='cart-icon']")
    private WebElement cartIcon;

    @FindBy(css = ".cart-count, .cart-item-count, [data-test='cart-count']")
    private WebElement cartCount;

    @FindBy(css = ".user-account, .account-dropdown, [data-test='user-account']")
    private WebElement userAccountDropdown;

    @FindBy(linkText = "我的账户")
    private WebElement myAccountLink;

    @FindBy(linkText = "订单历史")
    private WebElement orderHistoryLink;

    @FindBy(linkText = "退出登录")
    private WebElement logoutLink;

    // 页面元素 - 主内容区域
    @FindBy(css = ".welcome-message, .welcome-text, [data-test='welcome-message']")
    private WebElement welcomeMessage;

    @FindBy(css = ".product-grid, .products-container, [data-test='product-grid']")
    private WebElement productGrid;

    @FindBy(css = ".product-card, .product-item, [data-test='product-card']")
    private List<WebElement> productCards;

    @FindBy(css = ".category-nav .active, .active-category, [data-test='active-category']")
    private WebElement activeCategory;

    @FindBy(css = ".promo-banner, .banner, [data-test='promo-banner']")
    private WebElement promoBanner;

    // 页面元素 - 页脚
    @FindBy(linkText = "联系我们")
    private WebElement contactUsLink;

    @FindBy(linkText = "关于我们")
    private WebElement aboutUsLink;

    @FindBy(linkText = "帮助中心")
    private WebElement helpCenterLink;

    // 构造器
    public HomePage(WebDriver driver) {
        super(driver);
//        PageFactory.initElements(driver, this);
        waitForPageToLoad();
    }

    // 页面加载验证
    public boolean isHomePageLoaded() {
        try {
            return isDisplayed(logo) &&
                    driver.getCurrentUrl().contains(ConfigManager.getInstance().getWebBaseUrl());
        } catch (Exception e) {
            return false;
        }
    }

    public String getWelcomeMessage() {
        if (isDisplayed(welcomeMessage)) {
            return welcomeMessage.getText();
        }
        return "";
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public boolean isUserLoggedIn() {
        return isDisplayed(userAccountDropdown);
    }

    // 搜索功能
    public void searchProduct(String productName) {
        clearAndType(searchInput, productName);
        click(searchButton);
    }

    // 购物车功能
    public void openCart() {
        click(cartIcon);
    }

    public int getCartItemCount() {
        try {
            if (isDisplayed(cartCount)) {
                String countText = cartCount.getText().trim();
                return Integer.parseInt(countText);
            }
        } catch (Exception e) {
            // 如果无法解析数量，返回0
        }
        return 0;
    }

    // 用户账户操作
    public void openUserAccountDropdown() {
        click(userAccountDropdown);
        waitForElementToBeVisible(myAccountLink);
    }

    public void goToMyAccount() {
        openUserAccountDropdown();
        click(myAccountLink);
    }

    public void goToOrderHistory() {
        openUserAccountDropdown();
        click(orderHistoryLink);
    }

    public LoginPage logout() {
        openUserAccountDropdown();
        click(logoutLink);

        // 等待重定向到登录页
        waitForUrlContains("login");

        return new LoginPage(driver);
    }

    // 产品操作
    public List<WebElement> getProductCards() {
        waitForElementToBeVisible(productGrid);
        return productCards;
    }

    public int getProductCount() {
        return getProductCards().size();
    }

    public void openProductByIndex(int index) {
        List<WebElement> cards = getProductCards();
        if (index >= 0 && index < cards.size()) {
            WebElement productCard = cards.get(index);
            WebElement productLink = findElementWithin(productCard, By.cssSelector(".product-link, a"));
            click(productLink);
        } else {
            throw new IndexOutOfBoundsException("产品索引超出范围: " + index);
        }
    }

    public void openProductByName(String productName) {
        List<WebElement> cards = getProductCards();
        for (WebElement card : cards) {
            WebElement nameElement = findElementWithin(card, By.cssSelector(".product-name, h3, h4"));
            if (nameElement != null && nameElement.getText().contains(productName)) {
                WebElement productLink = findElementWithin(card, By.cssSelector(".product-link, a"));
                click(productLink);
                return;
            }
        }
        throw new RuntimeException("未找到产品: " + productName);
    }

    public String getActiveCategory() {
        if (isDisplayed(activeCategory)) {
            return activeCategory.getText();
        }
        return "";
    }

    // 页脚导航
    public void goToContactUs() {
        click(contactUsLink);
    }

    public void goToAboutUs() {
        click(aboutUsLink);
    }

    public void goToHelpCenter() {
        click(helpCenterLink);
    }

    // 辅助方法
    private void waitForPageToLoad() {
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(logo));
    }

    public void waitForHomePageToLoad() {
        waitForElementToBeVisible(logo);
        waitForElementToBeVisible(productGrid);
    }

    public boolean isPromoBannerDisplayed() {
        return isDisplayed(promoBanner);
    }

    public void closePromoBanner() {
        if (isPromoBannerDisplayed()) {
            WebElement closeButton = findElementWithin(promoBanner, By.cssSelector(".close-button, .close, [aria-label='Close']"));
            if (closeButton != null) {
                click(closeButton);
            }
        }
    }

    public HomePage refresh() {
        driver.navigate().refresh();
        waitForHomePageToLoad();
        return this;
    }

    public HomePage navigateToHome() {
        click(logo);
        waitForHomePageToLoad();
        return this;
    }

    // 从BasePage继承或新增的辅助方法

    private void waitForUrlContains(String text) {
        wait.until(ExpectedConditions.urlContains(text));
    }

    private WebElement findElementWithin(WebElement parent, By by) {
        try {
            return parent.findElement(by);
        } catch (Exception e) {
            return null;
        }
    }
}