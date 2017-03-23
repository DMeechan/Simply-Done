import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jfoenix.controls.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.hildan.fxgson.FxGson;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;

public class SchedulerController implements Initializable {
	
	@FXML private JFXSlider newTaskTimerSlider;
	@FXML private Label newTaskMinsLabel, newTaskSecsLabel;
	@FXML private JFXTextField newTaskNameTextField;
	@FXML private JFXButton newTaskButton, startTasksButton;
	@FXML private HBox editTaskBox;
	@FXML private JFXToggleButton tasksViewSwitch;
	//private final String saveFileLocation = "src" + java.nio.file.FileSystems.getDefault().getSeparator() + "tasks.json";
	@FXML private JFXColorPicker newTaskColour;
	// use @FXML injection to avoid overwriting the FXML View's objects and causing problems
	private boolean editModeActive;
	// store them in separate lists so can easily move tasks between them
	private ObservableList<Task> notDoneTasks, doneTasks;
	@FXML private JFXListView<Task> tasksListView;
	private final BooleanProperty sceneActive = new SimpleBooleanProperty();
	private BooleanProperty reset = new SimpleBooleanProperty();
	
	private File savedTasksFile;
	
	public SchedulerController() {
		setSceneActive(true);
		setReset(false);
		editModeActive = false;
	}
	
	@FXML public void initialize(URL location, ResourceBundle resources) {
		
		notDoneTasks = FXCollections.observableArrayList();
		doneTasks = FXCollections.observableArrayList();
		
		// optional to add sample data
		//addSampleData();
		
		setSavedTasksFile();
		loadSaveData();
		writeSaveData();
		
		// set up taskViewList listeners and CustomCells
		initializeTaskViewList();
		
		// listen for changes so edit task pane is disabled
		// if a task is moved or deleted by CustomCell
		// to prevent bugs from occurring
		listenForTaskChanges();
		
		// have the timer length label linked with the slider, and formatted properly!
		newTaskMinsLabel.textProperty().bind(newTaskTimerSlider.valueProperty().asString(("%.0f")));
		startTasksButton.setStyle("-fx-text-fill: #12854a; -fx-background-color: #101820; -fx-font-weight: bold");
		//15202b
		deactivateEditMode();
		
	}
	
	private void initializeTaskViewList() {
		// can easily switch its items between notDoneTasks and doneTasks
		tasksListView.setItems(notDoneTasks);
		
		// need custom cell for custom buttons and general task UI
		useCustomCell();
		
		// ensure that the only way for a task to be selected is by clicking on it
		// having scroll wheel & arrow key selection disabled (Event::consume)
		// prevents bugs from occurring with the edit task pane
		tasksListView.setOnScrollTo(Event::consume);
		tasksListView.setOnKeyPressed(Event::consume);
		
		tasksListView.setOnMouseClicked(v -> {
			if (tasksListView.getFocusModel().getFocusedItem() != null) {
				activateEditMode();
			}
		});
		
	}
	
	private void useCustomCell() {
		tasksListView.setCellFactory(v -> new CustomCell(notDoneTasks, doneTasks));
	}
	
	// FILE SAVING
	
	private void setSavedTasksFile() {
		try {
			//savedTasksFile = new File(this.getClass().getClassLoader().getResource("tasks.json").toURI());
			//savedTasksStream = getClass().getClassLoader().getResourceAsStream("tasks.json");
			Path folder = Paths.get(System.getProperty("user.home") + "/.simply-done");
			System.out.println(folder.toString());
			
			if(!Files.isDirectory(folder)) {
				Files.createDirectory(folder);
				if (System.getProperty("os.name").toLowerCase().contains("windows")) {
					Files.setAttribute(folder, "dos:hidden", Boolean.TRUE,  LinkOption.NOFOLLOW_LINKS);
				}
			}
			System.out.println(System.getProperty("os.name").toLowerCase());
			
			savedTasksFile = new File(folder + "/tasks.json");
			
		} catch (Exception e) {
			Main.outputError(e);
		}
	}
	
	private void loadSaveData() {
		
		if (savedTasksFile.exists()) {
			
			ObservableList<Task> list = FXCollections.observableArrayList();
			
			try {
				list.addAll(readGsonStream(savedTasksFile));
				
				for (Iterator<Task> item = list.iterator(); item.hasNext();) {
					Task task = item.next();
					if(task.isNotDone()) {
						notDoneTasks.add(task);
					} else {
						doneTasks.add(task);
					}
				}
				
			} catch (IOException e) {
				System.out.println("Error reading file. Please turn it off and on again.");
				Main.outputError(e);
			}
			
		} else {
			addSampleData();
		}
		
	}
	
