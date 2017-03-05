import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

class Task {
	
	private final StringProperty name = new SimpleStringProperty(""); // name of task
	private boolean isNotDone; // is the timer currently marked as done
	private Color colour; // task's user-defined colour
	
	private IntegerProperty minutes = new SimpleIntegerProperty(0);
	private IntegerProperty seconds = new SimpleIntegerProperty(0);
	
	/*
		private static boolean playingCountdownSound;
		private static MediaPlayer mediaPlayer;
		private Timeline timer;
		private boolean isRunning; // is the timer currently running
		private BooleanProperty overtime = new SimpleBooleanProperty(false); // is the timer currently in overtime
	*/
	
	public Task(String name, int minutes, Color colour) {
		setName(name);
		setNotDone(true);
		setMinutes(minutes);
		setColour(colour);
	}
	
	@Override
	public String toString() {
		System.out.println("Error: outputting string value of task");
		return getMinutes() + ":" + getSeconds() + " " + getName();
	}
	
	////////////////////////////
	
	
	public boolean isNotDone() {
		return isNotDone;
	}
	
	public void setNotDone(boolean notDone) {
		isNotDone = notDone;
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
	
	public Color getColour() {
		return colour;
	}
	
	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	public int getMinutes() {
		return minutes.get();
	}
	
	public IntegerProperty minutesProperty() {
		return minutes;
	}
	
	public void setMinutes(int minutes) {
		this.minutes.set(minutes);
	}
	
	public int getSeconds() {
		return seconds.get();
	}
	
	public IntegerProperty secondsProperty() {
		return seconds;
	}
	
	public void setSeconds(int seconds) {
		this.seconds.set(seconds);
	}
}

	/*

	
	private void setTimer() {
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
	
	private void startOvertime() {
		setOvertime(true);
		Toolkit.getDefaultToolkit().beep();
		// enable overtime label
		// change styling
		
	}
	
	public void stop() {
		// App.activeTasks--;
		timer.stop();
		
		if (getOvertime()) {
			setLength(1);
		}
		
		if (playingCountdownSound) {
			mediaPlayer.stop();
		}
		
		setRunning(false);
		setOvertime(false);
		
	}
	
	private void timerTick() {
		if (getOvertime()) { // keep increasing overtime
			setLength(getLength() + 1);
			
		} else if (getLength() == 0) {
			// countdown has hit 0; start overtime
			playingCountdownSound = false;
			startOvertime();
			
		} else if (getLength() == 31 && !playingCountdownSound) {
			// start countdown music with 30 secs left
			startCountdownSound();
			setLength(getLength() - 1);
			
		} else {
			// continue countdown
			setLength(getLength() - 1);
			
		}
	}
	
	private void startCountdownSound() {
		playingCountdownSound = true;
		String location = Main.resourcesDir + "countdown.mp3";
		Media sound = new Media(new File(location).toURI().toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
		
			public String getTimeLengthFormatted() {
		return timeLengthFormatted.get();
	}
	
	public StringProperty timeLengthFormattedProperty() {
		return timeLengthFormatted;
	}
	
	public int getLength() {
		return length;
	}
	
	private void updateTimeLengthFormatted() {
		int[] store = Main.secToMinsec(getLength());
		timeLengthFormatted.set(Main.minsecToStringMinsec(store[0], store[1]));
	}
		
	
	public void setLength(int length) {
		this.length = length;
		updateTimeLengthFormatted();
	}
	
	*/