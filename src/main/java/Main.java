import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;


public class Main extends Application {
	
	static String resourcesDir;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static int StringMinsecToSec(String value) {
		// Convert String 15:01 to minsec: 15 mins, 01 seconds
		// And then convert that into seconds (minsecToSec())
		
		String min = String.valueOf(value.toCharArray(), 0, 2);
		String sec = String.valueOf(value.toCharArray(), 3, 2);
		
		return minsecToSec(Integer.parseInt(min), Integer.parseInt(sec));
	}
	
	public static String minsecToStringMinsec(int min, int secs) {
		return String.format("%02d:%02d", min, secs);
	}
	
	public static String secToStringMinsec(int secs) {
		int[] minsec = secToMinsec(secs);
		return minsecToStringMinsec(minsec[0], minsec[1]);
	}
	
	public static int minsecToSec(int mins, int secs) {
		return secs + (mins * 60);
	}
	
	public static int[] secToMinsec(int secs) {
		return new int[]{(int) Math.floor(secs / 60), secs % 60};
	}
	
	private void setUpDirectories() {
		//"file:\\" +
		resourcesDir = System.getProperty("user.dir") + "/resources/";
		
	}
	
	@Override
	public void start(Stage window) throws Exception {
		setUpDirectories();
		Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		
		window.setTitle("Simply Done");
		Scene scene = new Scene(root, 620, 700);
		scene.getStylesheets().add("style.css");
		window.setScene(scene);
		
		System.out.println("testing out scheduler branch");
		
		try {
			String location = resourcesDir + "default-icon.png";
			window.getIcons().add(new Image(new File(location).toURI().toString()));
		} catch (Exception iconError) {
			System.out.println("Error: application icon not found");
		}
		
		window.show();
		
	}
	
	
}
