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
	private Label taskLabel, taskTimerLabel, totalTimerLabel;
	private Gauge timerClock;
	private JFXButton pauseButton, stopButton;
	private FadeTransition pauseFade, stopFade, timerLabelFade, timerClockFade;
	
	private Glyph stopIcon, pauseIcon, playIcon;
	private ObjectProperty<Color> activeColor = new SimpleObjectProperty<Color>(Color.web("#ffffff"));
	
	ClockView() {
		
		taskList = SchedulerController.getNotDoneTasks();
		
		int combinedLength = 0;
		for (Task t : taskList) {
			combinedLength += t.getMinutes();
		}
		
		setTotalLength(combinedLength * 60); // set the total timer length to combined tasks lengths
		GaugeBuilder builder = GaugeBuilder.create().skinType(Gauge.SkinType.SLIM);
		timerClock = builder.decimals(0).maxValue(getTotalLength()).build();
		timerClock.valueProperty().bind(totalLengthProperty());
		timerClock.setValueVisible(false);
		
		setUpGlyphs();
		container = setProperties();
		setUpFades();
		
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
			setTaskLength(activeTask.getMinutes()*60);
			taskLabel.setText(activeTask.getName().toUpperCase());
			
			String c = colorToHex(getActiveColor());

			setGlyphColours(c);
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
		taskLabel.setText("OVERTIME!");
		taskTimerLabel.setVisible(false);
		
		String c = colorToHex(getActiveColor());
		pauseButton.setStyle("-fx-background-color: " + c);
		stopButton.setStyle("-fx-background-color: " + c);
	}
	
	// SETTING UP
	
	private VBox setProperties() {
		
		taskLabel = new Label("INSERT TASK NAME HERE!");
		taskLabel.textFillProperty().bind(activeColorProperty());
		taskLabel.setAlignment(Pos.CENTER);
		taskLabel.setPadding(new Insets(0, 10, 0, 10));
		taskLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font-size: 18.0; -fx-font-style: italic");
		
		// LINEBAR
		
		leftSeparator = new Rectangle(200, 3);
		leftSeparator.setArcWidth(6);
		leftSeparator.setArcHeight(6);
		leftSeparator.fillProperty().bind(activeColorProperty());
		
		rightSeparator = new Rectangle(200, 3);
		rightSeparator.setArcWidth(6);
		rightSeparator.setArcHeight(6);
		rightSeparator.fillProperty().bind(activeColorProperty());
		
		taskTimerLabel = new Label("");
		taskTimerLabel.textFillProperty().bind(activeColorProperty());
		taskTimerLabel.setAlignment(Pos.CENTER);
		taskTimerLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font: bold italic 20pt \"Arial\"");
		taskLengthProperty().addListener(v -> {
			taskTimerLabel.setText(Main.secToStringMinsec(getTaskLength()));
		});
		
		HBox lineBar = new HBox();
		lineBar.setAlignment(Pos.CENTER);
		lineBar.setSpacing(10);
		lineBar.setPadding(new Insets(0,5,15,5));
		lineBar.getChildren().addAll(leftSeparator, taskTimerLabel, rightSeparator);
		
		// TIMER CENTRE
		
		totalTimerLabel = new Label("");
		totalTimerLabel.textFillProperty().set(Color.WHITE);
		totalTimerLabel.setAlignment(Pos.CENTER);
		totalTimerLabel.setStyle("-fx-font-smoothing-type: gray; -fx-font: 62pt \"Arial\"");
		totalTimerLabel.textFillProperty().bind(activeColorProperty());
		totalLengthProperty().addListener(v -> {
			totalTimerLabel.setText(Main.secToStringMinsec(getTotalLength()));
		});
		
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
		
		StackPane timerPane = new StackPane();
		timerPane.getChildren().addAll(timerClock, timerCentre);
		
		timerClock.barColorProperty().bind(activeColorProperty());
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
		String c = colorToHex(getActiveColor());
		setGlyphColours(c);
	}
	
	private void setGlyphColours(String c) {
		playIcon.setStyle("-fx-text-base-color: " + c);
		pauseIcon.setStyle("-fx-text-base-color: " + c);
		stopIcon.setStyle("-fx-text-base-color: " + c);
	}
	
	///////////////////////////////////////////////////////////
	
	public static String colorToHex(Color colour) {
		return String.format( "#%02X%02X%02X",
				(int)( colour.getRed() * 255 ),
				(int)( colour.getGreen() * 255 ),
				(int)( colour.getBlue() * 255 ) );
	}
	
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
	}
	
	public int getTaskLength() {
		return taskLength.get();
	}
	
	public IntegerProperty taskLengthProperty() {
		return taskLength;
	}
	
	public void setTaskLength(int taskLength) {
		this.taskLength.set(taskLength);
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
