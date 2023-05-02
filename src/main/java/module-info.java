module com.example.fxcopy {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fxcopy to javafx.fxml;
    exports com.example.fxcopy;
}