package com.company.ecommerce.utils.testdata.datacreate;

public class ChineseOrgName {
	public String toString() {
		String personalName = new ChineseName().toString();
		return "北京" + personalName + "科技股份有限公司";
	}

	public static void main(String[] args) {
		System.out.println(new ChineseOrgName());
	}
}
