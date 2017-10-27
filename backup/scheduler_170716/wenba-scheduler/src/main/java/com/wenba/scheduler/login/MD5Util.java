package com.wenba.scheduler.login;

import java.security.MessageDigest;

/**
 * @author zhangbo
 *
 */
public class MD5Util {

    // constants
    private static final int NUMBER_4 = 4;
    private static final int NUMBER_0XF = 0xf;

    public static final String md5(String s) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> NUMBER_4 & NUMBER_0XF];
                str[k++] = hexDigits[byte0 & NUMBER_0XF];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // System.out.println(MD5Util.md5("administrator"));
        // System.out.println(MD5Util.md5("Wenba01"));
        // for (int i = 0;; i++) {
        // System.out.println("i = " + i);
        // new Thread(new HoldThread()).start();
        // }
    }

}

/**
 * @author zhangbo
 *
 */
class HoldThread extends Thread {
    // CountDownLatch cdl = new CountDownLatch(1);
    //
    // public HoldThread() {
    // this.setDaemon(true);
    // }
    //
    // public void run() {
    // try {
    // cdl.await();
    // } catch (InterruptedException e) {
    // }
    // }
}
