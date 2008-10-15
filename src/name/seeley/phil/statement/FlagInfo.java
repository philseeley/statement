package name.seeley.phil.statement;

import javax.swing.Icon;

import name.seeley.phil.statement.jaxb.Flag;

public enum FlagInfo
{
  NONE   (Flag.NONE,    0x01, "clear"),
  CHECKED(Flag.CHECKED, 0x02, "checked"),
  MISSING(Flag.MISSING, 0x04, "missing");

  private Flag _flag;
  private int  _val;
  private Icon _icon;
  
  FlagInfo(Flag flag, int val, String icon)
  {
    _flag = flag;
    _val  = val;
    _icon = IconUtil.load(icon);
  }

  public static FlagInfo getInfo(Flag flag)
  {
    for(FlagInfo i : values())
      if(i.getFlag() == flag)
        return i;
    
    return null; // This should never happen
  }

  public int getVal()
  {
    return _val;
  }

  public Icon getIcon()
  {
    return _icon;
  }

  public Flag getFlag()
  {
    return _flag;
  }
}
