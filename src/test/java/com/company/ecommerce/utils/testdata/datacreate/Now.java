package com.company.ecommerce.utils.testdata.datacreate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Now {
	static String DATE_FORMAT = "yyyyMMddHHmmssSSS";

	public String toString() {
		SimpleDateFormat time = new SimpleDateFormat(DATE_FORMAT);
		String now = time.format(new Date());
		return now;
	}

	public static void main(String[] args) {
		System.out.println(new Now());
	}

}
