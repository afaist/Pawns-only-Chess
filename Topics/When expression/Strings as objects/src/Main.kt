fun main() {
    val input = readLine()!!
    // write code here
    if (input.isNotEmpty()) {
        println(

            when (input[0]) {
                'i' -> input.drop(1).toInt() + 1
                's' -> input.drop(1).reversed()
                else -> input
            }
        )
    }
}