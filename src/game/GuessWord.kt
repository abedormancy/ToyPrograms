import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import GuestWord.State.*


/**
 * Created by abedormancy@gmail.com on 2018/3/4.
 */
class GuestWord(val args: Array<String>) {

    // 单词库
    val words: List<String> by lazy {
        when (args.size) {
            1 -> { // 读取指定路径文件获取单词
                val str = Files.readAllLines(Paths.get(args[0])).joinToString(separator = " ")
                str.split("""[ ,;]""".toRegex()).filter { it.trim().length > 0 }.map { it.toLowerCase() }
            }
            in 2..Int.MAX_VALUE -> { // 将多个参数转换成单词库
                args.toList().map { it.toLowerCase() }
            }
            else -> throw IllegalArgumentException("no words!")
        }
    }
    // 遮罩标识
    val tag = "_ "
    private var tagLetter = ""
    // 分数
    private var point = 5
    // 当前要猜的单词索引
    private var currentWordIndex = -1
    // 已经猜过的字母
    private val guessedLetters = ArrayList<String>()
    // info
    private var info = ""

    fun play() {
        reset()

        while (true) {
            println("\n\n\n$tagLetter")
            print(info)
            println("Points: ${point}")
            println("You have guessed the following letters: ${guessedLetters.joinToString(separator = ", ")}")
            val scanner = Scanner(System.`in`)
            var answer = ""
            var state = LOSE

            if (point > 0) {
                print("Please enter a letter or word: ")
                answer = scanner.nextLine().trim().toLowerCase()
                if (answer.length > 0) state = verify(answer)
            }

            when (state) {
                WIN, LOSE -> {
                    println("You $state! the word is ${words[currentWordIndex]}")
                    print("Would you like to play again? (y/n) : ")
                    if (scanner.nextLine().trim().toLowerCase() == "y") reset() else return
                }
                REPEATED -> info = "You've already guessed the letter $answer\n"
                CONTINUE -> {
                    if (answer.length > 1) {
                        point--
                        info = "the word is not $answer !\n"
                    } else {
                        val count = words[currentWordIndex].filter { it == answer[0] }.length
                        if (count > 0) {
                            info = "$count letter${if (count > 1) "s" else ""} found !\n"
                        } else {
                            point--
                            info = "not found $answer !\n"
                        }

                    }
                }
                ERROR -> {
                    info = "just accept a letter or word !\n"
                }
            }
        }

    }

    /**
     * reset game
     */
    private fun reset() {
        point = 5
        currentWordIndex = ThreadLocalRandom.current().nextInt(0, words.size)
        tagLetter = words[currentWordIndex].map { "$tag" }.joinToString("")
        guessedLetters.clear()
        info = ""
    }


    fun verify(letterOrWord: String) = when {
        letterOrWord.length == 1 -> {
            when {
                letterOrWord[0] !in 'a'..'z' -> ERROR
                guessedLetters.contains(letterOrWord) -> REPEATED
                else -> {
                    guessedLetters.add(letterOrWord)
                    var index = 0
                    do {
                        index = words[currentWordIndex].indexOf(letterOrWord, index)
                        replaceTag2Letter(index)
                    } while (++index > 0)
                    if (tagLetter.indexOf(tag) < 0) WIN else CONTINUE
                }
            }
        }
        else -> if (letterOrWord == words[currentWordIndex]) WIN else CONTINUE
    }

    private fun replaceTag2Letter(index: Int) {
        if (index < 0) return

        val sb = StringBuffer(tagLetter)
        val len = index * tag.length
        tagLetter = sb.replace(len, len + 1, words[currentWordIndex][index].toString()).toString()

    }

    enum class State {
        CONTINUE, WIN, LOSE, REPEATED, ERROR
    }

}

fun main(args: Array<String>) {
    val game = GuestWord(args)
    game.play()
}