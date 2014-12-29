/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint.model;

import com.google.gson.annotations.Expose;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Marcel.Barbosa
 */
public class MarkInfo {
	@Expose
	private String id;
	@Expose
	private String toggleValue;
	@Expose
	private String group;
	@Expose
	private String type;
	@Expose
	private double x;
	@Expose
	private double y;
	@Expose
	private double width;
	@Expose
	private double height;
	
	private Rectangle rec;
	
	public MarkInfo(String id, String group, String type, String toggleValue, double x, double y, double width, double height) {
		this.id = id;
		this.group = group;
		this.type = type;
		this.toggleValue = toggleValue;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	void toggle() {
		if(rec != null){
			rec.setFill(Color.BLACK);
		}
	}

	public void render(String[] n) {
		for(String v : n){
			if(v.equals(toggleValue)){
				//System.out.println("v: " + v + " - " + toggleValue);
				toggle();
			}
		}
	}
	
	public void render(int size) {
		
		System.out.println(size +"---------"+ Integer.parseInt(toggleValue));
		
		if(size == Integer.parseInt(toggleValue)){
			toggle();
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToggleValue() {
		return toggleValue;
	}

	public void setToggleValue(String toggleValue) {
		this.toggleValue = toggleValue;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Rectangle getRec() {
		return rec;
	}

	public void setRec(Rectangle rec) {
		this.rec = rec;
	}
}
