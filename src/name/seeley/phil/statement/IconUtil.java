package name.seeley.phil.statement;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconUtil
{
  static public Icon load(String name)
  {
    URL file = IconUtil.class.getClassLoader().getResource("icons/"+name+".png");
    
    // We show which icon is missing because we simply get a NullPointerException
    // which does not include the file name.
    
    if(file == null)
      System.err.println("Icon for '"+name+"' missing");
    
    return new ImageIcon(file);
  }
}
