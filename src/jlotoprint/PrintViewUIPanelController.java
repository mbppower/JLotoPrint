/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintResolution;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 *
 * @author Marcel.Barbosa
 */
public class PrintViewUIPanelController implements Initializable {

	private List<Pane> pageList = new ArrayList<>();
	private int currentPage = -1;
	
	@FXML
	public Pagination pagination;			

	@FXML
	public GridPane gridContainer;
	
	@FXML
	public Label pageInfoLabel;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
	
		pagination.setPageFactory(new Callback<Integer, Node>() {
			@Override
			public Node call(Integer pageIndex) {
				if (pageIndex >= pageList.size()) {
					return null;
				}
				else {
					return createPage(pageIndex);
				}
			}
		});
	}

	public List<String> readTextFileAsList(File file) {
		List<String> lines = new ArrayList<>();
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (IOException ex) {
		}

		return lines;
	}

	public void renderLotoPanel() throws Exception {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a source");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File sourceFile = fileChooser.showOpenDialog(JLotoPrint.stage.getOwner());

		if (sourceFile != null) {
			List<String> lines = readTextFileAsList(sourceFile);
			int groupCount = 2;
			int count = 0;
			ArrayList<ArrayList> groupDataList = new ArrayList<>();
			ArrayList<String[]> data = new ArrayList<>();
			for (String line : lines) {
				if (count % groupCount == 0) {
					data = new ArrayList<>();
					groupDataList.add(data);
				}
				data.add(line.split(" - "));
				count++;
			}

			int colCount = 2;
			int rowCount = 1;
			int currentCol = 0;
			int currentRow = 0;

			//GridPane gridContainer = new GridPane();
			
			TilePane container = new TilePane(rowCount, colCount);
			
			pageList = new ArrayList<>();
			pageList.add(getNewPage(container));
			System.out.println("---------------- new page");
			
			for (ArrayList groupData : groupDataList) {
				
				if (currentCol > 0 && currentCol % colCount == 0) {
					currentRow++;
					currentCol = 0;
					if (currentRow > rowCount) {
						currentRow = 0;
						container = new TilePane(rowCount, colCount);
						System.out.println("---------------- new page");
						pageList.add(getNewPage(container));
					}
				}
				LotoPanel lotoPanel = new LotoPanel(false);
				lotoPanel.importMarks();
				
				lotoPanel.render(groupData);
				container.getChildren().add(lotoPanel);
				
				System.out.println("groupData: " + groupData.size() + " width: " + container.getBoundsInLocal().getWidth());
				
				currentCol++;
			}
			pagination.setPageCount(pageList.size());
			pagination.setCurrentPageIndex(0);
		}
	}
	private Pane createPage(Integer pageIndex) {	
		//HBox pageContainer = new HBox();
		Pane page = pageList.get(pageIndex);
		page.setScaleX(.2);
		page.setScaleY(.2);
		//HashMap<String, Double> result = resizeProportional(page.getBoundsInLocal().getWidth(), page.getBoundsInLocal().getHeight(), pagination.getBoundsInLocal().getWidth(), pagination.getBoundsInLocal().getHeight(), true);
		//page.getTransforms().clear();
		//page.getTransforms().add(new Scale(result.get("scaleX"), result.get("scaleY")));
		return page;
	}
	
	private Pane getNewPage(Pane container) {
		//Node page = new Shape();
		//A4 paper size
		//-fx-min-width:210mm; -fx-min-height:297mm;
		container.setStyle("-fx-background-color: red; -fx-border-width: 1px; -fx-border-color:black;");
		container.setMinWidth(1300.0);
		container.setMaxWidth(1300.0);
		return container;
	}
		
	@FXML
	private void handleLoadGamesAction(ActionEvent event) {
		try {
			renderLotoPanel();
		} catch (Exception ex) {
			Logger.getLogger(PrintViewUIPanelController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@FXML
	private void handlePrintAction(ActionEvent event) {
		print();
	}

	public void print() {

		Printer printer = Printer.getDefaultPrinter();
		double margin = 10.0;
		PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, margin, margin, margin, margin);
		
		PrinterJob job = PrinterJob.createPrinterJob();
		PrintResolution resolution = job.getJobSettings().getPrintResolution();
		
		if (job.showPrintDialog(JLotoPrint.stage.getOwner())) {
			try {
				for (Node node : pageList) {			
					node.getTransforms().clear();
					
					System.out.println("node width: " + node.getBoundsInLocal().getWidth());
					System.out.println("node height: " + node.getBoundsInLocal().getHeight());
					System.out.println(pageLayout.getPrintableWidth());
					System.out.println(pageLayout.getPrintableHeight());
					
					HashMap<String, Double> result = resizeProportional(node.getBoundsInLocal().getWidth(), node.getBoundsInLocal().getHeight(), pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight(), true);
					node.setScaleX(1);
					node.setScaleY(1);
					//node.getTransforms().add(new Scale(result.get("scaleX"), result.get("scaleY")));
					
					node.getTransforms().add(new Scale(.3, .3));
					job.printPage(node);
					node.getTransforms().clear();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally {
				job.endJob();
			}
		}
	}
	
	public HashMap<String, Double> resizeProportional(double ow, double oh, double w, double h, Boolean dontResizeWhenSmall) {
		HashMap<String, Double> result = new HashMap<>();
		double ph = w * oh / ow, pw = h * ow / oh;
		//dont resize
		if((ow < w && oh < h) && dontResizeWhenSmall) {
			result.put("width", ow);
			result.put("height", oh);
			result.put("scaleX", 1.0);
			result.put("scaleY", 1.0);
		}
		else{
			double fh, fw;
			if(ph > h){
				fw = pw;
				fh = h;
			}
			else{
				fh = ph;
				fw = w;
			}
			result.put("width", fw);
			result.put("height", fh);
			result.put("scaleX", fw / ow);
			result.put("scaleY", fh / oh);
		}
		return result;
	}
}