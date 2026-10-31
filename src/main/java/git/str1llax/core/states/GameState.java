package git.str1llax.core.states;

public interface GameState {
    public void load();
    public void update();
    public void render();
    public void unload();
}
