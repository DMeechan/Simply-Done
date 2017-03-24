import com.jfoenix.controls.JFXButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.Glyph;

/**
 * Created by Daniel on 23.03.2017.
 */
public class TaskCell extends HBox {
	private final static Glyph trash = new Glyph("FontAwesome", "TRASH_ALT");
	private final static Glyph check = new Glyph("FontAwesome", "CHECK_SQUARE");
	
	final Text minutesText = new Text("10");
	final Text secondsText = new Text(":00");
	final Separator separator = new Separator();
	final Text taskNameText = new Text("TASK NAME TEXT");
	final Pane spacer = new Pane();
	final JFXButton deleteButton = new JFXButton("");
	final JFXButton doneButton = new JFXButton("");
	
	Task task;
	IntegerProperty status = new SimpleIntegerProperty();
	// 0 = not done; 1 = done; 2 = deleted
	
	public TaskCell(Task task) {
		super();
		this.task = task;
		
		setProperties();
		
		deleteButton.setOnAction(v -> {
			setStatus(2);
		});
		
		doneButton.setOnAction(v -> {
			setStatus(
					(getStatus() + 1) % 2
			);
		});
		
	}
	
	private void setProperties() {
		this.setAlignment(Pos.CENTER_LEFT);
		this.setPrefSize(390.0, 25.0);
		
		separator.setOrientation(Orientation.VERTICAL);
		HBox.setMargin(separator, new Insets(0, 13.0, 0, 13.0));
		
		//HBox.setHgrow(spacer, Priority.SOMETIMES);
		
		setupText(minutesText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.RIGHT);
		setupText(secondsText, 14.0, 3.0, 0.0, 3.0, 0.0, TextAlignment.LEFT);
		setupText(taskNameText, 12.0, 5.0, 5.0, 5.0, 5.0, TextAlignment.CENTER);
		
		minutesText.textProperty().bind(task.minutesProperty().asString());
		taskNameText.textProperty().bind(task.nameProperty());
		
		minutesText.setWrappingWidth(19);
		secondsText.setWrappingWidth(19);
		
		//minutesText.setWrappingWidth(50.0);
		HBox.setHgrow(taskNameText, Priority.ALWAYS);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		setupButton(deleteButton);
		setupButton(doneButton);
		
		deleteButton.setGraphic(trash);
		doneButton.setGraphic(check);
		
		task.colourProperty().addListener(v -> {
			//this.setBackground(new Background(new BackgroundFill(task.getColour(), CornerRadii.EMPTY, Insets.EMPTY)));
		});
		
		//this.setBackground(new Background(new BackgroundFill(task.getColour(), CornerRadii.EMPTY, Insets.EMPTY)));
		
		this.getChildren().addAll(minutesText, secondsText, separator, taskNameText, spacer, deleteButton, doneButton);
	}
	
	private void setupText(Text text, double fontSize, double top, double right, double bottom, double left, TextAlignment alignment) {
		text.setTextAlignment(alignment);
		text.setTextOrigin(VPos.CENTER);
		text.setFont(new Font(fontSize));
		HBox.setMargin(text, new Insets(top, right, bottom, left));
	}
	
	private void setupButton(Button button) {
		button.setAlignment(Pos.CENTER);
		button.setContentDisplay(ContentDisplay.CENTER);
		button.setPrefSize(25.0, 25.0);
		button.setTextAlignment(TextAlignment.CENTER);
		button.setFocusTraversable(false);
		HBox.setMargin(button, new Insets(5.0));
	}
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}
	
	public int getStatus() {
		return status.get();
	}
	
	public IntegerProperty statusProperty() {
		return status;
	}
	
	public void setStatus(int status) {
		if (status >= 0 && status <= 2) {
			this.status.set(status);
		} else {
			Main.outputError("Invalid task status. Status must be 0, 1 or 2, not: " + status);
		}
	}
}
