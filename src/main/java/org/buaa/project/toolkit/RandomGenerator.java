package org.buaa.project.toolkit;

/**
 * 随机生成器
 */
public class RandomGenerator {

    /**
     * 生成6位数字验证码
     * @return 6位数字验证码
     */
    public static String generateSixDigitCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

}
