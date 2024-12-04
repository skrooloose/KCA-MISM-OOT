import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Year;
import java.util.List;

public class StudentManagement {

    private final DatabaseOperations dbOps = new DatabaseOperations();
    private TableView<Student> studentTable;

    /**
     * Returns the Student Management UI as a Parent object.
     * This can be dynamically added to any layout.
     */
    public Parent getView() {
        VBox studentView = new VBox(20);
        studentView.setAlignment(Pos.TOP_CENTER);

        // Heading
        Label heading = new Label("Student Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // TableView for students
        studentTable = new TableView<>();
        setupStudentTable();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a student...");
        searchField.setPrefWidth(300);

        // Add new student button
        Button addStudentButton = new Button("Add New Student");
        addStudentButton.setStyle("-fx-font-size: 14px;");

        // Delete selected student button
        Button deleteStudentButton = new Button("Delete Selected Student");
        deleteStudentButton.setStyle("-fx-font-size: 14px;");

        // Add action listeners
        addStudentButton.setOnAction(event -> showAddStudentDialog());
        deleteStudentButton.setOnAction(event -> deleteSelectedStudent());

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterStudents(newValue));

        // Buttons container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(addStudentButton, deleteStudentButton);

        // Add components to the view
        studentView.getChildren().addAll(heading, searchField, studentTable, buttonContainer);

        // Load students into the table
        loadStudents();

        return studentView;
    }

    /**
     * Set up the student table with columns.
     */
    private void setupStudentTable() {
        TableColumn<Student, Integer> admnoColumn = new TableColumn<>("Adm No");
        admnoColumn.setCellValueFactory(new PropertyValueFactory<>("admno"));

        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Student, Integer> formColumn = new TableColumn<>("Form");
        formColumn.setCellValueFactory(new PropertyValueFactory<>("form"));

        TableColumn<Student, Integer> termColumn = new TableColumn<>("Term");
        termColumn.setCellValueFactory(new PropertyValueFactory<>("term"));

        TableColumn<Student, Integer> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        studentTable.getColumns().addAll(admnoColumn, nameColumn, genderColumn, formColumn, termColumn, yearColumn);
        studentTable.setPrefHeight(400); // Adjust as needed
    }

    /**
     * Load students from the database into the table.
     */
    private void loadStudents() {
        studentTable.getItems().clear();
        List<String[]> students = dbOps.getAllStudents();
        for (String[] studentData : students) {
            Student student = new Student(
                    Integer.parseInt(studentData[0]), // Admno
                    studentData[1], // Name
                    studentData[2], // Gender
                    Integer.parseInt(studentData[3]), // Form
                    Integer.parseInt(studentData[4]), // Term
                    Integer.parseInt(studentData[5])  // Year
            );
            studentTable.getItems().add(student);
        }
    }

    /**
     * Filter students in the table based on the search query.
     *
     * @param query The search query.
     */
    private void filterStudents(String query) {
        if (query == null || query.isEmpty()) {
            loadStudents();
        } else {
            studentTable.getItems().clear();
            List<String[]> students = dbOps.getAllStudents();
            for (String[] studentData : students) {
                if (studentData[1].toLowerCase().contains(query.toLowerCase())) {
                    Student student = new Student(
                            Integer.parseInt(studentData[0]), // Admno
                            studentData[1], // Name
                            studentData[2], // Gender
                            Integer.parseInt(studentData[3]), // Form
                            Integer.parseInt(studentData[4]), // Term
                            Integer.parseInt(studentData[5])  // Year
                    );
                    studentTable.getItems().add(student);
                }
            }
        }
    }

    /**
     * Show a dialog to add a new student.
     */
    private void showAddStudentDialog() {
        // Dialog components
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female");
        genderComboBox.setPromptText("Gender");

        ComboBox<Integer> formComboBox = new ComboBox<>();
        formComboBox.getItems().addAll(1, 2, 3, 4);
        formComboBox.setPromptText("Form");

        ComboBox<Integer> termComboBox = new ComboBox<>();
        termComboBox.getItems().addAll(1, 2, 3);
        termComboBox.setPromptText("Term");

        TextField yearField = new TextField(String.valueOf(Year.now().getValue()));
        yearField.setEditable(false);

        // Layout
        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(new Label("Add New Student"), nameField, genderComboBox, formComboBox, termComboBox, yearField);

        // Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String name = nameField.getText();
                String gender = genderComboBox.getValue();
                Integer form = formComboBox.getValue();
                Integer term = termComboBox.getValue();
                Integer year = Integer.parseInt(yearField.getText());

                if (name != null && !name.isEmpty() && gender != null && form != null && term != null) {
                    dbOps.addStudent(name, gender, form, term, year); // Dynamically store the year in DB
                    loadStudents(); // Refresh the table
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("All fields are required.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Delete the selected student from the database.
     */
    private void deleteSelectedStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            dbOps.deleteStudent(selectedStudent.getAdmno());
            loadStudents(); // Refresh the table
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delete Student");
            alert.setHeaderText(null);
            alert.setContentText("Please select a student to delete.");
            alert.showAndWait();
        }
    }

    /**
     * Model class representing a Student.
     */
    public static class Student {
        private final SimpleIntegerProperty admno;
        private final SimpleStringProperty name;
        private final SimpleStringProperty gender;
        private final SimpleIntegerProperty form;
        private final SimpleIntegerProperty term;
        private final SimpleIntegerProperty year;

        public Student(int admno, String name, String gender, int form, int term, int year) {
            this.admno = new SimpleIntegerProperty(admno);
            this.name = new SimpleStringProperty(name);
            this.gender = new SimpleStringProperty(gender);
            this.form = new SimpleIntegerProperty(form);
            this.term = new SimpleIntegerProperty(term);
            this.year = new SimpleIntegerProperty(year);
        }

        public int getAdmno() { return admno.get(); }
        public String getName() { return name.get(); }
        public String getGender() { return gender.get(); }
        public int getForm() { return form.get(); }
        public int getTerm() { return term.get(); }
        public int getYear() { return year.get(); }
    }
}
