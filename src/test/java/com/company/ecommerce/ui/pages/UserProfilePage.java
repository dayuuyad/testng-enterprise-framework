// src/test/java/com/company/ecommerce/ui/pages/UserProfilePage.java
package com.company.ecommerce.ui.pages;

//import com.company.ecommerce.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static com.company.ecommerce.utils.WebDriverManager.*;

public class UserProfilePage extends BasePage {

    // Page URL
    private static final String PROFILE_URL = "/user/profile";

    // Page Elements
    @FindBy(id = "firstName")
    private WebElement firstNameInput;

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "phone")
    private WebElement phoneInput;

    @FindBy(id = "address")
    private WebElement addressTextarea;

    @FindBy(id = "city")
    private WebElement cityInput;

    @FindBy(id = "zipCode")
    private WebElement zipCodeInput;

    @FindBy(css = "button[type='submit']")
    private WebElement saveButton;

    @FindBy(className = "alert-success")
    private WebElement successMessage;

    @FindBy(className = "alert-danger")
    private WebElement errorMessage;

    @FindBy(css = ".profile-header h1")
    private WebElement profileHeader;

    @FindBy(id = "currentFirstName")
    private WebElement currentFirstName;

    @FindBy(id = "currentLastName")
    private WebElement currentLastName;

    @FindBy(linkText = "Edit Profile")
    private WebElement editProfileLink;

    @FindBy(linkText = "Change Password")
    private WebElement changePasswordLink;

    @FindBy(linkText = "Order History")
    private WebElement orderHistoryLink;

    @FindBy(css = ".avatar-container img")
    private WebElement avatarImage;

    @FindBy(id = "uploadAvatar")
    private WebElement uploadAvatarInput;

    @FindBy(id = "avatarSubmit")
    private WebElement avatarSubmitButton;

    // Constructor
    public UserProfilePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    // Page Methods

    /**
     * 导航到用户资料页面
     */
    public void navigate() {
        navigateTo(PROFILE_URL);
        waitForPageLoad();
    }

    /**
     * 更新用户资料
     * @param firstName 名字
     * @param lastName 姓氏
     */
    public void updateProfile(String firstName, String lastName) {
        // 点击编辑按钮
        clickEditProfile();

        // 清空并输入新的名字
        clearAndType(firstNameInput, firstName);

        // 清空并输入新的姓氏
        clearAndType(lastNameInput, lastName);

        // 点击保存按钮
        clickSave();

        // 等待操作完成
        waitForSuccessMessage();
    }

    /**
     * 更新完整资料
     */
    public void updateFullProfile(String firstName, String lastName, String phone,
                                  String address, String city, String zipCode) {
        clickEditProfile();

        clearAndType(firstNameInput, firstName);
        clearAndType(lastNameInput, lastName);
        clearAndType(phoneInput, phone);
        clearAndType(addressTextarea, address);
        clearAndType(cityInput, city);
        clearAndType(zipCodeInput, zipCode);

        clickSave();
        waitForSuccessMessage();
    }

    /**
     * 点击编辑资料链接
     */
    public void clickEditProfile() {
        wait.until(ExpectedConditions.elementToBeClickable(editProfileLink));
        editProfileLink.click();
        waitForElementToBeVisible(firstNameInput);
    }

    /**
     * 点击保存按钮
     */
    public void clickSave() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        saveButton.click();
    }

    /**
     * 上传头像
     * @param filePath 头像文件路径
     */
    public void uploadAvatar(String filePath) {
        wait.until(ExpectedConditions.elementToBeClickable(avatarImage)).click();
        uploadAvatarInput.sendKeys(filePath);
        avatarSubmitButton.click();
        waitForSuccessMessage();
    }

    /**
     * 导航到更改密码页面
     */
    public void goToChangePassword() {
        changePasswordLink.click();
    }

    /**
     * 导航到订单历史页面
     */
    public void goToOrderHistory() {
        orderHistoryLink.click();
    }

    // Getter Methods

    /**
     * 获取成功消息
     * @return 成功消息文本
     */
    public String getSuccessMessage() {
        waitForElementToBeVisible(successMessage);
        return successMessage.getText().trim();
    }

    /**
     * 获取错误消息
     * @return 错误消息文本
     */
    public String getErrorMessage() {
        waitForElementToBeVisible(errorMessage);
        return errorMessage.getText().trim();
    }

    /**
     * 获取当前显示的名字
     * @return 名字
     */
    public String getFirstName() {
        return currentFirstName.getText().trim();
    }

    /**
     * 获取当前显示的姓氏
     * @return 姓氏
     */
    public String getLastName() {
        return currentLastName.getText().trim();
    }

    /**
     * 获取邮箱地址
     * @return 邮箱
     */
    public String getEmail() {
        return emailInput.getAttribute("value");
    }

    /**
     * 获取电话号码
     * @return 电话
     */
    public String getPhone() {
        return phoneInput.getAttribute("value");
    }

    /**
     * 获取地址
     * @return 地址
     */
    public String getAddress() {
        return addressTextarea.getText().trim();
    }

    /**
     * 获取城市
     * @return 城市
     */
    public String getCity() {
        return cityInput.getAttribute("value");
    }

    /**
     * 获取邮政编码
     * @return 邮编
     */
    public String getZipCode() {
        return zipCodeInput.getAttribute("value");
    }

    /**
     * 获取页面标题
     * @return 页面标题
     */
    public String getPageHeader() {
        return profileHeader.getText().trim();
    }

    /**
     * 检查是否在资料页面
     * @return boolean
     */
    public boolean isProfilePageLoaded() {
        return getCurrentUrl().contains(PROFILE_URL) &&
                profileHeader.isDisplayed();
    }

    /**
     * 检查成功消息是否显示
     * @return boolean
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查错误消息是否显示
     * @return boolean
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查头像是否显示
     * @return boolean
     */
    public boolean isAvatarDisplayed() {
        try {
            return avatarImage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 等待成功消息显示
     */
    private void waitForSuccessMessage() {
        wait.until(ExpectedConditions.visibilityOf(successMessage));
    }
}