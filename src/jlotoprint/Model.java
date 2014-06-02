package jlotoprint;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.HashMap;
import jlotoprint.MarkInfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Marcel.Barbosa
 */
public class Model {

	@Expose
	private String image = "/jlotoprint/lotofacil.png";
	@Expose
	private double imageWidth = 644;
	@Expose
	private double imageHeight = 1095;
	@Expose
	private int dpi = 200;

	@Expose
	public HashMap<String, ArrayList<MarkInfo>> groupMap = new HashMap<>();

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public double getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(double imageWidth) {
		this.imageWidth = imageWidth;
	}

	public double getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(double imageHeight) {
		this.imageHeight = imageHeight;
	}

	public HashMap<String, ArrayList<MarkInfo>> getGroupMap() {
		return groupMap;
	}

	public void setGroupMap(HashMap<String, ArrayList<MarkInfo>> groupMap) {
		this.groupMap = groupMap;
	}

	public int getDpi() {
		return dpi;
	}

	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

	public Model() {

	}
}
