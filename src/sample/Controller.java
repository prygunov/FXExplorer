package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {

  File path = new File("C:\\");
  boolean find = false;

  @FXML
  private TextField tfPath;
  @FXML
  private ListView lvWindow;

  @FXML
  public void initialize() {
    pathUpdated();
    lvWindow.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount()==2)
          if (find)
            openFile((String) lvWindow.getSelectionModel().getSelectedItem());
          else{
            String old = path.getAbsolutePath();
            if (!path.getAbsolutePath().endsWith("\\"))
              old += "\\";

            String newPath = old + lvWindow.getSelectionModel().getSelectedItem();

            if (new File(newPath).isDirectory()){
              tfPath.setText(newPath);
              updateList(newPath);
            }else openFile(newPath);
          }
      }
    });
  }

  public void alert(String message){
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Ошибка");
    alert.setHeaderText(message);
    alert.showAndWait();
  }

  public void toParent(){
    if (find){
      tfPath.setText(path.getAbsolutePath());
      updateList(path.getAbsolutePath());
    }else {
      String newPath = path.getAbsolutePath();
      if (newPath.lastIndexOf("\\") != 2) {
        if (newPath.endsWith("\\")) {
          newPath = newPath.substring(0, newPath.length() - 2);
        }
        newPath = newPath.substring(0, newPath.lastIndexOf("\\"));
      } else newPath = newPath.substring(0, newPath.lastIndexOf("\\") + 1);
      tfPath.setText(newPath);
      updateList(newPath);
    }
  }

  public void openFile(String absolutePath){
    File file = new File(absolutePath);
    if (file.exists() && file.canRead()) try {
      FileReader fileReader = new FileReader(file);
      Scanner scanner = new Scanner(fileReader);
      StringBuilder builder = new StringBuilder();
      while (scanner.hasNextLine()) {
        builder.append(scanner.nextLine()).append("\n");
      }
      ScrollPane root = new ScrollPane();
      Scene scene = new Scene(root, 500, 450);
      Text text = new Text(builder.toString());
      text.wrappingWidthProperty().bind(scene.widthProperty());
      root.setFitToWidth(true);
      root.setContent(text);
      Stage stage = new Stage();
      stage.setTitle(file.getName());
      stage.setScene(scene);
      stage.show();
    } catch (Exception e) {
      alert(e.getMessage());
    }
    else {
      alert("Не удалось открыть файл");
    }
  }

  public void pathUpdated() {
    updateList(tfPath.getText());
  }
  
  public void updateList(String key){
    File path = new File(key);
    if (path.exists()){
      find = false;
      if (path.isDirectory()){
        if (path.canRead() && path.list()!=null) {
          this.path = path;
          ObservableList<String> files = FXCollections.observableArrayList(path.list());
          lvWindow.setItems(files);
        }else alert("Невозможно отобразить директорию");
      }else{
        openFile(path.getAbsolutePath());
      }
    }else {
      ObservableList<String> files = FXCollections.observableArrayList(new ArrayList<>());

      for (File file : find(this.path, tfPath.getText()))
        files.add(file.getAbsolutePath());

      if (files.size()==0) {
        alert("Ничего не нашлось по данному запросу :(");
        tfPath.setText(this.path.getAbsolutePath());
      }
      else{
        find = true;
        lvWindow.setItems(files);
      }

    }
  }

  public List<File> find(File path,String key){
    if (path.listFiles()!=null) {
      List<File> files = new ArrayList<>();
      for (File file : path.listFiles()) {
        if (file.isDirectory()) {
          files.addAll(find(file, key));
        } else if (file.getName().contains(key)) {
          files.add(file);
        }
      }
      return files;
    }else return new ArrayList<>();
  }

}