	private ObservableList<Task> readGsonStream(File tasksFile) throws IOException {
		Gson gson = FxGson.coreBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		// change input to abc
		JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(tasksFile), "UTF-8"));
		ObservableList<Task> list = FXCollections.observableArrayList();
		reader.beginArray();
		while (reader.hasNext()) {
			Task task = gson.fromJson(reader, Task.class);
			list.add(task);
		}
		reader.endArray();
		reader.close();
		return list;
		
	}
	
	public void writeSaveData() {
		ObservableList<Task> list = FXCollections.observableArrayList();
		list.addAll(getNotDoneTasks());
		list.addAll(getDoneTasks());
		
		try {
			writeGsonStream(list);
			System.out.println("Save complete!");
		} catch (IOException e) {
			System.out.println("Error writing file. Please turn it off and on again.");
			Main.outputError(e);
		}
		
	}
	
	private void writeGsonStream(ObservableList<Task> list) throws IOException {
		//OutputStream savedTasksStream = getClass().getClassLoader().getResourceAsStream("tasks.json");
		
		Gson gson = FxGson.coreBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		OutputStream outputStream = new FileOutputStream(savedTasksFile);
		
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
		writer.setIndent("  ");
		writer.beginArray();
		for (Task task : list) {
			gson.toJson(task, Task.class, writer);
		}
		writer.endArray();
		writer.close();
		
	}
	
	private void addSampleData() {
		newTask("> This is a task. Here's how you can make your own:", 1, Color.web("#2ecc71"));
		newTask("---> 1. Type the name of a task into the textbox at the top", 2, Color.web("#3498db"));
		newTask("---> 2. Drag the slider to the amount of time you expect the task will take", 3, Color.web("#e74c3c"));
		newTask("---> 3. Click the coloured square on the right and choose a colour for the task", 4, Color.web("#e67e22"));
		newTask("---> 4. Click the 'Add Task' button to add the task to your to-do list", 5, Color.web("#4db6ac"));
		newTask("> Click the checkbox on the right to mark this task as complete!", 6, Color.web("#ffa726"));
		newTask("---> To view all your completed tasks, click 'Show Completed Tasks' above", 7, Color.web("#ba68c8"));
		newTask("---> Once you've added all your tasks, click the Start Tasks button below", 8, Color.web("#4E6A9C"));
		newTask("---> Then a timer will start counting the time left for the first task", 9, Color.web("#66bb6a"));
		newTask("---> And the total time you've got left until every task should be done", 10, Color.web("#64b5f6"));
		newTask("> Delete each of these tasks and then have fun getting everything Simply Done!", 11, Color.web("#12854a"));
		
	}
	
	// BUTTON CLICKS
	
	@FXML public void clickNewTaskButton(Event e) {
		// use same button for both adding tasks and updating tasks - editModeActive is used to switch between modes
		if (editModeActive) {
			// update task's values to those set in the edit task pane
			Task task = tasksListView.getSelectionModel().getSelectedItem();
			task.setMinutes(Integer.parseInt(newTaskMinsLabel.getText()));
			task.setName(newTaskNameTextField.getText());
			task.setColour(newTaskColour.getValue());
			
			deactivateEditMode();
			
		} else if (!newTaskNameTextField.getText().equals("")) {
			// Makes sure the textfield isn't empty
			// set up new task - doesn't need input values because it grabs them directly from the input fields
			newTask();
			resetEditModeUI();
		}
		
		clearListViewSelection();
		writeSaveData();
	}
	
	@FXML private void clickColourPicker() {
		Color colour = newTaskColour.getValue();
		// update the colours of the edit pane when the user picks a colour
		updateEditModeColours(colour);
	}
	
	@FXML private void clickStartTasks() {
		// make sure edit mode isn't disabled
		// otherwise it will still be enabled when the user re-opens the scheduler
		// which would give an inconsistent user experience
		deactivateEditMode();
		setSceneActive(false);
		writeSaveData();
	}
	
	@FXML public void clickToggleTasksView() {
		if(tasksViewSwitch.isSelected()) {
			// switch is active, so display the completed tasks
			if (editModeActive) {
				deactivateEditMode();
			}
			tasksListView.setItems(doneTasks);
			// make sure edit task box and start tasks button are disabled
			// the completed tasks page should only be for viewing completed tasks
			// and marking tasks as not complete
			startTasksButton.setDisable(true);
			editTaskBox.setDisable(true);
			
		}
		else {
			// switch is inactive, so display the to-do list
			// and re-enable the edit task box and start tasks button
			tasksListView.setItems(notDoneTasks);
			editTaskBox.setDisable(false);
			startTasksButton.setDisable(false);
			
		}
		writeSaveData();
	}
	
	@FXML private void reset() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Are you sure you want to delete all data?");
		alert.setContentText("Note that all saved data will be lost and Simply Done will restart.");
		
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			// reset
			setReset(true);
			
		} else {
			// cancel
			
		}
	}
	
	// EDIT MODE
	
	private void resetEditModeUI() {
		// after adding / editing a task, reset the edit task box to its default values
		Color colour = Color.web("#12854A");
		//String c = ClockView.colorToHex(colour);
		//newTaskColour.setStyle("fx-base: " + c);
		newTaskColour.setValue(colour);
		// set colours back to default
		updateEditModeColours(colour);
		
		editTaskBox.setStyle("-fx-background-color: transparent");
		newTaskButton.setText("ADD TASK");
		newTaskTimerSlider.setValue(10.0);
		newTaskNameTextField.setText("");
		
	}
	
	private void updateEditModeColours(Color colour) {
		// need to format the string because otherwise it's returned in a weird format
		// the weird format starts in 0x and ends in 2 additional characters for the alpha layer
		String c = String.format( "#%02X%02X%02X",
				(int)( colour.getRed() * 255 ),
				(int)( colour.getGreen() * 255 ),
				(int)( colour.getBlue() * 255 ) );
		
		// opportunity here for using generic color object property to make it  more efficient and reliable
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
		// take task which has been clicked on by the user
		Task task = tasksListView.getFocusModel().getFocusedItem();
		if(task.isNotDone()) {
			// Need to check item isn't done, because we don't want users editing tasks that are complete
			
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
		// Prevent more than 13 tasks being added because the ListView becomes buggy when displaying more than 13
		// Likely due to something with the Custom Cells
		if(notDoneTasks.size() >= 12) {
			Main.outputError("Too many tasks. Please complete some of them first!");
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
	
	public boolean isSceneActive() {
		return sceneActive.get();
	}
	
	public BooleanProperty sceneActiveProperty() {
		return sceneActive;
	}
	
	public void setSceneActive(boolean sceneActive) {
		this.sceneActive.set(sceneActive);
	}
	
	public ObservableList<Task> getNotDoneTasks() {
		return notDoneTasks;
	}
	
	private ObservableList<Task> getDoneTasks() {
		return doneTasks;
	}
	
	public boolean isReset() {
		return reset.get();
	}
	
	public BooleanProperty resetProperty() {
		return reset;
	}
	
	private void setReset(boolean reset) {
		this.reset.set(reset);
	}
	
	public File getSavedTasksFile() {
		return savedTasksFile;
	}
	
	public void setSavedTasksFile(File savedTasksFile) {
		this.savedTasksFile = savedTasksFile;
	}
	
	//////////////////////////////
	// CUSTOM CELL FOR LISTVIEW //
	//////////////////////////////
	
	static class CustomCell extends ListCell<Task> {
		
		private final Glyph trash = new Glyph("FontAwesome", "TRASH_ALT");
		private final Glyph check = new Glyph("FontAwesome", "CHECK_SQUARE");
		
		// private static Glyph play, stop;
		final HBox container = new HBox();
		final Text minutesText = new Text("10");
		final Text secondsText = new Text(":00");
		final Separator separator = new Separator();
		final Pane spacer = new Pane();
		final Text taskNameText = new Text("TASK NAME TEXT");
		final Pane pane = new Pane();
		final JFXButton deleteButton = new JFXButton("");
		final JFXButton doneButton = new JFXButton("");
		
		ObservableList<Task> notDoneTasks = FXCollections.observableArrayList();
		ObservableList<Task> doneTasks = FXCollections.observableArrayList();
		Task task = null;
		private CustomCell(ObservableList<Task> notDoneTasksList, ObservableList<Task> doneTasksList) {
			super();
			
			notDoneTasks = notDoneTasksList;
			doneTasks = doneTasksList;
			
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
			if (task.isNotDone() && doneTasks.size() < 12) {
					task.setNotDone(false);
					doneTasks.add(task);
					notDoneTasks.remove(task);
					
			} else if (!task.isNotDone() && notDoneTasks.size() < 12){
				task.setNotDone(true);
				notDoneTasks.add(task);
				doneTasks.remove(task);
				
			} else {
				System.out.println("Too many tasks in target list.");
				System.out.println("Please delete a task from target list first.");
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
			
			/*
			if(!getListView().getItems().isEmpty()){
				if(!getListView().getItems().get(0).isNotDone()){
					// getListView().getSelectionModel().setSelectionMode(new SelectionMode());
					
				}
			}
			*/
			
		}
		
		// SETTING UP THE CELL
		
		private void setProperties() {
			container.setAlignment(Pos.CENTER_LEFT);
			container.setPrefSize(390.0, 25.0);
			
			separator.setOrientation(Orientation.VERTICAL);
			HBox.setMargin(separator, new Insets(0, 13.0, 0, 13.0));
			
			//HBox.setHgrow(spacer, Priority.SOMETIMES);
			spacer.setMinWidth(5);
			spacer.setMaxWidth(10);
			
			setupText(minutesText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.RIGHT);
			setupText(secondsText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.LEFT);
			setupText(taskNameText, 12.0, 5.0, 5.0, 5.0, 5.0, TextAlignment.CENTER);
	
			minutesText.setWrappingWidth(19);
			secondsText.setWrappingWidth(19);
			
			//minutesText.setWrappingWidth(50.0);
			HBox.setHgrow(taskNameText, Priority.ALWAYS);
			HBox.setHgrow(pane, Priority.ALWAYS);
			
			setupButton(deleteButton);
			setupButton(doneButton);
			
			deleteButton.setGraphic(trash);
			doneButton.setGraphic(check);
			
			container.getChildren().addAll(minutesText, secondsText,
					//spacer,
					separator, taskNameText, pane, deleteButton, doneButton);
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
	

	
}