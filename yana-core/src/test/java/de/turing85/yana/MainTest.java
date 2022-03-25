package de.turing85.yana;

import static com.google.common.truth.Truth.assertThat;

import java.io.*;
import org.junit.jupiter.api.*;

@DisplayName("Main")
class MainTest {

  @Nested
  @DisplayName("Instance tests")
  class InstanceTest {
    @Test
    @DisplayName("Creation works as expected")
    void testCreateNewInstance() {
      // GIVEN
      final String expectedFoo = "expectedFoo";

      // WHEN
      final Main main = new Main(expectedFoo);

      // THEN
      assertThat(main.getFoo()).isEqualTo(expectedFoo);
    }
  }

  @Nested
  @DisplayName("Static method tests")
  class StaticTest {
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private PrintStream originalOutStream;
    private PrintStream originalErrStream;

    @BeforeEach
    void setup() {
      originalOutStream = System.out;
      originalErrStream = System.err;
      System.setOut(new PrintStream(outStream));
      System.setErr(new PrintStream(errStream));
    }

    @AfterEach
    void destroy() {
      System.setOut(originalOutStream);
      System.setErr(originalErrStream);
    }

    @Test
    @DisplayName("main(...) prints \"Hello world\" to System.out")
    void testExecution() {
      // WHEN
      Main.main(null);

      // THEN
      assertThat(outStream.toString()).matches("^Hello, world!\\s*$");
      assertThat(errStream.toString()).isEmpty();

      System.setOut(originalOutStream);
      System.setErr(originalErrStream);
    }
  }

}