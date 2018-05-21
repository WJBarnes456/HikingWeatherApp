package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class SettingsControl extends GridPane {
    private HostServices hostServices;

    public SettingsControl(HostServices hostServices) throws IOException {
        this.hostServices = hostServices;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingsControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        fxmlLoader.load();
    }

    @FXML
    private void handleLinkClick(MouseEvent event) {
        hostServices.showDocument("https://darksky.net/poweredby/");
    }
}
