/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import jlotoprint.model.Model;
import jlotoprint.model.MarkInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jlotoprint.model.Template;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Marcel.Barbosa
 */
public class LotoPanel extends Pane {

	private double originX;
	private double originY;
	public Boolean isEditEnabled = true;
    
	public Model model = new Model();
	
	
	//BEGIN selection value
	public ObjectProperty<MarkInfo> selectionProperty() { return selection; }
    private ObjectProperty<MarkInfo> selection = new SimpleObjectProperty<MarkInfo>(this, "selection") {
        MarkInfo oldValue;
        
        @Override protected void invalidated() {
            super.invalidated();
            MarkInfo newValue = get();
            
            if ((oldValue == null && newValue != null) ||
                    oldValue != null && ! oldValue.equals(newValue)) {
                valueInvalidated();
            }
            
            oldValue = newValue;
        }
    };
    public final void setSelection(MarkInfo value) { selectionProperty().set(value); }
    public final MarkInfo getSelection() { return selectionProperty().get(); }
	void valueInvalidated() {
        fireEvent(new ActionEvent());
    }
	//END selection value
	
	public LotoPanel(Boolean isEditEnabled) {
		this.isEditEnabled = isEditEnabled;
		double w = model.getImageWidth();
		double h = model.getImageHeight();
		String size = w + "px " + h + "px";
		String image = "file:" + Template.getTemplateDir() + Template.getTemplateFile().getParentFile().getName() + "/" + (isEditEnabled ? model.getImagePreview() : model.getImage());
		System.out.println(image);
		setStyle("-fx-border-color:red; -fx-background-color: #00ffaa; -fx-background-repeat: no-repeat; -fx-background-image: url(\"" + image + "\"); -fx-background-size:contain");
		setMinSize(w, h);
		setMaxSize(w, h);
	}
	
	public Rectangle createRect(final MarkInfo m) {
		Rectangle rec = new Rectangle();
		
		rec.setFill(isEditEnabled ? (m.getType().equals("numberCount") ? Color.RED : m.getColor()) : Color.TRANSPARENT);
		rec.setId(m.getId());
		rec.setTranslateX(m.getX());
		rec.setTranslateY(m.getY());
		rec.setWidth(m.getWidth());
		rec.setHeight(m.getHeight());
		if (isEditEnabled) {
			rec.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					if (e.isSecondaryButtonDown()) {
						Node target = ((Node) e.getTarget());
						String id = target.getId();
						for (Entry<String, ArrayList<MarkInfo>> list : model.getGroupMap().entrySet()) {
							for (MarkInfo m : list.getValue()) {
								if (m.getId() == id) {
									getChildren().remove(target);
									list.getValue().remove(m);
								}
							}
						}
					}
					else{
						setSelection(m);
					}
				}
			});
			rec.setOnMousePressed(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					Rectangle target = ((Rectangle) e.getTarget());
					originX = e.getX() - target.getX();
					originY = e.getY() - target.getY();
					target.setStyle("-fx-border-color:red");
				}
			});
			rec.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					Rectangle target = ((Rectangle) e.getTarget());
					target.setStyle("-fx-border-color:black");
				}
			});
			rec.setOnMouseDragged(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {

					Rectangle target = ((Rectangle) e.getTarget());
					Double targetWidth = target.getBoundsInLocal().getWidth();
					Double targetHeight = target.getBoundsInLocal().getHeight();

					Point2D localPoint = sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));

					Integer posX = (int) Math.min(Math.max((localPoint.getX() - originX), 0), getWidth() - targetWidth);
					Integer posY = (int) Math.min(Math.max((localPoint.getY() - originY), 0), getHeight() - targetHeight);
					//target.relocate(posX, posY);
					target.setTranslateX(posX);
					target.setTranslateY(posY);

					//update model
					m.setX(target.getTranslateX());
					m.setY(target.getTranslateY());
					e.consume();
				}
			});
		}
		m.setRec(rec);
		return rec;
	}
	public void onShowProperties(final MarkInfo m){
		
	}
	public void createMark(String groupName, String type, String toggleValue) {
		String id = UUID.randomUUID().toString();
		MarkInfo m = new MarkInfo(id, groupName, type, toggleValue, 0, 0, 16, 10);
		createRect(m);
		
		if(type.equals("numberCount")){
			model.getNumberCountMap().add(m);
		}
		else{
			if (model.getGroupMap().get(m.getGroup()) == null) {
				model.getGroupMap().put(m.getGroup(), new ArrayList());
			}
			model.getGroupMap().get(m.getGroup()).add(m);
		}
		getChildren().add(m.getRec());
	}

	public void createMarkFromInfo(HashMap<String, ArrayList<MarkInfo>> map) {
		for (Entry<String, ArrayList<MarkInfo>> list : map.entrySet()) {
			createMarkFromInfo(list.getValue());
		}
	}
	public void createMarkFromInfo(ArrayList<MarkInfo> markList) {
		for (MarkInfo m : markList) {
			createRect(m);
			getChildren().add(m.getRec());
		}
	}

	public String getJsonModel() {
		Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return g.toJson(model);
	}

	void render(ArrayList<String[]> data) throws Exception {
		Boolean isValid = true;
		int size = 0;
		int i = 0;
		groupMap:
		for (ArrayList<MarkInfo> group : model.getGroupMap().values()) {
			for (MarkInfo m : group) {
				if (i < data.size()) {
					if(i > 0 && data.get(i).length != data.get(i - 1).length){
						isValid = false;
						break groupMap;
					}
					else{
						size = data.get(i).length;
						m.render(data.get(i));
					}
				}
			}
			i++;
		}
		if(!isValid){
			throw new Exception("Numbers has diferent sizes.");
		}
		else{
			for (MarkInfo m : model.getNumberCountMap()) {
				m.render(size);
			}
		}
	}

	public void importMarks() {
		model = Model.load(Template.getTemplateFile());
        System.out.println("-----------------NumberCount:" + model.getNumberCountMap().toArray().length);
		createMarkFromInfo(model.getGroupMap());
		System.out.println("-----------------NumberCount:" + model.getNumberCountMap().toArray().length);
        createMarkFromInfo(model.getNumberCountMap());
	}
}
