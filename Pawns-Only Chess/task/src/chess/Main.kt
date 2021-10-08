package chess

const val DEFAULT_ROWS = 8      // 5-9
const val DEFAULT_COLUMNS = 8   // 5-9
const val DEBUG = false

fun main() {
    println("Pawns-Only Chess")
    val board = Board(8, 8)
    board.play()
    print("Bye!")
}

/**
 * Board for game.
 */
class Board(private val rows: Int = DEFAULT_ROWS, private val columns: Int = DEFAULT_COLUMNS) {
    private val board = Array(rows) { i -> Row(i + 1, columns) }
    private val lineSeparator = "  +${"---+".repeat(columns)}\n"
    private val columnsName = CharArray(columns) { i -> 'a' + i }
    private val lineBottom = columnsName.joinToString(prefix = "    ", separator = "   ")
    private val ruleForMove = Regex("[${columnsName.first()}-${columnsName.last()}][1-$rows]".repeat(2))
    private val firstPlayer: Player
    private val secondPlayer: Player
    private var tempPawn: TempPawn? = null
    private var moveCount = 0

    init {
        println("First Player's name:")
        var name = readLine()!!
        var pawns = Array(columns) { i -> Pawn(Color.W, 1, i) }
        firstPlayer = Player(name, Color.W, pawns, firstRow = 1, lastRow = rows - 1)
        println("Second Player's name:")
        name = readLine()!!
        pawns = Array(columns) { i -> Pawn(Color.B, rows - 2, i) }
        secondPlayer = Player(name, Color.B, pawns, firstRow = rows - 2, lastRow = 0)
        fillBoard()
    }

    //============================================================
    // Functions
    //============================================================
    /**
     * Fill board with pawns
     */
    private fun fillBoard() {
        clearBoard()
        // расставляем фигуры первого игрока
        for (pawn in firstPlayer.pawns) {
            board[pawn.row].putPawn(pawn)
        }
        for (pawn in secondPlayer.pawns) {
            board[pawn.row].putPawn(pawn)
        }
    }

    /**
     * get pawn from row and column
     */
    private fun getPawn(row: Int, column: Int): Pawn? {
        var pawn = firstPlayer.getPawn(row, column)
        if (pawn == null) {
            pawn = secondPlayer.getPawn(row, column)
        }
        return pawn
    }

    /**
     * Clear board
     */
    private fun clearBoard() {
        for (row in board) {
            row.clearRow()
        }
    }

    /**
     * Fill class Move from string
     */
    private fun getMove(move: String): Move? = if (move.matches(ruleForMove)) {
        val cellFrom = move.substring(0, 2)
        val cellTo = move.substring(2)
        val columnFrom = columnsName.indexOf(move.first())
        val rowFrom = move[1].digitToInt() - 1
        val columnTo = columnsName.indexOf(move[2])
        val rowTo = move.last().digitToInt() - 1
        if (rowTo !in 0..rows || rowFrom !in 0..rows || columnFrom !in 0..columns || columnTo !in 0..columns) {
            null
        } else {
            Move(rowFrom, columnFrom, rowTo, columnTo, cellFrom, cellTo)
        }
    } else {
        null
    }

