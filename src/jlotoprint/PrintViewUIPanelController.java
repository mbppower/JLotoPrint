/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jlotoprint;

import java.awt.image.BufferedImage;
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
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrintResolution;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import jlotoprint.model.Model;
import jlotoprint.model.Template;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

/**
 *
 * @author Marcel.Barbosa
 */
public class PrintViewUIPanelController implements Initializable {

	private List<Pane> pageList = new ArrayList<>();
	private int currentPage = -1;
	private Pagination pagination;
	
    @FXML
	public VBox initialContent;
    
	@FXML
	public AnchorPane paginationContainer;			

	@FXML
	public GridPane gridContainer;
	
	@FXML
	public Label pageInfoLabel;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
        initialContent.setOpacity(0.0);
        initialContent.setVisible(true);
        
        //fade
        FadeTransition ft = new FadeTransition(Duration.millis(2000), initialContent);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setCycleCount(1);
        ft.setAutoReverse(true);
        ft.play();
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
        Model model = Template.load(true);
        
		if (sourceFile != null && model != null) {
            try{

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
                    data.add(line.split("-"));
                    count++;
                }

                int colCount = 2;
                int rowCount = 1;
                int currentCol = 0;
                int currentRow = 0;

                GridPane container = new GridPane();
                
                
                pageList = new ArrayList<>();
                pageList.add(getNewPage(container));
                System.out.println("---------------- new page");

                for (ArrayList groupData : groupDataList) {

                    if (currentCol > 0 && currentCol % colCount == 0) {
                        currentRow++;
                        currentCol = 0;
                        if (currentRow > rowCount) {
                            currentRow = 0;
                            container = new GridPane();
                            System.out.println("---------------- new page");
                            pageList.add(getNewPage(container));
                        }
                    }
                    LotoPanel lotoPanel = new LotoPanel(model, false);
                    lotoPanel.loadTemplate();

                    lotoPanel.render(groupData);
                    container.add(lotoPanel, currentCol, currentRow);

                    System.out.println("groupData: " + groupData.size() + " width: " + container.getBoundsInLocal().getWidth());

                    currentCol++;
                }

                //clear previous content
                paginationContainer.getChildren().clear();
                pagination = null;

                //create pagination component
                pagination = new Pagination(pageList.size(), 0);
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

                AnchorPane.setTopAnchor(pagination, 0.0);
                AnchorPane.setLeftAnchor(pagination, 0.0);
                AnchorPane.setRightAnchor(pagination, 0.0);
                AnchorPane.setBottomAnchor(pagination, 0.0);
                paginationContainer.getChildren().add(pagination);
                
            }
            catch (Exception ex) {
                Logger.getLogger(PrintViewUIPanelController.class.getName()).log(Level.SEVERE, null, ex);
            }
		}
	}
	private Pane createPage(Integer pageIndex) {	
        
		Pane page = pageList.get(pageIndex);
		page.needsLayoutProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            double pageWidth = page.getLayoutBounds().getWidth();
            double pageHeight = page.getLayoutBounds().getHeight();
            double margin = 60.0;
            HashMap<String, Double> result = resizeProportional(pageWidth, pageHeight, pagination.getBoundsInLocal().getWidth() - margin, pagination.getBoundsInLocal().getHeight() - margin, true);
            page.setScaleX(result.get("scaleX"));
            page.setScaleY(result.get("scaleY"));
        });
		
		//center page
		GridPane.setHgrow(page, Priority.NEVER);
		GridPane.setVgrow(page, Priority.NEVER);
		
		GridPane centerGrid = new GridPane();
		centerGrid.setAlignment(Pos.TOP_CENTER);
		centerGrid.add(page, 0, 0);
        
		return centerGrid;
	}
	
	private Pane getNewPage(GridPane container) {
		container.setStyle("-fx-background-color: blue; -fx-border-width: 2px; -fx-border-color:orange;");
		
		//A4 paper size
		//-fx-min-width:210mm; -fx-min-height:297mm;
		container.setMinHeight(2200.0);
		container.setMinWidth(1300.0);
		container.setGridLinesVisible(true);
		container.setAlignment(Pos.TOP_LEFT);

		return container;
	}
		
	@FXML
	private void handleLoadGamesAction(ActionEvent event) {
        if(Template.isLoaded()){
            renderLotoPanel();
            initialContent.setVisible(false);
        }
        else{
            final Stage templateChooser = MainViewController.loadTemplateChooser();
            if(templateChooser != null){
                templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
                    templateChooser.close();
                    Template.load(true);
                    renderLotoPanel();
                    initialContent.setVisible(false);
                });
            }
        }
	}
	
	@FXML
	private void handlePrintAction(ActionEvent event) {
		print();
	}
    
    @FXML
	private void handleExportPDFAction(ActionEvent event) {
		exportToPDF();
	}
    
	public void print() {
		Printer printer = Printer.getDefaultPrinter();
        if(printer == null){
            System.out.println("No printer found, please add a printer");
        }
        else{
            double margin = 20.0;
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

            PrinterJob job = PrinterJob.createPrinterJob();
            PrintResolution resolution = job.getJobSettings().getPrintResolution();

            if (job.showPrintDialog(JLotoPrint.stage.getOwner())) {
                try {
                    for (Pane node : pageList) {			
                        node.getTransforms().clear();

                        HashMap<String, Double> result = resizeProportional(node.getBoundsInLocal().getWidth(), node.getBoundsInLocal().getHeight(), pageLayout.getPrintableWidth() , pageLayout.getPrintableHeight(), true);

                        node.setScaleX(1);
                        node.setScaleY(1);
                        node.getTransforms().add(new Translate(0.0, 0.0));
                        node.getTransforms().add(new Scale(result.get("scaleX"), result.get("scaleY")));
                            
                        job.printPage(node);
                        node.getTransforms().clear();
                        node.requestLayout();
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
	
	public HashMap<String, Double> resizeProportional(double ow, double oh, double w, double h, Boolean dontResizeWhenSmaller) {
		
		HashMap<String, Double> result = new HashMap<>();
		double ph = w * oh / ow, pw = h * ow / oh;
		//dont resize
		if((ow < w && oh < h) && dontResizeWhenSmaller) {
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
    
    public void exportToPDF(){
        
        FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a destination");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF File", "*.pdf"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("PDF Export");
		File sourceFile = fileChooser.showSaveDialog(JLotoPrint.stage.getOwner());

		if (sourceFile != null) {

            PDDocument doc = null;
            PDPage page = null;
            PDPageContentStream content = null;

            try {
                doc = new PDDocument();
                
                for (Pane node : pageList) {	

                    page = new PDPage();
                    doc.addPage(page);
                    
                    PDRectangle mediaBox = page.getMediaBox();
                    float pageWidth = mediaBox.getWidth();
                    float pageHeight = mediaBox.getHeight();
                    
                    node.getTransforms().clear();
                    HashMap<String, Double> result = resizeProportional(node.getBoundsInLocal().getWidth(), node.getBoundsInLocal().getHeight(), pageWidth , pageHeight, true);
                    node.setScaleX(1);
                    node.setScaleY(1);
                    node.getTransforms().add(new Translate(0.0, 0.0));
                    node.getTransforms().add(new Scale(result.get("scaleX"), result.get("scaleY")));
                    
                    //get node image
                    WritableImage nodeImage = node.snapshot(null, null);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(nodeImage, null);

                    //set page content
                    content = new PDPageContentStream(doc, page);
                    content.drawImage(new PDJpeg(doc, bufferedImage), 100, 100);
                    content.close();
                    node.getTransforms().clear();
                    node.requestLayout();
                }
                doc.save(sourceFile);
                
                //sucess
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF document sucessfully exported.", ButtonType.OK);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
            }
            catch(Exception ex) {
               MainViewController.showExceptionAlert("Error exporting the PDF document", ex.getStackTrace().toString());
            }
            finally {
                try {
                    if (content != null) { content.close(); }
                    if (doc != null) { doc.close(); }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}