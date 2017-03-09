import com.jfoenix.controls.JFXButton;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.controlsfx.glyphfont.Glyph;

import java.awt.*;
import java.net.URL;

public class ClockView {
	
	private final ObservableList<Task> taskList;
	private Task activeTask; // task which is currently being used for taskTimer
	private int activeTaskCount = 0; // array value of task which is currently active
	private boolean running, overtime, playingCountdownSound;
	// store if the timers are running, if the main timer is in overtime, and if the countdown sound is being played
	
	private Timeline timer;
	private final IntegerProperty totalLength = new SimpleIntegerProperty(60);
	// current timer value for main timer
	private final IntegerProperty taskLength = new SimpleIntegerProperty(0);
	// current timer value for task timer
	private MediaPlayer mediaPlayer;
	// used for playing countdown sound
	
	private final VBox container;
	private Label taskLabel, taskTimerLabel, totalTimerLabel;
	private final Gauge timerClock;
	private JFXButton pauseButton, stopButton;
	private FadeTransition pauseFade, stopFade, timerLabelFade, timerClockFade;
	
	private Glyph stopIcon, pauseIcon, playIcon;
	private final ObjectProperty<Color> activeColour = new SimpleObjectProperty<>(Color.web("#ffffff"));
	// activeColour represents the colour of the current active task
	// changing this value updates the colour of everything in this scene
	
	ClockView() {
		
		taskList = SchedulerController.getNotDoneTasks();
		
		int combinedLength = 0;
		for (Task t : taskList) {
			combinedLength += t.getMinutes();
		}
		
		setTotalLength(combinedLength * 60); // set the total timer length to combined tasks lengths
		// length * 60 because the length is in minutes
		// we need the timer to count down in seconds
		GaugeBuilder builder = GaugeBuilder.create().skinType(Gauge.SkinType.SLIM);
		timerClock = builder.decimals(0).maxValue(getTotalLength()).build();
		timerClock.valueProperty().bind(totalLengthProperty());
		timerClock.setValueVisible(false);
		// hide clock's native text field
		
		setUpGlyphs();
		container = setProperties();
		setUpFades();
		
		start();
		
	}
	
	private String colorToHex(Color colour) {
		return String.format("#%02X%02X%02X",
				(int) (colour.getRed() * 255),
				(int) (colour.getGreen() * 255),
				(int) (colour.getBlue() * 255));
	}
	
	// BUTTON CLICKS
	
	private void start() {
		// start timer countdown
		setActiveTask();
		
		setOvertime(false);
		setRunning(true);
		
		timer = new Timeline((new KeyFrame(
				Duration.millis(1000),
				event -> timerTick())));
		timer.setCycleCount(Animation.INDEFINITE);
		
		timer.play();

	}
	
	private void pauseClick() {
		if(isOvertime()) { // turn this button into a stop button if in overtime
			stopClick();
			
		} else if(isRunning()) { // currently running; so pause it
			pauseTimer();
			pauseButton.setGraphic(playIcon);
			playFades();
	
		} else { // currently not running; let's resume
			timer.play();
			pauseButton.setGraphic(pauseIcon);
			stopFades();
			setRunning(true);
		}
		
	}
	
	// TIMER STUFF
	
	private void stopClick() {
		pauseTimer();
		Main.switchScene();
	}
	
	private void setActiveTask() {
		// found area for optimisation: instead of storing a reference to the active task
		// just update the class's stored task colour, length, and name values
		// no need to store the task itself
		
		// task timer has reached 00:00 - time to move to next task
		if (!(activeTaskCount == taskList.size())) {
			// check there are still more tasks to go through
			if (isRunning()) {
				// if not running, then means the main timer has reached 0 too
				// so make sure it is running to avoid spamming the user with too many sounds
				Toolkit.getDefaultToolkit().beep();
			}
			// get next task in list
			activeTask = taskList.get(activeTaskCount);
			//System.out.println("New active task: " + activeTask.getName() + " of length: " + activeTask.getMinutes());
			
			// update global
			setActiveColour(activeTask.getColour());
			setTaskLength(activeTask.getMinutes() * 60);
			taskLabel.setText(activeTask.getName().toUpperCase());
			
			String c = colorToHex(getActiveColour());
			setGlyphColours(c);
			activeTaskCount++;
		} else {
			System.out.println("Error: next task doesn't exist...");
		}
	}
	
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
	
