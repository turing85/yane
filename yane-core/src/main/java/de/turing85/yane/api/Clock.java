package de.turing85.yane.api;

import java.util.*;

public class Clock {
  private long globalTime = 0;
  private ArrayList<Runnable> registered = new ArrayList<>();

  public void addListener(Runnable registree) {
    this.registered.add(registree);
  }

  public void clock() {
    registered.forEach(Runnable::run);
  }
}
