package chess

const val DEFAULT_ROWS = 8      // 1-9
const val DEFAULT_COLUMNS = 8   // 1-9
const val DEBUG = false

fun main() {
    println("Pawns-Only Chess")
    println("First Player's name:")
    val firstPlayer = Player(readLine()!!, Color.W)
    println("Second Player's name:")
    val secondPlayer = Player(readLine()!!, Color.B)
    val board = Board()
    println(board)
    println()
    var currentPlayer = firstPlayer
    println("$currentPlayer's turn:")
    var numberOfMoves = 0

    var move = readLine()!!.lowercase()
    while (move != "exit") {
        if (board.makeMove(currentPlayer, move)) {
            println(board)
            currentPlayer = if ((++numberOfMoves % 2) == 1) {
                secondPlayer
            } else {
                firstPlayer
            }
        }
        // Check win
        if (board.isWin) break
        println("$currentPlayer's turn:")
        move = readLine()!!.lowercase()
    }
    print("Bye!")
}

enum class Color(var longName: String, var title: String, var symbol: String) {
    B("black", "Black", "♟"), W("white","White","♙")
}

class Player(private var name: String, var pawnColor: Color) {
    val firstRow = if (pawnColor == Color.W) 1 else 6
    override fun toString(): String {
        return name
    }

    val moves = mutableListOf<String>()
}

open class Pawn(_color: Color) {
    val color = _color
    override fun toString(): String {
        return if (DEBUG) color.symbol
        else color.name
    }
}


class Cell(_pawn: Pawn? = null) {
    var pawn = _pawn
    fun putPawn(pawn: Pawn?) {
        this.pawn = pawn
    }

    fun removePawn() {
        this.pawn = null
    }

    override fun toString(): String = if (pawn == null) " " else pawn.toString()
}

class Row(private val number: Int, len: Int = 8) {
    private val cells = Array(len) { Cell(null) }

    fun fillPawns(color: Color) {
        for (cell in cells) {
            cell.putPawn(Pawn(color))
        }
    }

    override fun toString(): String = buildString {
        append("$number ")
        for (cell in cells) {
            append("|")
            if (cell.pawn == null) append("   ") else append(" $cell ")
        }
        append("|\n")
    }

    fun getPawn(column: Int): Pawn? = cells[column].pawn

    fun putPawn(column: Int, pawn: Pawn?) {
        cells[column].putPawn(pawn)
    }

    fun removePawn(column: Int): Pawn? {
        val localPawn = cells[column].pawn
        cells[column].removePawn()
        return localPawn
    }
}

/**
 * Board for game.
 */
class Board(rows: Int = DEFAULT_ROWS, private val columns: Int = DEFAULT_COLUMNS) {
    private val board = Array(rows) { i -> Row(i + 1, columns) }
    private val lineSeparator = "  +${"---+".repeat(columns)}\n"
    private val columnsName = CharArray(columns) { i -> 'a' + i }
    private val lineBottom = columnsName.joinToString(prefix = "    ", separator = "   ")
    private val ruleForMove = Regex("[${columnsName.first()}-${columnsName.last()}][1-$rows]".repeat(2))
    
    var isWin = false

    init {
        board[1].fillPawns(Color.W)
        board[6].fillPawns(Color.B)
    }

    private fun getPawn(row: Int, column: Int): Pawn? = if (column in 0 until columns)
        board[row].getPawn(column) else null

    private fun getPawnNearby(row: Int, column: Int, color: Color): TempPawn? {
        var pawn = getPawn(row, column - 1)
        if (pawn == null) {
            pawn = getPawn(row, column + 1)
        }
        if (pawn != null && pawn.color != color) {
            return TempPawn(color, row + if (color == Color.W) -1 else 1, column)
        }
        return null
    }

    private fun getPawnOpponent(row: Int, column: Int, color: Color): Pawn? {
        val pawn = getPawn(row, column)
        return if (pawn?.color == color) null else pawn
    }

    class TempPawn(color: Color, val row: Int, val column: Int) : Pawn(color)

    private var tempPawn: TempPawn? = null

    fun makeMove(player: Player, move: String): Boolean {
        if (move.matches(ruleForMove)) {
            val cellFrom = move.substring(0, 2)
            val cellTo = move.substring(2)
            val columnFrom = columnsName.indexOf(move.first())
            val rowFrom = move[1].digitToInt() - 1
            val columnTo = columnsName.indexOf(move[2])
            val rowTo = move.last().digitToInt() - 1
            val step = (rowFrom - rowTo) * if (player.pawnColor == Color.W) -1 else 1

            // we check that there is a pawn of the appropriate color in the initial cell
            if (getPawn(rowFrom, columnFrom)?.color != player.pawnColor) {
                println("No ${player.pawnColor.longName} pawn at $cellFrom")
                return false
            }
            if (step <= 0 || step > if (rowFrom == player.firstRow) 2 else 1) {
                println("Invalid Input")
                return false
            }
            if (columnFrom == columnTo) {
                if (getPawn(rowTo, columnTo) != null) {
                    println("Invalid Input")
                    return false
                }
                if (step == 2 && getPawn(rowFrom + if (player.pawnColor == Color.W) 1 else -1, columnFrom) != null) {
                    println("Invalid Input")
                    return false
                }
            } else if (columnTo - columnFrom !in -1..1) {
                println("Invalid Input")
                return false
            } else {
                if (getPawnOpponent(rowTo, columnTo, player.pawnColor) == null) {
                    if (tempPawn?.row == rowTo && tempPawn?.column == columnTo) {
                        // удалить пешку, связанную с tempPawn
                        val pawnRow = tempPawn!!.row + if (player.pawnColor == Color.W) -1 else +1
                        board[pawnRow].removePawn(columnTo)
                    } else {
                        println("Invalid Input")
                        return false
                    }
                }
            }
            tempPawn = if (step == 2) {
                getPawnNearby(rowTo, columnTo, player.pawnColor)
            } else {
                null
            }
            val pawn = board[rowFrom].removePawn(columnFrom)
            board[rowTo].putPawn(columnTo, pawn)
            player.moves.add("$cellFrom-$cellTo")
            if (rowTo == if(player.pawnColor == Color.W) 0 else board.lastIndex){
                println("${player.pawnColor.title} Wins!")
                isWin = true
            }
            // Check if not pawn for opponent
            // Check for Stalemate
            return true
        } else {
            println("Invalid Input")
            return false
        }
    }

    override fun toString(): String = buildString {
        for (i in board.lastIndex downTo 0) {
            append(lineSeparator)
            append(board[i].toString())
        }
        append(lineSeparator)
        append(lineBottom)
    }
}