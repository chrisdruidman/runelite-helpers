package com.osrshelper.agent;

public interface HelperModule {
    String getName();
    void run();
    void clickAt(int x, int y);
    void clickGameObject(int id);
}