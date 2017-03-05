import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.Glyph;

import java.net.URL;
import java.util.ResourceBundle;

public class SchedulerController implements Initializable {
	
	private static ObservableList<Task> notDoneTasks;
	private static ObservableList<Task> doneTasks;
	
	@FXML private JFXListView<Task> tasksListView;
	
	@FXML private JFXSlider newTaskTimerSlider;
	@FXML private Label newTaskMinsLabel, newTaskSecsLabel;
	@FXML private JFXTextField newTaskNameTextField;
	@FXML private JFXButton newTaskButton, startTasksButton;
	@FXML private HBox editTaskBox;
	@FXML private JFXToggleButton tasksViewSwitch;
	@FXML private JFXColorPicker newTaskColour;
	private boolean editModeActive;
	
	public SchedulerController() {
		editModeActive = false;
	}
	
	@FXML public void initialize(URL location, ResourceBundle resources) {
		notDoneTasks = FXCollections.observableArrayList();
		doneTasks = FXCollections.observableArrayList();
		
		addSampleData();
		initializeTaskViewList();
		listenForTaskChanges();
		
		newTaskMinsLabel.textProperty().bind(newTaskTimerSlider.valueProperty().asString(("%.0f")));
		startTasksButton.setStyle("-fx-text-fill: #12854a; -fx-background-color: #101820; -fx-font-weight: bold");
		//15202b
		deactivateEditMode();
		
	}
	
	private void initializeTaskViewList() {
		tasksListView.setItems(notDoneTasks);
		
		useCustomCell();
		
		tasksListView.setOnScrollTo(Event::consume);
		tasksListView.setOnKeyPressed(Event::consume);
		
		tasksListView.setOnMouseClicked(v -> {
			if (tasksListView.getFocusModel().getFocusedItem() != null) {
				activateEditMode();
			}
		});
		
	}
	
	private void addSampleData() {
		newTask("Email Mark", 1, Color.web("#e67e22"));
		newTask("Commit latest build to GitHub", 1, Color.web("#2ecc71"));
		newTask("Update documents", 1, Color.web("#e74c3c"));
		newTask("Finish writing report", 1, Color.web("#3498db"));
		
	}
	
	private void useCustomCell() {
		tasksListView.setCellFactory(v -> new CustomCell());
		
	}
	
	// BUTTON CLICKS
	
	@FXML public void clickNewTaskButton(Event e) {
		
		if (editModeActive) {
			Task task = tasksListView.getSelectionModel().getSelectedItem();
			task.setMinutes((int)newTaskTimerSlider.getValue());
			task.setName(newTaskNameTextField.getText());
			task.setColour(newTaskColour.getValue());
			
			deactivateEditMode();
			
		} else if (!newTaskNameTextField.getText().equals("")) {
			// Makes sure the textfield isn't empty
			newTask();
			resetEditModeUI();
			
		}
		
		clearListViewSelection();
		
	}
	
	@FXML private void clickColourPicker() {
		Color colour = newTaskColour.getValue();
		updateEditModeColours(colour);
	}
	
	@FXML private void clickStartTasks() {
		deactivateEditMode();
		Main.switchScene();
	}
	
	@FXML public void clickToggleTasksView() {
		if(tasksViewSwitch.isSelected()) {
			if (editModeActive) {
				deactivateEditMode();
			}
			tasksListView.setItems(doneTasks);
			startTasksButton.setDisable(true);
			editTaskBox.setDisable(true);
			
		}
		else {
			tasksListView.setItems(notDoneTasks);
			editTaskBox.setDisable(false);
			startTasksButton.setDisable(false);
			
		}
	}
	
	// EDIT MODE
	
	private void resetEditModeUI() {
		Color colour = Color.web("#12854A");
		//String c = ClockView.colorToHex(colour);
		//newTaskColour.setStyle("fx-base: " + c);
		newTaskColour.setValue(colour);
		updateEditModeColours(colour);
		
		editTaskBox.setStyle("-fx-background-color: transparent");
		newTaskButton.setText("ADD TASK");
		newTaskTimerSlider.setValue(10.0);
		newTaskNameTextField.setText("");
		
	}
	
	private void updateEditModeColours(Color colour) {
		String c = String.format( "#%02X%02X%02X",
				(int)( colour.getRed() * 255 ),
				(int)( colour.getGreen() * 255 ),
				(int)( colour.getBlue() * 255 ) );
		
		newTaskMinsLabel.setStyle("-fx-text-fill: " + c);
		newTaskSecsLabel.setStyle("-fx-text-fill: " + c);
		newTaskNameTextField.setStyle("-fx-text-fill: " + c);
		newTaskNameTextField.setFocusColor(colour);
		newTaskButton.setStyle("-fx-background-color: " + c);
		
		// + ";" + "-fx-background-color: " + c
	}
	
	private void deactivateEditMode() {
		editModeActive = false;
		resetEditModeUI();
		clearListViewSelection();
	}
	
	private void activateEditMode() {
		Task task = tasksListView.getFocusModel().getFocusedItem();
		if(task.isNotDone()) {
			// Item selected; let's update the task edit area
			editModeActive = true;
			newTaskButton.setText("UPDATE");
			newTaskTimerSlider.setValue(task.getMinutes());
			newTaskNameTextField.setText(task.getName());
			
			//String c = ClockView.colorToHex(task.getColour());
			//newTaskColour.setStyle("fx-base: " + c);
			newTaskColour.setValue(task.getColour());
			updateEditModeColours(task.getColour());
			editTaskBox.setStyle("-fx-background-color: #e3e9ed");
			
		}
		
	}
	
