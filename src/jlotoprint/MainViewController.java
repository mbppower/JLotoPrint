/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jlotoprint;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Marcel.Barbosa
 */
public class MainViewController implements Initializable {
	
	@FXML
	public Label label;
	@FXML
	public Pane imageContainer;
	@FXML
	public Pane printViewUIPanel;
	@FXML
	public ComboBox groupCombo;
	
	@FXML
	public ComboBox typeCombo;
	
	@FXML
	public TextField markValue;
	
	@FXML
	public Slider zoomBar;
	
	public static double ZOOM = .5;
	
	LotoPanel lotoPanel;
	
	@FXML
	private void handleAddItemAction(ActionEvent event) {
		String groupName = groupCombo.getValue().toString();
		String typeName = typeCombo.getValue().toString();
		lotoPanel.createMark(groupName, typeName, markValue.getText());
	}
	
	@FXML
	private void handleSaveModelAction(ActionEvent event) {
		System.out.print(lotoPanel.getJsonModel());
	}

	@FXML
	private void handleLoadModelAction(ActionEvent event) {
		lotoPanel.importMarks();		
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		lotoPanel = new LotoPanel(true);
		imageContainer.getChildren().add(lotoPanel);
		imageContainer.setScaleX(ZOOM);
		imageContainer.setScaleY(ZOOM);
		
		AnchorPane.setTopAnchor(lotoPanel, 10.0);
		AnchorPane.setLeftAnchor(lotoPanel, 10.0);
		
		groupCombo.setItems(FXCollections.observableArrayList(
			"Group_1",
			"Group_2"
		));
		groupCombo.setValue(groupCombo.getItems().get(0));
		
		typeCombo.setItems(FXCollections.observableArrayList(
			"option",
			"numberCount"
		));
		typeCombo.setValue(typeCombo.getItems().get(0));
		
		zoomBar.setValue(ZOOM);
		zoomBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ZOOM = newValue.doubleValue() / 100;
				imageContainer.setScaleX(ZOOM);
				imageContainer.setScaleY(ZOOM);
			}
		});
	}

}
