/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import jlotoprint.TemplateDesignerController;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Marcel.Barbosa
 */
public class Template {
    
    private static Model model;

    public static Model getModel() {
        return model;
    }

    public static void setModel(Model model) {
        Template.model = model;
    }
    
	//current template dir
	private static String templateDir;
	
	//current template file
	private static File templateFile;

	public static File getTemplateFile() {
		return templateFile;
	}

	public static void setTemplateFile(File templateFile) {
		Template.templateFile = templateFile;
	}

	public static String getTemplateDir() {
		return templateDir;
	}

	public static void setTemplateDir(String templateDir) {
		Template.templateDir = templateDir;
	}

    public static boolean isLoaded(){
        return Template.getTemplateFile() != null;
    }
    
    public static void unload(){
        model = null;
        Template.setTemplateFile(null);
    }
    
    
     public static Model load(File templateFile, boolean showFeedback){
        Model modelRef = null;
        try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(new FileInputStream(templateFile), writer, "UTF-8");
			Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			modelRef = g.fromJson(writer.toString(), Model.class);
		} 
		catch (Exception ex) {
            //Logger.getLogger(Template.class.getName()).log(Level.SEVERE, null, ex);
            if(showFeedback){
                Alert dialog = new Alert(Alert.AlertType.ERROR, "The template you are trying to load is invalid.", ButtonType.OK);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.showAndWait();
            }
		}
        return modelRef;
    }
     
    public static Model load(boolean showFeedback){
        model = load(getTemplateFile(), showFeedback);
        return model;
    }
}