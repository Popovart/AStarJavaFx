package com.app.astar.model

import java.util.*
import kotlin.system.measureTimeMillis


val DIRECTIONS = listOf(
    Point(-1, -1), // Top-Left
    Point(-1, 0),  // Top
    Point(-1, 1),  // Top-Right
    Point(0, -1),  // Left
    Point(0, 1),   // Right
    Point(1, -1),  // Bottom-Left
    Point(1, 0),   // Bottom
    Point(1, 1)    // Bottom-Right
)

fun <T> printMatrix(matrix: List<List<T>>) {
    for (row in matrix){
        for (value in row){
            print("$value ")
        }
        println()
    }
    println()
}

data class AStarNode(
    //вероятно лучше по дефолту ставить inf здесь
    var g: Int = 0,
    var h: Int = 0,
    var parent: Point = Point(-1,-1)
){
    val f: Int
        get() = g + h

    override fun toString(): String {
        return "[$g, $h, $f]"
    }

}

// logic of A* alg
class AStarAlgorithm (
    private var maze: Maze
) {
    // возможно стоит здесь бесконечность передавать или изменить конструктор без параметров для AStarNode, чтобы там были не 0, а inf
    private var node: AStarNode = AStarNode()
    private var priceMatrix: MutableList<MutableList<AStarNode>> = List(maze.rowCount) { MutableList(maze.colCount) { AStarNode() } }.toMutableList()
    private val diagonalStepPenalty = 14
    private val straightStepPenalty = 14
    private var openSet: LinkedHashSet<Point> = LinkedHashSet<Point>()
    private var closedSet: LinkedHashSet<Point> = LinkedHashSet<Point>()
    private var path: MutableList<Point> = mutableListOf()
    var message: String = ""
        private set


    init {

        println("maze.startPos ${maze.startPos}, maze.goalPos ${maze.goalPos}, maze.cols ${maze.colCount}, maze.rows ${maze.rowCount}")


        openSet.add(maze.startPos)

        //start position init
        // на данный момент это бессмысленная операция, так как там и так одни 0
        priceMatrix[maze.startPos.col][maze.goalPos.row].g = 0

        val timeTakenMillis = measureTimeMillis {
            // Ваш алгоритм или блок кода
            findPath()
        }

        val timeTakenSeconds = timeTakenMillis / 1000.0
        println("Time taken by function findPath: $timeTakenSeconds seconds")
        if (message.isEmpty()){
            message = "Time taken by function findPath: $timeTakenSeconds seconds"
        }
    }

    private fun getNoPathInfo() : String{
        return "It's impossible to find the goal position. Walls nearby"
    }

    private fun findPath(){
        val startPos = maze.startPos
        val goalPos = maze.goalPos

        val comparator = Comparator<Triple<Point, Int, Int>> { a, b ->
            when {
                a.third == b.third -> a.second.compareTo(b.second)
                else -> a.third.compareTo(b.third)
            }
        }

        val positionsToCheck = PriorityQueue(comparator)

        val initialH = priceMatrix[startPos.col][startPos.row].h
        val initialF = priceMatrix[startPos.col][startPos.row].f

        positionsToCheck.add(Triple(startPos, initialH, initialF))

        var noPath = true
        while (!positionsToCheck.isEmpty()){
            val currentPos = positionsToCheck.poll().first

            val tempPositions: LinkedHashSet<Point> = getPositionsToCheck(currentPos)

            // after processing the node, put it from th openSet to the closedSet
            openSet.remove(currentPos)
            closedSet.add(currentPos)

            if (tempPositions.contains(goalPos)){
                noPath = false
                reconstructPath()

                break
            }

            for (pos in tempPositions){
                if (!openSet.contains(pos)){
                    val h = priceMatrix[pos.col][pos.row].h
                    val f = priceMatrix[pos.col][pos.row].f
                    positionsToCheck.add(Triple(pos, h, f))
                    openSet.add(pos)
                }
            }

        }
        if(noPath){
            message = getNoPathInfo()
        }

    }

    private fun getPositionsToCheck(currentPos: Point) : LinkedHashSet<Point> {
        val nextPositions: LinkedHashSet<Point> = LinkedHashSet()

        // checking positions around
        for (direction in DIRECTIONS){
            val newPos = Point(currentPos.col + direction.col, currentPos.row + direction.row)

            if (!isWall(newPos) && !isPositionVisited(newPos)) {
                node.h = hCalc(newPos)

                val stepCost = if (direction.col != 0 && direction.row != 0) diagonalStepPenalty else straightStepPenalty
                node.g = priceMatrix[currentPos.col][currentPos.row].g + stepCost

                updatePriceMatrix(node, newPos, currentPos)

                if (newPos == maze.goalPos){
                    return linkedSetOf(newPos)
                }

                nextPositions.add(newPos)
            }
        }

        return nextPositions
    }

    private fun isWall(pos: Point) : Boolean {
        if(maze.outOfRange(pos)){
            return true
        }
        if (maze[pos] == Signs.WALL){
            return true
        }
        return false
    }

    private fun isPositionVisited(pos: Point) : Boolean {
        return closedSet.contains(pos)
    }

    private fun hCalc(pos: Point) : Int {
        return pos.getDistOfPoints(maze.goalPos)*straightStepPenalty
    }

    private fun updatePriceMatrix(node: AStarNode, pos: Point, parentPos: Point){
        priceMatrix[pos.col][pos.row] = AStarNode(node.g, node.h, parentPos)
    }

    private fun reconstructPath(){
        var current: Point = maze.goalPos
        while (current != maze.startPos){
            path.add(current)
            current = priceMatrix[current.col][current.row].parent
        }
        path.add(maze.startPos)
        path.reverse()

    }

    fun printMaze(){
        maze.print()
    }

    fun updateMazeByClosedSet(){
        for (point in closedSet){
            if (point != maze.startPos && point != maze.goalPos){
                maze[point] = Signs.PATH
            }
        }
    }

    fun printSolvedMaze(){
        maze.printSolved(path)
    }

    fun getSolvedMaze() : Maze {
        return maze.getSolvedMaze(path)
    }

    fun printMazeBySteps(){
        maze.printBySteps(path)
    }



}
