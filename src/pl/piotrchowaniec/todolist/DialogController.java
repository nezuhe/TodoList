package pl.piotrchowaniec.todolist;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pl.piotrchowaniec.todolist.datamodel.TodoData;
import pl.piotrchowaniec.todolist.datamodel.TodoItem;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextArea detailsTextArea;
    @FXML
    private DatePicker deadlinePicker;

    public TodoItem processResult() {
        String description = descriptionTextField.getText().trim();
        String details = detailsTextArea.getText().trim();
        LocalDate deadline = deadlinePicker.getValue();

        TodoItem newItem = new TodoItem(description, details, deadline);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }
}
