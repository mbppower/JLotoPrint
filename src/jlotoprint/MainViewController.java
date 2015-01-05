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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;


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
    public void handleOpenTemplateDesigner(ActionEvent event){
        final Stage stage = new Stage();
		try {
			Parent root = FXMLLoader.load(MainViewController.class.getResource("TemplateDesigner.fxml"));
			stage.setScene(new Scene(root));
			stage.setTitle("Template Designer");
			stage.initModality(Modality.WINDOW_MODAL);
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
}
