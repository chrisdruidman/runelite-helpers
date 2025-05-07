// New interface for modules
public interface Module {
    String getName();
    void run();
    void clickAt(int x, int y);
    void clickGameObject(int id);
}