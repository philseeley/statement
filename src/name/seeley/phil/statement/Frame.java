package name.seeley.phil.statement;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableColumn;

import name.seeley.phil.statement.jaxb.Flag;

@SuppressWarnings("serial")
public class Frame extends JFrame
{
  private Controller        _controller;
  private JTable            _table;
  private JTextField        _filterField;
  private HelpDialog        _helpDialog = new HelpDialog();
  private boolean           _filtered = false;
  private List<ButtonModel> _buttonModels = new ArrayList<ButtonModel>();
  
  private class HelpAction extends AbstractAction
  {
    private String _helpFilename;
    
    public HelpAction (String name, String filename)
    {
      super(name);
      
      _helpFilename = filename;
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0)
    {
      _helpDialog.showHelp(_helpFilename);
    }
  }

  public Frame(Controller controller) throws Exception
  {
    super();
    
    _controller = controller;
    
    Action a;

    // Let the window manager determine the initial location.
    
    setLocationByPlatform(true);
    
    // The controller should handle the exit.
    
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    
    addWindowListener(new WindowAdapter()
      {public void windowClosing(WindowEvent e){_controller.exit();}});

    // The main components are arranged around the border of the content pane, with
    // a scrollable table of entries in the middle. 
    
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    _table = new JTable(_controller.getTableModel());
    contentPane.add(new JScrollPane(_table), BorderLayout.CENTER);

    _table.setFont(new Font(Font.MONOSPACED, Font.ROMAN_BASELINE, 12));
    _table.setAutoCreateRowSorter(true);
    
    for(int i=0; i< _controller.getTableModel().getColumnCount(); ++i)
    {
      TableColumn c = _table.getColumnModel().getColumn(i);
      
      c.setPreferredWidth(_controller.getTableModel().getColumnWidth(i));
      c.setCellRenderer(_controller.getTableModel().getCellRenderer(i));
    }
    
    // Most operations are available from the menus and the toolbar.
    
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JToolBar toolBar = new JToolBar();
    contentPane.add(toolBar, BorderLayout.NORTH);
    toolBar.setFloatable(false);

    // File menu.
    
    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    a = new AbstractAction("Open", IconUtil.load("document-open"))
    {public void actionPerformed(ActionEvent e){_controller.openFile();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_O, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Save", IconUtil.load("document-save"))
    {public void actionPerformed(ActionEvent e){_controller.saveFile();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_S, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Save As...", IconUtil.load("document-save-as"))
    {public void actionPerformed(ActionEvent e){_controller.saveFileAs();}};
    addAction(a, fileMenu, null, 0, 0);

    toolBar.addSeparator();
    
    a = new AbstractAction("Import", IconUtil.load("list-add"))
    {public void actionPerformed(ActionEvent e){_controller.importFile();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_I, ActionEvent.CTRL_MASK);
    
    a = new AbstractAction("Exit")
    {public void actionPerformed(ActionEvent e){_controller.exit();}};
    addAction(a, fileMenu, null, KeyEvent.VK_Q, ActionEvent.CTRL_MASK);

    // Edit menu. Note that the actions for setting the entry's states
    // are also added to this menu later.
    
    JMenu editMenu = new JMenu("Edit");
    menuBar.add(editMenu);

    a = new AbstractAction("Delete", IconUtil.load("user-trash"))
    {public void actionPerformed(ActionEvent e){delete();}};
    addAction(a, editMenu, toolBar, 0, 0);
    
    // View menu.
    
    JMenu viewMenu = new JMenu("View");
    menuBar.add(viewMenu);

    toolBar.addSeparator();

    a = new AbstractAction("Show All", IconUtil.load("view-refresh"))
    {public void actionPerformed(ActionEvent e){showAll();}};
    addAction(a, viewMenu, toolBar, 0, 0);

    // The view toggle buttons.
    
    toolBar.addSeparator();

    toolBar.add(new JLabel(viewMenu.getText()+":"));
    
    a = new AbstractAction("None", FlagInfo.getInfo(Flag.NONE).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.NONE);}};
    addToggleAction(a, viewMenu, toolBar, 0);

    a = new AbstractAction("Checked", FlagInfo.getInfo(Flag.CHECKED).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.CHECKED);}};
    addToggleAction(a, viewMenu, toolBar, 0);

    a = new AbstractAction("Missing", FlagInfo.getInfo(Flag.MISSING).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.MISSING);}};
    addToggleAction(a, viewMenu, toolBar, 0);

    // The filter field only takes numbers.
    
    _filterField = new JTextField();
    toolBar.add(_filterField);
    _filterField.addKeyListener(new KeyAdapter()
    {
      public void keyTyped(KeyEvent k)
      {
        if("0123456789.".indexOf(k.getKeyChar()) == -1)
          k.consume();
      }
    });

    // The filter can be started by simply pressing return.
    
    a = new AbstractAction("Filter", IconUtil.load("edit-find"))
    {public void actionPerformed(ActionEvent e){filter();}};
    addAction(a, viewMenu, toolBar, KeyEvent.VK_ENTER, 0);

