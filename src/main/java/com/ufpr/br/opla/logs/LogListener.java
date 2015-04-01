/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.logs;

import com.ufpr.br.opla.utils.Time;
import javax.swing.JTextArea;
import logs.log_log.Listener;
import logs.log_log.LogLogData;

/**
 *
 * @author elf
 */
public class LogListener implements Listener{
  
  private JTextArea textArea;

  public LogListener(JTextArea logs) {
    this.textArea = logs;
  }

  @Override
  public void message() {
    
    this.textArea.append(Time.timeNow() + LogLogData.printLog()+"\n");
  }
  
}
