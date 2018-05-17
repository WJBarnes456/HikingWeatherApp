package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.Hike;

import java.io.IOException;
import java.util.Date;

public class HikeControl extends GridPane {
    private Date hikeDate;

    private String hikeName;

    private double rainProb;

    

    public HikeControl(Hike hike) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HomeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD


        fxmlLoader.load();
    }
}
