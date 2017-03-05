import com.jfoenix.controls.JFXButton;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.controlsfx.glyphfont.Glyph;

import java.awt.*;
import java.io.File;

/**
 * Created by Daniel on 05.03.2017.
 */
public class ClockView {
	
	private ObservableList<Task> taskList;
	private Task activeTask;
	private int activeTaskCount = 0;
	private boolean running, overtime, playingCountdownSound;
	
	private Timeline timer;
	private IntegerProperty totalLength = new SimpleIntegerProperty(60);
	private IntegerProperty taskLength = new SimpleIntegerProperty(0);
	private MediaPlayer mediaPlayer;
	
	private VBox container;
	private Rectangle leftSeparator, rightSeparator;
	private Label activeTaskLabel, activeTaskTimer;
	private Gauge timerClock;
	private JFXButton pauseButton, stopButton;
	
	private Glyph stopIcon, pauseIcon, playIcon;
	private ObjectProperty<Color> activeColor = new SimpleObjectProperty<Color>(Color.web("#ffffff"));
	
	ClockView() {
		
		taskList = SchedulerController.getNotDoneTasks();
		
		int combinedLength = 0;
		for (Task t : taskList) {
			combinedLength += t.getMinutes();
		}
		
		setTotalLength(combinedLength); // set the total timer length to combined tasks lengths
		System.out.println(getTotalLength());
		GaugeBuilder builder = GaugeBuilder.create().skinType(Gauge.SkinType.SLIM);
		timerClock = builder.decimals(0).maxValue(getTotalLength()).unit("TIME LEFT TODAY").build();
		timerClock.valueProperty().bind(totalLengthProperty());
		timerClock.setAnimationDuration(100);
		
		setUpGlyphs();
		container = setProperties();
		
		start();
		
	}
	
	public void start() {
		setActiveTask();
		
		setOvertime(false);
		setRunning(true);
		
		timer = new Timeline((new KeyFrame(
				Duration.millis(1000),
				event -> timerTick())));
		timer.setCycleCount(Animation.INDEFINITE);
		
		timer.play();

	}
	
	public Scene getScene() {
		return new Scene(container, 620, 620);
	}

	// BUTTON CLICKS
	
	private void pauseClick() {
		if(isOvertime()) { // turn this button into a stop button if in overtime
			stopClick();
			
		} else if(isRunning()) { // currently running; so pause it
			pauseButton.setGraphic(playIcon);
			pauseTimer();
	
		} else { // currently not running; let's resume
			pauseButton.setGraphic(pauseIcon);
			setRunning(true);
			timer.play();
		}
		
	}
	
	private void stopClick() {
		pauseTimer();
		Main.switchScene();
	}
	
	// TIMER STUFF
	
	private void timerTick(){
		if(isOvertime()) { // keep increasing overtime
			setTotalLength(getTotalLength() + 1);
			
		} else if (getTotalLength() == 0) { // countdown has hit 0; start overtime
			startOvertime();
			
			if(playingCountdownSound) {
				playingCountdownSound = false;
				mediaPlayer.stop();
			}
			
		} else if (getTaskLength() == 1) { // task is done; move to next one
			setTotalLength(getTotalLength() - 1);
			
			if(getTotalLength() > 0) {
				setActiveTask();
			} else {
				setTaskLength(getTaskLength() - 1);
			}
			
		} else { // increment both timers
			setTotalLength(getTotalLength() - 1);
			setTaskLength(getTaskLength() - 1);
			
		}
		
		if (!isOvertime() && getTotalLength() == 20 && !playingCountdownSound) {
			startCountdownSound();
		}
		
	}
	
	private void pauseTimer() {
		timer.stop();
		setRunning(false);
		
		if (playingCountdownSound) {
			mediaPlayer.stop();
		}
	}
	
	public void setActiveTask() {
		if(!(activeTaskCount==taskList.size())) {
			if(isRunning()) {
				Toolkit.getDefaultToolkit().beep();
			}
			activeTask = taskList.get(activeTaskCount);
			System.out.println("New active task: " + activeTask.getName() + " of length: " + activeTask.getMinutes());
			
			setActiveColor(activeTask.getColour());
			setTaskLength(activeTask.getMinutes());
			activeTaskLabel.setText(activeTask.getName().toUpperCase());
			
			String c = String.format( "#%02X%02X%02X",
					(int)( getActiveColor().getRed() * 255 ),
					(int)( getActiveColor().getGreen() * 255 ),
					(int)( getActiveColor().getBlue() * 255 ) );
			
			pauseButton.setStyle("-fx-background-color: " + c);
			stopButton.setStyle("-fx-background-color: " + c);
			activeTaskCount++;
		} else {
			System.out.println("Error: next task doesn't exist...");
		}
	}
	
