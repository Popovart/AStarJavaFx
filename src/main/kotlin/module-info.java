module com.app.astar {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens com.app.astar.controller to javafx.fxml;

    exports com.app.astar;
    exports com.app.astar.controller;
}