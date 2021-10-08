fun main() {
    val report = readLine()!!
    val regex = Regex("[0-9] wrong answers?")
    println(report.matches(regex))
}