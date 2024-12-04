import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.util.List;

public class ExamResultsManagement {

    private VBox mainContainer;
    private DatabaseOperations dbOps;

    public ExamResultsManagement() {
        dbOps = new DatabaseOperations();
    }

    public VBox getView() {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("Exam Results Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox filterContainer = createFilterOptions();

        VBox reportDisplayContainer = new VBox(20);
        reportDisplayContainer.setAlignment(Pos.TOP_CENTER);
        reportDisplayContainer.setPadding(new Insets(10));
        reportDisplayContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;");

        mainContainer.getChildren().addAll(title, filterContainer, reportDisplayContainer);

        return mainContainer;
    }

    private VBox createFilterOptions() {
        VBox filterContainer = new VBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);

        Label filterTitle = new Label("Filter Options:");
        filterTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ComboBox<String> formFilter = new ComboBox<>();
        formFilter.getItems().addAll("Form 1", "Form 2", "Form 3", "Form 4");
        formFilter.setPromptText("Select Form");

        ComboBox<String> termFilter = new ComboBox<>();
        termFilter.getItems().addAll("Term 1", "Term 2", "Term 3");
        termFilter.setPromptText("Select Term");

        ComboBox<String> yearFilter = new ComboBox<>();
        populateYearDropdown(yearFilter);
        yearFilter.setPromptText("Select Year");

        ComboBox<String> subjectFilter = new ComboBox<>();
        subjectFilter.getItems().addAll(dbOps.getAllSubjectNames()); // Fetch all subjects dynamically
        subjectFilter.setPromptText("Select Subject");

        ComboBox<String> studentDropdown = new ComboBox<>();
        studentDropdown.setPromptText("Select Admission Number");
        studentDropdown.setPrefWidth(200);
        populateStudentDropdown(studentDropdown);

        Button groupReportButton = new Button("Generate Group Report");
        groupReportButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 180px;");
        groupReportButton.setOnAction(event -> {
            String form = formFilter.getValue();
            String term = termFilter.getValue();
            String year = yearFilter.getValue();
            String subject = subjectFilter.getValue();

            generateGroupReport(form, term, year, subject);
        });

        Button individualReportButton = new Button("Generate Individual Report");
        individualReportButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 200px;");
        individualReportButton.setOnAction(event -> {
            String studentAdmNo = studentDropdown.getValue();
            generateIndividualReport(studentAdmNo);
        });

        HBox filterRow = new HBox(10, formFilter, termFilter, yearFilter, subjectFilter, studentDropdown, groupReportButton, individualReportButton);
        filterRow.setAlignment(Pos.CENTER);

        filterContainer.getChildren().addAll(filterTitle, filterRow);

        return filterContainer;
    }

    private void populateStudentDropdown(ComboBox<String> studentDropdown) {
        List<String[]> students = dbOps.getAllStudents();
        for (String[] student : students) {
            studentDropdown.getItems().add(student[0]);
        }
    }

    private void populateYearDropdown(ComboBox<String> yearDropdown) {
        List<String> yearsFromDatabase = dbOps.getYearsFromDatabase();
        yearDropdown.getItems().addAll(yearsFromDatabase);
    }

    private void generateGroupReport(String form, String term, String year, String subject) {
        VBox reportContainer = new VBox(20);
        reportContainer.setAlignment(Pos.TOP_CENTER);
        reportContainer.setPadding(new Insets(10));

        TableView<String[]> reportTable = createGroupReportTable(form, term, year, subject);
        BarChart<String, Number> chart = createGroupReportChart(form, term, year, subject);

        reportContainer.getChildren().addAll(new Label("Group Report"), reportTable, chart);

        Button exportButton = new Button("Download PDF");
        exportButton.setStyle("-fx-font-size: 14px;");
        exportButton.setOnAction(event -> exportToPDF(reportTable, chart, "Group_Report.pdf"));
        reportContainer.getChildren().add(exportButton);

        mainContainer.getChildren().add(reportContainer);
    }

    private void generateIndividualReport(String studentAdmNo) {
        VBox reportContainer = new VBox(20);
        reportContainer.setAlignment(Pos.TOP_CENTER);
        reportContainer.setPadding(new Insets(10));

        Label reportTitle = new Label("Individual Report for Admission No: " + studentAdmNo);
        reportTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TableView<String[]> reportTable = createIndividualReportTable(studentAdmNo);
        BarChart<String, Number> chart = createIndividualReportChart(studentAdmNo);

        reportContainer.getChildren().addAll(reportTitle, reportTable, chart);

        Button exportButton = new Button("Download PDF");
        exportButton.setStyle("-fx-font-size: 14px;");
        exportButton.setOnAction(event -> exportToPDF(reportTable, chart, "Individual_Report.pdf"));
        reportContainer.getChildren().add(exportButton);

        mainContainer.getChildren().add(reportContainer);
    }

    private TableView<String[]> createGroupReportTable(String form, String term, String year, String subject) {
        TableView<String[]> table = new TableView<>();

        TableColumn<String[], String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> colMarks = new TableColumn<>("Marks");
        colMarks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        table.getColumns().addAll(colStudent, colMarks);

        List<String[]> data = dbOps.getGroupResults(form, term, year, subject);
        table.getItems().addAll(data);

        return table;
    }

    private BarChart<String, Number> createGroupReportChart(String form, String term, String year, String subject) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Students");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Marks");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Group Marks");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Marks");

        List<String[]> data = dbOps.getGroupResults(form, term, year, subject);
        for (String[] record : data) {
            series.getData().add(new XYChart.Data<>(record[0], Integer.parseInt(record[1])));
        }

        barChart.getData().add(series);

        return barChart;
    }

    private TableView<String[]> createIndividualReportTable(String studentAdmNo) {
        TableView<String[]> table = new TableView<>();

        TableColumn<String[], String> colSubject = new TableColumn<>("Subject");
        colSubject.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> colMarks = new TableColumn<>("Marks");
        colMarks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        table.getColumns().addAll(colSubject, colMarks);

        List<String[]> data = dbOps.getIndividualResults(studentAdmNo);
        table.getItems().addAll(data);

        return table;
    }

    private BarChart<String, Number> createIndividualReportChart(String studentAdmNo) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Subjects");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Marks");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Subject Marks");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Marks");

        List<String[]> data = dbOps.getIndividualResults(studentAdmNo);
        for (String[] record : data) {
            series.getData().add(new XYChart.Data<>(record[0], Integer.parseInt(record[1])));
        }

        barChart.getData().add(series);

        return barChart;
    }

    private void exportToPDF(TableView<String[]> table, BarChart<String, Number> chart, String fileName) {
        try (PdfWriter writer = new PdfWriter(fileName);
             com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // Add title
            document.add(new Paragraph("Report").setBold().setFontSize(20));

            // Add Table
            Table pdfTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            pdfTable.addHeaderCell("Student");
            pdfTable.addHeaderCell("Marks");

            for (String[] row : table.getItems()) {
                pdfTable.addCell(row[0]); // Student
                pdfTable.addCell(row[1]); // Marks
            }

            document.add(pdfTable);

            // Add Chart Placeholder (charts need to be converted to an image to add to PDF)
            document.add(new Paragraph("Chart Placeholder").setItalic());

            // Confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("PDF exported successfully to: " + fileName);
            alert.showAndWait();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not write to file: " + fileName);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred during PDF export.");
            alert.showAndWait();
        }
    }

}
