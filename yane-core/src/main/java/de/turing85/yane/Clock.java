package de.turing85.yane;

import java.util.*;

public class Clock {
  private long globalTime = 0;
  private final ArrayList<Runnable> registered = new ArrayList<>();

  public void addListener(Runnable registree) {
    this.registered.add(registree);
  }

  public void tick() {
    ++globalTime;
    registered.forEach(Runnable::run);
  }
}