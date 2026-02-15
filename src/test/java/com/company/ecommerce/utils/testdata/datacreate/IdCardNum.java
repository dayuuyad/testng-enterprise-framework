package com.company.ecommerce.utils.testdata.datacreate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class IdCardNum {
	// 18位身份证号码各位的含义:
	// 1-2位省、自治区、直辖市代码；
	// 3-4位地级市、盟、自治州代码；
	// 5-6位县、县级市、区代码；
	// 7-14位出生年月日，比如19670401代表1967年4月1日；
	// 15-17位为顺序号，其中17位（倒数第二位）男为单数，女为双数；
	// 18位为校验码，0-9和X。
	// 作为尾号的校验码，是由把前十七位数字带入统一的公式计算出来的，
	// 计算的结果是0-10，如果某人的尾号是0－9，都不会出现X，但如果尾号是10，那么就得用X来代替，
	// 因为如果用10做尾号，那么此人的身份证就变成了19位。X是罗马数字的10，用X来代替10
	public static void main(String[] args) {
		IdCardNum randIdCard = new IdCardNum();
		System.out.println(randIdCard);
	}

	/**
	 * 生成随即密码
	 * 
	 * @param pwd_len
	 *            生成的密码的总长度
	 * @return 密码的字符串
	 */
	String genRandomNum(int len) {
		// 35是因为数组是从0开始的，26个字母+10个数字
		final int maxNum = 10;
		int i; // 生成的随机数
		int count = 0; // 生成的密码的长度

		char[] str = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while (count < len) {
			// 生成随机数，取绝对值，防止生成负数，

			i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为36-1

			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count++;
			}
		}

		return pwd.toString();
	}

	/**
	 * 获取随机生成的身份证号码
	 * 
	 * @author mingzijian
	 * @return
	 */
	public String toString() {
		// 随机生成省、自治区、直辖市代码 1-2
		String provinces[] = { "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36", "37",
				"41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63", "64", "65", "71",
				"81", "82" };
		String province = randomOne(provinces);
		// 随机生成地级市、盟、自治州代码 3-4
		String city = randomCityCode(18);
		// 随机生成县、县级市、区代码 5-6
		String county = randomCityCode(28);
		// 随机生成出生年月 7-14
		String birth = randomBirth(20, 50);
		// 随机生成顺序号 15-17(随机性别)
		String no = genRandomNum(3);
		// 随机生成校验码 18
		String id = province + city + county + birth + no;
		id += idCardVerifyCode(id);
		// 拼接身份证号码
		
		return id;
	}

	 public String idCardVerifyCode(String IDStr) {
	        String[] ValCodeArr = {"1","0", "x", "9", "8", "7", "6", "5", "4",
	            "3", "2" };
	        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
	            "9", "10", "5", "8", "4", "2" };
	        String Ai = IDStr;

	        // ================ 判断最后一位的值 ================
	        int TotalmulAiWi = 0;
	        for (int i = 0; i < 17; i++) {
	            TotalmulAiWi = TotalmulAiWi + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
	        }
	        int modValue = TotalmulAiWi % 11;
	        String strVerifyCode = ValCodeArr[modValue];
	        return strVerifyCode;
	    }
	
	/**
	 * 从String[] 数组中随机取出其中一个String字符串
	 * 
	 * @author mingzijian
	 * @param s
	 * @return
	 */
	String randomOne(String s[]) {
		return s[new Random().nextInt(s.length - 1)];
	}

	/**
	 * 随机生成两位数的字符串（01-max）,不足两位的前面补0
	 * 
	 * @author mingzijian
	 * @param max
	 * @return
	 */
	String randomCityCode(int max) {
		int i = new Random().nextInt(max) + 1;
		return i > 9 ? i + "" : "0" + i;
	}

	/**
	 * 随机生成minAge到maxAge年龄段的人的生日日期
	 * 
	 * @author mingzijian
	 * @param minAge
	 * @param maxAge
	 * @return
	 */
	String randomBirth(int minAge, int maxAge) {
		SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());// 设置当前日期
		// 随机设置日期为前maxAge年到前minAge年的任意一天
		int randomDay = 365 * minAge + new Random().nextInt(365 * (maxAge - minAge));
		date.set(Calendar.DATE, date.get(Calendar.DATE) - randomDay);
		return dft.format(date.getTime());
	}
}
