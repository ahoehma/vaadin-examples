package com.mymita.vaadin.demo;

import java.util.Random;

import javax.servlet.annotation.WebServlet;

import com.mymita.vaadin.demo.ProgressbarDialogDemoUI.Processor.ProgressListener;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
@Theme(Reindeer.THEME_NAME)
public class ProgressbarDialogDemoUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = ProgressbarDialogDemoUI.class)
  public static class Servlet extends VaadinServlet {
  }

  static class Processor {
    interface ProgressListener {
      void onProgress(long progress);
    }
    private static final int MAX_PROGRESS = 100;
    private final ProgressListener progressListener;
    Processor(final ProgressListener progressListener) {
      this.progressListener = progressListener;
    }
    void run() {
      for (int i = 0; i <= MAX_PROGRESS; i++) {
        progressListener.onProgress(i);
        try {
          Thread.sleep(100 + new Random().nextInt(250));
        } catch (final InterruptedException e) {
          break;
        }
      }
    }
  }

  @Override
  protected void init(final VaadinRequest request) {
    final VerticalLayout layout = new VerticalLayout();
    layout.setMargin(true);
    setContent(layout);
    final Button button = new Button("Click Me");
    button.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(final Button.ClickEvent event) {
        startProcessor();
      }
    });
    layout.addComponent(button);
  }

  protected void startProcessor() {
    final ProgressBar progressBar = new ProgressBar();
    progressBar.setWidth(400, Unit.PIXELS);
    final Window progressWindow = new Window("Progress", progressBar);
    progressWindow.setClosable(false);
    progressWindow.setResizable(false);
    progressWindow.center();
    new Thread(new Runnable() {

      @Override
      public void run() {
        new Processor(new ProgressListener() {

          @Override
          public void onProgress(final long progress) {
            UI.getCurrent().access(new Runnable() {

              @Override
              public void run() {
                // 0 .. 1
                final float progressBarValue = (float) progress / Processor.MAX_PROGRESS;
                progressBar.setValue(progressBarValue);
                if (progress == Processor.MAX_PROGRESS) {
                  UI.getCurrent().setPollInterval(-1);
                  UI.getCurrent().removeWindow(progressWindow);
                }
              }
            });
          }
        }).run();
      }
    }).start();
    UI.getCurrent().setPollInterval(250);
    UI.getCurrent().addWindow(progressWindow);
  }
}
