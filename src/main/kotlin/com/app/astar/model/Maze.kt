package com.app.astar.model

import com.app.astar.controller.GridController
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.util.Duration
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.concurrent.thread


class Maze private constructor() {
    var mazeList: MutableList<MutableList<Signs>> = mutableListOf()
        private set
    var goalPos: Point = Point(-1, -1)
        private set
    var startPos: Point = Point(-1, -1)
        private set
    var colCount: Int = -1
        private set
    var rowCount: Int = -1
        private set
    companion object {
        fun fromFile(pathToFile: String): Maze {
            val maze = Maze()
            try {
                maze.parseFile(File(pathToFile).readLines())
            } catch (e: Exception) {
                throw RuntimeException("Error reading file: $pathToFile, ${e.message}")
            }

            if (!maze.isGoalPosDefined() || !maze.isStartPosDefined()) {
                throw RuntimeException("The maze doesn't have start or exit position")
            }
            return maze
        }

        fun copyOf(other: Maze): Maze {
            return Maze().apply {
                goalPos = other.goalPos
                startPos = other.startPos
                colCount = other.colCount
                rowCount = other.rowCount
                mazeList = other.mazeList.map { it.toMutableList() }.toMutableList()
            }
        }
    }


    private fun parseFile(lines: List<String>) {
        rowCount = lines.size
        lines.forEachIndexed { rowIndex, line ->
            val row = parseLine(rowIndex, line)
            mazeList.add(row)
        }
        colCount = mazeList[0].size
    }


    private fun parseLine(rowIndex: Int, line: String): MutableList<Signs> {
        val row = mutableListOf<Signs>()
        line.forEach { char ->
            row.add(
                when (char) {
                    Signs.UNVISITED.char -> Signs.UNVISITED
                    Signs.WALL.char -> Signs.WALL
                    Signs.START.char -> {
                        startPos = Point(rowIndex, row.size)
                        Signs.START
                    }
                    Signs.EXIT.char -> {
                        goalPos = Point(rowIndex, row.size)
                        Signs.EXIT
                    }
                    ' ' -> return@forEach // skip space
                    else -> throw RuntimeException("Unknown character in the maze: $char")
                }
            )
        }
        return row
    }

    constructor(grid: GridPane) : this() {
        initMazeListFromGrid(grid)

    }



    private fun initMazeListFromGrid(grid: GridPane) {
        colCount = grid.columnCount
        rowCount = grid.rowCount

        mazeList = MutableList(rowCount) { MutableList(colCount) { Signs.UNVISITED } }

        for (node in grid.children) {
            if (node is Pane) {
                val colIndex = GridPane.getColumnIndex(node) ?: 0
                val rowIndex = GridPane.getRowIndex(node) ?: 0

                mazeList[rowIndex][colIndex] = getSignFromColor(node)
            }
        }
    }


    private fun getSignFromColor(node: Pane): Signs {
        val backgroundColor = node.background?.fills?.firstOrNull()?.fill
        return when (backgroundColor) {
            ColorSigns.WALL.color -> Signs.WALL
            ColorSigns.UNVISITED.color -> Signs.UNVISITED
            ColorSigns.EXIT.color -> {
                goalPos = Point(GridPane.getRowIndex(node), GridPane.getColumnIndex(node))
                Signs.EXIT
            }
            ColorSigns.START.color -> {
                startPos = Point(GridPane.getRowIndex(node), GridPane.getColumnIndex(node))
                Signs.START
            }
            ColorSigns.PATH.color -> Signs.PATH
            else -> throw RuntimeException("Unknown color in the maze: $backgroundColor")
        }
    }

    fun isGoalPosDefined() : Boolean {
        return goalPos != Point(-1,-1)
    }

    fun isStartPosDefined() : Boolean {
        return startPos != Point(-1,-1)
    }



    fun outOfRange(index: Point) : Boolean {
        return index.col < 0 || index.col >= rowCount || index.row < 0 || index.row >= colCount
    }

    operator fun get(point: Point): Signs {
        if (outOfRange(point)) {
            throw IndexOutOfBoundsException("Index out of bounds: $point")
        }
        return mazeList[point.col][point.row]
    }

    operator fun set(point: Point, sign: Signs) {
        if (outOfRange(point)) {
            throw IndexOutOfBoundsException("Index out of bounds: $point")
        }
        mazeList[point.col][point.row] = sign
    }

//    fun isWall(point: Point) : Boolean{
//        return this[point] == Signs.WALL
//    }


    fun printBySteps(path: Collection<Point>) {
        val tempMaze = copyOf(this)
        path.forEach { point ->
            if (point != startPos && point != goalPos) {
                tempMaze[point] = Signs.PATH
            }
        }
        tempMaze.print()
    }

    private fun getDelay(): Double = if (colCount + rowCount < 50) 100.0 else 50.0


    fun updateGridByStepsWithProbPos(
        gridController: GridController,
        probPath: Collection<Point>,
        path: Collection<Point>
    ) {
        updateGridBySteps(gridController, probPath.iterator(), ColorSigns.PROBABLE.color, getDelay()) {
            updateGridBySteps(gridController, path.iterator(), ColorSigns.PATH.color, getDelay() / 2)
        }
    }

    private fun updateGridBySteps(
        gridController: GridController,
        pointsIterator: Iterator<Point>,
        color: Color,
        delay: Double,
        onFinish: () -> Unit = {}
    ) {
        fun updateNextPoint() {
            if (pointsIterator.hasNext()) {
                val point = pointsIterator.next()
                Platform.runLater {
                    gridController.grid.children.forEach { node ->
                        if (node is Pane &&
                            GridPane.getColumnIndex(node) == point.row &&
                            GridPane.getRowIndex(node) == point.col &&
                            node != gridController.startPane &&
                            node != gridController.endPane
                        ) {
                            node.background = Background(BackgroundFill(color, null, null))
                        }
                    }
                }
                PauseTransition(Duration.millis(delay)).apply {
                    setOnFinished { updateNextPoint() }
                    play()
                }
            } else {
                onFinish()
            }
        }

        updateNextPoint()
    }

    fun printSolved(path: MutableList<Point>){
        val tempMaze = copyOf(this)
        for (point in path){
            if (point != startPos && point != goalPos){
                tempMaze[point] = Signs.PATH
            }
        }
        tempMaze.print()
    }

    fun print() {
        mazeList.forEach { row ->
            row.forEach { cell ->
                print("$cell ")
            }
            println()
        }
        println()
    }

    fun getSolvedMaze(path: MutableList<Point>): Maze {
        val tempMaze = copyOf(this)
        path.forEach { point ->
            if (point != startPos && point != goalPos) {
                tempMaze[point] = Signs.PATH
            }
        }
        return tempMaze
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Maze) return false

        if(this.colCount != other.colCount){
            println("this $colCount !=  other $colCount")
            return false
        }
        if(this.rowCount != other.rowCount){
            println("this $rowCount != other $rowCount")
            return false
        }
        for (col in 0 until this.colCount){
            for (row in 0 until this.rowCount) {
                if (this.mazeList[col][row] != other.mazeList[col][row]) {
                    println("${this.mazeList[col][row]} != ${other.mazeList[col][row]}")
                    return false
                }

            }
        }

        return true

    }



}

