import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.hildan.fxgson.FxGson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainController extends Stage {
	
	private final StackPane pane = new StackPane();
	private SchedulerController schedulerController;
	
	public MainController()  {
		loadSchedulerFXMLLoader();
		loadScene();
		
		if (schedulerController!=null) {
			schedulerController.sceneActiveProperty().addListener(v -> {
				// if the sceneActive property is updated, then...
				if (!schedulerController.isSceneActive()) {
					loadClockView();
				}
			});
		}
		
		try {
			loadSaveData();
		} catch (Exception e) {
			System.out.println("No file to read.");
		}
		
	}
	
	private void loadSaveData() {
		ObservableList<Task> list = FXCollections.observableArrayList();
		try {
			list.addAll(readGsonStream());
		} catch (IOException e) {
			System.out.println("Error reading file. Please turn it off and on again.");
			System.out.println(e);
		}
		ObservableList<Task> doneList = FXCollections.observableArrayList();
		for (Task task : list) {
			if(!task.isNotDone()) {
				doneList.add(task);
				list.remove(task);
			}
		}
		
		schedulerController.setDoneTasks(list);
		schedulerController.setDoneTasks(doneList);
		
	}
	
	private ObservableList<Task> readGsonStream() throws IOException {
		Gson gson = FxGson.coreBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		InputStream input = new FileInputStream("src\\tasks.json");
		
		JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
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
	
	private void loadSchedulerFXMLLoader() {
		FXMLLoader schedulerFXMLLoader = new FXMLLoader(getClass().getResource("SchedulerView.fxml"));
		
		try {
			pane.getChildren().add(schedulerFXMLLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		schedulerController = schedulerFXMLLoader.getController();
	}
	
	private void loadScene() {
		this.setScene(new Scene(pane, 620, 620));
		this.show();
		
		// ensure the window closes correctly
		this.setOnCloseRequest(v -> {
			Platform.exit();
			System.exit(0);
		});
		
		this.setTitle("Simply Done");
		this.setResizable(false);
		
		// try to load application icon
		// this implementation makes the file handling platform-agnostic
		// so the icon should work on different platforms
		// (however, setting the icon Dock icon on Mac requires making additional calls
		try {
			// new approach for adding the icon makes it much better for cross-platform support
			//window.getIcons().add(new Image(new File(location).toURI().toString()));
			this.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
		} catch (Exception iconError) {
			System.out.println("Error: application icon not found");
		}
	}
	
	private void loadClockView() {
		// it's no longer active... time to bring in the countdown timer
		ClockView clockView = new ClockView(schedulerController.getNotDoneTasks());
		// set up a new clock scene every time to ensure it
		// reflects the latest updates to the to-do list
		// enable css stylesheet so pause and stop buttons change colour properly
		switchScene(false, clockView.getNode());
		
		clockView.sceneActiveProperty().addListener(u -> {
			if (!clockView.isSceneActive()) {
				switchScene(true, clockView.getNode());
			}
		});
	}
	
	private  void switchScene(boolean isClockActive, Node nodeClock) {
		// use pause transition to give visual feedback to users and prevent it feeling too sudden
		PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(300));
		pause.setOnFinished(v -> {
			if (isClockActive) {
				// bring the scheduler controller to the front of the stackpane
				schedulerController.setSceneActive(true);
				// remove the clockview from the stackpane
				pane.getChildren().remove(nodeClock);
			} else {
				// remove the clockview to the stackpane
				pane.getChildren().add(nodeClock);
			}
		});
		// remember to play the transition, otherwise method won't be called
		pause.play();
	}
	
	
}
	
	