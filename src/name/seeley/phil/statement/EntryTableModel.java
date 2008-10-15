package name.seeley.phil.statement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import name.seeley.phil.statement.jaxb.*;

@SuppressWarnings("serial")
public class EntryTableModel extends AbstractTableModel 
{
  private class DateCellRenderer extends DefaultTableCellRenderer
  {
    private SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yy");

    @Override
    public void setValue(Object date)
    {
      setText(_dateFormat.format(date));
    }
  }

  private class ValueCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public void setValue(Object value)
    {
      setText(String.format("%8.2f", value));
    }
  }

  private final static int ALL = 0xFF;
  
  private String[]            _columnNames = {"Date", "Bank", "Description", "Value", "Checked"};
  private int[]               _columnWidths = {70, 30, 600, 60, 60};
  private TableCellRenderer[] _cellRenderers = {new DateCellRenderer(), null, null, new ValueCellRenderer(), null};
  private Statement           _statement;
  private List<Entry>         _filteredEntries = new ArrayList<Entry>();
  private int                 _filter = ALL;
  private boolean             _changed = false;

  public boolean getChanged()
  {
    return _changed;
  }
  
  public void setChanged(boolean changed)
  {
    _changed = changed;
  }
  
  public void setStatement(Statement statement)
  {
    _statement = statement;
    _filteredEntries.clear();
    _filteredEntries.addAll(_statement.getEntry());
    setChanged(false);
    refresh();
  }

  public Statement getStatement()
  {
    return _statement;
  }

  public void setEntry(int row, Flag flag)
  {
    _filteredEntries.get(row).setFlag(flag);
    fireTableCellUpdated(row, 4);
    setChanged(true);
  }

  public void delete(int entriesI[])
  {
    List<Entry> entries = new ArrayList<Entry>();
    
    for(int i : entriesI)
      entries.add(_filteredEntries.get(i));
    
    _filteredEntries.removeAll(entries);
    _statement.getEntry().removeAll(entries);

    setChanged(true);
    
    refresh();
  }

  public void refresh()
  {
    fireTableDataChanged();
  }

  @Override
  public int getColumnCount()
  {
    return 5;
  }

  @Override
  public String getColumnName(int col)
  {
    return _columnNames[col];
  }

  public int getColumnWidth(int col)
  {
    return _columnWidths[col];
  }

  public TableCellRenderer getCellRenderer(int col)
  {
    return _cellRenderers[col];
  }

  @Override
  public Class<?> getColumnClass(int col)
  {
    switch(col)
    {
    case 0:
      return Date.class;
    case 1:
    case 2:
      return String.class;
    case 3:
      return Float.class;
    case 4:
      return Icon.class;
    }
    
    return null;
  }
  
  @Override
  public int getRowCount()
  {
    return _filteredEntries.size();
  }

  @Override
  public Object getValueAt(int row, int col)
  {
    Entry e = _filteredEntries.get(row);
    
    switch(col)
    {
    case 0:
      return e.getDate().toGregorianCalendar().getTime();
    case 1:
      return e.getBankTLA();
    case 2:
      return e.getDescr();
    case 3:
      return e.getValue();
    case 4:
      return FlagInfo.getInfo(e.getFlag()).getIcon();
    }
    
    return null;
  }

  public void showAll()
  {
    _filter = ALL;
    setView();
  }

  public void setView(Flag flag, boolean selected)
  {
    if(selected)
      _filter |= FlagInfo.getInfo(flag).getVal();
    else
      _filter &= ~FlagInfo.getInfo(flag).getVal();

    setView();
  }

  private void setView()
  {
    _filteredEntries.clear();
    
    for(Entry e : _statement.getEntry())
      if((FlagInfo.getInfo(e.getFlag()).getVal() & _filter) != 0)
        _filteredEntries.add(e);

    refresh();
  }

  public void filter(float f)
  {
    _filteredEntries.clear();
    
    for(Entry e : _statement.getEntry())
      if(e.getValue() == f && e.getFlag() == Flag.NONE)
        _filteredEntries.add(e);

    refresh();
  }
}
