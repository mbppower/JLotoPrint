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
