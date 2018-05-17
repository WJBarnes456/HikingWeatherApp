package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;

import javafx.fxml.FXMLLoader;

public class DetailsControl {
	public DetailsControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DetailsControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        fxmlLoader.load();
    }
}
