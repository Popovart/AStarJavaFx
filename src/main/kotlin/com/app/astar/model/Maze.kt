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
        fun fromFile(pathToFile: String) : Maze {
            val maze = Maze() // using the private constructor
            try {
                val lines = File(pathToFile).readLines()
                maze.rowCount = lines.size
                lines.forEachIndexed { rowIndex, line ->
                    val row = mutableListOf<Signs>()
                    line.forEach { char ->
                        when (char) {
                            Signs.UNVISITED.char -> row.add(Signs.UNVISITED)
                            Signs.WALL.char -> row.add(Signs.WALL)
                            Signs.START.char -> {
                                row.add(Signs.START)
                                maze.startPos = Point(rowIndex, row.lastIndex)
                            }
                            Signs.EXIT.char -> {
                                row.add(Signs.EXIT)
                                maze.goalPos = Point(rowIndex, row.lastIndex)
                            }
                            ' ' -> {} // skip space
                            else -> throw RuntimeException("Unknown character in the maze: $char")
                        }
                    }
                    maze.mazeList.add(row)
                }
                maze.colCount = maze.mazeList[0].size
            } catch (e: Exception) {
                throw RuntimeException("Error reading file: $pathToFile, ${e.message}")
            }

            if (maze.startPos.col == -1 || maze.goalPos.col == -1){
                throw RuntimeException("The maze doesn't have start or exit position")
            }
            return maze
        }

        fun copyOf(other: Maze): Maze {
            val newMaze = Maze()
            newMaze.goalPos = other.goalPos
            newMaze.startPos = other.startPos
            newMaze.colCount = other.colCount
            newMaze.rowCount = other.rowCount
            newMaze.mazeList = other.mazeList.map { it.toMutableList() }.toMutableList()
            return newMaze
        }
    }

    constructor(grid: GridPane) : this() {
        initMazeListFromGrid(grid)

    }


    private fun initMazeListFromGrid(grid: GridPane) {
        colCount = grid.columnCount
        rowCount = grid.rowCount

        mazeList = MutableList(rowCount) { MutableList(colCount) { Signs.UNVISITED } } // Инициализация матрицы значением по умолчанию
        for (node in grid.children) {
            if (node is Pane) {
                val colIndex = GridPane.getColumnIndex(node) ?: 0
                val rowIndex = GridPane.getRowIndex(node) ?: 0

                // Проверяем цвет фона Pane и обновляем matrix
                val backgroundColor = node.background?.fills?.firstOrNull()?.fill
                val sign = when (backgroundColor) {
                    ColorSigns.WALL.color  -> Signs.WALL
                    ColorSigns.UNVISITED.color -> Signs.UNVISITED
                    ColorSigns.EXIT.color -> {
                        goalPos = Point(rowIndex, colIndex)
                        Signs.EXIT
                    }
                    ColorSigns.START.color -> {
                        startPos = Point(rowIndex, colIndex)
                        Signs.START
                    }
                    ColorSigns.PATH.color -> Signs.PATH
                    else -> throw RuntimeException("Unknown color in the maze: $backgroundColor")
                }
                mazeList[rowIndex][colIndex] = sign
            }
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

    fun isWall(point: Point) : Boolean{
        return this[point] == Signs.WALL
    }

    fun printBySteps(path: MutableList<Point>){
        val tempMaze = copyOf(this)
        for (point in path){
            if (point != startPos && point != goalPos){
                tempMaze[point] = Signs.PATH
            }
            tempMaze.print()
        }
    }

    fun printBySteps(path: LinkedHashSet<Point>) {
        val tempMaze = copyOf(this)
        for (point in path){
            if (point != startPos && point != goalPos){
                tempMaze[point] = Signs.PATH
            }
            tempMaze.print()
        }
    }

    ///TODO I must to make those 2 functions easier and kinda change their names
    fun updateGridByStepsWithProbPos(gridController: GridController, probPath: Collection<Point>, path: Collection<Point>) {
        updateGridBySteps(gridController, probPath.iterator(), ColorSigns.PROBABLE.color, 100.0) {
            // После завершения первой анимации начинаем вторую
            updateGridBySteps(gridController, path.iterator(), ColorSigns.PATH.color, 50.0)
        }
    }

    private fun updateGridBySteps(gridController: GridController, pointsIterator: Iterator<Point>, color: Color, delay: Double, onFinish: () -> Unit = {}) {
        // Функция для обновления следующей точки
        fun updateNextPoint() {
            if (pointsIterator.hasNext()) {
                val point = pointsIterator.next()
                Platform.runLater {
                    gridController.grid.children.forEach { node ->
                        if (node is Pane && GridPane.getColumnIndex(node) == point.row && GridPane.getRowIndex(node) == point.col && node != gridController.startPane && node != gridController.endPane) {
                            node.background = Background(BackgroundFill(color, null, null))
                        }
                    }
                }

                // Задаем задержку перед следующим обновлением
                PauseTransition(Duration.millis(delay)).apply {
                    setOnFinished { updateNextPoint() } // Продолжаем с следующей точки после задержки
                    play()
                }
            } else {
                onFinish() // Вызываем onFinish, если все точки обработаны
            }
        }

        updateNextPoint() // Начинаем с первой точки
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
        for (row in mazeList){
            for (cell in row){
                print("$cell ")
            }
            println();
        }
        println()
    }

    fun getSolvedMaze(path: MutableList<Point>): Maze {
        val tempMaze = copyOf(this)
        for (point in path){
            if (point != startPos && point != goalPos){
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

