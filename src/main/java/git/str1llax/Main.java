package git.str1llax;

import git.str1llax.core.Engine;

public class Main {
    public static void main(String[] args) {
        Engine instance = new Engine();

        try {
            instance.loadResources();

            instance.startGame();

            while (instance.isRunning()) {
                instance.update();
                instance.render();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
