package name.seeley.phil.statement;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableColumn;

import name.seeley.phil.statement.jaxb.*;

@SuppressWarnings("serial")
public class ReceiptsFrame extends JFrame
{
  private Controller        _controller;
  private JTable            _table;

  public ReceiptsFrame(Controller controller) throws Exception
  {
    super();

    _controller = controller;

    Action a;

    // Let the window manager determine the initial location.

    setLocationByPlatform(true);

    // The main components are arranged around the border of the content pane, with
    // a scrollable table of entries in the middle.

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    _table = new JTable(_controller.getReceiptsTableModel());
    contentPane.add(new JScrollPane(_table), BorderLayout.CENTER);

    _table.setFont(new Font(Font.MONOSPACED, Font.ROMAN_BASELINE, 12));
    _table.setAutoCreateRowSorter(true);
    _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    for(int i=0; i< _controller.getReceiptsTableModel().getColumnCount(); ++i)
    {
      TableColumn c = _table.getColumnModel().getColumn(i);

      c.setPreferredWidth(_controller.getReceiptsTableModel().getColumnWidth(i));
      c.setCellRenderer(_controller.getReceiptsTableModel().getCellRenderer(i));
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

    a = new AbstractAction("Import Receipts", IconUtil.load("list-add"))
    {public void actionPerformed(ActionEvent e){_controller.importReceipts();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_I, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Save Receipts", IconUtil.load("document-save"))
    {public void actionPerformed(ActionEvent e){_controller.saveReceipts();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_S, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Find", IconUtil.load("edit-find"))
    {public void actionPerformed(ActionEvent e){find();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_F, ActionEvent.CTRL_MASK);

    a = new AbstractAction("Delete", IconUtil.load("user-trash"))
    {public void actionPerformed(ActionEvent e){delete();}};
    addAction(a, fileMenu, toolBar, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
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

  private void find()
  {
    int viewEntriesI = _table.getSelectedRow();
    int modelEntriesI;;

    modelEntriesI = _table.convertRowIndexToModel(viewEntriesI);

    _controller.receiptFilter(_controller.getReceiptsTableModel().getStatement().getEntry().get(modelEntriesI).getValue());
  }

  private void delete()
  {
    int viewEntriesI = _table.getSelectedRow();

    if(viewEntriesI != -1)
    {
      int modelEntriesI;

      modelEntriesI = _table.convertRowIndexToModel(viewEntriesI);

      List<Entry> entries = _controller.getReceiptsTableModel().getStatement().getEntry();

      Entry e = entries.get(modelEntriesI);
      entries.remove(e);

      _controller.getReceiptsTableModel().showAll();

      if(entries.size() > 0)
      {
        _table.setRowSelectionInterval(0, 0);
        find();
      }
    }
  }
}
