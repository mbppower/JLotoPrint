/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
	
	public LotoPanel(Boolean isEditEnabled) {
		this.isEditEnabled = isEditEnabled;
		double w = model.getImageWidth();
		double h = model.getImageHeight();
		String size = w + "px " + h + "px";
		setStyle("-fx-border-color:red; -fx-background-color: #00ffaa; -fx-background-repeat: no-repeat; -fx-background-image: url(\"" + model.getImage() + "\"); -fx-background-size:contain");
		setMinSize(w, h);
		setMaxSize(w, h);
	}

	public Rectangle createRect(final MarkInfo m) {
		Rectangle rec = new Rectangle();
		rec.setFill(isEditEnabled ? Color.BLUE : Color.TRANSPARENT);
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

	public void createMark(String groupName, String type, String toggleValue) {
		String id = UUID.randomUUID().toString();
		MarkInfo m = new MarkInfo(id, groupName, type, toggleValue, 0, 0, 16, 10);
		createRect(m);
		if (model.getGroupMap().get(m.getGroup()) == null) {
			model.getGroupMap().put(m.getGroup(), new ArrayList());
		}
		model.getGroupMap().get(m.getGroup()).add(m);
		getChildren().add(m.getRec());
	}

	public void createMarkFromInfo(HashMap<String, ArrayList<MarkInfo>> map) {
		for (Entry<String, ArrayList<MarkInfo>> list : map.entrySet()) {
			for (MarkInfo m : list.getValue()) {
				createRect(m);
				getChildren().add(m.getRec());
			}
		}
	}

	public String getJsonModel() {
		Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return g.toJson(model);
	}

	void render(ArrayList<String[]> data) {
		int i = 0;
		for (ArrayList<MarkInfo> group : model.getGroupMap().values()) {
			for (MarkInfo m : group) {
				if(i < data.size()){
					m.render(data.get(i));
				}
			}
			i++;
		}
	}

	public void importMarks() {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(getClass().getResourceAsStream("defaultModel.json"), writer, "UTF-8");
			Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			model = g.fromJson(writer.toString(), Model.class);
			createMarkFromInfo(model.getGroupMap());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
