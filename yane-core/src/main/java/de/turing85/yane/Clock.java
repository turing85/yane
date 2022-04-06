package de.turing85.yane;

import java.util.ArrayList;

/**
 * A clock that calls all {@link #registered} callbacks on each {@link #tick()}.
 */
public class Clock {
  /**
   * <p>The registered callbacks, represented by {@link Runnable}s.</p>
   *
   * <p>Those are executed when {@link #tick()} is called</p>
   */
  private final ArrayList<Runnable> registered = new ArrayList<>();

  /**
   * Adds a {@link Runnable} to the {@link #registered} callbacks.
   *
   * @param callback the callback to register
   */
  public void addListener(Runnable callback) {
    this.registered.add(callback);
  }

  /**
   * Tick once, call all callbacks from {@link #registered}.
   */
  public void tick() {
    registered.forEach(Runnable::run);
  }
}