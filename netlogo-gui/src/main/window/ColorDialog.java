// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.I18N;
import org.nlogo.shape.VectorShape;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;

public strictfp class ColorDialog extends JDialog implements ActionListener,
    ItemListener,
    MouseListener,
    ClipboardOwner,
    WindowListener {

  private double step = 1; // The increment of the color index incremented  patches
  private boolean numberVisibilityFlag = false; // Flat to view color value on swatches
  //To remember the visibility after change by itemStateChanged generated by setSelected
  private boolean pointOneStepflag = false;

  private final NumberFormat formatter = new DecimalFormat("###.####");

  private int okCancelFlag = 0;
  private boolean plotPenMode = false;
  private JButton okButton;

  private Color selectedColor;
  private double selectedColorNumber;
  private String selectedColorName;

  private static final int SIZE_X = 600;
  private static final int SIZE_Y = 400;

  private final Container pane;
  private final ButtonGroup swatchesGroup = new ButtonGroup();
  private final Container swatchesContainer = new Container();
  private final Container leftNameContainer = new Container();
  private final Container topNameContainer = new Container();
  private final Container rightPreviewContainer = new Container();

  private JRadioButton oneStep;
  private JRadioButton pointFiveStep;
  private JRadioButton pointOneStep;

  private ImageIcon turtleIcon;
  private JCheckBox checkboxHideNumbers;
  private JLabel selectedColorLabel;
  private JLabel[] turtleLabel = new JLabel[16];

  public ColorDialog(Frame frame, boolean modalFlag) {
    super(frame, I18N.guiJ().get("tools.colorswatch"), modalFlag);
    setVisible(false);

    getContentPane().setLayout(new GridBagLayout());
    setSize(SIZE_X, SIZE_Y);
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    pane = getContentPane();
    turtleIcon = getShapeIcon(0);

    addWindowListener(this);

    createLeftNames();
    createTopNames(turtleIcon);
    createRightPreview(turtleIcon);
    createTopLeftLabel();
    createSwatches();
    org.nlogo.swing.Utils.addEscKeyAction
        (this,
            new javax.swing.AbstractAction() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                setVisible(false);
                dispose();
              }
            });
  }

  public void showDialog() {
    plotPenMode = false;

    createControls(plotPenMode);
    selectSwatch(Color.black);
    setVisible(true);
  }


  public Color showPlotPenDialog(Color initialColor) {
    plotPenMode = true;

    createControls(plotPenMode);
    selectSwatch(initialColor);
    setVisible(true);

    if (okCancelFlag == 1) {
      return selectedColor;
    } else {
      return null;
    }
  }

  public double showInputBoxDialog(double initialColor) {
    plotPenMode = true;
    createControls(plotPenMode);
    selectSwatch(org.nlogo.api.Color.getColor(initialColor));
    setVisible(true);
    if (okCancelFlag == 1) {
      return selectedColorNumber;
    } else {
      return initialColor;
    }
  }

  private void selectSwatch(Color initialColor) {
    double closest = org.nlogo.api.Color.getClosestColorNumberByARGB(initialColor.getRGB());
    Color closestColor = new Color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(closest));

    if (closest % 1 == 0 || closest >= 9.9) {
      oneStep.doClick();
    } else if (closest % 5 == 0) {
      pointOneStep.doClick();
    } else {
      pointFiveStep.doClick();
    }

    for (Enumeration<javax.swing.AbstractButton> e = swatchesGroup.getElements(); e.hasMoreElements();) {
      JToggleButton swatch = (JToggleButton) e.nextElement();
      Color color = swatch.getBackground();
      if (closestColor.equals(color)) {
        swatch.doClick();
      }
    }
  }

  /// Interface Creation

  // Create the Black and White Buttons at the top
  private void createTopNames(ImageIcon turtleIcon) {
    topNameContainer.setLayout(new GridLayout());
    // Create the black button
    Container blackButtonContainer = new Container();
    blackButtonContainer.setLayout(new GridLayout(1, 3));

    turtleLabel[14] = new JLabel(turtleIcon, javax.swing.SwingConstants.CENTER);
    turtleLabel[14].setPreferredSize(new Dimension(60, 20));
    turtleLabel[14].setBackground(new java.awt.Color(0, 0, 0));
    turtleLabel[14].setOpaque(true);
    blackButtonContainer.add(turtleLabel[14]);

    JToggleButton blackName = new JToggleButton(org.nlogo.api.Color.getColorNameByIndex(14) + " = 0");
    blackName.addActionListener(this);
    blackName.setActionCommand("0.0");
    blackName.setOpaque(true);
    blackName.setFont(new java.awt.Font("ArialNarrow", 0, 10));
    Insets insets = new Insets(0, 0, 0, 0);
    blackName.setMargin(insets);
    swatchesGroup.add(blackName);
    blackButtonContainer.add(blackName);
    blackButtonContainer.add(new JLabel(""));
    topNameContainer.add(blackButtonContainer);

    // Create the white button
    Container whiteButtonContainer = new Container();
    whiteButtonContainer.setLayout(new GridLayout(1, 3));

    whiteButtonContainer.add(new JLabel(""));

    JToggleButton whiteName = new JToggleButton(org.nlogo.api.Color.getColorNameByIndex(15) + " = 9.9");
    whiteName.addActionListener(this);
    whiteName.setActionCommand("9.9");
    whiteName.setOpaque(true);
    whiteName.setFont(new java.awt.Font("ArialNarrow", 0, 10));
    whiteName.setMargin(insets);
    swatchesGroup.add(whiteName);
    whiteButtonContainer.add(whiteName);

    turtleLabel[15] = new JLabel(turtleIcon, javax.swing.SwingConstants.CENTER);
    turtleLabel[15].setPreferredSize(new Dimension(60, 20));
    turtleLabel[15].setBackground(new java.awt.Color(255, 255, 255));
    turtleLabel[15].setOpaque(true);
    whiteButtonContainer.add(turtleLabel[15]);
    topNameContainer.add(whiteButtonContainer);

    // Add the container to the GridBag
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    pane.add(topNameContainer, c);
    getContentPane().validate();
  }

  private void createLeftNames() {

    leftNameContainer.setLayout(new GridLayout(14, 1));

    for (int i = 5; i < 140; i += 10) {
      // Create name Buttons
      String colorNameString = org.nlogo.api.Color.getColorNameByIndex(i / 10) + " = " + i;
      JToggleButton colorName = new JToggleButton(colorNameString);
      colorName.addActionListener(this);
      colorName.setActionCommand(String.valueOf(i));
      colorName.setFont(new java.awt.Font("ArialNarrow", 0, 10));
      swatchesGroup.add(colorName);
      leftNameContainer.add(colorName);
    }
    getContentPane().invalidate();

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.fill = GridBagConstraints.VERTICAL;
    c.insets = new Insets(5, 0, 0, 0);
    pane.add(leftNameContainer, c);
  }

  private void createRightPreview(ImageIcon turtleIcon) {
    rightPreviewContainer.setLayout(new GridLayout(14, 1));

    int turtleIconindex = 0;
    for (int i = 5; i < 140; i += 10) {
      // Create color turtles with variable background
      turtleLabel[turtleIconindex] = new JLabel(turtleIcon, javax.swing.SwingConstants.CENTER);
      turtleLabel[turtleIconindex].setBackground(new Color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(i)));
      turtleLabel[turtleIconindex].setOpaque(true);
      rightPreviewContainer.add(turtleLabel[turtleIconindex]);
      turtleIconindex++;
    }
    getContentPane().invalidate();

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 2;
    c.gridy = 1;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(5, 0, 0, 0);
    pane.add(rightPreviewContainer, c);
  }

  private void createTopLeftLabel() {
    JLabel upperLeftCorner = new JLabel(I18N.guiJ().get("tools.colorswatch.preview"));

    upperLeftCorner.setFont(new java.awt.Font("ArialNarrow", 0, 10));
    upperLeftCorner.setPreferredSize(new Dimension(45, 20));

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 2;
    c.gridy = 0;
    c.insets = new Insets(0, 2, 0, 0);
    pane.add(upperLeftCorner, c);
  }

  private void createSwatches() {
    final int rows = 14;
    double colorNumber = 0;
    int columnlength = 10;
    int swatchPerRow = (int) (StrictMath.round(columnlength / step));


    swatchesContainer.invalidate();

    swatchesContainer.setLayout(new GridLayout(rows, swatchPerRow));

    for (int i = 0; i < rows; i++) {
      colorNumber = i * 10;
      for (int j = 0; j < (swatchPerRow + 1); j++) {
        JToggleButton swatch = new JToggleButton();
        if (j >= swatchPerRow) {
          colorNumber = (i * 10) + 9.9;
        } else {
          swatch.setForeground(Color.BLACK);
        }
        swatch.setRolloverEnabled(true);
        swatch.setBorderPainted(true);
        swatch.setOpaque(true);
        swatch.setFocusPainted(false);
        swatch.setBorder(BorderFactory.createEmptyBorder());

        // I don't entirely understand what's going on here
        // on xp l&f java 1.5 the background color of the toggle
        //  buttons doesn't show up so we work around using
        // a color swatch (which is really just a Jpanel)
        // that works on windows but doesn't on mac.
        // however, setting the background of the button
        // seems to work fine so just do both. ev 3/17/06
        Color c = new Color
            (org.nlogo.api.Color.getARGBbyPremodulatedColorNumber
                (colorNumber));

        swatch.setBackground(c);
        org.nlogo.swing.ColorSwatch s = new org.nlogo.swing.ColorSwatch
            (swatch.getPreferredSize().width,
                swatch.getPreferredSize().height);
        s.setBackground(c);

        if (!numberVisibilityFlag && !pointOneStepflag) {
          JLabel label = new JLabel(formatter.format(colorNumber));
          label.setFont(new java.awt.Font("ArialNarrow", 0, 8));
          if (colorNumber % 10 < 3.5) {
            label.setForeground(Color.LIGHT_GRAY);
          }
          s.add(label);
        }
        swatch.add(s);
        swatch.setActionCommand(String.valueOf(colorNumber));
        swatch.addActionListener(this);
        swatch.addMouseListener(this);

        swatchesGroup.add(swatch);
        swatchesContainer.add(swatch);

        colorNumber = step + colorNumber;
      }
    }
    swatchesContainer.validate();

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(5, 5, 0, 2);

    pane.add(swatchesContainer, c);
  }

  private void createControls(boolean plotPenFlag) {
    Container controlsContainer = new Container();
    controlsContainer.setLayout(new BoxLayout(controlsContainer, BoxLayout.LINE_AXIS));
    // Copy button
    if (!plotPenFlag) {
      JButton copyButton = new JButton(I18N.guiJ().get("tools.colorswatch.copy"));
      copyButton.setActionCommand("Copy");
      copyButton.addActionListener(this);
      controlsContainer.add(copyButton);
    } else {
      controlsContainer.add(Box.createRigidArea(new Dimension(10, 0)));
      okButton = new JButton(I18N.guiJ().get("common.buttons.ok"));
      controlsContainer.add(okButton);
      okButton.addActionListener(this);
      controlsContainer.add(Box.createRigidArea(new Dimension(10, 0)));
      JButton cancelButton = new JButton(I18N.guiJ().get("common.buttons.cancel"));
      cancelButton.addActionListener(this);
      controlsContainer.add(cancelButton);
      controlsContainer.add(Box.createRigidArea(new Dimension(10, 0)));
      //JLabel selectedColorLabel = new JLabel("selected color");
      //controlsContainer.add(selectedColorLabel);
    }
    // selected Color Label
    selectedColorLabel = new JLabel(org.nlogo.api.Color.getColorNameByIndex(14));
    selectedColorLabel.setMinimumSize(new Dimension(100, 20));
    selectedColorLabel.setMaximumSize(new Dimension(100, 20));
    selectedColorLabel.setPreferredSize(new Dimension(100, 20));
    selectedColorLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    controlsContainer.add(selectedColorLabel);

    controlsContainer.add(Box.createHorizontalGlue());

    // Hide numbers checkbox
    checkboxHideNumbers = new JCheckBox(I18N.guiJ().get("tools.colorswatch.numbers"), true);
    controlsContainer.add(checkboxHideNumbers);
    checkboxHideNumbers.addItemListener(this);

    controlsContainer.add(Box.createHorizontalGlue());

    // Radio buttons

    JPanel stepJpanel = new JPanel();
    stepJpanel.setLayout(new BoxLayout(stepJpanel, BoxLayout.LINE_AXIS));
    stepJpanel.setBorder(BorderFactory.createLineBorder(Color.gray));

    oneStep = new JRadioButton("1");
    oneStep.setActionCommand("oneStep");
    stepJpanel.add(oneStep);
    oneStep.setSelected(true);

    pointFiveStep = new JRadioButton("0.5");
    pointFiveStep.setActionCommand("pointFiveStep");
    stepJpanel.add(pointFiveStep);

    pointOneStep = new JRadioButton("0.1");
    pointOneStep.setActionCommand("pointOneStep");
    stepJpanel.add(pointOneStep);

    ButtonGroup groupStep = new ButtonGroup();
    groupStep.add(oneStep);
    groupStep.add(pointFiveStep);
    groupStep.add(pointOneStep);

    oneStep.addActionListener(this);
    pointFiveStep.addActionListener(this);
    pointOneStep.addActionListener(this);

    controlsContainer.add(stepJpanel);

    JLabel gradationLabel = new JLabel(" " + I18N.guiJ().get("tools.colorswatch.increment"));
    controlsContainer.add(gradationLabel);

    controlsContainer.add(Box.createRigidArea(new Dimension(10, 0)));
    controlsContainer.add(new JLabel("  "));

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 3;
    pane.add(controlsContainer, c);
  }


  /// Auxiliary Methods

  // Create turtle icon
  private ImageIcon getShapeIcon(double colorValue) {
    VectorShape defaultShape = org.nlogo.shape.VectorShape.getDefaultShape();
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    org.nlogo.api.Graphics2DWrapper g2 = new org.nlogo.api.Graphics2DWrapper(image.createGraphics());
    g2.antiAliasing(true);
    defaultShape.paint
        (g2,
            new Color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(colorValue)),
            0, 0, 16, 0);
    g2.drawImage(image);
    return new ImageIcon(image);
  }

  // Check if the passed String is a number
  private boolean isNumber(String n) {
    try {
      Double.valueOf(n).doubleValue();
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public void swatchAction() {


  }

  /// Event Handling

  public void actionPerformed(ActionEvent e) {
    String actionCommand = e.getActionCommand();
    if (isNumber(actionCommand)) // Select a Color Swatch
    {
      selectedColorNumber = Double.valueOf(e.getActionCommand()).doubleValue();
      selectedColor = new Color(org.nlogo.api.Color.getARGBbyPremodulatedColorNumber(selectedColorNumber));
      selectedColorName = org.nlogo.api.Color.getClosestColorNameByARGB(selectedColor.getRGB());
      selectedColorLabel.setText(selectedColorName);
      turtleIcon = getShapeIcon(selectedColorNumber);
      for (int i = 0; i <= 15; i++) {
        turtleLabel[i].setIcon(turtleIcon);
      }
      rightPreviewContainer.repaint();
      rightPreviewContainer.revalidate();
    } else if (actionCommand.equals("Copy")) // Copy the Color value to the Clipboard
    {
      Toolkit tk = Toolkit.getDefaultToolkit();
      StringSelection st = new StringSelection(selectedColorName);
      Clipboard cp = tk.getSystemClipboard();
      cp.setContents(st, this);
    } else if (actionCommand.endsWith("Step")) // Change Swaches size
    {
      if (actionCommand.equals("oneStep")) {
        step = 1;
        pointOneStepflag = false;
        checkboxHideNumbers.setEnabled(true);
      } else if (actionCommand.equals("pointFiveStep")) {
        step = .5;
        pointOneStepflag = false;
        checkboxHideNumbers.setEnabled(true);

      } else if (actionCommand.equals("pointOneStep")) {
        step = .1;
        pointOneStepflag = true;
        checkboxHideNumbers.setEnabled(false);
      }
      swatchesContainer.setVisible(false);
      swatchesContainer.removeAll();
      createSwatches();
      swatchesContainer.setVisible(true);
      swatchesContainer.repaint();
    } else if (actionCommand.equals(I18N.guiJ().get("common.buttons.ok"))) {
      okCancelFlag = 1;
      dispose();
    } else if (actionCommand.equals(I18N.guiJ().get("common.buttons.cancel"))) {
      okCancelFlag = -1;
      dispose();
    }
  }

  // Hide or show color values
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      numberVisibilityFlag = false;
    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
      numberVisibilityFlag = true;
    }
    swatchesContainer.removeAll();
    createSwatches();
  }

  // Illuminate the patch under the mouse cusor as it moves.
  public void mouseEntered(MouseEvent e) {

    JToggleButton selectedSwatch = (JToggleButton) e.getSource();
    selectedSwatch.setBorder(BorderFactory.createLineBorder(Color.gray));

    // Why does (e.getModifiers()) return 16 instead of
    // 503 which is equal to MouseEvent.MOUSE_PRESSED
    // Thus (e.getModifiers() & MouseEvent.MOUSE_PRESSED) should return 503?
    if ((e.getModifiers() & MouseEvent.MOUSE_PRESSED) == 16) {
      selectedSwatch.doClick();
    }
  }

  public void mouseExited(MouseEvent e) {
    JToggleButton selectedSwatch = (JToggleButton) e.getSource();
    selectedSwatch.setSelected(false);
    selectedSwatch.setBorder(BorderFactory.createEmptyBorder());
  }

  public void windowClosing(WindowEvent e) {
    // I check the flag below to see if we are in PlotPenMode
    // In that case closing the window is the same as pressing OK
    // This is a different behavior than in the "MenuToolsMode"
    if (plotPenMode) {
      okCancelFlag = 1;
      okButton.doClick();
    }
  }

  // The following callback are not used for anything
  // but the Interfaces demands them
  public void mouseDragged(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void lostOwnership(Clipboard clip, Transferable tr) {
  }

  public void componentMoved(ComponentEvent arg0) {
  }

  public void componentShown(ComponentEvent arg0) {
  }

  public void componentHidden(ComponentEvent arg0) {
  }

  public void windowOpened(WindowEvent arg0) {
  }

  public void windowClosed(WindowEvent arg0) {
  }

  public void windowIconified(WindowEvent arg0) {
  }

  public void windowDeiconified(WindowEvent arg0) {
  }

  public void windowActivated(WindowEvent arg0) {
  }

  public void windowDeactivated(WindowEvent arg0) {
  }
}
