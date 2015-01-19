/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import jlotoprint.model.Template;


/**
 *
 * @author Marcel.Barbosa
 */
public class StartScreenController implements Initializable {

    @FXML
	public Pane initialContent;
    		
    @FXML
	public Pane contentContainer;
	
	@FXML
	public Label pageInfoLabel;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        initialContent.setOpacity(0.0);
        initialContent.setVisible(true);
        
        //fade
        FadeTransition fadeTran = new FadeTransition(Duration.seconds(1));
        fadeTran.setFromValue(0.0);
        fadeTran.setToValue(1.0);
        fadeTran.setCycleCount(1);
        
        ScaleTransition scaleTran = new ScaleTransition(Duration.seconds(1.2));
        scaleTran.setFromX(0.5);
        scaleTran.setFromY(0.5);
        scaleTran.setToX(1);
        scaleTran.setToY(1);
        scaleTran.setCycleCount(1);

        ParallelTransition parallelTransition = new ParallelTransition(initialContent, fadeTran, scaleTran);
        parallelTransition.setDelay(Duration.seconds(.5));
        parallelTransition.play();
	}

	@FXML
	private void handleLoadGamesAction(ActionEvent event) {
        if(Template.isLoaded()){
            showLotoPrintView();
        }
        else{
            final Stage templateChooser = MainViewController.loadTemplateChooser();
            if(templateChooser != null){
                templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
                    templateChooser.close();
                    Template.load(true);
                    showLotoPrintView();
                });
            }
        }
	}
	
	private void showLotoPrintView() {
        File source = MainViewController.chooseGameSourceFile();
        if(source != null){
            try{
                Parent root = (Parent) FXMLLoader.load(StartScreenController.class.getResource("PrintViewUIPanel.fxml"));
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(root);
            }
            catch(Exception ex){
               Logger.getLogger(StartScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }
}