package com.app.astar.controller

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.controlsfx.control.spreadsheet.Grid

class MazeController {
    @FXML
    private lateinit var fileButton: Button

    @FXML
    private lateinit var startButton: Button

    @FXML
    private lateinit var closeButton: Button

    @FXML
    private var label: Label? = null

    @FXML
    private lateinit var grid: GridPane

    @FXML
    private fun onHelloButtonClick() {
        label?.text = "Welcome to JavaFX Application!"
    }
}