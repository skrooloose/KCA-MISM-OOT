import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class TeacherManagement {

    private final DatabaseOperations dbOps = new DatabaseOperations();
    private TableView<Teacher> teacherTable;
    private TableView<AssignedSubject> assignedSubjectsTable;

    /**
     * Returns the Teacher Management UI as a Parent object.
     */
    public Parent getView() {
        VBox teacherView = new VBox(20);
        teacherView.setAlignment(Pos.TOP_CENTER);

        // Heading
        Label heading = new Label("Teacher Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // TableView for teachers
        teacherTable = new TableView<>();
        setupTeacherTable();

        // Assigned subjects table
        assignedSubjectsTable = new TableView<>();
        setupAssignedSubjectsTable();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a teacher...");
        searchField.setPrefWidth(300);

        // Add new teacher button
        Button addTeacherButton = new Button("Add New Teacher");
        addTeacherButton.setStyle("-fx-font-size: 14px;");

        // Assign subject button
        Button assignSubjectButton = new Button("Assign Subject");
        assignSubjectButton.setStyle("-fx-font-size: 14px;");

        // Delete teacher button
        Button deleteTeacherButton = new Button("Delete Selected Teacher");
        deleteTeacherButton.setStyle("-fx-font-size: 14px;");

        // Add action listeners
        addTeacherButton.setOnAction(event -> showAddTeacherDialog());
        assignSubjectButton.setOnAction(event -> showAssignSubjectDialog());
        deleteTeacherButton.setOnAction(event -> deleteSelectedTeacher());

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTeachers(newValue));

        // Buttons container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(addTeacherButton, assignSubjectButton, deleteTeacherButton);

        // Add components to the view
        teacherView.getChildren().addAll(heading, searchField, teacherTable, assignedSubjectsTable, buttonContainer);

        // Load teachers into the table
        loadTeachers();

        return teacherView;
    }

    /**
     * Set up the teacher table with columns.
     */
    private void setupTeacherTable() {
        TableColumn<Teacher, String> staffNumberColumn = new TableColumn<>("Staff Number");
        staffNumberColumn.setCellValueFactory(new PropertyValueFactory<>("staffNumber"));

        TableColumn<Teacher, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Teacher, String> idColumn = new TableColumn<>("ID/Passport");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idPassport"));

        teacherTable.getColumns().addAll(staffNumberColumn, nameColumn, idColumn);
        teacherTable.setPrefHeight(200);

        teacherTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadAssignedSubjects(newSelection.getStaffNumber());
            }
        });
    }

    /**
     * Set up the assigned subjects table with columns.
     */
    private void setupAssignedSubjectsTable() {
        TableColumn<AssignedSubject, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<AssignedSubject, String> formColumn = new TableColumn<>("Form");
        formColumn.setCellValueFactory(new PropertyValueFactory<>("form"));

        TableColumn<AssignedSubject, String> termColumn = new TableColumn<>("Term");
        termColumn.setCellValueFactory(new PropertyValueFactory<>("term"));

        assignedSubjectsTable.getColumns().addAll(subjectColumn, formColumn, termColumn);
        assignedSubjectsTable.setPrefHeight(200);
    }

    /**
     * Load teachers from the database into the table.
     */
    private void loadTeachers() {
        teacherTable.getItems().clear();
        List<String[]> teachers = dbOps.getAllTeachers();
        for (String[] teacherData : teachers) {
            Teacher teacher = new Teacher(
                    teacherData[0], // Staff Number
                    teacherData[1], // Name
                    teacherData[2]  // ID/Passport
            );
            teacherTable.getItems().add(teacher);
        }
    }

    /**
     * Filters the teachers displayed in the table based on the search query.
     *
     * @param query The search query entered by the user.
     */
    private void filterTeachers(String query) {
        teacherTable.getItems().clear();

        if (query == null || query.isEmpty()) {
            loadTeachers();
        } else {
            List<String[]> teachers = dbOps.getAllTeachers();
            for (String[] teacherData : teachers) {
                String name = teacherData[1].toLowerCase();
                String staffNumber = teacherData[0].toLowerCase();
                if (name.contains(query.toLowerCase()) || staffNumber.contains(query.toLowerCase())) {
                    Teacher teacher = new Teacher(
                            teacherData[0], // Staff Number
                            teacherData[1], // Name
                            teacherData[2]  // ID/Passport
                    );
                    teacherTable.getItems().add(teacher);
                }
            }
        }
    }

    /**
     * Load assigned subjects for the selected teacher into the table.
     */
    private void loadAssignedSubjects(String teacherId) {
        assignedSubjectsTable.getItems().clear();
        List<String[]> subjects = dbOps.getTeacherSubjects(teacherId);
        for (String[] subjectData : subjects) {
            AssignedSubject subject = new AssignedSubject(
                    subjectData[0], // Subject
                    subjectData[1], // Form
                    subjectData[2]  // Term
            );
            assignedSubjectsTable.getItems().add(subject);
        }
    }

    /**
     * Show a dialog to add a new teacher.
     */
    private void showAddTeacherDialog() {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField staffNumberField = new TextField();
        staffNumberField.setPromptText("Staff Number");

        TextField idField = new TextField();
        idField.setPromptText("ID/Passport");

        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(new Label("Add New Teacher"), nameField, staffNumberField, idField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Teacher");
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                dbOps.addTeacher(nameField.getText(), staffNumberField.getText(), idField.getText());
                loadTeachers();
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show a dialog to assign a subject to a teacher.
     */
    private void showAssignSubjectDialog() {
        Teacher selectedTeacher = teacherTable.getSelectionModel().getSelectedItem();
        if (selectedTeacher == null) {
            showAlert("No Teacher Selected", "Please select a teacher to assign a subject.");
            return;
        }

        ComboBox<String> subjectComboBox = new ComboBox<>();
        subjectComboBox.getItems().addAll("English", "Math", "Science", "History", "Geography");
        subjectComboBox.setPromptText("Select Subject");

        ComboBox<Integer> formComboBox = new ComboBox<>();
        formComboBox.getItems().addAll(1, 2, 3, 4);
        formComboBox.setPromptText("Select Form");

        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(new Label("Assign Subject"), subjectComboBox, formComboBox);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Assign Subject");
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String subject = subjectComboBox.getValue();
                Integer form = formComboBox.getValue();
                int term = determineCurrentTerm();

                if (subject != null && form != null) {
                    dbOps.addTeacherSubject(selectedTeacher.getStaffNumber(), subject, form, term);
                    loadAssignedSubjects(selectedTeacher.getStaffNumber());
                } else {
                    showAlert("Incomplete Information", "Please select both a subject and a form.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Delete the selected teacher.
     */
    private void deleteSelectedTeacher() {
        Teacher selectedTeacher = teacherTable.getSelectionModel().getSelectedItem();
        if (selectedTeacher != null) {
            dbOps.deleteTeacher(selectedTeacher.getStaffNumber());
            loadTeachers();
        } else {
            showAlert("No Teacher Selected", "Please select a teacher to delete.");
        }
    }

    /**
     * Determine the current term based on the date.
     */
    private int determineCurrentTerm() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 1 && month <= 4) return 1;
        if (month >= 5 && month <= 8) return 2;
        return 3;
    }

    /**
     * Show an alert dialog.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Model class representing a Teacher.
     */
    public static class Teacher {
        private final SimpleStringProperty staffNumber;
        private final SimpleStringProperty name;
        private final SimpleStringProperty idPassport;

        public Teacher(String staffNumber, String name, String idPassport) {
            this.staffNumber = new SimpleStringProperty(staffNumber);
            this.name = new SimpleStringProperty(name);
            this.idPassport = new SimpleStringProperty(idPassport);
        }

        public String getStaffNumber() { return staffNumber.get(); }
        public String getName() { return name.get(); }
        public String getIdPassport() { return idPassport.get(); }
    }

    /**
     * Model class representing an Assigned Subject.
     */
    public static class AssignedSubject {
        private final SimpleStringProperty subject;
        private final SimpleStringProperty form;
        private final SimpleStringProperty term;

        public AssignedSubject(String subject, String form, String term) {
            this.subject = new SimpleStringProperty(subject);
            this.form = new SimpleStringProperty(form);
            this.term = new SimpleStringProperty(term);
        }

        public String getSubject() { return subject.get(); }
        public String getForm() { return form.get(); }
        public String getTerm() { return term.get(); }
    }
}
