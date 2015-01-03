/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import com.sun.glass.ui.Application;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jlotoprint.model.MarkInfo;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class MainViewController implements Initializable {

	@FXML
	public Label label;
	@FXML
	public GridPane imageContainer;
	@FXML
	public Pane printViewUIPanel;
	@FXML
	public ComboBox groupCombo;

	@FXML
	public TextField currentSelection;

	@FXML
	public ComboBox typeCombo;

	@FXML
	public TextField markValue;

	@FXML
	public Slider zoomBar;
    @FXML
    public TextField templateName;
    @FXML
    public TextField templateImage;
    @FXML
    public TextField templateImagePreview;
    
	public static double ZOOM = .5;

	LotoPanel lotoPanel;

	@FXML
	private void handleAddItemAction(ActionEvent event) {
		String groupName = groupCombo.getValue().toString();
		String typeName = typeCombo.getValue().toString();
		lotoPanel.createMark(groupName, typeName, markValue.getText());
	}

	@FXML
	private void handleOpenTemplateAction(ActionEvent event) {
		//show dialog
		try {
			final Stage stage = new Stage();
			FXMLLoader dialog = new FXMLLoader(getClass().getResource("TemplateDialog.fxml"));
			Parent root = (Parent)dialog.load();          
			TemplateDialogController dialogController = dialog.<TemplateDialogController>getController();
			
			root.addEventHandler(TemplateDialogEvent.CANCELED, (actionEvent) -> {
				stage.close();
			});
			
			root.addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
				stage.close();
				loadPanel();
			});
			
			stage.setScene(new Scene(root));
			stage.setTitle("Choose a template");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(JLotoPrint.stage.getScene().getWindow());
			stage.show();
		}
		catch (IOException ex) {
			Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void handleSaveModelAction(ActionEvent event) {
        if(lotoPanel != null){
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JLotoPrint Model (*.json)", "*.json");
			fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setInitialDirectory(Template.getTemplateFile() != null ? Template.getTemplateFile().getParentFile() : new File(Template.getTemplateDir()));
            fileChooser.setInitialFileName("template");
			//Show save file dialog
			File file = fileChooser.showSaveDialog(JLotoPrint.stage);
			if (file != null) {
				try {
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.write(lotoPanel.getJsonModel());
					fileWriter.close();
				} catch (IOException ex) {
					Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
	}
	
	public void loadPanel(){
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

		groupCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				MarkInfo m = lotoPanel.getSelection();
				if (m != null) {
					m.setGroup(newValue);
				}
			}
		});
		typeCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				MarkInfo m = lotoPanel.getSelection();
				if (m != null) {
					m.setType(newValue);
				}
			}
		});
		markValue.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				MarkInfo m = lotoPanel.getSelection();
				if (m != null) {
					m.setToggleValue(newValue);
				}
			}
		});
        
		lotoPanel = new LotoPanel(true);
		lotoPanel.selectionProperty().addListener(new ChangeListener<MarkInfo>() {
			@Override
			public void changed(ObservableValue<? extends MarkInfo> observable, MarkInfo oldValue, MarkInfo newValue) {
				if (newValue != null) {
					currentSelection.setText(newValue.getId());
					groupCombo.setValue(newValue.getGroup());
					typeCombo.setValue(newValue.getType());
					markValue.setText(newValue.getToggleValue());
				}
			}
		});
        lotoPanel.importMarks();
        templateName.setText(lotoPanel.model.getName());
        templateName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                lotoPanel.model.setName(newValue);
			}
		});
        templateImage.setText(lotoPanel.model.getImage());
        templateImage.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				lotoPanel.model.setImage(newValue);
			}
		});
        templateImagePreview.setText(lotoPanel.model.getImagePreview());
        templateImagePreview.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				lotoPanel.model.setImagePreview(newValue);
			}
		});
		imageContainer.setAlignment(Pos.CENTER);
		imageContainer.add(lotoPanel, 0, 0);
		GridPane.setHalignment(lotoPanel, HPos.CENTER);
		GridPane.setValignment(lotoPanel, VPos.CENTER);
		imageContainer.setScaleX(ZOOM);
		imageContainer.setScaleY(ZOOM);
	}
}
