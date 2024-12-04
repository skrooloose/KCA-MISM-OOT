import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EXO_Dash extends Application {

    private BorderPane root; // Main layout container

    @Override
    public void start(Stage primaryStage) {
        // Create navigation buttons
        Button studentsButton = new Button("STUDENTS");
        Button teachersButton = new Button("TEACHERS");
        Button examResultsButton = new Button("EXAM RESULTS");
        Button subjectsButton = new Button("SUBJECTS");

        // Button styling
        String buttonStyle = "-fx-font-size: 18px; -fx-pref-width: 200px; -fx-pref-height: 50px;";
        studentsButton.setStyle(buttonStyle);
        teachersButton.setStyle(buttonStyle);
        examResultsButton.setStyle(buttonStyle);
        subjectsButton.setStyle(buttonStyle);

        // Add buttons to navigation panel
        VBox navigationPanel = new VBox(20);
        navigationPanel.getChildren().addAll(studentsButton, teachersButton, examResultsButton, subjectsButton);
        navigationPanel.setAlignment(Pos.CENTER);

        // Main layout
        root = new BorderPane();
        root.setLeft(navigationPanel);

        // Default Welcome Screen
        showWelcomeScreen();

        // Button Actions
        studentsButton.setOnAction(event -> showStudentsView());
        teachersButton.setOnAction(event -> showTeachersView());
        examResultsButton.setOnAction(event -> showExamResultsView());
        subjectsButton.setOnAction(event -> showSubjectsView());

        // Scene and Stage setup
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Exam Officer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Full-screen support
        primaryStage.show();
    }

    /**
     * Displays the Welcome Screen (Static Content).
     */
    private void showWelcomeScreen() {
        Label welcomeLabel = new Label("Welcome to the Exam Officer Dashboard");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox welcomeScreen = new VBox(welcomeLabel);
        welcomeScreen.setAlignment(Pos.CENTER);
        root.setCenter(welcomeScreen);
    }

    /**
     * Displays the Students Management View (Dynamic Data Handling via StudentManagement).
     */
    private void showStudentsView() {
        StudentManagement studentManagement = new StudentManagement(); // Delegate to StudentManagement
        root.setCenter(studentManagement.getView()); // Dynamically fetch and display student data
    }

    /**
     * Displays the Teachers Management View (Dynamic Data Handling via TeacherManagement).
     */
    private void showTeachersView() {
        TeacherManagement teacherManagement = new TeacherManagement(); // Delegate to TeacherManagement
        root.setCenter(teacherManagement.getView()); // Dynamically fetch and display teacher data
    }

    /**
     * Displays the Exam Results Management View (Dynamic Data Handling via ExamResultsManagement).
     */
    private void showExamResultsView() {
        ExamResultsManagement examResultsManagement = new ExamResultsManagement(); // Delegate to ExamResultsManagement
        root.setCenter(examResultsManagement.getView()); // Dynamically fetch and display exam result data
    }

    /**
     * Displays the Subjects Management View (Dynamic Data Handling via SubjectManagement).
     */
    private void showSubjectsView() {
        SubjectManagement subjectManagement = new SubjectManagement(); // Delegate to SubjectManagement
        root.setCenter(subjectManagement.getView()); // Dynamically fetch and display subject data
    }

    public static void main(String[] args) {
        launch(args);
    }
}
