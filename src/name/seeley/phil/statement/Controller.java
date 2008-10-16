package name.seeley.phil.statement;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
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
  private File				_file;
  private JFileChooser      _importFileChooser;
  private JFileChooser      _fileChooser;
  private EntryTableModel   _tableModel = new EntryTableModel();
  private List<ButtonModel> _viewModels = new ArrayList<ButtonModel>();

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
    _marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    _tableModel.setStatement(_objectFactory.createStatement());
    
    _frame = new Frame(this);
    
    _importFileChooser = new JFileChooser();
    _importFileChooser.setAcceptAllFileFilterUsed(false);
    _importFileChooser.setMultiSelectionEnabled(true);
    
    for(Bank p : Banks.getBanks())
      _importFileChooser.addChoosableFileFilter(new BankFilter(p));

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

  public List<ButtonModel> getViewModels()
  {
    return _viewModels;
  }
  
  public void exit()
  {
    if(_tableModel.getChanged())
      if(showConfirm("Discard changes and exit?") != JOptionPane.YES_OPTION)
        return;

    System.exit(0);
  }

  public void open(File f)
  {
    try
    {
      _file = f;
      
      _frame.setTitle(_file.getCanonicalPath());

      _fileChooser.setCurrentDirectory(_file.getParentFile());
      
      Statement s = (Statement) _unMarshaller.unmarshal(f);
  
      _tableModel.setStatement(s);
      
      _frame.showAll();
    }
    catch(Exception e)
    {
      showError(e);
    }
  }
  
  public void openFile()
  {
    if(_tableModel.getChanged())
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
        
      _tableModel.setChanged(false);

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
          _tableModel.setChanged(true);
        }
        catch(Exception e)
        {
          showError(e);
        }
      }
      
      Collections.sort(_tableModel.getStatement().getEntry(), new EntryComparitor());
      
      showAll();
    }
  }

  public void delete(int entriesI[])
  {
    if(showConfirm("Delete the selected enrties?") == JOptionPane.YES_OPTION)
      _tableModel.delete(entriesI);
  }

  public void showAll()
  {
    for(ButtonModel bm : _viewModels)
      bm.setSelected(true);
    
    _tableModel.showAll();
  }

  public void setView(ActionEvent e, Flag flag)
  {
    AbstractButton tb = (AbstractButton)e.getSource();
    _tableModel.setView(flag, tb.isSelected());
  }
}
