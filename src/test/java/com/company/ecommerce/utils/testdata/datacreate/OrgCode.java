package com.company.ecommerce.utils.testdata.datacreate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OrgCode {
	final String[] codeNo = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
	final String[] staVal = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
			"16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33",
			"34", "35" };


	@SuppressWarnings("unused")
	public String toString() {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < codeNo.length; i++) {
			map.put(codeNo[i], staVal[i]);
		}

		final int maxNum = codeNo.length;
		StringBuffer orgCode = new StringBuffer();
		Random r = new Random();
		for (int i = 0; i < 8; i++) {
			int rand = Math.abs(r.nextInt(9));
			orgCode.append(codeNo[rand]);
		}

		final int[] wi = { 3, 7, 9, 10, 5, 8, 4, 2 };
		final char[] values = orgCode.toString().toCharArray();
		int parity = 0;
		for (int i = 0; i < values.length; i++) {
			final String val = Character.toString(values[i]);
			parity += wi[i] * Integer.parseInt(map.get(val).toString());
		}
		String check = "";
		switch (11 - parity % 11) {
			case 10:
				check = "X";
				break;
			case 11:
				check = "0";
				break;
			default:
				check = Integer.toString((11 - parity % 11));
		}
		return orgCode.toString() + "-" + check;
	}

	public String toString2() {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < codeNo.length; i++) {
			map.put(codeNo[i], staVal[i]);
		}

		final int maxNum = codeNo.length;
		StringBuffer orgCode = new StringBuffer();
		Random r = new Random();
		for (int i = 0; i < 8; i++) {
			int rand = Math.abs(r.nextInt(9));
			orgCode.append(codeNo[rand]);
		}

		final int[] wi = { 3, 7, 9, 10, 5, 8, 4, 2 };
		final char[] values = orgCode.toString().toCharArray();
		int parity = 0;
		for (int i = 0; i < values.length; i++) {
			final String val = Character.toString(values[i]);
			parity += wi[i] * Integer.parseInt(map.get(val).toString());
		}
		String check = "";
		switch (11 - parity % 11) {
			case 10:
				check = "X";
				break;
			case 11:
				check = "0";
				break;
			default:
				check = Integer.toString((11 - parity % 11));
		}
		return orgCode.toString() + check;
	}

	public static void main(String[] args) {
		System.out.println(new OrgCode());
	}

}
