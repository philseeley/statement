package name.seeley.phil.statement;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BankFilter extends FileFilter
{
  private FileNameExtensionFilter _fileFilter;
  private Bank                    _bank;
  
  public BankFilter(Bank p)
  {
    _bank = p;
    _fileFilter = new FileNameExtensionFilter(p.description()+" - "+p.bankTLA()+" (*."+p.extension()+")", p.extension());
  }

  public Bank getBank()
  {
    return _bank;
  }
  
  @Override
  public boolean accept(File file)
  {
    return _fileFilter.accept(file);
  }

  @Override
  public String getDescription()
  {
    return _fileFilter.getDescription();
  }
}
