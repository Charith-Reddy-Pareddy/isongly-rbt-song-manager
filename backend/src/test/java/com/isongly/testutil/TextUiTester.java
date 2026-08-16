package com.isongly.testutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Redirects System.in/System.out/System.err for the duration of a test so
 * that text-based CLI interactions can be scripted and their output
 * captured, without the test needing to touch the real console.
 */
public class TextUiTester {

  private final PrintStream saveSystemOut;
  private final PrintStream saveSystemErr;
  private final InputStream saveSystemIn;
  private final ByteArrayOutputStream redirectedOut;
  private final ByteArrayOutputStream redirectedErr;

  public TextUiTester(String programInput) {
    this.saveSystemOut = System.out;
    this.saveSystemErr = System.err;
    this.saveSystemIn = System.in;

    this.redirectedOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(this.redirectedOut));
    this.redirectedErr = new ByteArrayOutputStream();
    System.setErr(new PrintStream(this.redirectedErr));
    System.setIn(new ByteArrayInputStream(programInput.getBytes()));
  }

  /** Restores standard I/O and returns everything written to System.out/System.err during the test. */
  public String checkOutput() {
    try {
      return redirectedOut.toString() + redirectedErr.toString();
    } finally {
      System.setOut(saveSystemOut);
      System.setErr(saveSystemErr);
      System.setIn(saveSystemIn);
    }
  }
}
