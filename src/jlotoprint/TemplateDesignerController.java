/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import com.sun.javafx.event.EventUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import jlotoprint.model.MarkInfo;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class TemplateDesignerController implements Initializable {
    
	@FXML
	public Pane imageContainer;
    @FXML
    public Label zoomLevel;
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
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        if(Template.isLoaded()){
            loadPanel();
        }
	}
	@FXML
	private void handleAddItemAction(ActionEvent event) {
		String groupName = groupCombo.getValue().toString();
		String typeName = typeCombo.getValue().toString();
		lotoPanel.createMark(groupName, typeName, markValue.getText());
	}

	@FXML
	private void handleOpenTemplateAction(ActionEvent event) {
		final Stage templateChooser = MainViewController.loadTemplateChooser();
        if(templateChooser != null){
            templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
				templateChooser.close();
				loadPanel();
			});
        }
	}
    @FXML
	private void handleNewModelAction(ActionEvent event) {
    }
   
    @FXML
	private void handleExitAction(ActionEvent event) {
        if(showSaveChangesDialog()){
            EventUtil.fireEvent(new TemplateDesignerEvent(TemplateDesignerEvent.CLOSE), imageContainer);
        }
    }
    
    public boolean showSaveChangesDialog(){
        if(Template.isLoaded()){
            
            ButtonType saveAndExit = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType dontSave = new ButtonType("Don't save", ButtonBar.ButtonData.NO);

            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes?", saveAndExit, dontSave, ButtonType.CANCEL);
            dialog.initModality(Modality.WINDOW_MODAL);
            Optional<ButtonType> result = dialog.showAndWait();
            //save and exit
            if (result.get() == saveAndExit){
                saveModel();
                return true;
            }
            //don't save
            else if (result.get() == dontSave){
                return true;
            }   
            //cancel
            else{
                return false;
            }
        }
        else{
            //exit
            return true;
        }
    }
      
    @FXML
	private void handleSaveModelAction(ActionEvent event) {
        saveModel();
    }
    
	@FXML
	private void handleSaveAsModelAction(ActionEvent event) {
        saveAsDialog();
	}
    private void saveModel(){
        if(lotoPanel != null){
            File file = Template.getTemplateFile();
            if(file != null && file.exists()){
                //Show save file dialog
                saveModelFile(file);
            }
            else{
                saveAsDialog();
            }
		}
    }
    
	private void saveAsDialog(){
        if(lotoPanel != null){
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JLotoPrint Model (*.json)", "*.json");
			fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setInitialDirectory(Template.getTemplateFile() != null ? Template.getTemplateFile().getParentFile() : new File(Template.getTemplateDir()));
            fileChooser.setInitialFileName("template");
			//Show save file dialog
			File file = fileChooser.showSaveDialog(JLotoPrint.stage);
			saveModelFile(file);
		}
    }
    private void saveModelFile(File file){
        if (file != null) {
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(lotoPanel.getJsonModel());
                fileWriter.close();
            }
            catch (IOException ex) {
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
        
		zoomBar.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            ZOOM = newValue.doubleValue() / 100;
            zoomLevel.setText(((int)newValue.doubleValue()) + "%");
            imageContainer.setScaleX(ZOOM);
            imageContainer.setScaleY(ZOOM);
		});
        zoomBar.setValue(ZOOM * 100);

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
		markValue.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            MarkInfo m = lotoPanel.getSelection();
            if (m != null) {
                m.setToggleValue(newValue);
            }
		});
        
		lotoPanel = new LotoPanel(true);
		lotoPanel.selectionProperty().addListener((ObservableValue<? extends MarkInfo> observable, MarkInfo oldValue, MarkInfo newValue) -> {
            if (newValue != null) {
                currentSelection.setText(newValue.getId());
                groupCombo.setValue(newValue.getGroup());
                typeCombo.setValue(newValue.getType());
                markValue.setText(newValue.getToggleValue());
            }
		});
        lotoPanel.importMarks();
        templateName.setText(lotoPanel.model.getName());
        templateName.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            lotoPanel.model.setName(newValue);
		});
        templateImage.setText(lotoPanel.model.getImage());
        templateImage.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			lotoPanel.model.setImage(newValue);
		});
        
        templateImagePreview.setText(lotoPanel.model.getImagePreview());
        templateImagePreview.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			lotoPanel.model.setImagePreview(newValue);
		});
        
		imageContainer.getChildren().add(lotoPanel);
	}
}