/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import jlotoprint.model.MarkInfo;
import jlotoprint.model.Template;
import org.controlsfx.control.PopOver;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 *
 * @author Marcel.Barbosa
 */
public class TemplateDesignerEditorController implements Initializable {
    @FXML
	public Pane editorContainer;
	@FXML
	public Pane imageContainer;
    @FXML
    public Label zoomLevel;
	@FXML
	public ComboBox groupCombo;
	@FXML
	public TextField currentSelection;
	@FXML
	public ComboBox typeCombo;
	@FXML
	public TextField markValue;
	@FXML
	public Slider zoomBar;
    @FXML
    public TextField templateName;
    @FXML
    public TextField templateImage;
    @FXML
    public TextField templateImagePreview;
    
	public static double ZOOM = .5;

	public LotoPanel lotoPanel;
    public ObservableList<String> groupObservableList;
    
    @FXML
    public Button groupEditButton;
    @FXML
	public Button imagePickButton;
    @FXML
	public Button imagePreviewPickButton;
    @FXML
	public Button addMarkButton;
    @FXML
	public Button deleteMarkButton;
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        //icons
        imagePickButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.FOLDER_ALT));
        imagePreviewPickButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.FOLDER_ALT));
        groupEditButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PENCIL));
        
        addMarkButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.PLUS_SQUARE_ALT));
        deleteMarkButton.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.MINUS_SQUARE_ALT));
        
        loadPanel();
	}
	@FXML
	private void handleAddItemAction(ActionEvent event) {
		String groupName = groupCombo.getValue().toString();
		String typeName = typeCombo.getValue().toString();
		lotoPanel.createMark(groupName, typeName, markValue.getText());
	}

    @FXML
	private void handleImageSelectAction(ActionEvent event) {
        File imageRef = showImageSelection("image_default");
        if(imageRef != null){
            templateImage.setText(imageRef.getName());
            lotoPanel.setImage();
        }
        
    }
    @FXML
	private void handleImagePreviewSelectAction(ActionEvent event) {
        File imageRef = showImageSelection("image_preview");
        if(imageRef != null){
            templateImagePreview.setText(imageRef.getName());
            lotoPanel.setImage();
        }
    }
    
	private File showImageSelection(String imageName) {
        File imageFile = null;
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Image", new String[]{"*.png","*.jpg","*.jpeg"});
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(JLotoPrint.stage);
        if(file != null){
            String fileName = file.getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            String targetPath = Template.getTemplateFile().getParent() + "/" + imageName + "." + fileExtension;
            try {
                Files.copy(Paths.get(file.getPath()), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
                imageFile = new File(targetPath);
            }
            catch (IOException ex) {
                MainViewController.showExceptionAlert(ex.getMessage(), ex.getStackTrace().toString());
            }
        }
        return imageFile;
    }
    
    @FXML
	private void handleOpenGroupListAction(ActionEvent event) {
        
        //content
        ListView<String> listView = new ListView<>(groupObservableList);
        listView.setEditable(true);
        listView.setPrefWidth(200);
        listView.setPrefHeight(300);
        listView.setPadding(new Insets(10));
        listView.setCellFactory(TextFieldListCell.forListView());
        
        listView.setOnEditCommit((ListView.EditEvent<String> target) -> {
            String name = target.getNewValue();
            if(listView.getItems().contains(name)){
                name = UUID.randomUUID().toString();
            }
            listView.getItems().set(target.getIndex(), name);
        });
        
        //add
        Button addButton = new Button("Add new", new Glyph("FontAwesome", FontAwesome.Glyph.PLUS_SQUARE_ALT));
        addButton.setPadding(new Insets(5));
        addButton.setDefaultButton(true);
        addButton.setOnAction((ActionEvent actionEvent) -> {
            String name = "Group_" + (listView.getItems().size() + 1);
            if(listView.getItems().contains(name)){
                name = UUID.randomUUID().toString();
            }
            listView.getItems().add(name);
        });
        
        //remove
        Button removeButton = new Button("Remove selected", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS_SQUARE_ALT).color(Color.RED));
        removeButton.setPadding(new Insets(5));
        removeButton.setDefaultButton(false);
        removeButton.setOnAction((ActionEvent actionEvent) -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if(selectedIndex > -1)
                listView.getItems().remove(selectedIndex);
        });
        
        //top
        VBox vbox = new VBox(new Label("Available groups:"), listView);
        vbox.setPadding(new Insets(5, 5, 0, 5));
        vbox.setSpacing(5);
        
        //bottom
        HBox hBox = new HBox(removeButton, addButton);
        hBox.setPadding(new Insets(5));
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER);
        
        //popover
        PopOver popOver = new PopOver(BorderPaneBuilder.create().center(vbox).bottom(hBox).build());
        popOver.setStyle("-fx-base:#ccc;");
        popOver.setAutoHide(true);
        popOver.show((Node)event.getSource());
    }
    
	public void loadPanel(){
         
        //nodes
		typeCombo.setItems(FXCollections.observableArrayList(
            "option",
            "numberCount"
		));
		typeCombo.setValue(typeCombo.getItems().get(0));
        imageContainer.setOnScroll((ScrollEvent event) -> {
            double oldValue = zoomBar.getValue();
            double direction = event.getDeltaY() < 0 ? -1.0 : 1.0;
            zoomBar.setValue(oldValue + ((1 - oldValue) / 10.0 * direction));
        });
		zoomBar.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            ZOOM = newValue.doubleValue() / 100;
            zoomLevel.setText(((int)newValue.doubleValue()) + "%");
            imageContainer.setScaleX(ZOOM);
            imageContainer.setScaleY(ZOOM);
		});
        zoomBar.setValue(ZOOM * 100);
        
        //lotoPanel
        lotoPanel = new LotoPanel(Template.getModel(), true);
		lotoPanel.selectionProperty().addListener((ObservableValue<? extends MarkInfo> observable, MarkInfo oldValue, MarkInfo newValue) -> {
            if (newValue != null) {
                currentSelection.setText(newValue.getId());
                groupCombo.setValue(newValue.getGroup());
                typeCombo.setValue(newValue.getType());
                markValue.setText(newValue.getToggleValue());
            }
		});
        lotoPanel.loadTemplate();
        
		groupCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				MarkInfo m = lotoPanel.getSelection();
				if (m != null) {
					m.setGroup(newValue);
				}
			}
		});
		typeCombo.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				MarkInfo m = lotoPanel.getSelection();
				if (m != null) {
					m.setType(newValue);
				}
			}
		});
		markValue.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            MarkInfo m = lotoPanel.getSelection();
            if (m != null) {
                m.setToggleValue(newValue);
            }
		});

         //template 
        groupObservableList = FXCollections.observableArrayList(Template.getModel().getGroupList());
        groupObservableList.addListener((ListChangeListener.Change<? extends String> c) -> {
            Template.getModel().setGroupList(new ArrayList<>(groupObservableList));
            groupCombo.setItems(groupObservableList);
        });
        groupCombo.setItems(groupObservableList);
		groupCombo.setValue(groupCombo.getItems().get(0));
        
        templateName.setText(Template.getModel().getName());
        templateName.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            Template.getModel().setName(newValue);
		});
        templateImage.setText(Template.getModel().getImage());
        templateImage.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			Template.getModel().setImage(newValue);
		});
        
        templateImagePreview.setText(Template.getModel().getImagePreview());
        templateImagePreview.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
			Template.getModel().setImagePreview(newValue);
		});

        imageContainer.getChildren().add(lotoPanel);
	}
}