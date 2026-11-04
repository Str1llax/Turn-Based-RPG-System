package git.str1llax.core.managers;

import git.str1llax.core.states.GameState;

import java.util.Stack;

public class StateManager {
    private final Stack<GameState> stateMachine;

    public StateManager() {
        stateMachine = new Stack<>();
    }

    public void push(GameState gameState) {
        stateMachine.push(gameState).load();
    }

    public void pop() {
        stateMachine.pop().unload();
        if (!stateMachine.isEmpty()) stateMachine.peek().load();
    }

    public GameState peek() {
        return stateMachine.peek();
    }

    public boolean isEmpty() {
        return stateMachine.isEmpty();
    }
}
