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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextInputDialog;
import jlotoprint.model.Model;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class TemplateDesignerController implements Initializable {
    @FXML
	public Pane editorContainer;
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        if(Template.isLoaded()){
            Template.load(true);
            loadPanel();
        }
        else{
            editorContainer.setVisible(false);
        }
	}

	@FXML
	private void handleOpenTemplateAction(ActionEvent event) {
		final Stage templateChooser = MainViewController.loadTemplateChooser();
        if(templateChooser != null){
            templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
				templateChooser.close();
                Template.load(true);
				loadPanel();
			});
        }
	}
    @FXML
	private void handleNewModelAction(ActionEvent event) {
        if(showSaveChangesDialog()){
            Template.unload();
            createNewTemplate();
        }
    }
    private void createNewTemplate(){
        TextInputDialog dialog = new TextInputDialog("Untitled");
        dialog.setTitle("New template");
        dialog.setHeaderText("New template");
        dialog.setContentText("Please, insert your template name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            File templateDir = new File(Template.getTemplateDir() + "/" + name);
            if(templateDir.exists() && templateDir.isDirectory() && templateDir.list().length > 0){
                Alert alert = new Alert(Alert.AlertType.ERROR, "The folder already exists and is not empty, please choose another folder.", ButtonType.OK);
                dialog.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                createNewTemplate();
            }
            else{
                templateDir.mkdir();
                Template.setTemplateFile(new File(templateDir + "/" + "template.json"));
                Template.setModel(new Model());
                loadPanel();
            }
        });
    }
    
    @FXML
	private void handleExitAction(ActionEvent event) {
        if(showSaveChangesDialog()){
            EventUtil.fireEvent(new TemplateDesignerEvent(TemplateDesignerEvent.CLOSE), editorContainer);
        }
    }
    
    @FXML
    public void handleAboutAction(ActionEvent event){
        MainViewController.loadAboutWindow();
    }
    public boolean showSaveChangesDialog(){
        if(Template.isLoaded()){
            
            ButtonType saveAndExit = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType dontSave = new ButtonType("Don't save", ButtonBar.ButtonData.NO);

            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save changes?", saveAndExit, dontSave, ButtonType.CANCEL);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(JLotoPrint.stage.getScene().getWindow());
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
        if(Template.isLoaded()){
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
       if(Template.isLoaded()){
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
                fileWriter.write(Template.getModel().toJson());
                fileWriter.close();
            }
            catch (IOException ex) {
                MainViewController.showExceptionAlert("Error when saving the file", ex.getMessage());
            }
        }
    }
    
	public void loadPanel(){
        try {
            //clear previous
            editorContainer.getChildren().clear();
            Parent editor = (Parent)FXMLLoader.load(TemplateDesignerController.class.getResource("TemplateDesignerEditor.fxml"));
            editorContainer.getChildren().add(editor);
            editorContainer.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(TemplateDesignerController.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}