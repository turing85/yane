package de.turing85.yane.api;

public interface CpuBus {
  void write(int address, int data);
  int read(int address);
}