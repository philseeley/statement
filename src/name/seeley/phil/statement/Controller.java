package name.seeley.phil.statement;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import name.seeley.phil.statement.jaxb.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Controller
{
  static final private String FILE_EXT = "stm";
  
  private ObjectFactory     _objectFactory = new ObjectFactory();
  private JAXBContext       _jaxbContext = JAXBContext.newInstance(Controller.class.getPackage().getName()+".jaxb");
  private Marshaller        _marshaller = _jaxbContext.createMarshaller();
  private Unmarshaller      _unMarshaller = _jaxbContext.createUnmarshaller();
  private Frame             _frame;
  private ReceiptsFrame     _receiptsFrame;
  private File              _file;
  private JFileChooser      _importFileChooser;
  private JFileChooser      _importReceiptsFileChooser;
  private JFileChooser      _fileChooser;
  private EntryTableModel   _tableModel = new EntryTableModel();
  private EntryTableModel   _receiptsTableModel = new EntryTableModel();
  private boolean           _changed = false;

  private class EntryComparitor implements Comparator<Entry>
  {
    @Override
    public int compare(Entry e0, Entry e1)
    {
      return e0.getDate().compare(e1.getDate());
    }
  }
  
  public Controller() throws Exception
  {
    _tableModel.setController(this);
    _receiptsTableModel.setController(this);

    _marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    _tableModel.setStatement(_objectFactory.createStatement());
    _receiptsTableModel.setStatement(_objectFactory.createStatement());

    _frame = new Frame(this);
    _receiptsFrame = new ReceiptsFrame(this);
    setChanged(false);

    _importFileChooser = new JFileChooser();
    _importFileChooser.setAcceptAllFileFilterUsed(false);
    _importFileChooser.setMultiSelectionEnabled(true);

    for(Bank p : Banks.getBanks())
      _importFileChooser.addChoosableFileFilter(new BankFilter(p));

    _importReceiptsFileChooser = new JFileChooser();
    _importReceiptsFileChooser.setAcceptAllFileFilterUsed(false);
    _importReceiptsFileChooser.setMultiSelectionEnabled(true);

    _importReceiptsFileChooser.addChoosableFileFilter(new BankFilter(new Receipts01()));

    _fileChooser = new JFileChooser();
    _fileChooser.setAcceptAllFileFilterUsed(false);
    _fileChooser.setMultiSelectionEnabled(false);
    
    _fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Statement (*."+FILE_EXT+")", FILE_EXT));
  }

  private void showError(Exception e)
  {
    JOptionPane.showMessageDialog(_frame, e, "Error", JOptionPane.ERROR_MESSAGE);
    
    e.printStackTrace(System.err);
  }
  
  private int showConfirm(String msg)
  {
    return JOptionPane.showConfirmDialog(_frame, msg, "Confirm", JOptionPane.YES_NO_OPTION);
  }
  
  private void showMsg(String msg)
  {
    JOptionPane.showMessageDialog(_frame, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
  }
  
  public void setSize(int width, int height)
  {
    _frame.setSize(width, height);
  }
  
  public void setVisible(boolean b)
  {
    _frame.setVisible(b);
  }
  
  public EntryTableModel getTableModel()
  {
    return _tableModel;
  }

  public EntryTableModel getReceiptsTableModel()
  {
    return _receiptsTableModel;
  }

  public boolean getChanged()
  {
    return _changed;
  }
  
  public void setChanged(boolean changed)
  {
    _changed = changed;

    String title = "";

    try
    {
      if (_file != null)
        title = _file.getCanonicalPath();
    } catch (IOException e)
    {
      showError(e);
    }
    if (_changed)
      title += "*";

    _frame.setTitle(title);
  }
  
  public void exit()
  {
    if(getChanged())
      if(showConfirm("Discard changes and exit?") != JOptionPane.YES_OPTION)
        return;

    System.exit(0);
  }

  public void open(File f)
  {
    try
    {
      _file = f;
      
      _fileChooser.setCurrentDirectory(_file.getParentFile());
      
      Statement s = (Statement) _unMarshaller.unmarshal(f);
  
      _tableModel.setStatement(s);
      setChanged(false);
      
      _frame.showAll();
    }
    catch(Exception e)
    {
      showError(e);
    }
  }
  
  public void openFile()
  {
    if(getChanged())
      if(showConfirm("Discard changes?") != JOptionPane.YES_OPTION)
        return;

    if(_fileChooser.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION)
    {
      File f = _fileChooser.getSelectedFile();
      
      open(f);
    }
  }

  public void save()
  {
    try
    {
      OutputStream os = new FileOutputStream(_file);
      _marshaller.marshal(_tableModel.getStatement(), os);
      os.close();
        
      setChanged(false);

      _frame.setTitle(_file.getCanonicalPath());
      
      showMsg("File '"+_file.getName()+"'saved");
    }
    catch(Exception e)
    {
      showError(e);
    }
  }

  public void saveFile()
  {
    if(_file == null)
      saveFileAs();
    else
      save();
  }

  public void saveFileAs()
  {
    if(_fileChooser.showSaveDialog(_frame) == JFileChooser.APPROVE_OPTION)
    {
      File f = _fileChooser.getSelectedFile();
  
      String ext = "."+FILE_EXT;
      
      if(f.getName().lastIndexOf(ext) == -1)
        f = new File(f.getAbsolutePath()+ext);

      if(f.exists())
          if(showConfirm("Overwrite file '"+f.getName()+"?") != JOptionPane.YES_OPTION)
            return;

      _file = f;
      
      save();
    }
  }

  public void importFile()
  {
    if(_importFileChooser.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION)
    {
      BankFilter p = (BankFilter) _importFileChooser.getFileFilter();
      
      File[] files = _importFileChooser.getSelectedFiles();
      
      for(File f : files)
      {
        try
        {
          List<Entry> el = p.getBank().parse(_objectFactory, f);
          
          for(Entry e : el)
          {
            e.setBankTLA(p.getBank().bankTLA());
            e.setFlag(Flag.NONE);
          }
          
          _tableModel.getStatement().getEntry().addAll(el);
          setChanged(true);
        }
        catch(Exception e)
        {
          showError(e);
        }
      }
      
      Collections.sort(_tableModel.getStatement().getEntry(), new EntryComparitor());
      
      _frame.showAll();
    }
  }

  public void importReceipts()
  {
    if(_importReceiptsFileChooser.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION)
    {
      BankFilter p = (BankFilter) _importReceiptsFileChooser.getFileFilter();

      File[] files = _importReceiptsFileChooser.getSelectedFiles();

      for(File f : files)
      {
        try
        {
          List<Entry> el = p.getBank().parse(_objectFactory, f);

          for(Entry e : el)
          {
            e.setBankTLA(p.getBank().bankTLA());
            e.setFlag(Flag.NONE);
          }

          _receiptsTableModel.getStatement().getEntry().addAll(el);
        }
        catch(Exception e)
        {
          showError(e);
        }
      }

      _receiptsTableModel.showAll();
      _receiptsFrame.setVisible(true);
    }
  }

  public void showReceipts()
  {
    _receiptsFrame.setSize(900, 600);
    _receiptsFrame.setVisible(true);
  }

  public void delete(int entriesI[])
  {
    if(showConfirm("Delete the selected entries?") == JOptionPane.YES_OPTION)
      _tableModel.delete(entriesI);
  }

  public void filter(Float f)
  {
    _tableModel.filter(f);
  }

  public void receiptFilter(Float f)
  {
    _frame.filter(f);
    _frame.setVisible(true);
  }

  public void showAll()
  {
    _tableModel.showAll();
  }

  public void setView(ActionEvent e, Flag flag)
  {
    AbstractButton tb = (AbstractButton)e.getSource();
    _tableModel.setView(flag, tb.isSelected());
  }
}
