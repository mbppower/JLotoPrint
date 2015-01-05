/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jlotoprint;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.swing.ImageIcon;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class JLotoPrint extends Application {
	static Parent root;
	static Stage stage;
	
	@Override
	public void start(Stage stage) throws Exception {
		//INIT CONFIG
		Template.setTemplateDir("resources/templates/");
		//END CONFIG
		
		Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
		JLotoPrint.root = root;
		Scene scene = new Scene(root);
		stage.setTitle("JLotoPrint");
		stage.getIcons().add(new Image("file:resources/icon.png"));
		stage.setScene(scene);
		stage.show();
		this.stage = stage;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