	private void startCountdownSound() {
		playingCountdownSound = true;
		//String location = Main.resourcesDir + "countdown.mp3";
		URL source = this.getClass().getResource("ticking.mp3");
		Media sound = new Media(source.toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
	}
	
	// SETTING UP
	
	private void startOvertime() {
		setOvertime(true);
		Toolkit.getDefaultToolkit().beep();
		setActiveColour(Color.web("#e74c3c"));
		taskLabel.setText("OVERTIME!");
		taskTimerLabel.setVisible(false);
		
		String c = colorToHex(getActiveColour());
		setGlyphColours(c);
	}
	
	private VBox setProperties() {
		
		taskLabel = new Label("INSERT TASK NAME HERE!");
		
		taskLabel.setAlignment(Pos.CENTER);
		taskLabel.setPadding(new Insets(0, 10, 0, 10));
		taskLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font-size: 18.0; -fx-font-style: italic");
		
		// LINEBAR
		
		Rectangle leftSeparator, rightSeparator;
		leftSeparator = new Rectangle(200, 3);
		leftSeparator.setArcWidth(6);
		leftSeparator.setArcHeight(6);
		
		
		rightSeparator = new Rectangle(200, 3);
		rightSeparator.setArcWidth(6);
		rightSeparator.setArcHeight(6);
		
		taskTimerLabel = new Label("");
		taskTimerLabel.setAlignment(Pos.CENTER);
		taskTimerLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font: bold italic 20pt \"Arial\"");
		taskLengthProperty().addListener(v -> taskTimerLabel.setText(Main.secToStringMinsec(getTaskLength())));
		
		HBox lineBar = new HBox();
		lineBar.setAlignment(Pos.CENTER);
		lineBar.setSpacing(10);
		lineBar.setPadding(new Insets(0,5,15,5));
		lineBar.getChildren().addAll(leftSeparator, taskTimerLabel, rightSeparator);
		
		// TIMER CENTRE
		
		totalTimerLabel = new Label("");
		//totalTimerLabel.setText("Loading");
		totalTimerLabel.textFillProperty().set(Color.WHITE);
		totalTimerLabel.setAlignment(Pos.CENTER);
		totalTimerLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font: 62pt \"Arial\"");
		
		totalLengthProperty().addListener(v -> totalTimerLabel.setText(Main.secToStringMinsec(getTotalLength())));
		
		stopButton = new JFXButton();
		stopButton.setPrefSize(36,36);
		stopButton.setStyle("-fx-background-color: transparent");
		stopButton.setAlignment(Pos.CENTER);
		stopButton.setFocusTraversable(false);
		stopButton.setGraphic(stopIcon);
		stopButton.setOnAction(v -> stopClick());
		
		pauseButton = new JFXButton();
		pauseButton.setPrefSize(15,15);
		pauseButton.setStyle("-fx-background-color: transparent");
		pauseButton.setAlignment(Pos.CENTER);
		pauseButton.setFocusTraversable(false);
		pauseButton.setGraphic(pauseIcon);
		pauseButton.setOnAction(v -> pauseClick());
		
		HBox buttonsBox = new HBox();
		buttonsBox.setAlignment(Pos.CENTER);
		buttonsBox.setSpacing(10);
		buttonsBox.setPadding(new Insets(0,5,15,5));
		buttonsBox.getChildren().addAll(pauseButton, stopButton);
		
		VBox timerCentre = new VBox();
		timerCentre.setAlignment(Pos.CENTER);
		timerCentre.setSpacing(10);
		timerCentre.getChildren().addAll(totalTimerLabel, buttonsBox);
		
		// bind the colour of all items in the interface to the activeColourProperty
		// NOTE: can't bind colour of button graphics ; can only bind their background colour
		taskLabel.textFillProperty().bind(activeColourProperty());
		leftSeparator.fillProperty().bind(activeColourProperty());
		rightSeparator.fillProperty().bind(activeColourProperty());
		taskTimerLabel.textFillProperty().bind(activeColourProperty());
		totalTimerLabel.textFillProperty().bind(activeColourProperty());
		timerClock.barColorProperty().bind(activeColourProperty());
		
