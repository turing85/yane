package de.turing85.yane;

public interface CpuBus {

  void write(int address, int data);

  int read(int address);
}