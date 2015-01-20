/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.io.Console;
import jlotoprint.model.Model;
import jlotoprint.model.MarkInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jlotoprint.model.Template;

/**
 *
 * @author Marcel.Barbosa
 */
public class LotoPanel extends Pane {

	private double originX;
	private double originY;
    private int numberCount;

    public int getNumberCount() {
        return numberCount;
    }
	public Boolean isEditEnabled = true;
    
	private Model model;

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
    public final void setSelection(MarkInfo value) {
        
        MarkInfo oldValue = selectionProperty().getValue();
        if(oldValue != null){
            oldValue.getRect().setStyle("-fx-stroke-width: 0");
        }
        if(value != null){
            value.getRect().setStyle("-fx-stroke: black;" +
                "-fx-stroke-type: outside; " +
                "-fx-stroke-width: 2;" +
                "-fx-stroke-dash-array: 2 2 2 2;" +
                "-fx-stroke-dash-offset: 4;" +
                "-fx-stroke-line-cap: butt;");
        }
        selectionProperty().set(value);
    }
    public final MarkInfo getSelection() { return selectionProperty().get(); }
	void valueInvalidated() {
        fireEvent(new ActionEvent());
    }
	//END selection value
	
	public LotoPanel(Model model, Boolean isEditEnabled) {
        this.model = model;
		this.isEditEnabled = isEditEnabled;
		setImage();
	}
    
    public final void setImage(){
        double w = model.getImageWidth();
		double h = model.getImageHeight();
		String size = w + "px " + h + "px";
		String image = Template.getTemplateDir() + Template.getTemplateFile().getParentFile().getName() + "/" + (isEditEnabled ? model.getImagePreview() : model.getImage());
        File imageFile = new File(image);
        if(imageFile.exists()){
            setStyle("-fx-border-color:black;");
            setBackground(new Background(new BackgroundImage(new Image("file:" + image), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        }
        setMinSize(w, h);
		setMaxSize(w, h);
    }
	
	public Rectangle createRect(final MarkInfo m) {
		Rectangle rect = new Rectangle();
		
		rect.setFill(isEditEnabled ? (m.getType().equals(Model.NUMBER_COUNT_TYPE) ? Color.RED : m.getColor()) : Color.TRANSPARENT);
		rect.setId(m.getId());
		rect.setTranslateX(m.getX());
		rect.setTranslateY(m.getY());
		rect.setWidth(m.getWidth());
		rect.setHeight(m.getHeight());
        
		if (isEditEnabled) {
			rect.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					setSelection(m);
				}
			});
			rect.setOnMousePressed(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					Rectangle target = ((Rectangle) e.getTarget());
					originX = e.getX() - target.getX();
					originY = e.getY() - target.getY();
					target.setStyle("-fx-border-color:red");
				}
			});
			rect.setOnMouseReleased(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					Rectangle target = ((Rectangle) e.getTarget());
					target.setStyle("-fx-border-color:black");
				}
			});
			rect.setOnMouseDragged(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {

					Rectangle target = ((Rectangle) e.getTarget());
					Double targetWidth = target.getBoundsInLocal().getWidth();
					Double targetHeight = target.getBoundsInLocal().getHeight();

					Point2D localPoint = sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));

					Integer posX = (int) Math.min(Math.max((localPoint.getX() - originX), 0), getWidth() - targetWidth);
					Integer posY = (int) Math.min(Math.max((localPoint.getY() - originY), 0), getHeight() - targetHeight);
					
					target.setTranslateX(posX);
					target.setTranslateY(posY);

					//update model
					m.setX(target.getTranslateX());
					m.setY(target.getTranslateY());
					e.consume();
				}
			});
		}
		m.setRect(rect);
		return rect;
	}

	public MarkInfo createMark(String groupName, String type, String toggleValue) {
		String id = UUID.randomUUID().toString();
		MarkInfo m = new MarkInfo(id, groupName, type, toggleValue, 0, 0, 16, 10);
		createRect(m);
		
		if(type.equals(Model.NUMBER_COUNT_TYPE)){
			model.getNumberCountMap().add(m);
		}
		else{
			if (model.getGroupMap().get(m.getGroup()) == null) {
				model.getGroupMap().put(m.getGroup(), new ArrayList());
			}
			model.getGroupMap().get(m.getGroup()).add(m);
		}
		getChildren().add(m.getRect());
        
        return m;
	}
    
    public void deleteMark(MarkInfo markInfo){
        if(markInfo != null){
            if(markInfo.equals(getSelection())){
                setSelection(null);
            }
            String id = markInfo.getId();

            //remove from group list
            for (Entry<String, ArrayList<MarkInfo>> list : model.getGroupMap().entrySet()) {
                for (MarkInfo m : list.getValue()) {
                    if (m.getId() == id) {
                        getChildren().remove(markInfo.getRect());
                        list.getValue().remove(m);
                        break;
                    }
                }
            }
            //remove from option list
            ArrayList<MarkInfo> list = model.getNumberCountMap();
            for (MarkInfo m : list) {
                if (m.getId() == id) {
                    getChildren().remove(markInfo.getRect());
                    list.remove(m);
                    break;
                }
            }
        }
    }
    
	public void createMarkFromInfo(HashMap<String, ArrayList<MarkInfo>> map) {
		for (Entry<String, ArrayList<MarkInfo>> list : map.entrySet()) {
			createMarkFromInfo(list.getValue());
		}
	}
	public void createMarkFromInfo(ArrayList<MarkInfo> markList) {
		for (MarkInfo m : markList) {
			createRect(m);
			getChildren().add(m.getRect());
		}
	}

	void render(ArrayList<String[]> data) throws Exception {
		int size = 0;
		int i = 0;
		for (ArrayList<MarkInfo> group : model.getGroupMap().values()) {
			int toggledSize = 0;
            if (i < data.size()) {
                for (MarkInfo m : group) {
					if(i > 0 && data.get(i).length != data.get(i - 1).length){
						throw new Exception("Size mismatch: " + Arrays.toString(data.get(i)) + " != " + Arrays.toString(data.get(i - 1)));
					}
					else{
						size = data.get(i).length;
						m.render(data.get(i));
                        if(m.getToggled()){
                            toggledSize++;
                        }
					}
        		}
                if(size > 0 && toggledSize != size){
                    throw new Exception("Number mismatch.\r\nExpected size: " + size + "\r\nActual size: " + toggledSize + "\r\nGame: " + Arrays.toString(data.get(i)));
                }
            }
			i++;
		}
        
        numberCount = size;
        for (MarkInfo m : model.getNumberCountMap()) {
            m.render(size);
        }
	}

	public void loadTemplate() {
        //for groups
        createMarkFromInfo(model.getGroupMap());
        //for options
        createMarkFromInfo(model.getNumberCountMap());
	}
}
