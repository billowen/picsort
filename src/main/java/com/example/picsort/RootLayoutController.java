package com.example.picsort;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class RootLayoutController {
    @FXML
    private TextField inputField;

    @FXML
    private TextField outputField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button startButton;

    private Stage parentStage;

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    @FXML
    private void handleClickInputBtn() {
        DirectoryChooser dc = new DirectoryChooser();
        File dir = dc.showDialog(parentStage);

        if (dir != null) {
            inputField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void handleClickOutputBtn() {
        DirectoryChooser dc = new DirectoryChooser();
        File dir = dc.showDialog(parentStage);

        if (dir != null) {
            outputField.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void handleClickStartBtn() {
        String message = "";
        if (inputField.getText() == null || inputField.getText().isEmpty()) {
            message += "Please select the input directory.\n";
        } else {
            File f = new File(inputField.getText());
            if (!f.exists()) {
                message += "The selected input directory is not existed.\n";
            } else if (!f.isDirectory()) {
                message += "The input directory should be a directory.\n";
            }
        }

        if (outputField.getText() == null || outputField.getText().isEmpty()) {
            message += "Please select the output directory.\n";
        }

        if (!message.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(parentStage);
            alert.setTitle("Error");
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            startButton.setDisable(true);
            Runnable r = () -> {
                LocalDate birthday = LocalDate.of(2019, 3, 27);
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zdt = birthday.atStartOfDay(zoneId);
                Date birthTime = Date.from(zdt.toInstant());

                String inputDir = inputField.getText();
                String outputDir = outputField.getText();
                File[] files = new File(inputField.getText()).listFiles();

                if (files != null) {
                    int cnt = 0;
                    for (File pic : files) {
                        if (pic.isDirectory()) {
                            continue;
                        }
                        if (!pic.getName().toLowerCase().endsWith(".jpg")) {
                            continue;
                        }
                        String name = pic.getName();
                        try {
                            Metadata metadata = JpegMetadataReader.readMetadata(pic);
                            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            int year = localDate.getYear();
                            File newDir = new File(outputDir, Integer.toString(year));
                            if (!newDir.exists()) {
                                newDir.mkdir();
                            }
                            long days = (date.getTime() - birthTime.getTime()) / (3600 * 1000 * 24) + 1;
                            long years = days / 365;

                            String newName = String.format("%d-%d-%s", years, days, name);
                            System.out.println(newName);
                            Path copied = Paths.get(outputDir, String.valueOf(year), newName);
                            Path original = Paths.get(inputDir, name);
                            Files.copy(original, copied, StandardCopyOption.REPLACE_EXISTING);
                            final double progress = (++cnt) / (double) files.length;
//                            Platform.runLater(() -> progressBar.setProgress( progress ));
                            progressBar.progressProperty().set(progress);
                        } catch (JpegProcessingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initOwner(parentStage);
                    alert.setTitle("Successful");
                    alert.setContentText("Successful.");
                    alert.showAndWait();
                    startButton.setDisable(false);
                });




            };

            Thread t = new Thread(r);
            t.start();
        }
    }

    static class GenericExtFilter implements FilenameFilter {

        private String ext;

        public GenericExtFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(ext));
        }
    }
}