	private void startCountdownSound() {
		playingCountdownSound = true;
		//String location = Main.resourcesDir + "countdown.mp3";
		String location = Main.resourcesDir + "ticking.mp3";
		Media sound = new Media(new File(location).toURI().toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
	}
	
	private void startOvertime() {
		setOvertime(true);
		Toolkit.getDefaultToolkit().beep();
		setActiveColor(Color.web("#e74c3c"));
		activeTaskLabel.setText("OVERTIME!");
		activeTaskTimer.setVisible(false);
		
		String c = String.format( "#%02X%02X%02X",
				(int)( getActiveColor().getRed() * 255 ),
				(int)( getActiveColor().getGreen() * 255 ),
				(int)( getActiveColor().getBlue() * 255 ) );
		
		pauseButton.setStyle("-fx-background-color: " + c);
		stopButton.setStyle("-fx-background-color: " + c);
	}
	
	// SETTING UP
	
	private VBox setProperties() {
		
		leftSeparator = new Rectangle(200, 3);
		leftSeparator.setArcWidth(6);
		leftSeparator.setArcHeight(6);
		leftSeparator.fillProperty().bind(activeColorProperty());
		
		rightSeparator = new Rectangle(200, 3);
		rightSeparator.setArcWidth(6);
		rightSeparator.setArcHeight(6);
		rightSeparator.fillProperty().bind(activeColorProperty());
		
		stopButton = new JFXButton();
		stopButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		stopButton.setPrefSize(10,10);
		stopButton.setAlignment(Pos.TOP_CENTER);
		stopButton.setFocusTraversable(false);
		stopButton.setGraphic(stopIcon);
		stopButton.setOnAction(v -> stopClick());
		
		pauseButton = new JFXButton();
		pauseButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		pauseButton.setPrefSize(10,10);
		pauseButton.setAlignment(Pos.TOP_CENTER);
		pauseButton.setFocusTraversable(false);
		pauseButton.setGraphic(pauseIcon);
		pauseButton.setOnAction(v -> pauseClick());
		
		HBox buttonsBar = new HBox();
		buttonsBar.setAlignment(Pos.CENTER);
		buttonsBar.setSpacing(10);
		buttonsBar.setMargin(stopButton, new Insets(5.0));
		buttonsBar.setMargin(pauseButton, new Insets(5.0));
		buttonsBar.getChildren().addAll(leftSeparator, pauseButton, stopButton, rightSeparator);
		
		activeTaskTimer = new Label("99:99");
		activeTaskTimer.textFillProperty().bind(activeColorProperty());
		activeTaskTimer.setAlignment(Pos.CENTER);
		activeTaskTimer.setPadding(new Insets(5, 0, 10, 0));
		activeTaskTimer.setStyle("-fx-font-smoothing-type: gray; -fx-font-size: 32.0");
		activeTaskTimer.textProperty().bind(taskLengthProperty().asString());
		
		activeTaskLabel = new Label("INSERT TASK NAME HERE!");
		activeTaskLabel.textFillProperty().bind(activeColorProperty());
		activeTaskLabel.setAlignment(Pos.CENTER);
		activeTaskLabel.setPadding(new Insets(10, 0, 10, 0));
		activeTaskLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font-size: 18.0");
		
		
		timerClock.barColorProperty().bind(activeColorProperty());
		//timerClock.setBarBackgroundColor(Color.web("#272c32"));
		timerClock.setAnimated(true);
		timerClock.setPrefSize(500,500);
		

		
		VBox vbox = new VBox(activeTaskLabel, buttonsBar, activeTaskTimer, timerClock);
		vbox.setSpacing(3);
		vbox.setAlignment(Pos.CENTER);
		vbox.setBackground(new Background(new BackgroundFill(Color.web("#272c32"), CornerRadii.EMPTY, Insets.EMPTY)));
		vbox.setPadding(new Insets(20));
		return vbox;
	}
	
	private void setUpGlyphs() {
		stopIcon = new Glyph("FontAwesome", "STOP");
		pauseIcon = new Glyph("FontAwesome", "PAUSE");
		playIcon = new Glyph("FontAwesome", "PLAY");
	}
	
	///////////////////////////////////////////////////////////
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isOvertime() {
		return overtime;
	}
	
	public void setOvertime(boolean overtime) {
		this.overtime = overtime;
	}
	
	public IntegerProperty totalLengthProperty() {
		return totalLength;
	}
	
	public void setTotalLength(int totalLength) {
		this.totalLength.set(totalLength);
		System.out.println(getTotalLength());
	}
	
	public int getTaskLength() {
		return taskLength.get();
	}
	
	public IntegerProperty taskLengthProperty() {
		return taskLength;
	}
	
	public void setTaskLength(int taskLength) {
		this.taskLength.set(taskLength);
		System.out.println(getTaskLength());
	}
	
	public int getTotalLength() {
		return totalLength.get();
	}
	
	public Color getActiveColor() {
		return activeColor.get();
	}
	
	public ObjectProperty<Color> activeColorProperty() {
		return activeColor;
	}
	
	public void setActiveColor(Color activeColor) {
		this.activeColor.set(activeColor);
	}
}
