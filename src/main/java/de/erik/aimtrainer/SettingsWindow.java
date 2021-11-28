package de.erik.aimtrainer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsWindow extends Stage {

	private AimtrainerApp app;
	private IntegerProperty radius = new SimpleIntegerProperty();
	private IntegerProperty max = new SimpleIntegerProperty();
	private double interval;

	public SettingsWindow(AimtrainerApp app) {
		this.app = app;
		interval = app.getInterval();
		var scene = new Scene(createContent(), Color.TRANSPARENT);
		scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
		scene.setOnKeyPressed(evt -> {
			if (evt.getCode() == KeyCode.ESCAPE)
				app.toggleSettings();
		});
		setWidth(300);
		setHeight(400);
		setScene(scene);
		setTitle("Settings");
		setAlwaysOnTop(true);
		initStyle(StageStyle.TRANSPARENT);
		getIcons().add(new Image(getClass().getResource("/icons/planet.png").toExternalForm()));
		setOnCloseRequest(evt -> {
			closeWindow();
		});
	}

	private Parent createContent() {
		var root = new VBox(10);
		root.setPrefSize(250, 300);
		root.setId("root-settings");
		var head = new Text("settings");
		head.setId("settings-head");

		var rText = new Text("radius");
		rText.getStyleClass().add("settings-text");
		radius.set((int) app.getRadius());
		rText.textProperty().bind(radius.asString("radius: %s"));
		var rSlider = new Slider(5, 70, radius.get());
		rSlider.getStyleClass().add("slider");
		radius.bind(rSlider.valueProperty());
		VBox.setMargin(rText, new Insets(10, 0, 0, 0));

		var iText = new Text("interval: ca. " + interval + " / s");
		iText.getStyleClass().add("settings-text");
		var iSlider = new Slider(0.3, 2, interval);
		iSlider.getStyleClass().add("slider");
		iSlider.valueProperty().addListener((o, oldVal, newVal) -> {
			iText.setText(String.format("interval: ca. %.2f / s", newVal));
			interval = (double) newVal;
		});
		VBox.setMargin(iText, new Insets(10, 0, 0, 0));

		var mText = new Text("max");
		mText.getStyleClass().add("settings-text");
		max.set(app.getMax());
		mText.textProperty().bind(max.asString("max: %s"));
		var mSlider = new Slider(2, 20, max.get());
		mSlider.getStyleClass().add("slider");
		max.bind(mSlider.valueProperty());
		VBox.setMargin(mText, new Insets(10, 0, 0, 0));

		var btn = new Button("apply and restart");
		btn.getStyleClass().add("button");
		btn.setStyle("-fx-font-size: 14");
		VBox.setMargin(btn, new Insets(20, 0, 0, 0));
		btn.setOnAction(evt -> {
			app.clearCircles();
			app.setCounter(0);
			app.setTimer(0);
			app.setRadius(radius.intValue());
			app.setInterval(interval);
			app.setMax(max.intValue());
			closeWindow();
		});

		var exit = new Button("exit");
		exit.getStyleClass().add("button");
		exit.setOnAction(evt -> app.closeAll());
		VBox.setMargin(exit, new Insets(10, 0, 10, 0));

		root.getChildren().addAll(head, new Separator(), rText, rSlider, iText, iSlider, mText, mSlider, btn, exit);
		return root;
	}

	public void closeWindow() {
		app.btnBG.setFill(Color.GREEN);
		app.timer.start();
		close();
	}

}
