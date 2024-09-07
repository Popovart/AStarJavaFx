
package com.app.astar.controller

import com.app.astar.model.AStarAlgorithm
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.FileChooser
import javafx.stage.Stage
import com.app.astar.model.Maze
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality

class MazeController {

    private lateinit var gridController: GridController
    private lateinit var aStar: AStarAlgorithm
    private lateinit var maze: Maze

    @FXML
    private lateinit var gridContainer: VBox
    @FXML
    private lateinit var fileButton: Button
    @FXML
    private lateinit var startButton: Button
    @FXML
    private lateinit var byStepsButton: Button
    @FXML
    private lateinit var rowsInput: TextField
    @FXML
    private lateinit var colsInput: TextField
    @FXML
    private lateinit var clearAllButton: Button
    @FXML
    private lateinit var initMazeButton: Button
    @FXML
    private lateinit var createMazeButton: Button
    @FXML
    private lateinit var modalStage: Stage
    @FXML
    private var label: Label? = null
    private lateinit var path: String

    private fun setupGridController(maze: Maze) {
        gridController = GridController(maze)
        gridController.onPathClearedCallback = { unlockPathButtons() }
        clearGridContainer()
        gridContainer.children.add(gridController.grid)
    }

    private fun unlockPathButtons() {
        startButton.isDisable = false
        byStepsButton.isDisable = false
    }

    @FXML
    private fun onFileButtonClick() {
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Text Files", "*.txt"))
        val selectedFile = fileChooser.showOpenDialog(fileButton.scene.window)
        path = selectedFile.absolutePath

        if (path.isNotEmpty()) {
            unlockPathButtons()
            clearAllButton.isDisable = false
            val maze = Maze.fromFile(path)
            setupGridController(maze)
        }
    }

    @FXML
    private fun onStartButtonClick() {
        processMaze { maze ->
            aStar = AStarAlgorithm(maze)
            label?.text = aStar.message
            val solvedMaze = aStar.getSolvedMaze()
            setupGridController(solvedMaze)
        }
    }

    @FXML
    private fun onByStepsButtonClick() {
        processMaze { maze ->
            aStar = AStarAlgorithm(maze)
            if (::aStar.isInitialized) {
                aStar.updateGridByStepsWithProbPos(gridController)
                startButton.isDisable = true
                byStepsButton.isDisable = true
            }
        }
    }

    private fun processMaze(action: (Maze) -> Unit) {
        gridController.clearPath()
        maze = Maze(gridController.grid)
        if (!isStartOrGoalDefined()) {
            label?.text = "Maze doesn't have start or goal position! Goal position = ${maze.goalPos}. Start position = ${maze.startPos}"
            return
        }
        action(maze)
    }

    @FXML
    private fun onClearAllButtonClick() {
        if (::gridContainer.isInitialized) {
            gridController.clearAll()
            unlockPathButtons()
        }
    }

    private fun isStartOrGoalDefined(): Boolean {
        return if (!maze.isStartPosDefined() || !maze.isGoalPosDefined()) {
            false
        } else {
            label?.text = ""
            true
        }
    }

    private fun clearGridContainer() {
        if (gridContainer.children.isNotEmpty()) {
            gridContainer.children.clear()
        }
    }

    @FXML
    private fun onCreateMazeButtonClick() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/com/app/astar/modalWindow-view.fxml"))
        fxmlLoader.setController(this)
        val modalRoot: Parent = fxmlLoader.load()
        val modalScene = Scene(modalRoot)
        modalStage = Stage()

        modalStage.scene = modalScene
        modalStage.initModality(Modality.APPLICATION_MODAL)
        modalStage.title = "Settings window"
        modalStage.showAndWait()

        clearAllButton.isDisable = false
    }

    @FXML
    fun onInitMazeButtonClick(actionEvent: ActionEvent) {

        modalStage.close()

        val rows = rowsInput.text.toInt()
        val cols = colsInput.text.toInt()
        gridController = GridController(rows, cols)
        gridController.onPathClearedCallback = { unlockPathButtons() }

        clearGridContainer()
        gridContainer.children.add(gridController.grid)
        unlockPathButtons()

    }
}