	// OTHER
	
	private void newTask() {
		if(notDoneTasks.size() == 10) {
			System.out.println("Soo many tasks. Please mark one as done first!");
			//Alert alert = new Alert();
		} else {
			notDoneTasks.add(new Task(newTaskNameTextField.getText(), Integer.parseInt(newTaskMinsLabel.getText()),newTaskColour.getValue()));
		}
	}
	
	private void newTask(String name, int mins, Color colour) {
		notDoneTasks.add(new Task(name, mins, colour));
	}
	
	private void clearListViewSelection() {
		tasksListView.getSelectionModel().clearSelection();
	}
	
	private void listenForTaskChanges() {
		notDoneTasks.addListener((ListChangeListener<Task>) c -> deactivateEditMode());
		
		doneTasks.addListener((ListChangeListener<Task>) c -> deactivateEditMode());
	}
	
	//////////////////////////////
	// CUSTOM CELL FOR LISTVIEW //
	//////////////////////////////
	
	static class CustomCell extends ListCell<Task> {
		
		private static Glyph trash, check;
		// private static Glyph play, stop;
		final HBox container = new HBox();
		final Text minutesText = new Text("10");
		final Text secondsText = new Text(":00");
		final Separator separator = new Separator();
		final Text taskNameText = new Text("GET GOOD MR TEXT");
		final Pane pane = new Pane();
		final JFXButton deleteButton = new JFXButton("");
		final JFXButton doneButton = new JFXButton("");
		
		Task task = null;
		
		private CustomCell() {
			super();
			
			setUpGlyphs();
			setProperties();
			
			deleteButton.setOnAction(v -> {
				updateTaskVariable();
				clickDelete();
			});
			
			doneButton.setOnAction(v -> {
				updateTaskVariable();
				clickDone();
			});
			
		}
		
		private void setUpGlyphs() {
			trash = new Glyph("FontAwesome", "TRASH_ALT");
			check = new Glyph("FontAwesome", "CHECK_SQUARE");
		}
		
		private void updateTaskVariable() {
			task = getItem();
		}
		
		// PERFORMING ACTIONS ON TASKS
		
		private void clickDelete() {
			if (task.isNotDone()) {
				notDoneTasks.remove(task);
			} else {
				doneTasks.remove(task);
			}
		}
		
		private void clickDone() {
			if (task.isNotDone()) {
				task.setNotDone(false);
				doneTasks.add(task);
				notDoneTasks.remove(task);
			} else {
				task.setNotDone(true);
				notDoneTasks.add(task);
				doneTasks.remove(task);
			}
		}
		
		// UPDATING THE CELL APPEARANCE
		
		@Override
		public void updateItem(Task task, boolean empty) {
			super.updateItem(task, empty);
			
			if (empty || task == null) {
				setGraphic(null);
			} else {
				
				minutesText.textProperty().bind(task.minutesProperty().asString());
				taskNameText.textProperty().bind(task.nameProperty());
				//("%.0f")
				
				setGraphic(container);
			}
			
			if(!getListView().getItems().isEmpty()){
				if(!getListView().getItems().get(0).isNotDone()){
					//	getListView().getSelectionModel().setSelectionMode(new SelectionMode());
					
				}
			}
			
		}
		
		// SETTING UP THE CELL
		
		private void setProperties() {
			container.setAlignment(Pos.CENTER);
			
			container.setPrefSize(390.0, 25.0);
			
			separator.setOrientation(Orientation.VERTICAL);
			HBox.setMargin(separator, new Insets(0, 13.0, 0, 13.0));
			
			setupText(minutesText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.RIGHT);
			setupText(secondsText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.LEFT);
			setupText(taskNameText, 12.0, 5.0, 5.0, 5.0, 5.0, TextAlignment.CENTER);
			
			//minutesText.setWrappingWidth(50.0);
			HBox.setHgrow(taskNameText, Priority.ALWAYS);
			HBox.setHgrow(pane, Priority.ALWAYS);
			
			setupButton(deleteButton);
			setupButton(doneButton);
			
			deleteButton.setGraphic(trash);
			doneButton.setGraphic(check);
			
			container.getChildren().addAll(minutesText, secondsText, separator, taskNameText, pane, deleteButton, doneButton);
		}
		
		private void setupText(Text text, double fontSize, double top, double right, double bottom, double left, TextAlignment alignment) {
			text.setTextAlignment(alignment);
			text.setTextOrigin(VPos.CENTER);
			text.setFont(new Font(fontSize));
			HBox.setMargin(text, new Insets(top, right, bottom, left));
		}
		
		private void setupButton(Button button) {
			button.setAlignment(Pos.CENTER);
			button.setContentDisplay(ContentDisplay.CENTER);
			button.setPrefSize(25.0, 25.0);
			button.setTextAlignment(TextAlignment.CENTER);
			button.setFocusTraversable(false);
			HBox.setMargin(button, new Insets(5.0));
		}
		
	}
	
	public static ObservableList<Task> getNotDoneTasks() {
		return notDoneTasks;
	}
	
}