import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application {
	
	static String applicationDir = "file:\\" + System.getProperty("user.dir");
	static String resourcesDir = applicationDir + "\\resources\\";
	
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
		int[] out = {(int) Math.floor(secs / 60), secs % 60};
		return out;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		
		try {
			
			Stage window = primaryStage;
			window.setTitle("Simply Done");
			Scene scene = new Scene(root, 620, 700);
			scene.getStylesheets().add("style.css");
			window.setScene(scene);
			
			try {
				window.getIcons().add(new Image(resourcesDir + "default-icon.png"));
			} catch (Exception iconError) {
				System.out.println("Error: application icon not found");
			}
			
			window.show();
			
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
}
