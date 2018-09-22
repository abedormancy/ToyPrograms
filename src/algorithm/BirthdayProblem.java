package algorithm;

import java.util.concurrent.ThreadLocalRandom;

/**
 * birthday problem or birthday paradox
 * 生日问题是指，如果一个房间里的人数 >=23 那么至少有两个人生日相同的概率 >50%，如果人数 >=60 那么此概率 >99%
 * 这个从数学事实与一般直觉相抵触的意义上，也可以称作生日悖论。因为大多数人会认为23个人中有2个人生日相同的概率要远远小于50%
 * Created by Abe on 1/10/2017.
 */
public class BirthdayProblem {

    /**
     * 通过计算至少2人生日相同概率
     * @param personCount 人数
     * @return 当前人数至少2人相同生日概率
     */
    private static double birthdayParadox(int personCount) {
        int days = 365;
        double prob = 1;
        for (int i = 0; i < personCount; i++) {
            prob *= (days - i) / 1f / days;
        }
        return 1 - prob;
    }

    /**
     * 通过随机模拟验证此概率
     * @param personCount 人数
     * @param opCount 计算次数
     * @return 当前人数至少2人生日相同概率
     */
    private static double birthdayParadox(int personCount, int opCount) {
        int days = 365;
        int sameCount = 0;
        // 通过多次计算模拟出概率，计算次数越多越精确
        for (int i = 0; i < opCount; i++) {
            boolean[] placeholder = new boolean[days];
            for (int x = 0; x < personCount; x++) {
                int val = ThreadLocalRandom.current().nextInt(0, days);
                if (placeholder[val]) {
                    sameCount += 1;
                    break;
                }
                placeholder[val] = true;
            }
        }
        return sameCount / 1f / opCount;
    }

    public static void main(String[] args) {
        double prob = birthdayParadox(23);
        System.out.println(prob);
        prob = birthdayParadox(23, 10_000_000);
        System.out.println(prob);
    }
}
