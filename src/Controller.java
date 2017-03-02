import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
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

public class Controller implements Initializable {
	
	public static ObservableList<Task> livingTasks;
	//public ListView<Task> taskDisplay;
	public static ObservableList<Task> deadTasks;
	
	@FXML
	ListView<Task> tasksListView;
	
	@FXML
	Slider newTaskTimerSlider;
	@FXML
	Label newTaskTimerLabel;
	@FXML
	TextField newTaskNameTextField;
	@FXML
	Button newTaskButton;
	@FXML
	HBox newUIBox;
	
	private boolean editModeActive;
	private int viewState;
	// 0 = to do list
	// 1 = completed tasks
	// 2 = analytics
	
	public Controller() {
		viewState = 0;
		editModeActive = false;
	}
	
	@FXML
	public void initialize(URL location, ResourceBundle resources) {
		livingTasks = FXCollections.observableArrayList();
		deadTasks = FXCollections.observableArrayList();
		
		newTask("Get good", 0, 32);
		newTask("Get good 2", 0, 32);
		newTask("Do Maths homework", 14, 30);
		// newTask("Wash dishes", 35, 0);
		
		tasksListView.setItems(livingTasks);
		
		useCustomCell();

		tasksListView.setOnScrollTo(new EventHandler<ScrollToEvent<Integer>>() {
			@Override
			public void handle(ScrollToEvent<Integer> event) {

			}
		});

		tasksListView.setOnMouseClicked(v -> {
			if (tasksListView.getFocusModel().getFocusedItem() != null) {
				activateEditMode();
			}

		});
		
		tasksListView.setOnKeyPressed(v -> v.consume());
		
		listenForChanges();
		
	}
	
	@FXML
	public void runTaskButton(Event e) {
		
		if (editModeActive) {
			updateTask();
			deactivateEditMode();
			
		} else if (!newTaskNameTextField.getText().equals("")) {
			// Makes sure the textfield isn't empty
			newTask();
			resetNewTaskUI(false);
			
		}
		
		clearListViewSelection();
		
	}
	
	public void updateTask() {
		// Update selected task
		Task task = tasksListView.getSelectionModel().getSelectedItem();
		task.stop();
		task.setTimerLength(Main.StringMinsecToSec(newTaskTimerLabel.getText()));
		task.setName(newTaskNameTextField.getText());
		//tasksListView.refresh();
	}
	
	public void deactivateEditMode() {
		editModeActive = false;
		newUIBox.setStyle("-fx-background-color: transparent");
		newTaskTimerLabel.setStyle("-fx-text-fill: #2c3e50");
		resetNewTaskUI(false);
		clearListViewSelection();
	}
	
	public void activateEditMode() {
		editModeActive = true;
		
		// Item selected; let's update the task edit area
		Task task = tasksListView.getFocusModel().getFocusedItem();
		
		newTaskButton.setText("UPDATE");
		newUIBox.setStyle("-fx-background-color: #2c3e50;");
		
		newTaskTimerLabel.setStyle("-fx-text-fill: white");
		newTaskTimerLabel.setText(task.getTimeLengthFormatted());
		newTaskTimerSlider.setValue(task.getTimerLength());
		newTaskNameTextField.setText(task.getName());
	}
	
	public void useCustomCell() {
		tasksListView.setCellFactory(v -> {
			return new CustomCell();
		});
	}
	
	// SWITCH DISPLAY
	
	@FXML
	public void switchToLivingTasks() {
		tasksListView.setItems(livingTasks);
		newUIBox.setDisable(false);
		setViewState(0);
		
	}
	
	@FXML
	public void switchToDeadTasks() {
		for (Task task : livingTasks) {
			task.stop();
		}
		if (editModeActive) {
			deactivateEditMode();
		}
		tasksListView.setItems(deadTasks);
		newUIBox.setDisable(true);
		setViewState(1);
	}
	
	@FXML
	public void switchToAnalytics() {
		//setViewState(2);
	}
	
	// MINOR METHODS
	
	@FXML
	public void setTaskTimer() {
		newTaskTimerLabel.setText(Main.secToStringMinsec((int) newTaskTimerSlider.getValue()));
	}
	
	public void newTask() {
		livingTasks.add(new Task(newTaskNameTextField.getText(), Main.StringMinsecToSec(newTaskTimerLabel.getText())));
	}
	
	public void newTask(String name, int mins, int secs) {
		livingTasks.add(new Task(name, Main.minsecToSec(mins, secs)));
	}
	
	public void clearListViewSelection() {
		tasksListView.getSelectionModel().clearSelection();
	}
	
	public void resetNewTaskUI(boolean resetTimer) {
		newTaskButton.setText("ADD TASK");
		newTaskNameTextField.setText("");
		
		if (resetTimer) {
			newTaskTimerLabel.setText("10:00");
			newTaskTimerSlider.setValue(600);
		}
	}
	
	public void listenForChanges() {
		livingTasks.addListener(new ListChangeListener<Task>() {
			@Override
			public void onChanged(Change<? extends Task> c) {
				deactivateEditMode();
				stopAllTasks();
			}
		});
		
		deadTasks.addListener(new ListChangeListener<Task>() {
			@Override
			public void onChanged(Change<? extends Task> c) {
				deactivateEditMode();
				stopAllTasks();
			}
		});
	}
	
	public void stopAllTasks() {
		for (Task task : livingTasks) {
			task.stop();
		}
	}
	
	//////////////////////////////
	// CUSTOM CELL FOR LISTVIEW //
	//////////////////////////////
	
