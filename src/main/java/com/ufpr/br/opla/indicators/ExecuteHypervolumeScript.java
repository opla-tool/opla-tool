package com.ufpr.br.opla.indicators;


import com.ufpr.br.opla.configuration.UserHome;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author elf
 */
public class ExecuteHypervolumeScript {

  public static List<Double> exec(String referencePoint, String pathToFile) throws IOException {
    String hyperVolumeBin = UserHome.getOplaUserHome() + "bins/./hv";

    ProcessBuilder builder = new ProcessBuilder(hyperVolumeBin, "-r", referencePoint, pathToFile);
    builder.redirectErrorStream(true);
    Process p = builder.start();
    
    return inheritIO(p.getInputStream());
    
  }

  private static List<Double> inheritIO(final InputStream src) {
    List<Double> values = new ArrayList<>();

    Scanner sc = new Scanner(src);
    while (sc.hasNextLine())
      values.add(Double.parseDouble(sc.nextLine()));

    return values;
  }

}
