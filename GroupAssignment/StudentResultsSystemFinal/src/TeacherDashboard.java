import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.application.Platform;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherDashboard extends Application {

    private VBox mainContainer;
    private DatabaseOperations dbOps; // DatabaseOperations instance to fetch teacher data
    private ObservableList<StudentRow> studentRows; // To store students' marks data
    private static String teacherId; // Static teacherId to allow passing dynamically

    // Default constructor for JavaFX
    public TeacherDashboard() {
        dbOps = new DatabaseOperations();
    }

    // Setter to provide teacherId before launching
    public static void setTeacherId(String id) {
        teacherId = id;
    }

    private int getSubjectIdFromName(String subject) {
        // Implement logic to map subject name to subject ID
        switch (subject.split(" ")[0].toLowerCase()) {
            case "mathematics":
                return 1;
            case "english":
                return 2;
            case "biology":
                return 3;
            default:
                throw new IllegalArgumentException("Unknown subject: " + subject);
        }
    }

    private int getTermFromSubject(String subject) {
        if (subject.toLowerCase().contains("term 1")) return 1;
        if (subject.toLowerCase().contains("term 2")) return 2;
        if (subject.toLowerCase().contains("term 3")) return 3;
        throw new IllegalArgumentException("No term found in subject string: " + subject);
    }

    @Override
    public void start(Stage primaryStage) {
        if (teacherId == null) {
            // Exit the application if teacherId is not provided
            System.err.println("Teacher ID is not set.");
            Platform.exit();
            return;
        }

        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Title
        Text title = new Text("Teacher Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Fetch subjects assigned to this teacher
        List<String> subjects = dbOps.getSubjectsForTeacher(teacherId);

        // Create buttons for each subject
        VBox subjectButtonsContainer = new VBox(10);
        subjectButtonsContainer.setAlignment(Pos.CENTER);

        for (String subject : subjects) {
            Button subjectButton = new Button(subject);
            subjectButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px;");
            subjectButton.setOnAction(event -> showSubjectView(subject));
            subjectButtonsContainer.getChildren().add(subjectButton);
        }

        mainContainer.getChildren().addAll(title, subjectButtonsContainer);

        Scene scene = new Scene(mainContainer, 800, 600);
        primaryStage.setTitle("Teacher Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showSubjectView(String subject) {
        mainContainer.getChildren().clear();

        // Title for the selected subject
        Text subjectTitle = new Text("Managing Marks for: " + subject);
        subjectTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Table for managing marks
        TableView<StudentRow> tableView = createMarksTable(subject);

        // DONE Button
        Button doneButton = new Button("DONE");
        doneButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 150px;");
        doneButton.setOnAction(event -> showSortingOptions(subject, tableView));

        // Add components to the main container
        mainContainer.getChildren().addAll(subjectTitle, tableView, doneButton);
    }

    private TableView<StudentRow> createMarksTable(String subject) {
        int subjectId = getSubjectIdFromName(subject);
        int term = getTermFromSubject(subject);

        // Fetch students using subjectId and term
        List<String[]> students = dbOps.getStudentsForSubject(subjectId, term);

        // Populate student rows
        studentRows = FXCollections.observableArrayList(
                students.stream().map(StudentRow::new).collect(Collectors.toList())
        );

        TableView<StudentRow> table = new TableView<>(studentRows);

        // Columns
        TableColumn<StudentRow, String> admNoCol = new TableColumn<>("AdmNo");
        admNoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().admNo));

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));

        TableColumn<StudentRow, String> cat1Col = createEditableColumn("CAT 1 (/10)", "cat1");
        TableColumn<StudentRow, String> cat2Col = createEditableColumn("CAT 2 (/10)", "cat2");
        TableColumn<StudentRow, String> cat3Col = createEditableColumn("CAT 3 (/10)", "cat3");
        TableColumn<StudentRow, String> cat4Col = createEditableColumn("CAT 4 (/10)", "cat4");
        TableColumn<StudentRow, String> examCol = createEditableColumn("Exam (/60)", "exam");

        TableColumn<StudentRow, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTotal()));

        TableColumn<StudentRow, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));

        table.getColumns().addAll(admNoCol, nameCol, cat1Col, cat2Col, cat3Col, cat4Col, examCol, totalCol, gradeCol);
        table.setEditable(true);

        return table;
    }

    private TableColumn<StudentRow, String> createEditableColumn(String title, String property) {
        TableColumn<StudentRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProperty(property)));

        column.setCellFactory(tc -> {
            TableCell<StudentRow, String> cell = new TextFieldTableCell<>();
            cell.setOnMouseClicked(event -> {
                StudentRow student = studentRows.get(cell.getIndex());
                validateInput(student, cell.getText(), title);
            });
            return cell;
        });

        return column;
    }

    private void validateInput(StudentRow student, String input, String title) {
        try {
            int max = title.contains("Exam") ? 60 : 10;
            int value = Integer.parseInt(input);

            if (value < 0 || value > max) {
                throw new NumberFormatException();
            }

            student.setProperty(title, String.valueOf(value));
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setContentText("Please enter a valid number between 0 and the maximum for this field.");
            alert.showAndWait();
        }
    }

    private void showSortingOptions(String subject, TableView<StudentRow> tableView) {
        mainContainer.getChildren().clear();

        Text title = new Text("Sort Results for: " + subject);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button sortByName = new Button("Sort by Name");
        sortByName.setOnAction(event -> studentRows.sort((a, b) -> a.name.compareToIgnoreCase(b.name)));

        Button sortByAdmNo = new Button("Sort by AdmNo");
        sortByAdmNo.setOnAction(event -> studentRows.sort((a, b) -> a.admNo.compareToIgnoreCase(b.admNo)));

        HBox sortingButtons = new HBox(10, sortByName, sortByAdmNo);
        sortingButtons.setAlignment(Pos.CENTER);

        Button generateReport = new Button("Generate Report");
        generateReport.setOnAction(event -> exportResultsToPDF(subject, tableView));

        mainContainer.getChildren().addAll(title, sortingButtons, generateReport);
    }

    private void exportResultsToPDF(String subject, TableView<StudentRow> tableView) {
        System.out.println("Exporting to PDF...");
    }

    public static class StudentRow {
        private String admNo;
        private String name;
        private String cat1;
        private String cat2;
        private String cat3;
        private String cat4;
        private String exam;

        public StudentRow(String[] studentData) {
            this.admNo = studentData[0];
            this.name = studentData[1];
            this.cat1 = "";
            this.cat2 = "";
            this.cat3 = "";
            this.cat4 = "";
            this.exam = "";
        }

        public String getProperty(String property) {
            switch (property) {
                case "cat1":
                    return cat1;
                case "cat2":
                    return cat2;
                case "cat3":
                    return cat3;
                case "cat4":
                    return cat4;
                case "exam":
                    return exam;
                default:
                    return "";
            }
        }

        public void setProperty(String property, String value) {
            switch (property) {
                case "cat1":
                    cat1 = value;
                    break;
                case "cat2":
                    cat2 = value;
                    break;
                case "cat3":
                    cat3 = value;
                    break;
                case "cat4":
                    cat4 = value;
                    break;
                case "exam":
                    exam = value;
                    break;
            }
        }

        public String getTotal() {
            int total = Integer.parseInt(cat1.isEmpty() ? "0" : cat1)
                    + Integer.parseInt(cat2.isEmpty() ? "0" : cat2)
                    + Integer.parseInt(cat3.isEmpty() ? "0" : cat3)
                    + Integer.parseInt(cat4.isEmpty() ? "0" : cat4)
                    + Integer.parseInt(exam.isEmpty() ? "0" : exam);
            return String.valueOf(total);
        }

        public String getGrade() {
            int total = Integer.parseInt(getTotal());
            if (total >= 80) return "A";
            if (total >= 70) return "B";
            if (total >= 60) return "C";
            if (total >= 50) return "D";
            if (total >= 40) return "E";
            return "F";
        }
    }

    public static void main(String[] args) {
        TeacherDashboard.setTeacherId("teacher123"); // Provide teacherId dynamically
        launch(args);
    }
}
