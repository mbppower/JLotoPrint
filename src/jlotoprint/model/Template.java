/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint.model;

import java.io.File;

/**
 *
 * @author Marcel.Barbosa
 */
public class Template {
	//template folder
	private static String basePath;
	
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
	
	public static String getBasePath() {
		return basePath;
	}

	public static void setBasePath(String basePath) {
		Template.basePath = basePath;
	}
    public static boolean isLoaded(){
        return Template.getTemplateFile() != null;
    }
}
