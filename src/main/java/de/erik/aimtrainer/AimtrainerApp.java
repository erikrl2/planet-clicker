package de.erik.aimtrainer;

import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AimtrainerApp extends Application {

	private Stage stage;
	private Pane root;
	private SettingsWindow settings;
	private int max = 15;
	private int counter;
	private double radius = 25;
	private double interval = 0.5;
	private int time;
	private boolean usedClean;
	public Rectangle btnBG;
	public AnimationTimer timer;

	public Parent createContent() {
		root = new Pane();
		root.setId("root-main");
		root.setPrefSize(1280, 720);
		root.setOnMousePressed(e -> {
			if (!settings.isShowing())
				counter--;
		});

		settings = new SettingsWindow(this);

		var scoreText = new Text("");
		scoreText.getStyleClass().add("score-text");
		IntegerProperty scoreIP = new SimpleIntegerProperty();
		scoreText.textProperty().bind(scoreIP.asString("score: %s"));

		var timeText = new Text("");
		timeText.getStyleClass().add("score-text");
		IntegerProperty timeIP = new SimpleIntegerProperty();
		timeText.textProperty().bind(timeIP.asString("time: %s"));

		var score = new HBox(50, scoreText, timeText);
		score.setTranslateX(15);
		score.setTranslateY(20);

		btnBG = new Rectangle(70, 25);
		btnBG.setFill(Color.GREEN);
		btnBG.setEffect(new DropShadow(5, Color.BLACK));
		btnBG.setEffect(new GaussianBlur(8));
		btnBG.setVisible(false);

		var btnText = new Text("settings");
		btnText.setId("settings-btn-text");
		var settingsBtn = new StackPane(btnBG, btnText);
		settingsBtn.setTranslateX(5);
		settingsBtn.setTranslateY(690);
		settingsBtn.setOnMouseMoved(e -> btnBG.setVisible(true));
		settingsBtn.setOnMouseExited(e -> btnBG.setVisible(false));
		settingsBtn.setOnMouseClicked(e -> toggleSettings());

		var hint = new Text(root.getPrefWidth() - 110, root.getPrefHeight() - 40,
				"ESC - settings\nSpace - clear\n(1 clear per game)");
		hint.setId("hint");

		timer = new AnimationTimer() {
			public double t1 = 0, t2 = 0;

			@Override
			public void handle(long now) {
				t1 += 0.016;
				t2 += 0.016;
				scoreIP.set(counter);
				timeIP.set(time);
				if (t1 >= interval) {
					if (!maxReached())
						addCircle();
					t1 = 0;
				}
				if (t2 >= 1) {
					time++;
					t2 = 0;
					if (time % 2 == 0)
						interval -= 0.01;
				}
			}
		};
		timer.start();

		root.getChildren().addAll(settingsBtn, score, hint);
		root.getChildren().forEach(e -> e.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> evt.consume()));
		return root;
	}

	public void toggleSettings() {
		if (settings.isShowing()) {
			settings.closeWindow();
		} else {
			timer.stop();
			btnBG.setFill(Color.RED);
			settings.show();
		}
	}

	private void addCircle() {
		var circle = new Circle(radius);
		do {
			circle.setCenterX(Math.random() * (root.getWidth() - radius * 2) + radius);
			circle.setCenterY(Math.random() * (root.getHeight() - radius * 2) + radius);
		} while (root.getChildren().stream().anyMatch(e -> e.intersects(circle.getBoundsInLocal())));
		String highlight = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
		String color = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
		String ring = String.format("%06x", (int) (Math.random() * 0xFFFFFF));
		circle.setStyle(
				"-fx-fill: radial-gradient(focus-angle 45.0deg, focus-distance 20.0%, center 25.0% 25.0%, radius 50.0%, reflect, #"
						+ highlight + ", #" + color + " 75.0%, #" + ring + ")");
		circle.setOnMousePressed(this::circleClicked);
		root.getChildren().add(circle);
	}

	private void removeCircle(Node circle) {
		((Shape) circle).setFill(Color.RED);
		var st = new ScaleTransition(Duration.seconds(0.1), circle);
		st.setToX(0);
		st.setToY(0);
		st.setOnFinished(e -> root.getChildren().remove(circle));
		st.play();
	}

	private void circleClicked(MouseEvent evt) {
		evt.consume();
		if (!settings.isShowing()) {
			counter++;
			removeCircle((Node) evt.getSource());
		}
	}

	private boolean maxReached() {
		boolean b = root.getChildren().stream().filter(e -> e instanceof Circle).collect(Collectors.toList())
				.size() == max;
		if (b) {
			toggleSettings();
		}
		return b;
	}

	public void clearCircles() {
		root.getChildren().stream().filter(e -> e instanceof Circle).forEach(this::removeCircle);
	}

	public void closeAll() {
		if (settings.isShowing())
			settings.close();
		stage.close();
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		this.interval = interval;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void setTimer(int time) {
		this.time = time;
		this.usedClean = false;
	}

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		var scene = new Scene(createContent(), Color.grayRgb(32, 0.7));
		scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
		scene.setOnKeyPressed(evt -> {
			evt.consume();
			if (evt.getCode() == KeyCode.ESCAPE)
				toggleSettings();
			else if (evt.getCode() == KeyCode.SPACE) {
				if (!usedClean) {
					clearCircles();
					usedClean = true;
				}
			}
		});
		stage.setScene(scene);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.getIcons().add(new Image(getClass().getResource("/icons/planet.png").toExternalForm()));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
