package name.seeley.phil.statement;

import java.io.File;
import java.util.List;

import name.seeley.phil.statement.jaxb.Entry;
import name.seeley.phil.statement.jaxb.ObjectFactory;

public interface Bank
{
  public List<Entry> parse(ObjectFactory factory, File file) throws Exception;
  
  public String bankTLA();
  public String description();
  public String extension();
  public String help();
}
