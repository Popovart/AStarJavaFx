package com.app.astar.controller

import com.app.astar.model.AStarAlgorithm
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.controlsfx.control.spreadsheet.Grid
import javafx.stage.FileChooser
import javafx.stage.Stage
import com.app.astar.model.Maze
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Modality
import kotlin.system.exitProcess

class MazeController {

    private lateinit var gridController: GridController
    private lateinit var aStar: AStarAlgorithm

    @FXML
    private lateinit var gridContainer: VBox


    @FXML
    private lateinit var fileButton: Button

    @FXML
    private lateinit var startButton: Button

    @FXML
    private lateinit var clearPathButton: Button

    @FXML
    private lateinit var byStepsButton: Button

    @FXML
    private lateinit var rowsInput: TextField

    @FXML
    private lateinit var colsInput: TextField

    @FXML
    private lateinit var initMazeButton: Button

    private lateinit var maze: Maze

    @FXML
    private var label: Label? = null

    @FXML
    private lateinit var createMazeButton: Button

    private lateinit var path: String
    @FXML
    private fun onFileButtonClick() {
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Text Files", "*.txt"))
        val selectedFile = fileChooser.showOpenDialog(fileButton.scene.window)
        path = selectedFile.absolutePath

        if (path.isNotEmpty()){

            clearGridContainer()

            startButton.isDisable = false
            byStepsButton.isDisable = false

            val maze = Maze.fromFile(path)
            gridController = GridController(maze)
            // Добавляем GridPane в пользовательский интерфейс
            gridContainer.children.add(gridController.grid)
        }

    }

    @FXML
    private fun onStartButtonClick() {
        gridController.clearPath()
        maze = Maze(gridController.grid)

        if (!isStartOrGoalDefined()){
            label?.text = "Maze doesn't have start or goal position! Goal position = ${maze.goalPos}. Start position = ${maze.startPos}"
            return
        }

        aStar = AStarAlgorithm(maze)
        label?.text = aStar.message
        val solvedMaze = aStar.getSolvedMaze()




        gridController = GridController(solvedMaze)

        clearGridContainer()

        gridContainer.children.add(gridController.grid)
    }

    private fun isStartOrGoalDefined() : Boolean{
        if (!maze.isStartPosDefined() || !maze.isGoalPosDefined()){
            return false
        }

        label?.text = ""
        return true
    }

    private fun clearGridContainer() {
        if(gridContainer.children.isNotEmpty()){
            gridContainer.children.clear()
        }
    }




    @FXML
    private fun onCreateMazeButtonClick() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/com/app/astar/modalWindow-view.fxml"))
        fxmlLoader.setController(this) // Установить текущий контроллер
        val modalRoot: Parent = fxmlLoader.load()

        // Создаем новую сцену для модального окна
        val modalScene = Scene(modalRoot)
        val modalStage = Stage()

        // Устанавливаем модальность и другие свойства
        modalStage.scene = modalScene
        modalStage.initModality(Modality.APPLICATION_MODAL)
        modalStage.title = "Settings window"

        // Показываем модальное окно и ждем, пока оно не закроется
        modalStage.showAndWait()
    }


    @FXML
    fun onInitMazeButtonClick(actionEvent: ActionEvent) {
        // Получаем источник события, который является элементом управления, вызвавшим событие
        val source = actionEvent.source as Node
        // Получаем текущую сцену через источник события
        val stage = source.scene.window as Stage
        // Закрываем окно
        stage.close()


        if (::gridContainer.isInitialized) {
            val rows = rowsInput.text.toInt()
            val cols = colsInput.text.toInt()
            gridController = GridController(rows, cols)

            clearGridContainer()


            // Добавляем GridPane в пользовательский интерфейс
            gridContainer.children.add(gridController.grid)

            startButton.isDisable = false
            byStepsButton.isDisable = false

        } else {
            println("gridContainer has not been initialized")
        }
    }



}