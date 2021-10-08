fun main() {
    val (n1, operator, n2) = readLine()!!.split(" ")
    val a = n1.toLong()
    val b = n2.toLong()
    println(
        when (operator) {
            "+" -> a + b
            "-" -> a - b
            "/" -> {
                if (0L != b) {
                    a / b
                } else {
                    "Division by 0!"
                }
            }
            "*" -> a * b
            else -> "Unknown operator"
        }
    )
}