// GUI Java classes
//

import java.awt.*;

public class GUI extends Frame {

  // Flags to disable standard GUI components:
  public boolean NoInput       = false;
  public boolean NoOutput      = false;
  public boolean NoGraphics    = false;
  public boolean NoRunButton   = false;
  public boolean NoResetButton = false;
  public int BigText           = 0;

  public String RunLabel   = new String("Run");
  public String ResetLabel = new String("Reset");

  public String InitialInput = "";

  Graphics background;  // used for double buffering
  Image im;
  int width = 640;
  int height = 480;

  protected Panel panel;
  TextField inputText;
  TextArea outputText;
  GUICanvas canvas;

  Color colors[];
  int NumColors = 16;

  public String getAppletInfo() {
    return "GUI classes for SE428";
  }

  public void init() {

//    String param = getParameter("width");
//    if (param != null) width = Integer.parseInt(param);
//
//    param = getParameter("height");
//    if (param != null) height = Integer.parseInt(param);
	  
	width = this.getWidth();
	height = this.getHeight();

    setSize(width, height);

    panel = new Panel();
    panel.setLayout(new FlowLayout());
    panel.setSize(width - 10, height / 2);

    if (NoRunButton == false)   panel.add(new Button(RunLabel));
    if (NoResetButton == false) panel.add(new Button(ResetLabel));

    if (NoInput == false) {
      inputText = new TextField(InitialInput, 30);
      panel.add(new Label("Input:"));
      panel.add(inputText);
    }
    if (BigText==0) {
      if (NoOutput == false) {
        outputText = new TextArea("", 5, 35);
        panel.add(outputText);
      }
      add(panel);
    }

    if (BigText!=0) {
      add(panel);
      if (NoOutput == false) {
        if (BigText==1) {
          outputText = new TextArea("", 9, 45);
        } else if (BigText==2) {
          outputText = new TextArea("", 15, 60);
        } else {
          outputText = new TextArea("", 20, 70);
        }

        add(outputText);
      }
    }

    if (NoGraphics == false) {
      canvas = new GUICanvas();
      canvas.parent = this;

      int c_width = width - 20;
      int c_height = (3 * height) / 2;
      canvas.setSize(c_width, c_height);
      add(canvas);

      panel.setBackground(Color.darkGray);
      setBackground(Color.lightGray);

      colors = new Color[NumColors];
      for (int c=0; c<NumColors; c++) {
          float blue = 1.0f - (float)c / (float)NumColors;
          float red = (float)c / (float)NumColors;
          colors[c] = new Color(red, 0.0f, blue);
       }

      canvas.init();
    }
  }

  //public void run() {
  //  System.out.println("In GUI.run() stub method");
  //  // Some derived classes will use a work thread
  //  // which calls the run() method.
  //}

  // reduce applet "flicker" by defining update():
  public void update(Graphics g)
  {
    if (NoGraphics == false)  paint(g);
  }

  public void paint(Graphics g) {
  }

  protected void paintGridCell(Graphics g, int x, int y, int size,
                               double value, double min, double max) {
    int index =
      (int)(((value - min) * (double)NumColors) / (max - min));
    if (index < 0) index = 0;
    else if (index > (NumColors - 1)) index = NumColors - 1;
    g.setColor(colors[index]);
    g.fillRect(x, y, size, size);
    g.setColor(Color.black);
    g.drawRect(x, y, size, size);
  }

  public void paintToDoubleBuffer(Graphics g) {
    System.out.println("entered GUI::paintToDoubleBuffer\n");
    paintGridCell(g, 20, 20, 30, 0.5f, 0.0f, 1.0f);
    System.out.println("leaving GUI::paintToDoubleBuffer\n");
  }

  public void repaint() {
    if (NoGraphics == false) {
      canvas.repaint();
      super.repaint();
    }
  }


  // subclasses should redefine these three functions:
  public void doRunButton()
  { System.out.print("Default GUI::doDoRunButton()"); }
  public void doResetButton()
  {  System.out.print("Default GUI::doDoResetButton()"); }
  public void doMouseDown(int x, int y) {
    System.out.print("Default GUI::doMouseDown(");
    System.out.print(x);
    System.out.print(", ");
    System.out.print(y);
    System.out.println("\n");
  }

  // Utility to get the input text field:
  public String GetInputText() {
    String s = inputText.getText();
    //P("input text:" + s + "\n");
    return s;
  }
  // Utility to set the input text field:
  public void SetInputText(String s) {
    inputText.setText(s);
  }
  // Utilities for output text field:
  public void ClearOutput() {
    outputText.replaceRange("",0, 32000);
  }
  public void P(String s) {
    outputText.append(s);
  }
  public void P(int i) {
    StringBuffer sb = new StringBuffer();
    sb.append(i);
    String s2 = new String(sb);
    outputText.append(s2);
  }
  public void P(double x) {
    StringBuffer sb = new StringBuffer();
    sb.append(x);
    String s2 = new String(sb);
    outputText.append(s2);
  }

  public boolean action(Event evt, Object obj) {
    System.out.println (evt.id);
    if (evt.target instanceof Button) {
      String label = (String)obj;
      if (label.equals(RunLabel)) {
        System.out.println("Run button pressed\n");
        doRunButton();
        repaint();
        if (canvas!=null) canvas.repaint();
        return true;
      }
      if (label.equals(ResetLabel)) {
        System.out.println("Reset button pressed\n");
        doResetButton();
        repaint();
        if (canvas!=null) canvas.repaint();
        return true;
      }
    }
    if (evt.id == 1001)  {
      if (NoInput == false) {
        // User hit a carriage return: execute doRunButton
        doRunButton();
        return true;
      }
    }
    return false;
  }
  private void drawOnCanvas() {
    if (canvas!=null) canvas.repaint();
  }



	//////////////////////////////////////////////////////////////////
	// Definition of the utiliy class GUICanvas that is used internally
	// by the class GUI:
	// This is a private class inside GUI

	class GUICanvas extends Canvas {
	  Graphics background;  // used for double buffering
	  Image im;
	  public GUI parent;
	  public void init() {
		//System.out.println("entering GUICanvas::init\n");
		try {
		  Dimension d = getSize();
		  im = createImage(d.width, d.height);
		  background = im.getGraphics();
		} catch (Exception ex) {
		  background = null;
		}
	  }
	  public void paint(Graphics g) {
		//System.out.println("GUICanvas::paint()\n");
		if(background == null)  {  // Can not use double buffering
		  parent.paintToDoubleBuffer(g);
		  System.out.println("No double buffer available");
		} else {
		  // draw into the copy the background (doble buffering)
		  parent.paintToDoubleBuffer(background);
		  g.drawImage(im,0,0,this);
		}
	  }
	  // reduce applet "flicker" by defining update():
	  public void update(Graphics g)
	  {
		paint(g);
	  }

	  public boolean mouseDrag(Event evt, int x, int y) {
		// Call the containing apps'mose handling function:
		parent.doMouseDown(x, y);
		return false;
	  }

	}
}