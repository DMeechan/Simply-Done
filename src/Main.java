import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;


public class Main extends Application {
	
	static String resourcesDir;
	private static Stage window;
	private static Scene schedulerScene;
	private static boolean clockActive;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// set up basic window - all activity will happen inside the window object
		setUpDirectories();
		window = primaryStage;
		window.setTitle("Simply Done");
		window.setResizable(false);
		
		// try to load application icon
		// this implementation makes the file handling platform-agnostic
		// so the icon should work on different platforms
		// (however, setting the icon Dock icon on Mac requires making additional calls
		try {
			String location = resourcesDir + "default-icon.png";
			window.getIcons().add(new Image(new File(location).toURI().toString()));
		} catch (Exception iconError) {
			System.out.println("Error: application icon not found");
		}
		
		// set up scheduler interface from FXML file
		Parent root = FXMLLoader.load(getClass().getResource("scheduler.fxml"));
		schedulerScene = new Scene(root, 620, 620);
		//schedulerScene.getStylesheets().add("style.css");
		
		clockActive = false;
		window.setScene(schedulerScene);
		window.show();
		
		// ensure the window closes correctly
		window.setOnCloseRequest(v -> {
			Platform.exit();
			System.exit(0);
		});
		
		//SchedulerController.getNotDoneTasks().add(new Task("Get good", 1, Color.web("#2ecc71")));
		
	}
	
	public static void switchScene() {
		// use pause transition to give visual feedback to users and prevent it feeling too sudden
		PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(300));
		pause.setOnFinished(v -> {
			if(clockActive) {
				// currently on clock scene
				// switch back to scheduler scene
				clockActive = false;
				window.setScene(schedulerScene);
			} else {
				// set up a new clock scene every time to ensure it
				// reflects the latest updates to the to-do list
				ClockView clock = new ClockView();
				Scene clockScene = clock.getScene();
				// enable css stylesheet so pause and stop buttons change colour properly
				clockScene.getStylesheets().add("style.css");
				window.setScene(clockScene);
				clockActive = true;
			}
		});
		// remember to play the transition, otherwise method won't be called
		pause.play();
	}
	
	
	private static String minsecToStringMinsec(int min, int secs) {
		return String.format("%02d:%02d", min, secs);
	}
	
	public static String secToStringMinsec(int secs) {
		int[] minsec = secToMinsec(secs);
		return minsecToStringMinsec(minsec[0], minsec[1]);
	}
	
	private static int[] secToMinsec(int secs) {
		return new int[]{(int) Math.floor(secs / 60), secs % 60};
	}
	
	private void setUpDirectories() {
		//"file:\\" +
		resourcesDir = System.getProperty("user.dir") + "/resources/";
		
	}
	
}
