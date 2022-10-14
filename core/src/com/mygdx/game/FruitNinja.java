package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture background;
	Texture apple;
	Texture pear;
	Texture heart;
	Texture health;
	Texture rock;

	BitmapFont font;
	FreeTypeFontGenerator fontGenerator;

	Random random = new Random();
	Array<Fruit> fruitArray = new Array<Fruit>();

	int lives = 4;
	int score = 0;
	int highScore;

	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;
	Preferences prefs;

	private double currentTime;
	private double gameOverTime = -1f; //istenen parametreler float olarak istendiği için



	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("islandbackground.png");
		apple = new Texture("apple.png");
		pear = new Texture("pear.png");
		heart = new Texture("heart.png");
		health = new Texture("health.png");
		rock = new Texture("rock.png");
		Fruit.radius = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 20f;

		prefs = Gdx.app.getPreferences("gamePreferences");



		Gdx.input.setInputProcessor(this);

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("abhayalibre.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.color = Color.BLACK;
		parameter.size = 80;
		parameter.characters = "0123456789 CutoPlayScreHigh:.+-";
		font = fontGenerator.generateFont(parameter);

	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime, 0.3);
		float deltaTime = (float) frameTime;
		currentTime = newTime;

		if (score > prefs.getInteger("highscore")) {
			prefs.putInteger("highscore", score);
			prefs.flush();
			highScore = prefs.getInteger("highscore");
		}


		if (lives <= 0 && gameOverTime == 0f) {
			//game over
			gameOverTime = currentTime;

		}


		if (lives > 0) {
			//game mode

			genSpeed -= deltaTime * 0.01f;

			if (genCounter <= 0f) {
				genCounter = genSpeed;
				addItem();
			}else {
				genCounter -= deltaTime;
			}

			for (int i = 0; i < lives; i++) {
				batch.draw(health, i * 45f + 50f, Gdx.graphics.getHeight() - 40f, 40f, 40f);
			}

			for (Fruit fruit : fruitArray) {
				fruit.update(deltaTime);

				switch (fruit.type) {
					case REGULAR:
						batch.draw(apple,fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case EXTRA:
						batch.draw(pear,fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case ENEMY:
						batch.draw(rock,fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case LIFE:
						batch.draw(health,fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;

				}
			}

			boolean holdLives = false;

			Array<Fruit> toRemove = new Array<Fruit>();
			for (Fruit fruit : fruitArray) {
				if (fruit.outOfScreen()) {
					toRemove.add(fruit);

					if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
						lives--;
						holdLives = true;
						break;
					}
				}
			}

			if (holdLives) {
				for (Fruit fruit : fruitArray) {
					fruit.living = false;
				}
			}

			for (Fruit fruit : toRemove) {
				fruitArray.removeValue(fruit, true);
			}
		}

		font.draw(batch, "Score: " + score, 40, 60);
		if (lives <= 0) {
			font.draw(batch, "Cut to Play", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
			font.draw(batch, "High Score: " + prefs.getInteger("highscore"), Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f + 150f);
		}

		batch.end();
	}

	private void addItem() {
		float pos = random.nextFloat() * Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());

		Fruit item = new Fruit(new Vector2(pos, -Fruit.radius), new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f + (random.nextFloat() - 0.5f)), Gdx.graphics.getHeight() * 0.5f));

		float type = random.nextFloat();
		if (type > 0.98) {
			item.type = Fruit.Type.LIFE;
		} else if (type > 0.88) {
			item.type = Fruit.Type.EXTRA;
		} else if (type > 0.78) {
			item.type = Fruit.Type.ENEMY;
		}

		fruitArray.add(item);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();

		fontGenerator.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) { //basılı tut ve sürükledi
		if (lives <= 0 && currentTime - gameOverTime > 2f) {
			gameOverTime = 0f;
			score = 0;
			lives = 4;
			genSpeed = startGenSpeed;
			fruitArray.clear();
		} else {
			Array<Fruit> toRemove = new Array<Fruit>();
			Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
			int plusScore = 0;

			for (Fruit fruit : fruitArray) {
				if (fruit.clicked(pos)) {
					toRemove.add(fruit);

					switch (fruit.type) {
						case REGULAR:
							plusScore++;
							break;
						case EXTRA:
							plusScore += 2;
							break;
						case ENEMY:
							lives--;
							break;
						case LIFE:
							lives++;
							break;
					}

				}
			}

			score += plusScore * plusScore;

			for (Fruit fruit : toRemove) {
				fruitArray.removeValue(fruit, true);
			}

		}

		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