    a = new AbstractAction("Clear", IconUtil.load("edit-clear"))
    {public void actionPerformed(ActionEvent e){clear();}};
    addAction(a, viewMenu, toolBar, KeyEvent.VK_ESCAPE, 0);

    // The buttons for setting the state of the selected entries.
    // Note that these are added to the Edit menu.
    
    toolBar.addSeparator();
    
    a = new AbstractAction("Checked", FlagInfo.getInfo(Flag.CHECKED).getIcon())
    {public void actionPerformed(ActionEvent e){setFlag(Flag.CHECKED);}};
    addAction(a, editMenu, toolBar, KeyEvent.VK_SPACE, 0);

    a = new AbstractAction("Missing", FlagInfo.getInfo(Flag.MISSING).getIcon())
    {public void actionPerformed(ActionEvent e){setFlag(Flag.MISSING);}};
    addAction(a, editMenu, toolBar, KeyEvent.VK_M, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Reset", FlagInfo.getInfo(Flag.NONE).getIcon())
    {public void actionPerformed(ActionEvent e){setFlag(Flag.NONE);}};
    addAction(a, editMenu, toolBar, KeyEvent.VK_R, ActionEvent.CTRL_MASK);

    // Help menu.
    
    JMenu helpMenu = new JMenu("Help");
    menuBar.add(helpMenu);

    a = new AbstractAction("Index")
    {public void actionPerformed(ActionEvent e){_helpDialog.showHelp("help/index.html");}};
    addAction(a, helpMenu, null, KeyEvent.VK_F1, 0);

    // Each supported bank should have its own help.
    
    JMenu bankHelpMenu = new JMenu("Banks");
    helpMenu.add(bankHelpMenu);
    
    for(Bank p : Banks.getBanks())
    {
      a = new HelpAction(p.description(), p.help());

      addAction(a, bankHelpMenu, null, 0, 0);
    }
    showAll();
  }

  private void addAction(Action action, JMenu menu, JToolBar toolBar, int key, int keyMask)
  {
    JMenuItem mi = new JMenuItem(action);
    
    if(key != 0)
      mi.setAccelerator(KeyStroke.getKeyStroke(key, keyMask));
    
    menu.add(mi);

    JButton b = new JButton(action);
    
    b.setHideActionText(true);
    b.setToolTipText(action.getValue(Action.NAME).toString());
    
    if(toolBar != null)
      toolBar.add(b);
  }

  private void addToggleAction(Action action, JMenu menu, JToolBar toolBar, int keyEvent)
  {
    ButtonModel bm = new JToggleButton.ToggleButtonModel();
    _buttonModels.add(bm);
    
    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(action);
    
    cbmi.setModel(bm);
    if(keyEvent != 0)
      cbmi.setAccelerator(KeyStroke.getKeyStroke(keyEvent, ActionEvent.CTRL_MASK));

    menu.add(cbmi);

    // Note that as we're sharing the button model, we don't pass the full action into
    // the toolbar button as this would cause the action to fire twice.

    JCheckBox cb = new JCheckBox(action.getValue(Action.NAME).toString());
    
    cb.setModel(bm);
    cb.setToolTipText(action.getValue(Action.NAME).toString());
    
    if(toolBar != null)
      toolBar.add(cb);
  }

  private void resetFilter(boolean resetFocus)
  {
    _filterField.setText(null);

    if(resetFocus)
      _filterField.requestFocus();
    
    _filtered = false;
  }
  
  public void showAll()
  {
    for(ButtonModel bm : _buttonModels)
      bm.setSelected(true);
    
    _controller.showAll();
    
    resetFilter(true);
  }
  
  private void clear()
  {
    _controller.filter(null);

    resetFilter(true);
  }
  
  private void setFlag(Flag flag)
  {
    for(int i : _table.getSelectedRows())
      _controller.getTableModel().setEntry(_table.convertRowIndexToModel(i), flag);
    
    resetFilter(_filtered);
  }

  private void delete()
  {
    int viewEntriesI[] = _table.getSelectedRows();
    int modelEntriesI[] = new int[viewEntriesI.length];

    for(int i=0; i<viewEntriesI.length; ++i)
      modelEntriesI[i] = _table.convertRowIndexToModel(viewEntriesI[i]);
    
    _controller.delete(modelEntriesI);
  }

  private void filter()
  {
    try
    {
      float f = Float.parseFloat(_filterField.getText());
  
      _controller.filter(f);
      
      // By default select/highlight the first row as this is usually the
      // one to check.
      
      _table.changeSelection(0, 0, false, false);

      // If there is more than one entry found, then put the keyboard
      // focus on the list so that the arrow keys can be used to select
      // the correct entry.
      
      if(_table.getRowCount() > 1)
        _table.requestFocus();
      
      _filtered = true;
    }
    catch (NumberFormatException e)
    {
      // This should only happen if the number contain more than one '.'.
    }
  }
}
