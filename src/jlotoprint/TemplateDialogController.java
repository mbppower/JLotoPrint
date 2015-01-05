/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jlotoprint;

import com.sun.javafx.event.EventUtil;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import jlotoprint.model.Model;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class TemplateDialogController implements Initializable {
	
	
	@FXML
	public Button cancelButton;
	
	@FXML
	public Button selectButton;
    
        @FXML
	public ImageView previewImage;
	
	@FXML
	ListView<String> templateList = new ListView<String>();
	 
	@FXML
	public Label nameText;
    
        @FXML
    	public Label officialSiteText;

	@FXML
	private void handleCancelAction(ActionEvent event) {
		ActionEvent actionEvent = new ActionEvent("cancel", cancelButton);
		EventUtil.fireEvent(new TemplateDialogEvent(TemplateDialogEvent.CANCELED), selectButton);
	}
	@FXML
	private void handleSelectAction(ActionEvent event) {
		selectAction();
	}
	private void selectAction(){
        String selectedItem = templateList.getSelectionModel().selectedItemProperty().getValue();
		if(selectedItem != null){
			Template.setTemplateFile(new File(selectedItem));
            EventUtil.fireEvent(new TemplateDialogEvent(TemplateDialogEvent.SELECTED), selectButton);
        }
    }
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
		ArrayList<String> templates = new ArrayList<>();
		
		
		//get available templates
		File templateDir = new File(Template.getTemplateDir());
		for(String name : templateDir.list()){
			File file = new File(Template.getTemplateDir() + "/" + name);
			if (file.isDirectory()) {
                            File json = new File(file + "/template.json");
                            if(json.exists()){
                                templates.add(json.getAbsolutePath());
                            }
			}
		}
		
		templateList.setItems(FXCollections.observableArrayList(templates));
 
        templateList.setCellFactory(new Callback<ListView<String>, 
            ListCell<String>>() {
                @Override 
                public ListCell<String> call(ListView<String> list) {
                    return new TemplateCell();
                }
            }
        );
        
		templateList.setOnMouseClicked((MouseEvent mouseEvent) -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    selectAction();
                }
            }
        });
        
		templateList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        templateList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            File template = new File(new_val);
            Model model = Model.load(template);
            previewImage.setImage(new Image("file:" + template.getParent() + "/" + model.getImagePreview()));
            nameText.setText(model.getName());
        });
	}
	
	static class TemplateCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (
                item != null) {
                
                File template = new File(item);
                Model model = Model.load(template);
                
				Label label = new Label(model.getName() != null ? model.getName() : item);
                
				setGraphic(label);
            }
        }
    }
}
