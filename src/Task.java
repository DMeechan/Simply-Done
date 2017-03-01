/**
 * Created by Daniel on 22.02.2017.
 */

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;

/**
 * Created by Daniel on 05.02.2017.
 */
public class Task {
	
	private static boolean playingCountdownSound;
	private static MediaPlayer mediaPlayer;
	public StringProperty timeLengthFormatted = new SimpleStringProperty("");
	private int timerLength; // length of timer value (seconds)
	private StringProperty name = new SimpleStringProperty(""); // name of task
	private BooleanProperty overtime = new SimpleBooleanProperty(false); // is the timer currently in overtime
	
	private Timeline timer;
	private boolean isRunning; // is the timer currently running
	private boolean isLiving; // is the timer currently marked as done
	
	public Task(String name, int timerLength) {
		
		setName(name);
		setTimerLength(timerLength);
		updateTimeLengthFormatted();
		
		setRunning(false);
		setOvertime(false);
		setLiving(true);
		setTimer();
	}
	
	public void setTimer() {
		timer = new Timeline((new KeyFrame(
				Duration.millis(1000),
				event -> timerTick()
		
		)));
		timer.setCycleCount(Animation.INDEFINITE);
	}
	
	public void start() {
		// overtime must be false when the timer first starts
		// because the timer continues even when it goes into overtime
		setOvertime(false);
		setRunning(true);
		
		timer.play();
	}
	
	public void startOvertime() {
		setOvertime(true);
		Toolkit.getDefaultToolkit().beep();
		// enable overtime label
		// change styling
		
	}
	
	public void stop() {
		// App.activeTasks--;
		timer.stop();
		
		if (getOvertime()) {
			setTimerLength(1);
		}
		
		if (playingCountdownSound) {
			mediaPlayer.stop();
		}
		
		setRunning(false);
		setOvertime(false);
		
	}
	
	public void timerTick() {
		if (getOvertime()) { // keep increasing overtime
			setTimerLength(getTimerLength() + 1);
			
		} else if (getTimerLength() == 0) {
			// countdown has hit 0; start overtime
			playingCountdownSound = false;
			startOvertime();
			
		} else if (getTimerLength() == 31 && playingCountdownSound == false) {
			// start countdown music with 30 secs left
			startCountdownSound();
			setTimerLength(getTimerLength() - 1);
			
		} else {
			// continue countdown
			setTimerLength(getTimerLength() - 1);
			
		}
	}
	
	public void startCountdownSound() {
		playingCountdownSound = true;
		String musicFile = "resources\\countdown.mp3";     // For example
		
		Media sound = new Media(new File(musicFile).toURI().toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
	}
	
	public void updateTimeLengthFormatted() {
		int[] store = Main.secToMinsec(getTimerLength());
		timeLengthFormatted.set(Main.minsecToStringMinsec(store[0], store[1]));
	}
	
	
	@Override
	public String toString() {
		return timeLengthFormatted.getValue() + " " + name;
	}
	
	////////////////////////////
	
	
	public int timerLengthProperty() {
		return timerLength;
	}
	
	public String getTimeLengthFormatted() {
		return timeLengthFormatted.get();
	}
	
	public void setTimeLengthFormatted(String timeLengthFormatted) {
		this.timeLengthFormatted.set(timeLengthFormatted);
	}
	
	public StringProperty timeLengthFormattedProperty() {
		return timeLengthFormatted;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void setRunning(boolean running) {
		isRunning = running;
	}
	
	public boolean getOvertime() {
		return overtime.get();
	}
	
	public void setOvertime(boolean overtime) {
		this.overtime.set(overtime);
	}
	
	public BooleanProperty overtimeProperty() {
		return overtime;
	}
	
	public boolean isLiving() {
		return isLiving;
	}
	
	public void setLiving(boolean living) {
		isLiving = living;
	}
	
	public int getTimerLength() {
		return timerLength;
	}
	
	public void setTimerLength(int timerLength) {
		this.timerLength = timerLength;
		updateTimeLengthFormatted();
	}
	
	public Timeline getTimer() {
		return timer;
	}
	
	public void setTimer(Timeline timer) {
		this.timer = timer;
	}
	
	public String getName() {
		return name.get();
	}
	
	public void setName(String name) {
		this.name.set(name);
	}
	
	public StringProperty nameProperty() {
		return name;
	}
	
}