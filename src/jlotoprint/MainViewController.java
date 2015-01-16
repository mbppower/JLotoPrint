/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jlotoprint.model.Template;


/**
 *
 * @author Marcel.Barbosa
 */
public class MainViewController implements Initializable {

	@FXML
	public Pane printViewUIPanel;
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
	}
    @FXML
    public void handleAboutAction(ActionEvent event){
        loadAboutWindow();
    }
    @FXML
    public void handleOpenTemplateAction(ActionEvent event){
        final Stage templateChooser = MainViewController.loadTemplateChooser();
        if(templateChooser != null){
            templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
				templateChooser.close();
                Template.load(true);
			});
        }
    }
    @FXML
    public void handleExitAction(ActionEvent event){
        JLotoPrint.stage.close();
    }
    @FXML
    public void handleOpenTemplateDesigner(ActionEvent event){
       
		try {
            FXMLLoader dialog = new FXMLLoader(MainViewController.class.getResource("TemplateDesigner.fxml"));
			Parent root = (Parent)dialog.load();
            final Stage stage = new Stage();
            stage.setOnCloseRequest((WindowEvent windowEvent) -> {
                boolean shouldClose = ((TemplateDesignerController)dialog.getController()).showSaveChangesDialog();
                //cancel event
                if (!shouldClose) {
                    windowEvent.consume();
                }
            });
			root.addEventHandler(TemplateDesignerEvent.CLOSE, actionEvent -> {
				stage.close();
			});
			stage.setScene(new Scene(root));
            stage.getIcons().add(new Image("file:resources/icon.png"));
			stage.setTitle("Template Designer");
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initOwner(JLotoPrint.stage.getScene().getWindow());
			stage.show();
		}
		catch (IOException ex) {
			Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
		}
    }
    public static Stage loadTemplateChooser(){
        final Stage stage = new Stage();
		try {
			FXMLLoader dialog = new FXMLLoader(MainViewController.class.getResource("TemplateDialog.fxml"));
			Parent root = (Parent)dialog.load();
			root.addEventHandler(TemplateDialogEvent.CANCELED, (actionEvent) -> {
				stage.close();
			});
			stage.setScene(new Scene(root));
			stage.setTitle("Choose a template");
            stage.getIcons().add(new Image("file:resources/icon.png"));
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(JLotoPrint.stage.getScene().getWindow());
			stage.show();
		}
		catch (IOException ex) {
			Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
		}
        return stage;
    }
    
    public static void loadAboutWindow(){
        
        //setup
        Dialog dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setTitle("About JLotoPanel");
        dialog.setHeaderText("JLotoPanel v1.0");
        ImageView icon = new ImageView("file:resources/icon.png");
        icon.setSmooth(true);
        icon.setFitHeight(48.0);
        icon.setFitWidth(48.0);
        dialog.setGraphic(icon);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        //content
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 0, 10));
        
        //text
        TextArea textArea = new TextArea("For more info, please visit: https://github.com/mbppower/JLotoPanel");
        textArea.setWrapText(true);
        grid.add(textArea, 0, 0);
        dialog.getDialogPane().setContent(grid);
        
        dialog.showAndWait();
    }
    
    public static void showExceptionAlert(String message, String details){
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 0, 10)); 
        grid.add(textArea, 0, 0);

        //set content
        alert.getDialogPane().setExpandableContent(grid);
        alert.showAndWait();
    }
}