    fun play() {
        var currentPlayer = firstPlayer
        var opponent = secondPlayer
        var input: String
        println(this)
        do {
            println("$currentPlayer's turn:")
            input = readLine()!!.lowercase()
            if (input == "exit") break

            val move = getMove(input)
            if (move == null) {
                println("Invalid Input")
                continue
            }
            val pawnFrom = currentPlayer.getPawn(move.rowFrom, move.columnFrom)
            if (pawnFrom == null) {
                println("No ${currentPlayer.pawnColor.longName} pawn at ${move.cellFrom}")
                continue
            }
            val step = (move.rowFrom - move.rowTo) * if (currentPlayer.pawnColor == Color.W) -1 else 1
            if (step !in 1..2) {
                // Bad step
                println("Invalid Input")
                continue
            }
            if (step == 2 && currentPlayer.firstRow != move.rowFrom) {
                // Bad step
                println("Invalid Input")
                continue
            }

            val pawnTo = getPawn(move.rowTo, move.columnTo)
            if (pawnTo?.color == currentPlayer.pawnColor) {
                println("Invalid Input")
                continue
            }

            if (move.columnFrom == move.columnTo) {
                if (pawnTo != null) {
                    println("Invalid Input")
                    continue
                }
                if (step == 2) {
                    if (getPawn((move.rowFrom + move.rowTo) / 2, move.columnTo) != null) {
                        println("Invalid Input")
                        continue
                    }
                }
            } else {
                if (pawnTo == null) {
                    if (tempPawn?.row == move.rowTo && tempPawn?.column == move.columnTo) {
                        opponent.removePawn(tempPawn?.pawn)
                    } else {
                        println("Invalid Input")
                        continue
                    }
                } else {
                    opponent.removePawn(pawnTo)
                }
            }
            tempPawn = null
            if (!pawnFrom.move(move.rowTo, move.columnTo)) {
                println("Invalid Input")
                continue
            } else {
                if (step == 2) {
                    var pawn = opponent.getPawn(move.rowTo, move.columnTo - 1)
                    if (pawn == null) {
                        pawn = opponent.getPawn(move.rowTo, move.columnTo + 1)
                    }
                    tempPawn = if (pawn != null) {
                        TempPawn(currentPlayer.pawnColor,
                            pawnFrom.row + if (pawnFrom.color == Color.W) -1 else 1,
                            pawnFrom.column,
                            pawnFrom)
                    } else null
                }
            }
            currentPlayer.moves.add(move)
            fillBoard()
            println(this)
            moveCount++
            if (opponent.getPawnsCount() == 0 || currentPlayer.lastRow == move.rowTo) {
                println("${currentPlayer.pawnColor.title} Wins!")
                break
            }
            if (moveCount > 16) {
                if (getStalemate(currentPlayer, opponent)) {
                    println("Stalemate!")
                    break
                }
            }
            currentPlayer = opponent.also { opponent = currentPlayer }

        } while (input != "exit")
    }

    /**
     *
     */
    private fun getStalemate(player: Player, opponent: Player): Boolean {
        for (pawn in opponent.pawns) {
            if (!player.getStalemate(pawn))
                return false
        }
        return true
    }

    /**
     *
     */
    override fun toString(): String = buildString {
        for (i in board.lastIndex downTo 0) {
            append(lineSeparator)
            append(board[i].toString())
        }
        append(lineSeparator)
        append(lineBottom)
    }

    /********************************************************************************
     * Classes
     *******************************************************************************/
    /**
     * Class for pawn
     */
    open class Pawn(
        val color: Color,
        var row: Int,
        var column: Int,
    ) {
        fun move(rowTo: Int, columnTo: Int): Boolean {
            if (row == rowTo) {
                return false
            }
            row = rowTo
            column = columnTo
            return true
        }

        override fun toString(): String {
            return if (DEBUG) color.symbol
            else color.name
        }
    }

    /**
     * Temp Pawn
     */
    class TempPawn(color: Color, row: Int, column: Int, val pawn: Pawn) : Pawn(color, row, column)

    /**
     * Class for player
     */
    class Player(
        private var name: String,
        var pawnColor: Color,
        _pawns: Array<Pawn>,
        val firstRow: Int,
        var lastRow: Int,
    ) {
        override fun toString(): String {
            return name
        }

        val pawns = mutableListOf(*_pawns)
        val moves = mutableListOf<Move>()
        fun getPawn(row: Int, column: Int): Pawn? {
            return pawns.firstOrNull { it.row == row && it.column == column }
        }

        fun getStalemate(pawn: Pawn): Boolean {
            val row = pawn.row + if (pawnColor == Color.W) -1 else 1
            val column = pawn.column
            if (pawns.firstOrNull { it.row == row && it.column == column } != null) {
                if (pawns.firstOrNull { it.row == row && it.column == (column - 1) } == null &&
                    pawns.firstOrNull { it.row == row && it.column == (column + 1) } == null) {
                    return true
                }
            }
            return false
        }

        fun removePawn(pawn: Pawn?) {
            if (pawn != null) {
                pawns.removeIf {
                    it.row == pawn.row && it.column == pawn.column
                }
            }
        }

        fun getPawnsCount() = pawns.size
    }

    /**
     * Class for row
     */
    class Row(private val number: Int, len: Int = 8) {
        private val cells = Array(len) { Cell(null) }

        fun clearRow() {
            for (cell in cells) {
                cell.removePawn()
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

        fun putPawn(pawn: Pawn) {
            cells[pawn.column].putPawn(pawn)
        }
    }

    /**
     * Class for cell
     */
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

    /**
     * Class for move
     */
    data class Move(
        val rowFrom: Int,
        val columnFrom: Int,
        val rowTo: Int,
        val columnTo: Int,
        val cellFrom: String,
        val cellTo: String,
    )

    enum class Color(var longName: String, var title: String, var symbol: String) {
        B("black", "Black", "♟"), W("white", "White", "♙")
    }
}