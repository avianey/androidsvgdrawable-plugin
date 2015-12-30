package fr.avianey.androidsvgdrawable.sample;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CardDeckApplication implements ApplicationListener {

    private static final String[] RANKS = new String [] {"ace", "king", "queen", "jack", "ten", "nine", "eight", "seven", "six", "five", "four", "three", "two"};
    private static final String[] SUITS = new String [] {"spade", "heart", "diamond", "club"};

    private static final int BACKGROUND = 0x0A6B0DFF;
    private static final float r = ((BACKGROUND >> 24) & 0xFF) / ((float) 0xFF);
    private static final float g = ((BACKGROUND >> 16) & 0xFF) / ((float) 0xFF);
    private static final float b = ((BACKGROUND >>  8) & 0xFF) / ((float) 0xFF);

    private Skin skin;
    private Stage stage;
    private TextureAtlas atlas;
    private AssetManager manager;
    private boolean initialized = false;

    @Override
    public void create () {
        manager = new AssetManager();
        manager.load("skin.json", Skin.class);
        manager.load("skin.atlas", TextureAtlas.class);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(r, g, b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!manager.update()) {
            // still loading
            return;
        } else if (!initialized) {
            // loading done
            initializeAssets();
            initialized = true;
        }

        stage.act(Gdx.graphics.getRawDeltaTime());
        stage.draw();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        initialized = false;
        stage.dispose();
        manager.dispose();
    }

    private void initializeAssets() {
        skin = manager.get("skin.json", Skin.class);
        atlas = manager.get("skin.atlas", TextureAtlas.class);

        // layout
        Table table = new Table();
        table.pad(10);
        table.defaults().space(10);
        table.setFillParent(true);

        // header
        table.row();
        table.add(new Label("LibGDX Card Deck", skin, "cantarell")).colspan(RANKS.length);

        // deck
        for (String suit : SUITS) {
            table.row();
            for (String rank : RANKS) {
                table.add(
                        new Image(
                                new SpriteDrawable(atlas.createSprite(String.format("card_suit_%s_rank_%s", suit, rank))),
                                Scaling.fit));
            }
        }

        stage.addActor(table);
    }
}