	public int getViewState() {
		return viewState;
	}
	
	
	// GETTERS AND SETTERS
	
	public void setViewState(int viewState) {
		this.viewState = viewState;
		tasksListView.refresh();
		clearListViewSelection();
	}
	
	static class CustomCell extends ListCell<Task> {
		
		HBox container = new HBox();
		
		Text taskTimerText = new Text("10:00");
		Separator separator = new Separator();
		Text taskNameText = new Text("GET GOOD MR TEXT");
		Text overtimeText = new Text("OVERTIME!");
		Pane pane = new Pane();

		private static Glyph play, stop, trash, check;


		Button runButton = new Button("");
		Button deleteButton = new Button("");
		Button doneButton = new Button("");
		
		Task task = null;
		
		public CustomCell() {
			super();

			setUpGlyphs();
			setProperties();


			deleteButton.setOnAction(v -> {
				updateTask();
				clickDelete();
			});
			runButton.setOnAction(v -> {
				updateTask();
				clickRun();
			});
			doneButton.setOnAction(v -> {
				updateTask();
				clickDone();
			});
			
		}

		private void setUpGlyphs() {
			play = new Glyph("FontAwesome", "PLAY");
			stop = new Glyph("FontAwesome", "STOP");
			trash = new Glyph("FontAwesome", "TRASH_ALT");
			check = new Glyph("FontAwesome", "CHECK_SQUARE");
			// remove = new Glyph("FontAwesome", "REMOVE");
		}
		
		public void updateTask() {
			task = getItem();
		}
		
		// PERFORMING ACTIONS ON TASKS
		
		public void clickDelete() {
			if (task.isLiving()) {
				task.stop();
				livingTasks.remove(task);
			} else {
				deadTasks.remove(task);
			}
		}
		
		public void clickRun() {
			setUpLiving(task);
			if (task.isRunning()) {
				task.stop();
				runButton.setGraphic(play);
			} else {
				task.start();
				runButton.setGraphic(stop);
				
			}
		}
		
		public void clickDone() {
			if (task.isLiving()) {
				task.stop();
				task.setLiving(false);
				deadTasks.add(task);
				livingTasks.remove(task);
			} else {
				task.setLiving(true);
				livingTasks.add(task);
				deadTasks.remove(task);
			}
		}
		
		// UPDATING THE CELL APPEARANCE
		
		@Override
		public void updateItem(Task task, boolean empty) {
			super.updateItem(task, empty);
			
			if (empty) {
				setGraphic(null);
			} else {
				if(this.task == null) {
					if (task.isLiving()) {
						setUpLiving(task);
					} else {
						setUpDead(task);
					}
				}
				
				taskNameText.textProperty().bind(task.nameProperty());
				taskTimerText.textProperty().bind(task.timeLengthFormattedProperty());
				
				setGraphic(container);
			}
			
		}
		
		public void setUpLiving(Task task) {
			runButton.setVisible(true);
			
			task.overtimeProperty().addListener(v -> {
				if (task.getOvertime()) {
					this.overtimeText.setVisible(true);
					/*
					setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white");
					this.taskNameText.setFill(Color.WHITE);
					this.taskTimerText.setFill(Color.WHITE);
					*/
					
				} else {
					this.overtimeText.setVisible(false);
					/*
					setStyle("-fx-background-color: transparent");
					this.taskNameText.setFill(Color.BLACK);
					this.taskTimerText.setFill(Color.BLACK);
					*/
				}
			});
		}
		
		public void setUpDead(Task task) {
			runButton.setVisible(false);
		}
		
		// SETTING UP THE CELL
		
		public void setProperties() {
			container.setAlignment(Pos.CENTER);
			
			container.setPrefSize(390.0, 25.0);
			
			separator.setOrientation(Orientation.VERTICAL);
			HBox.setMargin(separator, new Insets(0, 5.0, 0, 5.0));
			
			setupText(taskTimerText, 14.0, 3.0, 8.0, 3.0, 3.0);
			setupText(taskNameText, 12.0, 5.0, 5.0, 5.0, 5.0);
			setupText(overtimeText, 12.0, 5.0, 10.0, 5.0, 10.0);
			
			taskTimerText.setWrappingWidth(50.0);
			HBox.setHgrow(taskNameText, Priority.ALWAYS);
			HBox.setHgrow(pane, Priority.ALWAYS);
			
			setupButton(deleteButton);
			setupButton(runButton);
			setupButton(doneButton);

			runButton.setGraphic(play);
			deleteButton.setGraphic(trash);
			doneButton.setGraphic(check);
			
			
			overtimeText.setVisible(false);
			overtimeText.setFill(Color.RED);
			overtimeText.setStyle("-fx-font-weight: bold");
			
			
			container.getChildren().addAll(taskTimerText, separator, taskNameText, overtimeText, pane, runButton, deleteButton, doneButton);
		}
		
		public void setupText(Text text, double fontSize, double top, double right, double bottom, double left) {
			text.setTextAlignment(TextAlignment.CENTER);
			text.setTextOrigin(VPos.CENTER);
			text.setFont(new Font(fontSize));
			HBox.setMargin(text, new Insets(top, right, bottom, left));
		}
		
		public void setupButton(Button button) {
			button.setAlignment(Pos.CENTER);
			button.setContentDisplay(ContentDisplay.CENTER);
			button.setPrefSize(25.0, 25.0);
			button.setTextAlignment(TextAlignment.CENTER);
			HBox.setMargin(button, new Insets(5.0));
		}
		
	}
	
}