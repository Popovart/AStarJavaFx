package com.app.astar

import com.app.astar.model.AStarAlgorithm
import com.app.astar.model.Maze
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage


class App : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(App::class.java.getResource("maze-view.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.title = "Maze Solver"
        stage.scene = scene
        stage.minWidth = 1920.0
        stage.minHeight = 1080.0
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
//            val maze = Maze.fromFile("/Users/dmitrypopov/IdeaProjects/AStar/src/main/resources/maze2.txt")
//            maze.print()
//            println("maze cols = ${maze.colCount}")
//            println("maze cols = ${maze.rowCount}")
//            val aStar = AStarAlgorithm(maze)
//            aStar.printSolvedMaze()

        }
    }
}