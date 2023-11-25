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
        stage.minWidth = 412.5
        stage.minHeight = 247.0
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java)
            //val maze = Maze.fromFile("/Users/dmitrypopov/IdeaProjects/AStar/src/main/resources/maze.txt")
            //maze.print()
            //val aStar = AStarAlgorithm(maze)
            //aStar.printSolvedMaze()

        }
    }
}