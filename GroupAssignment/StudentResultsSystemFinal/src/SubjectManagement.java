import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class SubjectManagement {

    private final DatabaseOperations dbOps = new DatabaseOperations();
    private TableView<Subject> subjectTable;

    /**
     * Returns the Subject Management UI as a Parent object.
     */
    public Parent getView() {
        VBox subjectView = new VBox(20);
        subjectView.setAlignment(Pos.TOP_CENTER);

        // Heading
        Label heading = new Label("Subject Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // TableView for subjects
        subjectTable = new TableView<>();
        setupSubjectTable();

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search for a subject...");
        searchField.setPrefWidth(300);

        // Add new subject button
        Button addSubjectButton = new Button("Add New Subject");
        addSubjectButton.setStyle("-fx-font-size: 14px;");

        // Edit subject button
        Button editSubjectButton = new Button("Edit Selected Subject");
        editSubjectButton.setStyle("-fx-font-size: 14px;");

        // Delete subject button
        Button deleteSubjectButton = new Button("Delete Selected Subject");
        deleteSubjectButton.setStyle("-fx-font-size: 14px;");

        // Add action listeners
        addSubjectButton.setOnAction(event -> showAddSubjectDialog());
        editSubjectButton.setOnAction(event -> showEditSubjectDialog());
        deleteSubjectButton.setOnAction(event -> deleteSelectedSubject());

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSubjects(newValue));

        // Buttons container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(addSubjectButton, editSubjectButton, deleteSubjectButton);

        // Add components to the view
        subjectView.getChildren().addAll(heading, searchField, subjectTable, buttonContainer);

        // Load subjects into the table
        loadSubjects();

        return subjectView;
    }

    /**
     * Set up the subject table with columns.
     */
    private void setupSubjectTable() {
        TableColumn<Subject, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Subject, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Subject, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        subjectTable.getColumns().addAll(idColumn, nameColumn, descriptionColumn);
        subjectTable.setPrefHeight(300);
    }

    /**
     * Load subjects from the database into the table.
     */
    private void loadSubjects() {
        subjectTable.getItems().clear();
        List<String[]> subjects = dbOps.getAllSubjects();
        for (String[] subjectData : subjects) {
            Subject subject = new Subject(
                    Integer.parseInt(subjectData[0]), // ID
                    subjectData[1], // Name
                    subjectData[2]  // Description
            );
            subjectTable.getItems().add(subject);
        }
    }

    /**
     * Show a dialog to add a new subject.
     */
    private void showAddSubjectDialog() {
        TextField nameField = new TextField();
        nameField.setPromptText("Subject Name");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(new Label("Add New Subject"), nameField, descriptionField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                dbOps.addSubject(nameField.getText(), descriptionField.getText());
                loadSubjects(); // Refresh the table
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show a dialog to edit the selected subject.
     */
    private void showEditSubjectDialog() {
        Subject selectedSubject = subjectTable.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) {
            showAlert("No Subject Selected", "Please select a subject to edit.");
            return;
        }

        TextField nameField = new TextField(selectedSubject.getName());
        nameField.setPromptText("Subject Name");

        TextField descriptionField = new TextField(selectedSubject.getDescription());
        descriptionField.setPromptText("Description");

        VBox dialogLayout = new VBox(10);
        dialogLayout.getChildren().addAll(new Label("Edit Subject"), nameField, descriptionField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Subject");
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                dbOps.updateSubject(selectedSubject.getId(), nameField.getText(), descriptionField.getText());
                loadSubjects(); // Refresh the table
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Delete the selected subject.
     */
    private void deleteSelectedSubject() {
        Subject selectedSubject = subjectTable.getSelectionModel().getSelectedItem();
        if (selectedSubject != null) {
            dbOps.deleteSubject(selectedSubject.getId());
            loadSubjects(); // Refresh the table
        } else {
            showAlert("No Subject Selected", "Please select a subject to delete.");
        }
    }

    /**
     * Filter subjects based on the search query.
     */
    private void filterSubjects(String query) {
        subjectTable.getItems().clear();
        if (query == null || query.isEmpty()) {
            loadSubjects();
        } else {
            List<String[]> subjects = dbOps.searchSubjectsByName(query);
            for (String[] subjectData : subjects) {
                Subject subject = new Subject(
                        Integer.parseInt(subjectData[0]), // ID
                        subjectData[1], // Name
                        subjectData[2]  // Description
                );
                subjectTable.getItems().add(subject);
            }
        }
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
     * Model class representing a Subject.
     */
    public static class Subject {
        private final int id;
        private final String name;
        private final String description;

        public Subject(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
