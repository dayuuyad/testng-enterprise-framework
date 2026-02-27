package com.company.ecommerce.ui.pages;

import cn.hutool.core.map.MapUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContractListPage extends BasePage {

    // 合同状态
    @FindBy(className = "query-right")
    private WebElement contractStatus;
    // 展开按钮
    @FindBy(xpath = "(//div[@class=\"el-row\"])[1]")
    private WebElement expandButton;

    @FindBy(xpath = "//input[@placeholder=\"合同名称\"]")
    private WebElement contractName;

    @FindBy(xpath = "//input[@placeholder=\"请输入发起人\"]")
    private WebElement contractCreate;

    @FindBy(xpath = "//input[@placeholder=\"请输入签署人手机号\"]")
    private WebElement signeePhoneNumber;

    @FindBy(xpath = "//input[@placeholder=\"请输入合同编号\"]")
    private WebElement contractNum;
    // 搜索按钮
    @FindBy(xpath = "//button/span[text()='查询']/..")
    private WebElement searchButton;

    // 重置按钮
    @FindBy(xpath = "//button/span[text()='重置']/..")
    private WebElement resetButton;

    // 表格体
    @FindBy(xpath = "(//tbody)[1]")
    private WebElement tableBody;

    // 表格行
    @FindBy(xpath = "(//tbody)[1]/tr")
    private List<WebElement> tableRows;

    // 分页信息
    @FindBy(className = "pagination-info")
    private WebElement paginationInfo;

    // 无数据提示
    @FindBy(className = "no-data")
    private WebElement noDataMessage;

    private Map<String, WebElement> webElementMaps;


    // 构造器
    public ContractListPage(WebDriver driver) {
        super(driver);
        webElementMaps = MapUtil.<String, WebElement>builder()
                .put("合同名称",contractName)
                .put("发起人",contractCreate)
                .put("签署人手机号",signeePhoneNumber)
                .put("合同编号",contractNum)
                .build();
    }



    public void searchByMap(Map<String, String> map) {
        waitToClick(expandButton);
        //expandButton.click();
        map.forEach((key, value) -> {
            System.out.println("key: " + key + ", value: " + value);
            if (key.equals("合同状态")) {
                driver.findElement(
                        By.xpath("//div[contains(text(),'"+value+"')]")
                ).click();
            }
            type(webElementMaps.get(key),value,key);
        });
        searchButton.click();
    }



    // 页面操作方法
    public void navigateTo() {
        navigateTo("#/subview/contractweb/contractManage/contractList");
//        waitForPageLoad();
    }


    // 定义查询字段枚举
    public enum SearchField {
        USERNAME("用户名", "username"),
        EMAIL("邮箱", "email"),
        PHONE("手机号", "phone"),
        DEPARTMENT("部门", "department"),
        STATUS("状态", "status"),
        CREATE_TIME("创建时间", "createTime");

        private final String displayName;
        private final String fieldName;

        SearchField(String displayName, String fieldName) {
            this.displayName = displayName;
            this.fieldName = fieldName;
        }

        public String getDisplayName() { return displayName; }
        public String getFieldName() { return fieldName; }
    }



    /**
     * 输入搜索关键词
     */
    public void enterSearchKeyword(String keyword) {
        contractStatus.clear();
        contractStatus.sendKeys(keyword);
    }

    /**
     * 点击搜索按钮
     */
    public void clickSearch() {
        searchButton.click();
        waitForTableLoad();
    }

    /**
     * 执行搜索操作
     */
    public void search(String keyword) {
        enterSearchKeyword(keyword);
        clickSearch();
    }

    /**
     * 重置搜索
     */
    public void resetSearch() {
        resetButton.click();
        waitForTableLoad();
    }

    /**
     * 等待表格加载完成
     */
    private void waitForTableLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tableBody")));
    }

    /**
     * 获取搜索结果数量
     */
    public int getSearchResultCount() {
        wait.until(ExpectedConditions.visibilityOfAllElements(tableRows));
        return tableRows.size();
    }

    /**
     * 获取所有用户名
     */
    public List<String> getAllUserNames() {
        List<String> names = new ArrayList<>();
        List<WebElement> nameCells = driver.findElements(By.xpath("//tbody/tr/td[2]"));
        for (WebElement cell : nameCells) {
            names.add(cell.getText());
        }
        return names;
    }

    /**
     * 获取所有邮箱
     */
    public List<String> getAllEmails() {
        List<String> emails = new ArrayList<>();
        List<WebElement> emailCells = driver.findElements(By.xpath("//tbody/tr/td[3]"));
        for (WebElement cell : emailCells) {
            emails.add(cell.getText());
        }
        return emails;
    }

    /**
     * 检查是否显示无数据提示
     */
    public boolean isNoDataDisplayed() {
        return noDataMessage.isDisplayed();
    }

    /**
     * 获取分页信息
     */
    public String getPaginationInfo() {
        return paginationInfo.getText();
    }

    /**
     * 获取表格列值
     */
    public List<String> getColumnValues(int columnIndex) {
        List<String> values = new ArrayList<>();
        String xpath = String.format("//tbody/tr/td[%d]", columnIndex);
        List<WebElement> cells = driver.findElements(By.xpath(xpath));
        for (WebElement cell : cells) {
            values.add(cell.getText());
        }
        return values;
    }
}
