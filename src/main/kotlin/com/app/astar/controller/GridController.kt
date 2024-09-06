package com.app.astar.controller

import com.app.astar.model.Signs
import com.app.astar.model.ColorSigns
import com.app.astar.model.Maze
import javafx.geometry.Insets
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.layout.*

class GridController(private var rows: Int = 0, private var cols: Int = 0) {

    val grid = GridPane()

    var startPane: Pane? = null
    var endPane: Pane? = null

    var onPathClearedCallback: (() -> Unit)? = null

    init {
        initializeGrid()
    }

    constructor(maze: Maze) : this(maze.colCount, maze.rowCount) {
        initializeGridFromMaze(maze)
    }

    // Метод для получения цвета панели на основе знака
    private fun getPaneColor(sign: Signs): Color {
        return when (sign) {
            Signs.EXIT -> ColorSigns.EXIT.color
            Signs.UNVISITED -> ColorSigns.UNVISITED.color
            Signs.WALL -> ColorSigns.WALL.color
            Signs.START -> ColorSigns.START.color
            Signs.PATH -> ColorSigns.PATH.color
            Signs.PROBABLE -> ColorSigns.PROBABLE.color
        }
    }

    // Объединенный метод для очистки панелей
    private fun clearNodes(condition: (Color) -> Boolean) {
        for (node in grid.children) {
            if (node is Pane) {
                val backgroundColor = (node.background?.fills?.firstOrNull()?.fill as? Color) // Приведение к Color
                if (backgroundColor != null && condition(backgroundColor)) {
                    updatePaneColor(node, ColorSigns.UNVISITED.color)
                }
            }
        }
    }

    // Очистка только путей и вероятных путей
    fun clearPath() {
        clearNodes { it == ColorSigns.PATH.color || it == ColorSigns.PROBABLE.color }
        onPathClearedCallback?.invoke()
    }

    // Полная очистка сетки
    fun clearAll() {
        clearNodes { it != ColorSigns.UNVISITED.color }
        startPane = null
        endPane = null
    }

    // Метод для создания ячеек с обработчиками кликов
    private fun createPane(paneBackgroundColor: Color): Pane {
        return Pane().apply {
            background = Background(BackgroundFill(paneBackgroundColor, null, null))
            setPrefSize(20.0, 20.0)
            border = Border(BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS))

            setOnMouseClicked { event ->
                handlePaneClick(this, event.button)
            }
        }
    }

    // Обработчик кликов для панелей
    private fun handlePaneClick(currentPane: Pane, button: MouseButton) {
        clearPath()

        when (button) {
            MouseButton.PRIMARY -> toggleWall(currentPane)
            MouseButton.SECONDARY -> setStartOrEnd(currentPane)
            else -> { /* No action needed */ }
        }
    }

    // Логика переключения между стеной и пустым местом
    private fun toggleWall(currentPane: Pane) {
        val currentColor = currentPane.background.fills[0].fill
        if (currentPane == startPane) startPane = null
        if (currentPane == endPane) endPane = null

        val newColor = if (currentColor == ColorSigns.UNVISITED.color) ColorSigns.WALL.color else ColorSigns.UNVISITED.color
        updatePaneColor(currentPane, newColor)
    }

    // Логика установки стартовой и конечной точек
    private fun setStartOrEnd(currentPane: Pane) {
        val currentColor = currentPane.background.fills[0].fill
        var newColor = if (currentColor == ColorSigns.START.color) ColorSigns.EXIT.color else ColorSigns.START.color

        if (currentPane == startPane) {
            updatePaneColor(endPane, ColorSigns.UNVISITED.color)
            endPane = startPane
            startPane = null
        } else if (currentPane == endPane) {
            updatePaneColor(startPane, ColorSigns.UNVISITED.color)
            startPane = endPane
            endPane = null
        } else if (startPane != null && endPane != null) {
            newColor = ColorSigns.UNVISITED.color
        } else if (startPane == null) {
            startPane = currentPane
        } else {
            endPane = currentPane
            newColor = ColorSigns.EXIT.color
        }

        updatePaneColor(currentPane, newColor)
    }

    // Метод для обновления цвета панели
    private fun updatePaneColor(pane: Pane?, color: Color) {
        pane?.background = Background(BackgroundFill(color, null, null))
    }

    // Инициализация сетки
    private fun initializeGrid() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pane = createPane(Color.WHITE)
                grid.add(pane, col, row)
            }
        }
    }

    // Инициализация сетки из лабиринта
    private fun initializeGridFromMaze(maze: Maze) {
        for (rowIndex in maze.mazeList.indices) {
            for (colIndex in maze.mazeList[rowIndex].indices) {
                val sign = maze.mazeList[rowIndex][colIndex]
                val paneColor = getPaneColor(sign)
                val pane = createPane(paneColor)
                if (paneColor == ColorSigns.START.color) {
                    startPane = pane
                } else if (paneColor == ColorSigns.EXIT.color) {
                    endPane = pane
                }

                grid.add(pane, colIndex, rowIndex)
            }
        }
    }
}