package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import javafx.scene.control.RadioButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.util.Deque;
import java.util.ArrayDeque;

import java.io.IOException;

public class SettingsControl extends GridPane {

    private HostServices hostServices;

    Deque<RadioButton> availableToSelect;
    Deque<RadioButton> currentlySelected;

    @FXML
    private RadioButton precipProbRadio;

    @FXML
    private RadioButton aveTempRadio;

    @FXML
    private RadioButton maxTempRadio;

    @FXML
    private RadioButton minTempRadio;

    @FXML
    private RadioButton visibilityRadio;

    @FXML
    private RadioButton humidityRadio;

    @FXML
    private RadioButton airPressureRadio;

    @FXML
    private RadioButton groundCondRadio;

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

    private void markAllUnset() {
        precipProbRadio.setSelected(false);
        aveTempRadio.setSelected(false);
        maxTempRadio.setSelected(false);
        minTempRadio.setSelected(false);
        visibilityRadio.setSelected(false);
        humidityRadio.setSelected(false);
        airPressureRadio.setSelected(false);
        groundCondRadio.setSelected(false);
    }

    private void setDefaultSelection() {
        precipProbRadio.setSelected(true);
        maxTempRadio.setSelected(true);
        minTempRadio.setSelected(true);
        groundCondRadio.setSelected(true);
    }

    private void initializeCurrentlySelected() {
        currentlySelected = new ArrayDeque<>();
        currentlySelected.offer(groundCondRadio);
        currentlySelected.offer(precipProbRadio);
    }

    private void initializeAvailableToSelect() {
        availableToSelect = new ArrayDeque<>();
        availableToSelect.offer(aveTempRadio);
        availableToSelect.offer(visibilityRadio);
        availableToSelect.offer(humidityRadio);
        availableToSelect.offer(airPressureRadio);
    }

    /**
     * method ensures that only four weather factors can selected by the user at any one time
     * */
    private void handleSelectionChange(RadioButton radioButton) {
        if (currentlySelected.contains(radioButton)) { // if deselecting radioButton
            // automatically select a replacement
            availableToSelect.getFirst().setSelected(true);
            currentlySelected.offerFirst(availableToSelect.getFirst());
            availableToSelect.removeFirst();
            // handle deselection
            radioButton.setSelected(false);
            availableToSelect.offerFirst(radioButton);
            currentlySelected.remove(radioButton);
        } else { // if selecting radioButton
            // automatically select a victim
            currentlySelected.getFirst().setSelected(false);
            availableToSelect.offerFirst(currentlySelected.getFirst());
            currentlySelected.removeFirst();
            // handle selection
            radioButton.setSelected(true);
            currentlySelected.offerFirst(radioButton);
            availableToSelect.remove(radioButton);
        }
    }

    @FXML
    public void initialize() {

        markAllUnset();
        setDefaultSelection();
        initializeCurrentlySelected();
        initializeAvailableToSelect();

        precipProbRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(precipProbRadio);
            }
        });

        aveTempRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(aveTempRadio);
            }
        });

        maxTempRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                maxTempRadio.setSelected(true);
            }
        });

        minTempRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                minTempRadio.setSelected(true);
            }
        });

        visibilityRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(visibilityRadio);
            }
        });

        humidityRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(humidityRadio);
            }
        });

        airPressureRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(airPressureRadio);
            }
        });

        groundCondRadio.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleSelectionChange(groundCondRadio);
            }
        });
    }
}
