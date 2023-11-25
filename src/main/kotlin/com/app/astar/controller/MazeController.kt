package com.app.astar.controller

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.controlsfx.control.spreadsheet.Grid
import javafx.stage.FileChooser
import javafx.stage.Stage
import com.app.astar.model.Maze
import javafx.application.Platform
import kotlin.system.exitProcess

class MazeController {
    @FXML
    private lateinit var fileButton: Button

    @FXML
    private lateinit var startButton: Button

    @FXML
    private lateinit var byStepsButton: Button

    @FXML
    private lateinit var closeButton: Button

    @FXML
    private var label: Label? = null

    @FXML
    private lateinit var grid: GridPane

    private lateinit var path: String
    @FXML
    private fun onFileButtonClick() {
        val fileChooser = FileChooser()
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Text Files", "*.txt"))
        val selectedFile = fileChooser.showOpenDialog(fileButton.scene.window)
        path = selectedFile.absolutePath
        if (path.isNotEmpty()){
            startButton.isDisable = false
            byStepsButton.isDisable = false
        }
    }

    @FXML
    private fun onStartButtonClick() {

    }

    private fun initGrid(){

    }


}