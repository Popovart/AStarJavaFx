package com.app.astar.controller

import com.app.astar.model.Signs
import com.app.astar.model.ColorSigns
import com.app.astar.model.Maze
import javafx.geometry.Insets
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.layout.*

class GridController (private var rows: Int = 0, private var cols: Int = 0) {

    val grid = GridPane()

    init {
        initializeGrid()
    }

    constructor(maze: Maze) : this(maze.colCount, maze.rowCount) {
        for (rowIndex in maze.mazeList.indices) {
            for (colIndex in maze.mazeList[rowIndex].indices) {
                val sign = maze.mazeList[rowIndex][colIndex]
                val paneColor = getPaneColor(sign)
                val pane = createPane(paneColor)
                grid.add(pane, colIndex, rowIndex)
            }
        }
    }

    private fun getPaneColor(sign: Signs) : Color {
        return when (sign) {
            Signs.EXIT -> ColorSigns.EXIT.color
            Signs.UNVISITED -> ColorSigns.UNVISITED.color
            Signs.WALL -> ColorSigns.WALL.color
            Signs.START -> ColorSigns.START.color
            Signs.PATH -> ColorSigns.PATH.color
        }
    }

    fun clearPath(){
        for (node in grid.children){
            if (node is Pane) {
                val backgroundColor = node.background?.fills?.firstOrNull()?.fill
                if (backgroundColor == ColorSigns.PATH.color){
                    // Создаем новый объект BackgroundFill с новым цветом
                    val newBackgroundFill = BackgroundFill(ColorSigns.UNVISITED.color, CornerRadii.EMPTY, Insets.EMPTY)
                    // Создаем новый объект Background с новым BackgroundFill
                    val newBackground = Background(newBackgroundFill)
                    // Устанавливаем новый фон для Pane
                    node.background = newBackground
                }
            }
        }
    }

    private fun createPane(paneBackgroundColor: Color): Pane {
        return Pane().apply {
            background = Background(BackgroundFill(paneBackgroundColor, null, null))
            setPrefSize(20.0, 20.0)
            border = Border(BorderStroke(Color.rgb(0,0,0), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS))
            setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY) {
                    // Переключение цвета фона между белым и черным
                    val currentColor = background.fills[0].fill

                    val newColor = if (currentColor == ColorSigns.UNVISITED.color) ColorSigns.WALL.color else ColorSigns.UNVISITED.color
                    background = Background(BackgroundFill(newColor, null, null))
                }
                if (event.button == MouseButton.SECONDARY) {
                    // Переключение цвета фона между белым и черным
                    val currentColor = background.fills[0].fill
                    val newColor = if (currentColor == ColorSigns.START.color) ColorSigns.EXIT.color  else ColorSigns.START.color
                    background = Background(BackgroundFill(newColor, null, null))
                }
            }
        }
    }

    private fun initializeGrid() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pane = createPane(Color.WHITE)
                grid.add(pane, col, row)
            }
        }

    }


}