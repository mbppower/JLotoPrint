/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jlotoprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jlotoprint.model.Template;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.glyphfont.GlyphFont;
/**
 *
 * @author Marcel.Barbosa
 */
public class JLotoPrint extends Application {
	static Parent root;
	static Stage stage;
	static GlyphFont fontAwesome;
    
    
	@Override
	public void start(Stage stage) throws Exception {
        fontAwesome = GlyphFontRegistry.font("FontAwesome");
        
        //theme
        setUserAgentStylesheet(STYLESHEET_MODENA);
            
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
