import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

public class StudentDashboard extends Application {

    private VBox mainContainer;
    private DatabaseOperations dbOps;
    private String studentAdmNo; // Dynamically retrieved
    private String studentName;  // Dynamically retrieved

    public StudentDashboard(String studentAdmNo) {
        this.studentAdmNo = studentAdmNo;
        dbOps = new DatabaseOperations();
        this.studentName = dbOps.getStudentNameByAdmNo(studentAdmNo); // Retrieve student name dynamically
    }

    @Override
    public void start(Stage primaryStage) {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Title
        Text title = new Text("Student Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Filtering Options
        VBox filterContainer = createFilterOptions();

        // Result Display Area
        VBox resultDisplayContainer = new VBox(20);
        resultDisplayContainer.setAlignment(Pos.TOP_CENTER);
        resultDisplayContainer.setPadding(new Insets(10));
        resultDisplayContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;");

        mainContainer.getChildren().addAll(title, filterContainer, resultDisplayContainer);

        Scene scene = new Scene(mainContainer, 800, 600);
        primaryStage.setTitle("Student Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createFilterOptions() {
        VBox filterContainer = new VBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);

        Label filterTitle = new Label("Select Form and Term to View Results:");
        filterTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ComboBox<String> formFilter = new ComboBox<>();
        formFilter.getItems().addAll("Form 1", "Form 2", "Form 3", "Form 4");
        formFilter.setPromptText("Select Form");

        ComboBox<String> termFilter = new ComboBox<>();
        termFilter.getItems().addAll("Term 1", "Term 2", "Term 3");
        termFilter.setPromptText("Select Term");

        Button generateResultsButton = new Button("Generate Results");
        generateResultsButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 200px;");
        generateResultsButton.setOnAction(event -> {
            String form = formFilter.getValue();
            String term = termFilter.getValue();
            if (form != null && term != null) {
                generateResultSlip(Integer.parseInt(form.split(" ")[1]), Integer.parseInt(term.split(" ")[1]));
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select both form and term.");
                alert.showAndWait();
            }
        });

        HBox filterRow = new HBox(10, formFilter, termFilter, generateResultsButton);
        filterRow.setAlignment(Pos.CENTER);

        filterContainer.getChildren().addAll(filterTitle, filterRow);

        return filterContainer;
    }

    private void generateResultSlip(int form, int term) {
        mainContainer.getChildren().removeIf(node -> node instanceof VBox && node != mainContainer.getChildren().get(0));

        VBox resultSlipContainer = new VBox(20);
        resultSlipContainer.setAlignment(Pos.TOP_CENTER);
        resultSlipContainer.setPadding(new Insets(20));
        resultSlipContainer.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 20px;");

        // Fetch results from the database
        List<String[]> results = dbOps.getResultsForStudent(studentAdmNo, form, term);

        // Student Information
        Text studentInfo = new Text("RESULT SLIP FOR: " + studentName + "\n" +
                "AdmNo: " + studentAdmNo + "\n" +
                "Form: " + form + ", Term: " + term);
        studentInfo.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Table for Results
        GridPane resultsTable = new GridPane();
        resultsTable.setHgap(10);
        resultsTable.setVgap(10);
        resultsTable.setAlignment(Pos.CENTER);

        resultsTable.add(new Text("SUBJECT"), 0, 0);
        resultsTable.add(new Text("TOTAL"), 1, 0);
        resultsTable.add(new Text("GRADE"), 2, 0);

        int row = 1;
        int totalScore = 0;
        int subjectCount = 0;
        for (String[] result : results) {
            String subject = result[0];
            int total = Integer.parseInt(result[1]);
            String grade = result[2];

            totalScore += total;
            subjectCount++;

            resultsTable.add(new Text(subject), 0, row);
            resultsTable.add(new Text(String.valueOf(total)), 1, row);
            resultsTable.add(new Text(grade), 2, row);
            row++;
        }

        int average = subjectCount > 0 ? totalScore / subjectCount : 0;
        String overallGrade = getGradeFromAverage(average);

        // Calculate overall grade and promotion status
        dbOps.saveOverallGradeAndPromotionStatus(
                studentAdmNo,        // Admission number
                overallGrade,        // Overall grade (e.g., A, B, C, etc.)
                form,                // Current form
                term,                // Current term
                overallGrade.equals("F") ? "Repeat Current Term" : "Proceed to Next Term" // Promotion status
        );

        Text overallGradeText = new Text("Overall Grade: " + overallGrade);
        overallGradeText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text promotionStatus = new Text(overallGrade.equals("F") ? "Repeat Current Term" : "Proceed to Next Term");
        promotionStatus.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        promotionStatus.setStyle("-fx-text-fill: " + (overallGrade.equals("F") ? "red" : "green") + ";");

        resultSlipContainer.getChildren().addAll(studentInfo, resultsTable, overallGradeText, promotionStatus);

        mainContainer.getChildren().add(resultSlipContainer);
    }

    private String getGradeFromAverage(int average) {
        if (average >= 80) return "A";
        if (average >= 70) return "B";
        if (average >= 60) return "C";
        if (average >= 50) return "D";
        if (average >= 40) return "E";
        return "F";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
