package nl.saxion.ptbc.CSVLoader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;

/**
 * Controller class for managing CSV file uploads, imports, and viewing imported files.
 * Handles user interactions with the UI components and file operations.
 */
public class CSVLoaderController {
    private File file;
    private ArrayList<File> files = new ArrayList<>();

    @FXML
    private Button uploadButton;
    @FXML
    private Button viewButton;
    @FXML
    private Button importButton;
    @FXML
    private Label fileNameLabel;

    /**
     * Initializes the controller by setting up button actions and event handlers.
     * Handles file upload, import, and viewing of imported files.
     */
    @FXML
    public void initialize() {
        try {
            uploadButton.setOnAction(event -> {
                file = uploadFile(uploadButton.getScene().getWindow());
                fileNameLabel.setText("Selected File: " + file.getName());
            });
        } catch (CSVLoaderException c) {
            System.err.println(c.getMessage());
        }

        try {
            importButton.setOnAction(event -> {
                if (file.getName().endsWith(".csv")) {
                    files.add(file);
                    System.out.println("File added: " + file.getName());
                    fileNameLabel.setText("File imported: " + file.getName());
                    file = null;
                }
            });
        } catch (CSVLoaderException c) {
            System.err.println(c.getMessage());
        }

        viewButton.setOnAction(event -> {
            if (files.isEmpty()) {
                System.out.println("No files in the system");
            } else {
                displayFiles();
            }
        });
    }

    /**
     * Opens a file chooser dialog for the user to select a file.
     *
     * @param window The parent window for the file chooser dialog.
     * @return The selected file.
     * @throws CSVLoaderException if no file is selected.
     */
    private File uploadFile(Window window) throws CSVLoaderException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file");

        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getName());
        } else {
            throw new CSVLoaderException("No file has been selected");
        }

        return selectedFile;
    }

    /**
     * Displays the list of imported files in the console.
     * Each file is displayed with a unique number for identification.
     */
    public void displayFiles() {
        int counter = 1;
        for (File file : files) {
            System.out.println("File number: " + counter + " " + file);
            counter++;
        }
    }
}
