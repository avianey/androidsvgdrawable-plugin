package fr.avianey.androidsvgdrawable.sample;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 512;
		config.title = "AndroidSvgDrawable LibGDX Sample";
		new LwjglApplication(new CardDeckApplication(), config);
	}
}
