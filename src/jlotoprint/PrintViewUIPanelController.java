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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import jlotoprint.model.MarkInfo;
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

    private List<Group> pageList = new ArrayList<>();
    private Pagination pagination;

    int totalGames = 0;
    int totalTickets = 0;
    @FXML
    public TableView optionGroupTable;

    @FXML
    public AnchorPane paginationContainer;
    @FXML
    public TextField totalTicketsField;
    @FXML
    public TextField totalGamesField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        renderLotoPanel();
    }

    public List<String> readTextFileAsList(File file) {
        List<String> lines = new ArrayList<>();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
            br.close();
        } catch (IOException ex) {
        }

        return lines;
    }

    public ArrayList<Group> importPageList(File sourceFile, Model model) throws Exception {
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
            data.add(line.split(Model.NUMBER_SEPARATOR));
            count++;
        }

        totalGames = lines.size();
        totalTickets = groupDataList.size();
        HashMap<Integer, AtomicInteger> optionMap = new HashMap<>();

        int colCount = 2;
        int rowCount = 1;
        int currentCol = 0;
        int currentRow = 0;

        GridPane container = new GridPane();

        ArrayList<Group> pageList = new ArrayList<>();
        pageList.add(getNewPage(container));

        for (ArrayList groupData : groupDataList) {

            if (currentCol > 0 && currentCol % colCount == 0) {
                currentRow++;
                currentCol = 0;
                if (currentRow > rowCount) {
                    currentRow = 0;
                    container = new GridPane();
                    pageList.add(getNewPage(container));
                }
            }
            LotoPanel lotoPanel = new LotoPanel(model, false);
            lotoPanel.loadTemplate();
            lotoPanel.render(groupData);
            container.add(lotoPanel, currentCol, currentRow);
            currentCol++;

            //count
            Integer optionKey = lotoPanel.getNumberCount();
            optionMap.putIfAbsent(optionKey, new AtomicInteger(0));
            optionMap.get(optionKey).incrementAndGet();
        }

        optionGroupTable.getColumns().clear();
        TableColumn firstCol = new TableColumn("Number Count");
        TableColumn secondCol = new TableColumn("Tickets");

        optionGroupTable.getColumns().addAll(firstCol, secondCol);
        firstCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        secondCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        ArrayList<TableCellData> tableModel = new ArrayList<>();
        for (Map.Entry<Integer, AtomicInteger> t : optionMap.entrySet()) {
            tableModel.add(new TableCellData(t.getKey().toString(), t.getValue().toString()));
        }
        optionGroupTable.setItems(FXCollections.observableArrayList(tableModel));

        return pageList;
    }

    public void renderLotoPanel() {

        if (Template.getSourceFile() != null && Template.getModel() != null) {
            try {

                pageList = importPageList(Template.getSourceFile(), Template.getModel());

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
                        } else {
                            return createPage(pageIndex);
                        }
                    }
                });

                //add
                AnchorPane.setTopAnchor(pagination, 0.0);
                AnchorPane.setLeftAnchor(pagination, 0.0);
                AnchorPane.setRightAnchor(pagination, 0.0);
                AnchorPane.setBottomAnchor(pagination, 0.0);

                paginationContainer.getChildren().add(pagination);

                //render info
                totalTicketsField.setText(totalTickets + "");
                totalGamesField.setText(totalGames + "");
            }
            catch (Exception ex) {
                MainViewController.showExceptionAlert("Error on rendering tickets", ex.getMessage());
            }
        }
    }

    private Parent createPage(Integer pageIndex) {

        Group page = pageList.get(pageIndex);
        Group paper = new Group(page) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                //auto resize paper
                double pageWidth = page.getLayoutBounds().getWidth();
                double pageHeight = page.getLayoutBounds().getHeight();
                double margin = 100.0;
                HashMap<String, Double> result = resizeProportional(pageWidth, pageHeight, pagination.getWidth() - margin, pagination.getHeight() - margin, true);
                page.setScaleX(result.get("scaleX"));
                page.setScaleY(result.get("scaleY"));
            }
        };
        paper.setStyle("-fx-effect: dropshadow( one-pass-box , black , 20 , 0.0 , 0 , 0 ); -fx-background-color: white; -fx-border-width: 1px; -fx-border-color:black; -fx-border-style: solid outside;");
        //layout when window is resized
        pagination.needsLayoutProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            paper.requestLayout();
        });

        return paper;
    }

    private Group getNewPage(GridPane grid) {
        grid.setGridLinesVisible(true);
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(1);
        grid.setVgap(1);

        Pane container = new Pane(grid);
        Paper paper = Paper.A4;
        int dpi = Template.getModel().getDpi();
        double w = paper.getWidth() * dpi / 72, h = paper.getHeight() * dpi / 72;
        container.setMaxSize(w, h);
        container.setMinSize(w, h);
        container.setTranslateX(0);
        container.setTranslateY(0);
        container.setStyle("-fx-background-color: white;");

        return new Group(container);
    }

    @FXML
    private void handleLoadGamesAction(ActionEvent event) {
        loadSourceFile();
    }

    private void loadSourceFile() {
        final Stage templateChooser = MainViewController.loadTemplateChooser();
        if (templateChooser != null) {
            templateChooser.getScene().getRoot().addEventHandler(TemplateDialogEvent.SELECTED, (actionEvent) -> {
                templateChooser.close();
                Template.load(true);
                File source = MainViewController.chooseGameSourceFile();
                if (source != null) {
                    renderLotoPanel();
                }
            });
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
        if (printer == null) {
            MainViewController.showExceptionAlert("No printer found, please add a printer", null);
        } else {
            double margin = 20.0;
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

            PrinterJob job = PrinterJob.createPrinterJob();
            PrintResolution resolution = job.getJobSettings().getPrintResolution();

            if (job.showPrintDialog(JLotoPrint.stage.getOwner())) {

                try {
                    List<Group> pageList = importPageList(Template.getSourceFile(), Template.getModel());

                    double pageWidth = pageLayout.getPrintableWidth();
                    double pageHeight = pageLayout.getPrintableHeight();

                    for (Parent node : pageList) {

                        node.applyCss();
                        node.layout();

                        HashMap<String, Double> result = resizeProportional(node.getBoundsInParent().getWidth(), node.getBoundsInParent().getHeight(), pageWidth, pageHeight, true);
                        node.getTransforms().add(new Scale(result.get("scaleX"), result.get("scaleY")));

                        job.printPage(node);
                    }
                } catch (Exception ex) {
                    MainViewController.showExceptionAlert("Error printing the document", ex.getMessage());
                } finally {
                    job.endJob();
                }
            }
        }
    }

    public HashMap<String, Double> resizeProportional(double ow, double oh, double w, double h, Boolean dontResizeWhenSmaller) {

        HashMap<String, Double> result = new HashMap<>();
        double ph = w * oh / ow, pw = h * ow / oh;
        //dont resize
        if ((ow < w && oh < h) && dontResizeWhenSmaller) {
            result.put("width", ow);
            result.put("height", oh);
            result.put("scaleX", 1.0);
            result.put("scaleY", 1.0);
        } else {
            double fh, fw;
            if (ph > h) {
                fw = pw;
                fh = h;
            } else {
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

    public void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a destination");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF File", "*.pdf"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("PDF Export");
        File sourceFile = fileChooser.showSaveDialog(JLotoPrint.stage.getOwner());
        try (PDDocument doc = generatePDF()) {
            if (doc != null) {
                doc.save(sourceFile);
                //sucess
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF document sucessfully exported.", ButtonType.OK);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MainViewController.showExceptionAlert("Error exporting the PDF document", ex.getStackTrace().toString());
        }
    }

    public PDDocument generatePDF() throws Exception {

        PDDocument doc = null;
        PDPage page = null;
        PDPageContentStream content = null;

        try {
            doc = new PDDocument();
            List<Group> pageList = importPageList(Template.getSourceFile(), Template.getModel());
            for (Parent node : pageList) {

                page = new PDPage(PDPage.PAGE_SIZE_A4);

                doc.addPage(page);

                PDRectangle mediaBox = page.getMediaBox();
                float pageWidth = mediaBox.getWidth();
                float pageHeight = mediaBox.getHeight();

                node.setTranslateX(0);
                node.setTranslateY(0);
                node.setScaleX(1);
                node.setScaleY(1);
                node.applyCss();
                node.layout();
                HashMap<String, Double> result = resizeProportional(node.getBoundsInParent().getWidth(), node.getBoundsInParent().getHeight(), pageWidth, pageHeight, true);

                //get node image
                WritableImage nodeImage = node.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(nodeImage, null);

                //set page content
                content = new PDPageContentStream(doc, page);

                PDJpeg image = new PDJpeg(doc, bufferedImage, 1f);
                content.drawXObject(image, 0, 0, result.get("width").intValue(), result.get("height").intValue());

                content.close();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (content != null) {
                    content.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return doc;
    }

    public class TableCellData {

        private String number;

        private String count;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public TableCellData(String number, String count) {
            this.number = number;
            this.count = count;
        }
    }
}
