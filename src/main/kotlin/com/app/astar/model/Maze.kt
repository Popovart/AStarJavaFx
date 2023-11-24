package com.app.astar.model

import java.io.File
import java.lang.Exception
import java.lang.RuntimeException

enum class Signs(val char: Char) {
    WALL('#'),
    UNVISITED('.'),
    EXIT('E'),
    START('S'),
    PATH('*');

    override fun toString() = char.toString();
}
class Maze private constructor() {
    private var mazeList: MutableList<MutableList<Signs>> = mutableListOf()
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
            val maze = Maze()
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

    fun printBySteps(path: ArrayList<Point>){
        val tempMaze = this
        for (point in path){
            if (point != startPos && point != goalPos){
                tempMaze[point] = Signs.PATH
            }
            tempMaze.print()
        }
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

}