		StackPane timerPane = new StackPane();
		timerPane.getChildren().addAll(timerClock, timerCentre);
		
		
		//timerClock.setBarBackgroundColor(Color.web("#272c32"));

		timerClock.setAnimationDuration(200);
		timerClock.setAnimated(true);
		timerClock.setPrefSize(500,500);
		
		VBox vbox = new VBox(taskLabel, lineBar, timerPane);
		vbox.setSpacing(3);
		vbox.setAlignment(Pos.CENTER);
		vbox.setBackground(new Background(new BackgroundFill(Color.web("#272c32"), CornerRadii.EMPTY, Insets.EMPTY)));
		vbox.setPadding(new Insets(20));
		
		return vbox;
	}
	
	private void setUpFades(){
		// set up the whole group of fade transitions
		// create fade transitions for every element that will fade in and out
		pauseFade = new FadeTransition(Duration.millis(600), pauseButton);
		stopFade = new FadeTransition(Duration.millis(600), stopButton);
		timerLabelFade = new FadeTransition(Duration.millis(600), totalTimerLabel);
		timerClockFade = new FadeTransition(Duration.millis(600), timerClock);
		
		setUpFade(pauseFade);
		setUpFade(stopFade);
		setUpFade(timerClockFade);
		setUpFade(timerLabelFade);
	}
	
	private void setUpFade(FadeTransition fade) {
		// set properties of each fade identically so every object fades in sync
		fade.setFromValue(1.0);
		fade.setToValue(0.6);
		fade.setCycleCount(Timeline.INDEFINITE);
		fade.setAutoReverse(true);
		fade.setInterpolator(Interpolator.EASE_IN);
	}
	
	private void stopFades() {
		pauseFade.stop();
		stopFade.stop();
		timerClockFade.stop();
		timerLabelFade.stop();
		
	}
	
	private void playFades() {
		pauseFade.play();
		stopFade.play();
		timerClockFade.play();
		timerLabelFade.play();
	}
	
	private void setUpGlyphs() {
		playIcon = new Glyph("FontAwesome", "PLAY");
		pauseIcon = new Glyph("FontAwesome", "PAUSE");
		stopIcon = new Glyph("FontAwesome", "STOP");
		playIcon.sizeFactor(2);
		pauseIcon.sizeFactor(2);
		stopIcon.sizeFactor(2);
		String c = colorToHex(getActiveColour());
		setGlyphColours(c);
	}
	
	public Scene getScene() {
		return new Scene(container, 620, 620);
	}
	
	///////////////////////////////////////////////////////////
	
	private void setGlyphColours(String c) {
		// update colour of glyphs to active colour
		// must call this method every time the active colour changes
		playIcon.setStyle("-fx-text-base-color: " + c);
		pauseIcon.setStyle("-fx-text-base-color: " + c);
		stopIcon.setStyle("-fx-text-base-color: " + c);
	}
	
	private boolean isRunning() {
		return running;
	}
	
	private void setRunning(boolean running) {
		this.running = running;
	}
	
	private boolean isOvertime() {
		return overtime;
	}
	
	private void setOvertime(boolean overtime) {
		this.overtime = overtime;
	}
	
	private IntegerProperty totalLengthProperty() {
		return totalLength;
	}
	
	private int getTaskLength() {
		return taskLength.get();
	}
	
	private void setTaskLength(int taskLength) {
		this.taskLength.set(taskLength);
	}
	
	private IntegerProperty taskLengthProperty() {
		return taskLength;
	}
	
	private int getTotalLength() {
		return totalLength.get();
	}
	
	private void setTotalLength(int totalLength) {
		this.totalLength.set(totalLength);
	}
	
	private Color getActiveColour() {
		return activeColour.get();
	}
	
	private void setActiveColour(Color activeColour) {
		this.activeColour.set(activeColour);
	}
	
	private ObjectProperty<Color> activeColourProperty() {
		return activeColour;
	}
}
