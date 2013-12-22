package name.seeley.phil.statement.banks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import name.seeley.phil.statement.jaxb.Entry;
import name.seeley.phil.statement.jaxb.ObjectFactory;
import name.seeley.phil.statement.Bank;

public class LloydsTSB02 implements Bank
{
  static private final String FILE_EXT = "csv";
  static private final String BANK_TLA = "LTB";
  
  @Override
  public List<Entry> parse(ObjectFactory factory, File file) throws Exception
  {
    List<Entry> entries = new ArrayList<Entry>();

    BufferedReader r = new BufferedReader(new FileReader(file));

    try
    {
      r.readLine(); // Discard header comment line
      
      SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
      NumberFormat nf = DecimalFormat.getInstance();
      
      String l;
      while((l = r.readLine()) != null)
      {
        String[] t = l.split(",");
  
        Date d = df.parse(t[0]);
        
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        
        XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
  
        Entry e = factory.createEntry();
  
        e.setDate(xc);
        
        e.setDescr(String.format("%3s - %s", t[1], t[4]));
        
        String v;
        
        if(t[5].length() == 0)
          v = t[6];
        else
          v = "-"+t[5];
        
        e.setValue(nf.parse(v).floatValue());
        
        entries.add(e);
      }
    }
    finally
    {
      r.close();
    }
    
    return entries;
  }

  @Override
  public String bankTLA()
  {
    return BANK_TLA;
  }

  @Override
  public String description()
  {
    return "Lloyds TSB comma separated, v2";
  }

  @Override
  public String extension()
  {
    return FILE_EXT;
  }
  
  @Override
  public String help()
  {
    return "help/lloydsTSB02.html";
  }
}
