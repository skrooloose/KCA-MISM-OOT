import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    private static final String URL = "jdbc:mysql://localhost:3306/results_system"; // Update with your DB URL
    private static final String USER = "root"; // Update with your DB username
    private static final String PASSWORD = "`1234567890-="; // Update with your DB password

    /**
     * Establishes a connection to the database.
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // -----------------------------------------
    // CRUD Operations for Students
    // -----------------------------------------

    public boolean addStudent(String name, String gender, int form, int term, int year) {
        String query = "INSERT INTO students (name, gender, form, term, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, gender);
            statement.setInt(3, form);
            statement.setInt(4, term);
            statement.setInt(5, year);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getAllStudents() {
        List<String[]> students = new ArrayList<>();
        String query = "SELECT * FROM students";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String[] student = {
                        String.valueOf(resultSet.getInt("admno")),
                        resultSet.getString("name"),
                        resultSet.getString("gender"),
                        String.valueOf(resultSet.getInt("form")),
                        String.valueOf(resultSet.getInt("term")),
                        String.valueOf(resultSet.getInt("year"))
                };
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean deleteStudent(int admno) {
        String query = "DELETE FROM students WHERE admno = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, admno);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------------------
    // CRUD Operations for Teachers
    // -----------------------------------------

    public boolean addTeacher(String name, String staffNumber, String idPassport) {
        String query = "INSERT INTO teachers (name, staff_number, id_passport) VALUES (?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, staffNumber);
            statement.setString(3, idPassport);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getAllTeachers() {
        List<String[]> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String[] teacher = {
                        resultSet.getString("staff_number"),
                        resultSet.getString("name"),
                        resultSet.getString("id_passport")
                };
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teachers;
    }

    public boolean deleteTeacher(String staffNumber) {
        String query = "DELETE FROM teachers WHERE staff_number = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, staffNumber);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllSubjectNames() {
        List<String> subjects = new ArrayList<>();
        String query = "SELECT name FROM subjects";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                subjects.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    public List<String[]> getIndividualResults(String studentAdmNo) {
        List<String[]> results = new ArrayList<>();
        String query = "SELECT sub.name, r.total FROM results r " +
                "JOIN subjects sub ON r.subject_id = sub.id " +
                "WHERE r.admno = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, studentAdmNo);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(new String[]{resultSet.getString("name"), resultSet.getString("total")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }





    public List<String[]> getGroupResults(String form, String term, String year, String subject) {
        List<String[]> results = new ArrayList<>();
        String query = "SELECT s.name, r.total FROM results r " +
                "JOIN students s ON r.admno = s.admno " +
                "JOIN subjects sub ON r.subject_id = sub.id " +
                "WHERE r.form = ? AND r.term = ? AND r.year = ? AND sub.name = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(form.split(" ")[1]));
            statement.setInt(2, Integer.parseInt(term.split(" ")[1]));
            statement.setInt(3, Integer.parseInt(year));
            statement.setString(4, subject);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(new String[]{resultSet.getString("name"), resultSet.getString("total")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }



    public boolean addTeacherSubject(String teacherId, String subject, int form, int term) {
        String query = "INSERT INTO teacher_subjects (teacher_id, subject, form, term) VALUES (?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, teacherId);
            statement.setString(2, subject);
            statement.setInt(3, form);
            statement.setInt(4, term);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String[]> getTeacherSubjects(String teacherId) {
        List<String[]> subjects = new ArrayList<>();
        String query = "SELECT ts.subject, ts.form, ts.term " +
                "FROM teacher_subjects ts " +
                "WHERE ts.teacher_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, teacherId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                subjects.add(new String[]{
                        resultSet.getString("subject"),
                        String.valueOf(resultSet.getInt("form")),
                        String.valueOf(resultSet.getInt("term"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    public List<String> getSubjectsForTeacher(String teacherId) {
        List<String> subjects = new ArrayList<>();
        String query = "SELECT subject FROM teacher_subjects WHERE teacher_id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, teacherId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                subjects.add(resultSet.getString("subject"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    public List<String[]> getStudentsForSubject(int subjectId, int term) {
        List<String[]> students = new ArrayList<>();
        String query = "SELECT s.admno, s.name FROM students s " +
                "JOIN results r ON s.admno = r.admno " +
                "WHERE r.subject_id = ? AND r.term = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, subjectId);
            statement.setInt(2, term);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                students.add(new String[]{
                        resultSet.getString("admno"),
                        resultSet.getString("name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean deleteTeacherSubject(int id) {
        String query = "DELETE FROM teacher_subjects WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------------------
    // CRUD Operations for Subjects
    // -----------------------------------------

    public List<String[]> getAllSubjects() {
        List<String[]> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String[] subject = {
                        String.valueOf(resultSet.getInt("id")),
                        resultSet.getString("name"),
                        resultSet.getString("description")
                };
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }


    public List<String[]> getResultsForStudent(String admNo, int form, int term) {
        List<String[]> results = new ArrayList<>();
        String query = "SELECT s.name AS subject, r.total, r.grade " +
                "FROM results r " +
                "JOIN subjects s ON r.subject_id = s.id " +
                "WHERE r.admno = ? AND r.form = ? AND r.term = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, admNo);
            statement.setInt(2, form);
            statement.setInt(3, term);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(new String[]{
                        resultSet.getString("subject"),
                        resultSet.getString("total"),
                        resultSet.getString("grade")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getStudentNameByAdmNo(String admNo) {
        String studentName = null;
        String query = "SELECT name FROM students WHERE admno = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, admNo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                studentName = resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentName;
    }

    public boolean saveOverallGradeAndPromotionStatus(String admNo, String grade, int form, int term, String promotionStatus) {
        String query = "UPDATE students SET overall_grade = ?, promotion_status = ? WHERE admno = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, grade);
            statement.setString(2, promotionStatus);
            statement.setString(3, admNo);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<String[]> searchStudentsByName(String query) {
        List<String[]> students = new ArrayList<>();
        String sqlQuery = "SELECT * FROM students WHERE name LIKE ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String[] student = {
                        String.valueOf(resultSet.getInt("admno")),
                        resultSet.getString("name"),
                        resultSet.getString("gender"),
                        String.valueOf(resultSet.getInt("form")),
                        String.valueOf(resultSet.getInt("term")),
                        String.valueOf(resultSet.getInt("year"))
                };
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public List<String[]> getStudentsByFormAndTerm(int form, int term) {
        List<String[]> students = new ArrayList<>();
        String query = "SELECT * FROM students WHERE form = ? AND term = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, form);
            statement.setInt(2, term);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String[] student = {
                        String.valueOf(resultSet.getInt("admno")),
                        resultSet.getString("name"),
                        resultSet.getString("gender"),
                        String.valueOf(resultSet.getInt("form")),
                        String.valueOf(resultSet.getInt("term")),
                        String.valueOf(resultSet.getInt("year"))
                };
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public void addSubject(String name, String description) {
        String query = "INSERT INTO subjects (name, description) VALUES (?, ?)";
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubject(int id, String name, String description) {
        String query = "UPDATE subjects SET name = ?, description = ? WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSubject(int id) {
        String query = "DELETE FROM subjects WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------
    // Fetching Unique Years for Reports
    // -----------------------------------------

    public List<String> getYearsFromDatabase() {
        List<String> years = new ArrayList<>();
        String query = "SELECT DISTINCT year FROM results WHERE year <= YEAR(CURDATE()) ORDER BY year ASC";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                years.add(resultSet.getString("year"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }

    public List<String[]> searchSubjectsByName(String query) {
        List<String[]> subjects = new ArrayList<>();
        String sqlQuery = "SELECT * FROM subjects WHERE name LIKE ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String[] subject = {
                        String.valueOf(resultSet.getInt("id")), // ID
                        resultSet.getString("name"),           // Name
                        resultSet.getString("description")     // Description
                };
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }


}
