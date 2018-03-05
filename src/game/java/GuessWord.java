package game.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abedormancy@gmail.com on 2013
 */
public class GuessWord {
    private static final String DISPLAY = "_ ";    //遮罩标识
    private static int points = 5;    //分数
    private static List<String> words = new ArrayList<String>();    //单词库

    static {
        //初始化单词库
        words.add("hangman");
        words.add("missing");
        words.add("some");
        words.add("one");
    }

    private static String word = words.get((int) (Math.random() * words.size()));    //随机获得一个单词
    private static String show = initShow();    //初始化单词遮罩
    private static String answer = "";    //已经答过的字母
    private static String info;    //显示用户答题信息
    private static String input;    //用户输入的字母或单词
    private static int wCount; //猜对的字母个数

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (points > 0) {
            print();
            input = br.readLine().trim().toLowerCase();
            info = guess(input);
            System.out.print("\n\n");
        }
        lose();
    }

    public static void print() {
        System.out.println(show);
        if (info != null) System.out.println(info);
        System.out.println("Points: " + points);
        System.out.println("You have guessed the following letters: " + answer);
        System.out.print("Please enter a letter or word: ");
    }

    public static void win() {
        System.out.println("\n\nCongratulations, the word is " + word + "!");
        System.out.println("You win!");
        System.exit(0);
    }

    public static void lose() {
        System.out.println("You lose!");
        System.exit(0);
    }

    public static String guess(String low) {
        String res = "啦啦啦~人家是字符检测咩~";
        for (int i = 0; i < low.length(); i++) {
            if (!(low.charAt(i) >= 'a' && low.charAt(i) <= 'z')) low = "";
        }
        if (low.isEmpty()) {
            res = "...Cε(┬_┬)3...,请不要输入一些奇怪的字符，人家看不懂咩~";
        } else if (low.length() == 1) {
            //如果这个字母还没输入过
            if (answer.indexOf(low) == -1) {
                //如果这个字母还没输入过就添加进回答过的字母
                answer += low + " ";
                int index = -1;
                int count = 0;
                //循环查找该单词有几个字母匹配
                while ((index = word.indexOf(low, index + 1)) != -1) {
                    //将匹配到的字母替换遮罩
                    show = replaceLetter(index, low);
                    count++;
                }
                if (count == 0) {
                    //一个字母都不匹配
                    res = "Sorry, there is not letter " + low;
                    points--;
                } else {
                    //count个字母匹配
                    res = "There are " + count + " letter " + low;
                    //如果猜对的字母总数和单词的length()一样，则证明已经赢了
                    if ((wCount += count) == word.length()) win();
                }
            } else {
                //这个字母已经输入过了就提示用户已经输入过该字母
                res = "A letter must be different!";
            }

        } else if (low.length() > 1) {
            //input a word
            if (word.equals(low)) {
                win();
            } else {
                res = "Sorry, the word is not " + low;
                points--;
            }
        }
        return res;
    }

    public static String initShow() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < word.length(); i++) sb.append(DISPLAY);
        return sb.toString();
    }

    public static String replaceLetter(int index, String str) {
        StringBuffer sb = new StringBuffer(show);
        for (int i = 0; i < word.length(); i++) {
            if (i == index) {
                sb.replace(index * DISPLAY.length(), index * DISPLAY.length() + 1, str);
            }
        }
        return sb.toString();
    }
}
