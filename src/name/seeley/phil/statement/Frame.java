package name.seeley.phil.statement;

import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.TableColumn;

import name.seeley.phil.statement.jaxb.Flag;

@SuppressWarnings("serial")
public class Frame extends JFrame
{
  private Controller  _controller;
  private JTable      _table;
  private JTextField  _filterField;
  private HelpDialog  _helpDialog = new HelpDialog();
  private boolean     _filtered = false;
  
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

    setLocationByPlatform(true);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    
    addWindowListener(new WindowAdapter()
      {public void windowClosing(WindowEvent e){_controller.exit();}});

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
    
    JPanel topPannel = new JPanel(new BorderLayout());
    contentPane.add(topPannel, BorderLayout.NORTH);

    JMenuBar menuBar = new JMenuBar();
    topPannel.add(menuBar, BorderLayout.NORTH);

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JToolBar toolBar = new JToolBar();
    topPannel.add(toolBar, BorderLayout.SOUTH);
    toolBar.setFloatable(false);

    a = new AbstractAction("Open", IconUtil.load("document-open"))
    {public void actionPerformed(ActionEvent e){_controller.open();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_O, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Save", IconUtil.load("document-save"))
    {public void actionPerformed(ActionEvent e){_controller.save();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_S, ActionEvent.CTRL_MASK);

    toolBar.addSeparator();
    
    a = new AbstractAction("Import", IconUtil.load("list-add"))
    {public void actionPerformed(ActionEvent e){_controller.importFile();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_I, ActionEvent.CTRL_MASK);
    
    a = new AbstractAction("Exit")
    {public void actionPerformed(ActionEvent e){_controller.exit();}};
    addAction(a, fileMenu, null, KeyEvent.VK_Q, ActionEvent.CTRL_MASK);

    JMenu editMenu = new JMenu("Edit");
    menuBar.add(editMenu);

    a = new AbstractAction("Delete", IconUtil.load("user-trash"))
    {public void actionPerformed(ActionEvent e){delete();}};
    addAction(a, editMenu, toolBar, 0, 0);
    
    JMenu viewMenu = new JMenu("View");
    menuBar.add(viewMenu);

    toolBar.addSeparator();

    a = new AbstractAction("Show All", IconUtil.load("view-refresh"))
    {public void actionPerformed(ActionEvent e){showAll();}};
    addAction(a, viewMenu, toolBar, KeyEvent.VK_ESCAPE, 0);

    toolBar.addSeparator();

    toolBar.add(new JLabel(viewMenu.getText()+":"));
    
    a = new AbstractAction("None", FlagInfo.getInfo(Flag.NONE).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.NONE);}};
    addToggleAction(a, _controller.getViewModels(), viewMenu, toolBar, 0);

    a = new AbstractAction("Checked", FlagInfo.getInfo(Flag.CHECKED).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.CHECKED);}};
    addToggleAction(a, _controller.getViewModels(), viewMenu, toolBar, 0);

    a = new AbstractAction("Missing", FlagInfo.getInfo(Flag.MISSING).getIcon())
    {public void actionPerformed(ActionEvent e){_controller.setView(e, Flag.MISSING);}};
    addToggleAction(a, _controller.getViewModels(), viewMenu, toolBar, 0);

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

    a = new AbstractAction("Filter", IconUtil.load("edit-find"))
    {public void actionPerformed(ActionEvent e){filter();}};

    addAction(a, viewMenu, toolBar, KeyEvent.VK_ENTER, 0);

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

    JMenu helpMenu = new JMenu("Help");
    menuBar.add(helpMenu);

    a = new AbstractAction("Index")
    {public void actionPerformed(ActionEvent e){_helpDialog.showHelp("help/index.html");}};
    addAction(a, helpMenu, null, KeyEvent.VK_F1, 0);

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

  private void addToggleAction(Action action, List<ButtonModel> list, JMenu menu, JToolBar toolBar, int keyEvent)
  {
    ButtonModel bm = new JToggleButton.ToggleButtonModel();
    list.add(bm);
    
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

  public void resetFilter(boolean resetFocus)
  {
    _filterField.setText(null);

    if(resetFocus)
      _filterField.requestFocus();
    
    _filtered = false;
  }
  
  public void showAll()
  {
    _controller.showAll();

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
  
      _controller.getTableModel().filter(f);
      
      _table.changeSelection(0, 0, false, false);

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
