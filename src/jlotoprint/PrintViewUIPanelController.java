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
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintResolution;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 *
 * @author Marcel.Barbosa
 */
public class PrintViewUIPanelController implements Initializable {

	private List<Pane> pageList = new ArrayList<>();
	private int currentPage = -1;
	@FXML
	public Pane pageContainer;

	@FXML
	public GridPane gridContainer;
	
	@FXML
	public Label pageInfoLabel;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		updatePageInfo();
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

	public void renderLotoPanel() {

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

			pageContainer.setScaleX(.5);
			pageContainer.setScaleY(.5);

			int colCount = 2;
			int rowCount = 1;
			int currentCol = 0;
			int currentRow = 0;

			GridPane gridContainer = new GridPane();
			pageContainer.getChildren().clear();
			pageList = new ArrayList<>();
			pageList.add(getNewPage(gridContainer));

			for (ArrayList groupData : groupDataList) {

				LotoPanel lotoPanel = new LotoPanel(false);
				lotoPanel.importMarks();

				System.out.println("groupData: " + groupData.size());
				lotoPanel.render(groupData);

				if (currentCol > 0 && currentCol % colCount == 0) {
					currentRow++;
					currentCol = 0;
					if (currentRow > rowCount) {
						currentRow = 0;
						gridContainer = new GridPane();
						pageList.add(getNewPage(gridContainer));
					}
				}
				gridContainer.add(lotoPanel, currentCol, currentRow);
				currentCol++;
			}
			//pageContainer.getChildren().addAll(pageList);
			currentPage = -1;
			renderNextPage();
		}
	}

	private Pane getNewPage(GridPane gridContainer) {
		Pane page = new Pane();
		//A4 paper size
		page.setStyle("-fx-min-width:210mm; -fx-min-height:297mm; -fx-background-color: white; -fx-border-width: 1px; -fx-border-color:black;");
		page.setPadding(new Insets(10));
		//page.setPrefSize(2000, 2000);
		page.getChildren().add(gridContainer);
		return page;
	}
	private void renderNextPage(){
		if(pageList.size() > 0){
			if(++currentPage >= pageList.size()){
				currentPage = 0;
			}
			pageContainer.getChildren().clear();
			pageContainer.getChildren().add(pageList.get(currentPage));
			updatePageInfo();
		}
	}
	private void renderPreviousPage(){
		if(pageList.size() > 0){
			if(--currentPage < 0){
				currentPage = pageList.size() - 1;
			}
			pageContainer.getChildren().clear();
			pageContainer.getChildren().add(pageList.get(currentPage));
			updatePageInfo();
		}
	}
	@FXML
	private void handleNextAction(ActionEvent event) {
		renderNextPage();
	}
	private void updatePageInfo() {
		pageInfoLabel.setText((pageList.size() > 0 ? (currentPage + 1) : 0) + "/" + pageList.size());
	}
	@FXML
	private void handlePreviousAction(ActionEvent event) {
		renderPreviousPage();
	}
	
	@FXML
	private void handleLoadGamesAction(ActionEvent event) {
		renderLotoPanel();
	}
	
	@FXML
	private void handlePrintAction(ActionEvent event) {
		print();
	}

	public void print() {

		Printer printer = Printer.getDefaultPrinter();
		PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

		PrinterJob job = PrinterJob.createPrinterJob();
		PrintResolution resolution = job.getJobSettings().getPrintResolution();
		
		if (job.showPrintDialog(JLotoPrint.stage.getOwner())) {
			try {
				
				for (Node node : pageList) {
					double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInLocal().getWidth();
					double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInLocal().getHeight();
					node.getTransforms().add(new Scale(scaleX, scaleY));
					job.printPage(node);
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
}
