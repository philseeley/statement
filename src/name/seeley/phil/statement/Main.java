package name.seeley.phil.statement;

import java.io.File;

public class Main
{
  static public void main(String[] args)
  {
    try
    {
      Controller c = new Controller();
      
      c.setSize(900, 600);
      c.setVisible(true);
      
      if(args.length > 0)
        c.openFile(new File(args[0]));
    }
    catch(Exception e)
    {
      e.printStackTrace(System.err);
    }
  }
}
