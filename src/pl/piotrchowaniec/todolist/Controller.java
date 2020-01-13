package pl.piotrchowaniec.todolist;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import pl.piotrchowaniec.todolist.datamodel.TodoData;
import pl.piotrchowaniec.todolist.datamodel.TodoItem;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    private List<TodoItem> todoItems;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea todoItemDetails;

    @FXML
    private Label dueLabel;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton todayItemsToggle;

    public void initialize() {
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            TodoItem item = todoListView.getSelectionModel().getSelectedItem();
            deleteItem(item);
        });
        listContextMenu.getItems().add(deleteMenuItem);

        todoItems = new ArrayList<>();
        todoItems.addAll(TodoData.getInstance().getTodoItems());

        todoListView.getSelectionModel().selectedItemProperty().addListener((observableValue, todoItem, t1) -> {
            if (t1 != null) {
                handleClickListView();
            }
        });


        SortedList<TodoItem> sortedList = new SortedList<>(TodoData.getInstance().getTodoItems(),
                new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dueLabel.setVisible(false);

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean b) {
                        super.updateItem(todoItem, b);
                        if (b) {
                            setText(null);
                        } else {
                            setText(todoItem.getDescription());
                            if (todoItem.getDeadline().isBefore(LocalDate.now())) {
                                setTextFill(Color.RED);
                            } else {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                    if (isNowEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(listContextMenu);
                    }
                });

                return cell;
            }
        });
    }

    @FXML
    public void showAddItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController dialogController = fxmlLoader.getController();
            TodoItem newItem = dialogController.processResult();
            todoListView.getSelectionModel().select(newItem);
        }
    }

    @FXML
    public void todayItemsToggleHandler() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (todayItemsToggle.isSelected()) {
            FilteredList<TodoItem> filteredList = TodoData.getInstance().getTodoItems()
                    .filtered(new Predicate<TodoItem>() {
                        @Override
                        public boolean test(TodoItem todoItem) {
                            return todoItem.getDeadline().isEqual(LocalDate.now());
                        }
                    });
            todoListView.setItems(filteredList);
            if (filteredList.isEmpty()) {
                todoItemDetails.clear();
                dueLabel.setVisible(false);
                deadlineLabel.setVisible(false);

            } else if (filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
                dueLabel.setVisible(true);
                deadlineLabel.setVisible(true);
            }
        } else {
            todoListView.setItems(TodoData.getInstance().getTodoItems());
        }
    }

    public void handleClickListView() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        if (isAnyItemSelected()) {
            todoItemDetails.setText(item.getDetails());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            deadlineLabel.setText(df.format(item.getDeadline()));
            setDeadlineLabelsVisible();
        }
    }

    @FXML
    public void handleDeleteKeyPressed(KeyEvent key) {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (key.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    public boolean isAnyItemSelected() {
        return todoListView.getSelectionModel().getSelectedItem() != null;
    }

    public void setDeadlineLabelsVisible() {
        dueLabel.setVisible(isAnyItemSelected());
        deadlineLabel.setVisible(isAnyItemSelected());
    }

    private void deleteItem(TodoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Selected Item");
        alert.setHeaderText("Delete Item: " + item.getDescription());
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}
