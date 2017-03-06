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
		setUpDirectories();
		window = primaryStage;
		window.setTitle("Simply Done");
		window.setResizable(false);
		
		try {
			String location = resourcesDir + "default-icon.png";
			window.getIcons().add(new Image(new File(location).toURI().toString()));
		} catch (Exception iconError) {
			System.out.println("Error: application icon not found");
		}
		
		System.out.println( getClass().getResource(getClass().getSimpleName() + ".class") );
		System.out.println(getClass().getResource("scheduler.fxml"));
		Parent root = FXMLLoader.load(getClass().getResource("scheduler.fxml"));
		schedulerScene = new Scene(root, 620, 620);
		//schedulerScene.getStylesheets().add("style.css");
		
		clockActive = false;
		window.setScene(schedulerScene);
		
		window.show();
		
		window.setOnCloseRequest(v -> {
			Platform.exit();
			System.exit(0);
		});
		
		//SchedulerController.getNotDoneTasks().add(new Task("Get good", 1, Color.web("#2ecc71")));
		
	}
	
	public static void switchScene() {
		PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(300));
		pause.setOnFinished(v -> {
			if(clockActive) {
				// load tasks schedulerScene
				clockActive = false;
				window.setScene(schedulerScene);
			} else {
				// load clock
				ClockView clock = new ClockView();
				Scene clockScene = clock.getScene();
				clockScene.getStylesheets().add("style.css");
				window.setScene(clockScene);
				clockActive = true;
			}
		});
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
